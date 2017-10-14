package ch.ethz.inf.vs.rsattler.webservices;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.w3c.dom.Text;

import ch.ethz.inf.vs.a2.http.HttpRawRequest;
import ch.ethz.inf.vs.a2.sensor.SensorListener;
import ch.ethz.inf.vs.a2.solution.http.JsonSensor;
import ch.ethz.inf.vs.a2.solution.http.RawHttpSensor;
import ch.ethz.inf.vs.a2.solution.http.TextSensor;

public class RestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    public void raw(View view) {
        Intent intent = new Intent(this, RawActivity.class);
        startActivity(intent);
    }
    public void text(View view) {
        Intent intent = new Intent(this, TextActivity.class);
        startActivity(intent);
    }

    public void json(View view) {
        Intent intent = new Intent(this, JsonActivity.class);
        startActivity(intent);
    }
}
