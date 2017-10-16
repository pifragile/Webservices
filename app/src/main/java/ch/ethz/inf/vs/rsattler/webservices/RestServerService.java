package ch.ethz.inf.vs.rsattler.webservices;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class RestServerService extends Service {

    private static final int NOTIFICATION_ID = 10;

    public RestServerService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        NetworkInterface networkInterface;

        try {
            networkInterface = NetworkInterface.getByName("wlan0");
            if (networkInterface == null) {
                Log.d("Service", "wlan0 not found");
                networkInterface = NetworkInterface.getByIndex(0);
                Log.d("Service", "using "+networkInterface.getName());
            }

            InetAddress address = networkInterface.getInetAddresses().nextElement();

            try (ServerSocket socket = new ServerSocket(8088, 50, address)) {

            } catch (IOException e) {
                e.printStackTrace();
            }

        } catch (SocketException e) {
            e.printStackTrace();
        }

        showNotification();

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("ch.ethz.inf.vs.rsattler.webservices.SERVER_CONFIGURATION");
        broadcastIntent.putExtra("ip", "test");
        broadcastIntent.putExtra("port", "8888");
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

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
