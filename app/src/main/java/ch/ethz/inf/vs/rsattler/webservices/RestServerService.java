package ch.ethz.inf.vs.rsattler.webservices;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

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

public class RestServerService extends Service {

    private ServerSocket serverSocket;
    private static final int NOTIFICATION_ID = 10;

    private Thread serverThread;

    public RestServerService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        NetworkInterface networkInterface;

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);

        try {
            networkInterface = NetworkInterface.getByName("wlan0");
            if (networkInterface == null) {
                networkInterface = NetworkInterface.getByIndex(0);
            }

            InetAddress address = networkInterface.getInetAddresses().nextElement();

            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction("ch.ethz.inf.vs.rsattler.webservices.SERVER_CONFIGURATION");
            broadcastIntent.putExtra("ip", address);
            broadcastIntent.putExtra("port", 8088);
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

            this.serverSocket = new ServerSocket(8088, 50, address);

            serverThread = new RestServerThread(serverSocket);
            serverThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

        showNotification();

        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        destroyNotification();
        serverThread.interrupt();

        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showNotification() {
        android.support.v4.app.NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_server)
                .setContentTitle("REST Server")
                .setContentText("The REST server is currently running.")
                .setOngoing(true);

        Intent newIntent = new Intent(this, RestServerActivity.class);
        newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        builder.setContentIntent(PendingIntent.getActivity(this, 0, newIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(NOTIFICATION_ID, builder.build());
    }

    private void destroyNotification() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(NOTIFICATION_ID);
    }

}
