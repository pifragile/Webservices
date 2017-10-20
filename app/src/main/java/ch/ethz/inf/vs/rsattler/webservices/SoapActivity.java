package ch.ethz.inf.vs.rsattler.webservices;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import ch.ethz.inf.vs.a2.sensor.SensorListener;

//LS
public class SoapActivity extends AppCompatActivity implements SensorListener {

    private String NAMESPACE = "http://webservices.vslecture.vs.inf.ethz.ch/";
    private String METHOD1_NAME = "getDiscoveredSpots";
    private String METHOD3_NAME = "getSpot";

    private String SOAP_ACTION1 = "http://webservices.vslecture.vs.inf.ethz.ch/SunSPOTWebservice/getDiscoveredSpots";
    private String SOAP_ACTION3 = "http://webservices.vslecture.vs.inf.ethz.ch/SunSPOTWebservice/getSpot";

    private String URL = "http://vslab.inf.ethz.ch:8080/SunSPOTWebServices/SunSPOTWebservice?WSDL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soap);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Log.wtf("TAG", "UNTIL HERE, IT WORKS!!");


        //LS
        // NETWORK OPERATIONS ARE NOT ALLOWED IN THE MAIN THREAD.
        AsyncTask.execute(() -> {

            // 1. CHECK IF BOTH SPOTS ARE AVAILABLE.
            //----------------------------------------------------------------------------------------------------
            SoapObject requestSpots = new SoapObject(NAMESPACE, METHOD1_NAME);

            // HAVE TO USE VERSION 10 FOR SOME REASON.
            SoapSerializationEnvelope envReqSpots = new SoapSerializationEnvelope(SoapEnvelope.VER10);
            envReqSpots.dotNet = true;
            envReqSpots.setOutputSoapObject(requestSpots);
            HttpTransportSE httpTransport = new HttpTransportSE(URL);

            try {
                httpTransport.call(SOAP_ACTION1, envReqSpots);
            } catch(Exception e) {
                e.printStackTrace();
            }
            Log.wtf("TAG", envReqSpots.bodyIn.toString());



            // 2. DECIDE ON AVAILABLE SPOTS WHAT TO DO.
            //----------------------------------------------------------------------------------------------------
            SoapObject reqData = new SoapObject(NAMESPACE, METHOD3_NAME);
            PropertyInfo property = new PropertyInfo();
            {
                property.name = "id";
                property.setNamespace(NAMESPACE);
                property.type = PropertyInfo.STRING_CLASS;
                property.setValue("Spot4");
            }
            reqData.addProperty(property);

            SoapSerializationEnvelope envReqData = new SoapSerializationEnvelope(SoapEnvelope.VER10);
            envReqData.dotNet = true;
            envReqData.setOutputSoapObject(reqData);
            HttpTransportSE httpTransport2 = new HttpTransportSE(URL, 20000);

            Log.wtf("TAG", "2. UNTIL HERE, IT WORKS!!");
            try {
                httpTransport2.call(SOAP_ACTION3, envReqData);
            } catch(Exception e) {
                e.printStackTrace();
            }

        });

    }



    public void onReceiveSensorValue(double value) {

    }

    public void onReceiveMessage(String message) {

    }

}

