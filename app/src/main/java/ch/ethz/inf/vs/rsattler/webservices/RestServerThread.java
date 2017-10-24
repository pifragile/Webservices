package ch.ethz.inf.vs.rsattler.webservices;

import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;


/**
 * Thread which waits for HTTP Requests and hands them to a RrestResponseTask
 */
class RestServerThread extends Thread {

    private ServerSocket serverSocket;
    private Context context; // necessary further down the call chain
    private LinkedList<AsyncTask> tasks = new LinkedList<>(); // necessary when interrupted

    /**
     * @param context Context required further down the call chain
     * @param serverSocket ServerSocket on which to listen
     */
    RestServerThread(Context context, ServerSocket serverSocket) {
        this.context = context;
        this.serverSocket = serverSocket;
    }

    public void run() {
        try {
            // timeout necessary to interrupt the thread gracefully
            serverSocket.setSoTimeout(50);

            while (!interrupted()) {
                try {
                    Socket socket = serverSocket.accept();

                    RestResponseTask task = new RestResponseTask(context, this);
                    task.execute(socket);
                    addTask(task);
                } catch (SocketTimeoutException e) {
                    //ignore
                }
            }

            // reachable when interrupted
            // cancel all running tasks
            for (AsyncTask t: tasks) {
                t.cancel(true);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Add Task to the list of running tasks
     * @param task Task to be added
     */
    private void addTask(AsyncTask task) {
        this.tasks.add(task);
    }

    /**
     * Called from tasks when finished
     * @param task Task to be removed
     */
    void removeTask(AsyncTask task) {
        this.tasks.remove(task);
    }
}
