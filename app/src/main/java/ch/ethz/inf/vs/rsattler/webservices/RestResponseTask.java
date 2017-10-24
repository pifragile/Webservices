package ch.ethz.inf.vs.rsattler.webservices;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RestResponseTask extends AsyncTask<Socket, Void, Void> {

    private Context context;
    private RestServerThread thread;

    private Version version = new Version(Version.V11);

    RestResponseTask(Context context, RestServerThread thread) {
        this.context = context;
        this.thread = thread;
    }

    @Override
    protected Void doInBackground(Socket... sockets) {
        Socket socket = sockets[0];

        try (InputStream request = socket.getInputStream()) {

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(request))) {
                String[] lines = readHeader(reader);

                Pattern requestPattern = Pattern.compile("^((GET)|(POST))\\s\\/[\\w\\.\\/]*\\sHTTP\\/(1\\.1)$");
                Matcher requestMatcher = requestPattern.matcher(lines[0]);

                if (!requestMatcher.find()) {
                    try (OutputStream output = socket.getOutputStream()) {
                        send(output, version, new Status(400));
                    }
                    return null;
                }

                Pattern getRequest = Pattern.compile("^GET");
                Pattern postRequest = Pattern.compile("^POST");
                Matcher getMatcher = getRequest.matcher(lines[0]);
                Matcher postMatcher = postRequest.matcher(lines[0]);

                try (OutputStream output = socket.getOutputStream()) {
                    if (getMatcher.find()) {
                        respondToGet(output, lines);
                    } else if (postMatcher.find()) {
                        respondToPost(output, lines, reader);
                    } else {
                        send(output, new Version(Version.V11), new Status(400));
                        return null;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String[] readHeader(BufferedReader reader) throws IOException {
        List<String> header = new ArrayList<>();
        String lastLines = null;
        while (!"".equals(lastLines)) {
            String newLine = reader.readLine();
            lastLines = newLine;
            header.add(newLine);
        }
        return header.toArray(new String[header.size()]);
    }

    private void send(OutputStream output, Version version, Status status) {
        PrintStream stream = new PrintStream(output);
        stream.print(version.toString()+" "+status.toString()+"\r\n\r\n");
    }

    private void respondToGet(OutputStream output, String[] header) {
        String path = getPath(header[0]);

        RestResource resource = RestResource.getResourceFromPath(context, path);

        boolean accpetsType = false;
        for (int i = 1; i < header.length; i++) {
            Pattern acceptLine = Pattern.compile("^Accept:(.*) (text\\/html)|(image/(\\*|x-icon))|(\\*/\\*)");
            Matcher m = acceptLine.matcher(header[i]);
            if (m.find()) accpetsType = true;
        }

        if (!accpetsType) {
            send(output, version, new Status(415));
            return;
        }

        if (resource == null) {
            send(output, version, new Status(404));
            return;
        }

        StringBuilder builder = new StringBuilder();

        builder.append(version.toString()).append(" ").append(new Status(200).toString()).append("\r\n");

        String contentType = resource.getContentType();
        String body = resource.write();
        int contentLength = body.getBytes().length;

        builder.append("Content-Length: ").append(contentLength).append("\r\n");
        builder.append("Content-Type: ").append(contentType).append("\r\n");
        builder.append("\r\n");
        builder.append(body);

        PrintStream stream = new PrintStream(output);
        stream.print(builder.toString());
        stream.flush();
    }

    private void respondToPost(OutputStream output, String[] header, BufferedReader reader) throws IOException {
        String path = getPath(header[0]);

        RestResource resource = RestResource.getResourceFromPath(context, path);

        if (resource == null) {
            send(output, version, new Status(404));
            return;
        }

        Pattern typePattern = Pattern.compile("^Content-Type: application/x-www-form-urlencoded");
        Pattern lengthPattern = Pattern.compile("^Content-Length: (\\d+)");
        Matcher m;
        boolean validType = false;
        int length = -1;
        for (String s : header) {
            m = typePattern.matcher(s);

            if (m.find()) {
                validType = true;
                continue;
            }

            m = lengthPattern.matcher(s);
            if (m.find()) {
                length = Integer.parseInt(m.group(1));
            }
        }

        if (!validType || length < 0) {
            send(output, version, new Status(400));
            return;
        }

        String body = readBody(reader, length);

        resource.setFromPost(body);

        respondToGet(output, header);
    }

    private String getPath(String line) {
        Pattern pathPattern = Pattern.compile("\\s(/[\\w\\.\\/]*)\\s");
        Matcher pathMatcher = pathPattern.matcher(line);

        if (pathMatcher.find()) {
            return pathMatcher.group(1);
        }

        return null;
    }

    private String readBody(BufferedReader reader, int length) throws IOException {
        StringBuilder builder = new StringBuilder();
        int read = 0;
        char[] buf;
        while (read < length) {
            buf = new char[length-read];
            read += reader.read(buf, 0, length-read);
            builder.append(buf);
        }
        return builder.toString();
    }

    @Override
    protected void onPostExecute(Void ignore) {
        thread.removeTask(this);
    }

    private static class Status {
        private int code;

        private Status(int code) {
            this.code = code;
        }

        public String toString() {
            String result = code+" ";
            switch (code) {
                case 100: result+="Continue"; break;
                case 101: result+="Switching Protocols"; break;
                case 200: result+="OK"; break;
                case 201: result+="Created"; break;
                case 202: result+="Accepted"; break;
                case 400: result+="Bad Request"; break;
                case 401: result+="Unauthorized"; break;
                case 404: result+="Not found"; break;
                case 405: result+="Method not Allowed"; break;
                case 415: result+="Unsupported Media Type"; break;
            }
            return result;
        }
    }

    private static class Version {
        private int version;

        private static final int V11 = 11;
        private static final int V20 = 20;

        Version(int version) {
            this.version = version;
        }

        @Nullable
        public String toString() {
            switch (version) {
                case V11:
                    return "HTTP/1.1";
                default:
                    return null;
            }
        }
    }
}
