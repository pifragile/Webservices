package ch.ethz.inf.vs.rsattler.webservices;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RestResponseTask extends AsyncTask<Socket, Void, Void> {

    private RestServerThread thread;

    RestResponseTask(RestServerThread thread) {
        this.thread = thread;
    }

    @Override
    protected Void doInBackground(Socket... sockets) {
        Socket socket = sockets[0];

        try (InputStream request = socket.getInputStream()) {
            try (Scanner scanner = new Scanner(request)) {
                scanner.useDelimiter("\r\n\r\n");

                if (scanner.hasNext()) {
                    String header = scanner.next();
                    Log.d("Service", header);

                    Response response = new Request(header).process().getResponse();

                    try (OutputStream output = socket.getOutputStream()) {
                        PrintStream stream = new PrintStream(output);
                        stream.print(response.compile());
                        stream.flush();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void ignore) {
        thread.removeTask(this);
    }

    private static class Request {
        String[] lines;
        Method method;
        String path;

        int status = 100;

        enum Method {
            GET, POST
        }

        Request(String header) {
            lines = header.split(System.getProperty("line.separator"));

            Pattern requestPattern = Pattern.compile("^((GET)|(POST))\\s\\/[\\w\\.\\/]*\\sHTTP\\/(1\\.1)|(2)$");
            Matcher requestMatcher = requestPattern.matcher(lines[0]);

            if (!requestMatcher.find()) {
                status = 400;
                return;
            }

            Pattern getRequest = Pattern.compile("^GET");
            Pattern postRequest = Pattern.compile("^POST");
            Matcher getMatcher;
            Matcher postMatcher;

            getMatcher = getRequest.matcher(lines[0]);
            postMatcher = postRequest.matcher(lines[0]);

            if (getMatcher.find()) {
                method = Method.GET;
            } else if (postMatcher.find()) {
                method = Method.POST;
            } else {
                status = 405;
                return;
            }

            Pattern pathPattern = Pattern.compile("\\s/[\\w\\.\\/]*\\s");
            Matcher pathMatcher = pathPattern.matcher(lines[0]);

            if (pathMatcher.find()) {
                this.path = pathMatcher.group(0).trim();
            } else {
                status = 404;
            }
        }

        Request process() {
            if (status > 399) return this;

            switch (method) {
                case GET:
                    return processGetRequest();
                case POST:
                    return processPostRequest();
                default:
                    return this;
            }
        }

        private Request processGetRequest() {

            boolean acceptsHtml = false;
            for (int i = 1; i < lines.length; i++) {
                Pattern acceptLine = Pattern.compile("^Accept:(.*)text\\/html");
                Matcher m = acceptLine.matcher(lines[i]);
                if (m.find()) acceptsHtml = true;
            }

            if (!acceptsHtml) {
                this.status = 415;
                return this;
            }

            return this;
        }

        private Request processPostRequest() {
            return this;
        }

        public Response getResponse() {
            return new Response();
        }
    }

    private static class Response {

        private static final String newLine = System.getProperty("line.separator");

        public String compile() {
            StringBuilder builder = new StringBuilder();
            builder.append("HTTP/1.1 200 OK").append(newLine);
            builder.append("Content-Type: text/html").append(newLine);
            builder.append("\r\n").append(newLine);
            builder.append("<p> Hello world! </p>").append(newLine);
            return builder.toString();
        }

    }
}
