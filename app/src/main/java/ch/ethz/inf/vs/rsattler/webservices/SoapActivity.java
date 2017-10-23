package ch.ethz.inf.vs.rsattler.webservices;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import ch.ethz.inf.vs.a2.sensor.AbstractSensor;
import ch.ethz.inf.vs.a2.sensor.SensorListener;
import ch.ethz.inf.vs.a2.sensor.SoapSensor;
import ch.ethz.inf.vs.a2.sensor.XmlSensor;

public class SoapActivity extends AppCompatActivity implements SensorListener {

    SoapSensor soapSensor;
    XmlSensor xmlSensor;
    AbstractSensor currentSensor;
    TextView t1;
    TextView t2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soap);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // initialize soap sensor.
        soapSensor = new SoapSensor();
        xmlSensor = new XmlSensor();
        currentSensor = soapSensor; // default.

    }

    public void askXmlSensor(View view) {

        currentSensor.unregisterListener(this);
        currentSensor = xmlSensor;
        currentSensor.registerListener(this);
        currentSensor.getTemperature();
        Log.wtf("Tag", "Xml sensor asked");
    }

    //LS
    public void askSoapSensor(View view) {

        currentSensor.unregisterListener(this);
        currentSensor = soapSensor;
        currentSensor.registerListener(this);
        currentSensor.getTemperature();
        Log.wtf("Tag", "Soap sensor asked");
    }

    public void onReceiveSensorValue(double value) {
            t1 = (TextView)findViewById(R.id.textView5);
            t1.setText(Double.toString(value));
            Log.wtf("Tag", "Sensor value received.");
    }

    public void onReceiveMessage(String message) {

        // update of text view must be declared like that, otherwise runs on wrong thread.
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                t2 = (TextView)findViewById(R.id.textView6);
                t2.setText(message);
            }
        });
        Log.wtf("Tag", message);
    }
}

