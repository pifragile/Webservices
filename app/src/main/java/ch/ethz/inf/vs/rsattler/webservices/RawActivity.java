package ch.ethz.inf.vs.rsattler.webservices;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import org.w3c.dom.Text;

import ch.ethz.inf.vs.a2.http.HttpRawRequest;
import ch.ethz.inf.vs.a2.sensor.SensorListener;
import ch.ethz.inf.vs.a2.solution.http.JsonSensor;
import ch.ethz.inf.vs.a2.solution.http.RawHttpSensor;
import ch.ethz.inf.vs.a2.solution.http.TextSensor;

public class RawActivity extends AppCompatActivity implements SensorListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raw);

        final RawHttpSensor hrr = new RawHttpSensor();

        hrr.registerListener(this);
        hrr.getTemperature();
    }

    @Override
    public void onReceiveSensorValue(double value) {
        TextView t=(TextView)findViewById(R.id.textView2);
        t.setText(Double.toString(value));
    }

    @Override
    public void onReceiveMessage(String message) {

    }
}