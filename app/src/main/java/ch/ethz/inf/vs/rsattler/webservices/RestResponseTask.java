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
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Task which handles one specific HTTP Request
 */
class RestResponseTask extends AsyncTask<Socket, Void, Void> {

    private Context context; // necessary further down the call chain
    private RestServerThread thread; // Parent thread

    /**
     * HTTP version
     */
    private Version version = new Version(Version.V11);

    /**
     * @param context Context necessary further down the call chain
     * @param thread Parent Thread
     */
    RestResponseTask(Context context, RestServerThread thread) {
        this.context = context;
        this.thread = thread;
    }

    @Override
    protected Void doInBackground(Socket... sockets) {
        Socket socket = sockets[0];

        try (InputStream request = socket.getInputStream()) {

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(request))) {
                // Read the header
                String[] lines = readHeader(reader);

                // Check if HTTP Request matches the supported methods, version and is correctly formated
                // if the Request doesn't match send error 400
                Pattern requestPattern = Pattern.compile("^((GET)|(POST))\\s\\/[\\w\\.\\/]*\\sHTTP\\/(1\\.1)$");
                Matcher requestMatcher = requestPattern.matcher(lines[0]);

                if (!requestMatcher.find()) {
                    try (OutputStream output = socket.getOutputStream()) {
                        sendStatus(output, version, new Status(400));
                    }
                    return null;
                }

                // Handle Request differently depending on the specified method
                Pattern methodPattern = Pattern.compile("(^GET)|(^POST)");
                Matcher methodMatcher = methodPattern.matcher(lines[0]);

                try (OutputStream output = socket.getOutputStream()) {
                    if (methodMatcher.find()) {
                        if (methodMatcher.group(1) != null) {
                            respondToGet(output, lines);
                        } else {
                            respondToPost(output, lines, reader);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Reads an HTTP Header from a BufferedReader
     * @param reader BufferedReader of the HTTP Request
     * @return String[] containing the lines of the Header
     * @throws IOException BufferedReader might throw exception
     */
    private String[] readHeader(BufferedReader reader) throws IOException {
        List<String> header = new LinkedList<>();
        String lastLines = null;
        // if the last line was empty, the end of the header was reached
        while (!"".equals(lastLines)) {
            String newLine = reader.readLine();
            lastLines = newLine;
            header.add(newLine);
        }
        return header.toArray(new String[header.size()]);
    }

    /**
     * Sends a response containing just a status without any further header lines or body
     * @param output OutputStream to be written to
     * @param version HTTP version
     * @param status HTTP status
     */
    private void sendStatus(OutputStream output, Version version, Status status) {
        PrintStream stream = new PrintStream(output);
        stream.print(version.toString()+" "+status.toString()+"\r\n\r\n");
    }

    /**
     * Handle GET Requests
     * @param output OutputStream to be written to
     * @param header The already read lines of the header
     */
    private void respondToGet(OutputStream output, String[] header) {
        // Extract the resource path from the request
        String path = getPath(header[0]);

        // Get RestResource corresponding to path
        RestResource resource = RestResource.getResourceFromPath(context, path);

        if (resource == null) {
            sendStatus(output, version, new Status(404));
            return;
        }

        // Check if the Request accepts the correct Content-Type
        boolean acceptsType = false;
        for (int i = 1; i < header.length; i++) {
            Pattern acceptLine = Pattern.compile("^Accept:(.*) (text\\/html)|(image/(\\*|x-icon))|(\\*/\\*)");
            Matcher m = acceptLine.matcher(header[i]);
            if (m.find()) acceptsType = true;
        }

        if (!acceptsType) {
            sendStatus(output, version, new Status(415));
            return;
        }

        // Build response
        StringBuilder builder = new StringBuilder();

        builder.append(version.toString()).append(" ").append(new Status(200).toString()).append("\r\n");

        String contentType = resource.getContentType();
        String body = resource.toHtml();
        int contentLength = body.getBytes().length;

        builder.append("Content-Length: ").append(contentLength).append("\r\n");
        builder.append("Content-Type: ").append(contentType).append("\r\n");
        builder.append("\r\n");
        builder.append(body);

        PrintStream stream = new PrintStream(output);
        stream.print(builder.toString());
        stream.flush();
    }

    /**
     * Respond to POST Request
     * @param output OutputStream to be written to
     * @param header The already read lines of the header
     * @param reader BufferedReader to read the body from
     * @throws IOException BufferedReader might throw exception
     */
    private void respondToPost(OutputStream output, String[] header, BufferedReader reader) throws IOException {
        String path = getPath(header[0]);

        RestResource resource = RestResource.getResourceFromPath(context, path);

        if (resource == null) {
            sendStatus(output, version, new Status(404));
            return;
        }

        // only accept Content-Type application/x-www-form-urlencoded
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
            sendStatus(output, version, new Status(400));
            return;
        }

        String body = readBody(reader, length);

        // Set Resource from POST Request body
        resource.setFromPost(body);

        respondToGet(output, header);
    }

    /**
     * Extract the path from the header line
     * @param line HTTP Request line
     * @return String path
     */
    private String getPath(String line) {
        Pattern pathPattern = Pattern.compile("\\s(/[\\w\\.\\/]*)\\s");
        Matcher pathMatcher = pathPattern.matcher(line);

        if (pathMatcher.find()) {
            return pathMatcher.group(1);
        }

        return null;
    }

    /**
     * Read the body from the HTTP Request
     * @param reader BufferedRead to be read from
     * @param length The length of the body
     * @return The body of the message
     * @throws IOException BufferedReader might throw exception
     */
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

    /**
     * If the task has finished, remove from parent Thread
     * @param ignore
     */
    @Override
    protected void onPostExecute(Void ignore) {
        thread.removeTask(this);
    }

    /**
     * Container for status codes
     */
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

    /**
     * Container for HTTP versions
     */
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
