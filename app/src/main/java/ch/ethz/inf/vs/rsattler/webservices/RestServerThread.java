package ch.ethz.inf.vs.rsattler.webservices;

import android.os.AsyncTask;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.LinkedList;


class RestServerThread extends Thread {

    private ServerSocket serverSocket;
    private LinkedList<AsyncTask> tasks = new LinkedList<>();

    RestServerThread(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void run() {
        try {
            serverSocket.setSoTimeout(50);

            while (!interrupted()) {
                try {
                    Socket socket = serverSocket.accept();

                    RestResponseTask task = new RestResponseTask(this);
                    task.execute(socket);
                    addTask(task);
                } catch (SocketTimeoutException e) {
                    //ignore
                }
            }

            for (AsyncTask t: tasks) {
                t.cancel(true);
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addTask(AsyncTask task) {
        this.tasks.add(task);
    }

    public void removeTask(AsyncTask task) {
        this.tasks.remove(task);
    }
}
