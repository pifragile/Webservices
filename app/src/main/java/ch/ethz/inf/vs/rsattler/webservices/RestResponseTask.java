package ch.ethz.inf.vs.rsattler.webservices;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;


public class RestResponseTask extends AsyncTask<Socket, Void, Void> {

    private RestServerThread thread;

    RestResponseTask(RestServerThread thread) {
        this.thread = thread;
    }

    @Override
    protected Void doInBackground(Socket... sockets) {
        Socket socket = sockets[0];

        try {
            InputStream request = socket.getInputStream();
            try (Scanner scanner = new Scanner(request)) {
                scanner.useDelimiter("\r\n\r\n");
                if (scanner.hasNext()) {
                    String header = scanner.next();
                    Log.d("Request", header);

                    try (OutputStream output = socket.getOutputStream()) {
                        PrintStream stream = new PrintStream(output);
                        stream.println("HTTP/1.1 200 OK");
                        stream.println("Content-Type: text/html");
                        stream.println("\r\n");
                        stream.println("<p> Hello world </p>");
                        stream.flush();
                    }
                }
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void ignore) {
        thread.removeTask(this);
    }
}
