package ch.ethz.inf.vs.rsattler.webservices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.net.InetAddress;

/**
 * Activity to start and stop the RestServerService
 */
public class RestServerActivity extends AppCompatActivity {

    /**
     * BroadcastReceiver to get the address and port from the Service
     */
    BroadcastReceiver configReceiver;
    /**
     * Receiver to get the stop message from the Service
     */
    BroadcastReceiver stopReceiver;

    TextView addressView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_server);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        addressView = (TextView) findViewById(R.id.address_view);

        // Setup the config and stop Receivers
        configReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                InetAddress address = (InetAddress) intent.getSerializableExtra("ip");
                int port = intent.getIntExtra("port", -1);

                addressView.setText(address.getHostAddress()+":"+port);
            }
        };

        stopReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                addressView.setText("");
            }
        };

        IntentFilter configFilter = new IntentFilter("ch.ethz.inf.vs.rsattler.webservices.SERVER_CONFIGURATION");
        IntentFilter stopFilter = new IntentFilter("ch.ethz.inf.vs.rsattler.webservices.SERVER_STOPPED");
        LocalBroadcastManager.getInstance(this).registerReceiver(configReceiver, configFilter);
        LocalBroadcastManager.getInstance(this).registerReceiver(stopReceiver, stopFilter);

        // Request the current configuration, answer expected only if the Service is running
        sendConfigRequest();
    }

    /**
     * Send the ConfigRequest to the Service. SERVER_CONFIGURATION broadcast expected if the Service is sunning
     */
    public void sendConfigRequest() {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("ch.ethz.inf.vs.rsattler.webservices.CONFIG_REQUEST");
        LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
    }

    /**
     * Button ClickHandler to toggle the Service
     * @param view
     */
    public void toggleService(View view) {
        // stop if service is running
        boolean serviceStopped = stopService(new Intent(this, RestServerService.class));

        // start service only if it wasn't running
        if (!serviceStopped) {
            Intent serviceIntent = new Intent(this, RestServerService.class);
            startService(serviceIntent);
        }
    }

    @Override
    public void onDestroy() {
        // unregister the BroadcastReceivers
        LocalBroadcastManager.getInstance(this).unregisterReceiver(configReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(stopReceiver);
        super.onDestroy();
    }
}
