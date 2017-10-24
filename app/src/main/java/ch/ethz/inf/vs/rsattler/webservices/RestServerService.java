package ch.ethz.inf.vs.rsattler.webservices;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Scanner;

/**
 * Service to provide the RestServer
 */
public class RestServerService extends Service {

    /**
     * BroadcastReceiver to receive configuration requests
     */
    BroadcastReceiver receiver;

    private static final int NOTIFICATION_ID = 10;

    private ServerSocket serverSocket;
    private InetAddress address;
    private int port = 8088;

    /**
     * Thread in which the Server is running
     */
    private Thread serverThread;

    public RestServerService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            // Get the Wifi NetworkInterface
            NetworkInterface networkInterface = NetworkInterface.getByName("wlan0");

            // If the Interface isn't null get ServerSocket and start RestServerThread
            // else show toast to ask the user to connect to Wifi first
            if (networkInterface != null || networkInterface.getInetAddresses().hasMoreElements()) {
                address = networkInterface.getInetAddresses().nextElement();
                sendBroadcast();

                this.serverSocket = new ServerSocket(port, 50, address);

                serverThread = new RestServerThread(this, serverSocket);
                serverThread.start();

                showNotification();
            } else {
                Toast toast = Toast.makeText(this, "Connect to WiFi first", Toast.LENGTH_LONG);
                toast.show();
                this.stopSelf();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        // Configure the BroadcastReceiver to handle CONFIG_REQUEST
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                sendBroadcast();
            }
        };

        IntentFilter configFilter = new IntentFilter("ch.ethz.inf.vs.rsattler.webservices.CONFIG_REQUEST");
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, configFilter);

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Sends Broadcast containing the address and the port on which the Server is running.
     */
    private void sendBroadcast() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("ch.ethz.inf.vs.rsattler.webservices.SERVER_CONFIGURATION");
        broadcastIntent.putExtra("ip", address);
        broadcastIntent.putExtra("port", port);
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }

    @Override
    public void onDestroy() {
        // Unregister the BroadcastReceiver
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);

        // Destroy the notification
        destroyNotification();
        if (serverThread != null) {
            serverThread.interrupt();
        }

        // Close ServerSocket
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Send SERVER_STOPPED broadcast
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("ch.ethz.inf.vs.rsattler.webservices.SERVER_STOPPED");
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }

    /**
     * Show a notification with the current server configuration
     */
    private void showNotification() {
        android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_server)
                .setContentTitle("REST Server")
                .setContentText("Server address: "+address.getHostAddress()+":"+port)
                .setOngoing(true);

        Intent newIntent = new Intent(this, RestServerActivity.class);
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        builder.setContentIntent(PendingIntent.getActivity(this, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID, builder.build());
    }

    /**
     * Destroys the notification
     */
    private void destroyNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_ID);
    }

}
