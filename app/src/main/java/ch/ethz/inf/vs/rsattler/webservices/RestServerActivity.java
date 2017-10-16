package ch.ethz.inf.vs.rsattler.webservices;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class RestServerActivity extends AppCompatActivity {

    BroadcastReceiver receiver;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_server);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("TEST", "Broadcast received");
            }
        };
        IntentFilter filter = new IntentFilter("ch.ethz.inf.vs.rsattler.webservices.SERVER_CONFIGURATION");
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    public void toggleService(View view) {
        // if stop if service is running
        boolean serviceStopped = stopService(new Intent(this, RestServerService.class));

        // start service only if it wasn't running
        if (!serviceStopped) {
            Intent serviceIntent = new Intent(this, RestServerService.class);
            startService(serviceIntent);
        }
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onDestroy();
    }
}
