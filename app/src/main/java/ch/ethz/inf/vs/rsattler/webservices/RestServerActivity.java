package ch.ethz.inf.vs.rsattler.webservices;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class RestServerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_server);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
