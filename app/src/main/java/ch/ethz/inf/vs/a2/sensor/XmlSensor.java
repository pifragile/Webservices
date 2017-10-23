package ch.ethz.inf.vs.a2.sensor;


import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Vector;

import ch.ethz.inf.vs.rsattler.webservices.R;
//LS

public class XmlSensor extends AbstractSensor {

    private URL url;
    private HttpURLConnection connection;
    private OutputStream outputStream;
    private BufferedReader buffReader;
    private String req1;
    private String req2;
    private String req21;
    private String req22;
    private String response1 = "";
    private String response2 = "";
    private String response22 = "";
    private String response23 = "";
    private String spot = "";

    String temp;
    private int i = 0;

    // for the implementation of the parseResponse(String response) method use the XmlPullParser,
    // which is also delivered by Android.
    @Override
    public double parseResponse(String response) {
        return Double.parseDouble(response);
    }

    //@RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public String executeRequest() throws Exception {

        setStrings();

        // 1. CHECK WHICH SPOTS ARE AVAILABLE.
        //----------------------------------------------------------------------------------------------------
        //try {
        url = new URL("http://vslab.inf.ethz.ch:8080/SunSPOTWebServices/SunSPOTWebservice");
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
        connection.setFixedLengthStreamingMode(req1.getBytes("UTF-8").length);
        outputStream = connection.getOutputStream();
        outputStream.write(req1.getBytes("UTF-8"));
        outputStream.close();

        // read the input char by char.
        buffReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        while((temp = buffReader.readLine()) != null) response1 += temp;
        buffReader.close();
        Log.wtf("tag", response1);
        //} catch (Exception e) {}

        // "analyze" the answer.
        if (response1.contains("Spot3")) { spot = "Spot3"; req2 = req21 + spot + req22;}
        else if(response1.contains("Spot4")) { spot = "Spot4"; req2 = req21 + spot + req22;}
        else { sendMessage("No Spot could be contacted."); return "0";}

        // 2. QUERY THE CHOSEN SPOT.
        //----------------------------------------------------------------------------------------------------
        url = new URL("http://vslab.inf.ethz.ch:8080/SunSPOTWebServices/SunSPOTWebservice");
        connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setRequestProperty("Content-Type", "text/xml; charset=UTF-8");
        connection.setFixedLengthStreamingMode(req2.getBytes("UTF-8").length);
        outputStream = connection.getOutputStream();
        outputStream.write(req2.getBytes("UTF-8"));
        outputStream.close();

        // read the input char by char.
        buffReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        while((temp = buffReader.readLine()) != null) response2 += temp;
        buffReader.close();
        Log.wtf("tag", response2);

        // "analyze" the answer.
        int x;
        for (x=0; x<response2.length(); x++) { if (response2.regionMatches(x,"<temperature>",0,13)) break; }
        response22 = response2.substring(x+13);
        Log.wtf("tag", response22);
        for (x=0; x<response22.length(); x++) { if (response22.regionMatches(x,"</temperature>",0,14)) break; }
        response23 = response22.substring(0,x);
        Log.wtf("tag", response23);

        sendMessage("Value retrieved super fast from " + spot);
        return response23;



    //return "0";
    }

    // tried to read the strings directly from text files, but did not work properly.
    public void setStrings() {

        // getDiscoveredSpots()
        req1 =  "<?xml version=\"1.0\" encoding=\"UTF-8\"?><S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">";
        req1 += "<S:Header/> <S:Body>";
        req1 += "<ns2:getDiscoveredSpots xmlns:ns2=\"http://webservices.vslecture.vs.inf.ethz.ch/\"/>";
        req1 += "</S:Body></S:Envelope>";
        // getSpot(..), part 1
        req21 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><S:Envelope xmlns:S=\"http://schemas.xmlsoap.org/soap/envelope/\">";
        req21 += "<S:Header/><S:Body>";
        req21 += "<ns2:getSpot xmlns:ns2=\"http://webservices.vslecture.vs.inf.ethz.ch/\"><id>";
        // getSpot(..), part 2
        req22 = "</id></ns2:getSpot></S:Body></S:Envelope>";
    }




}
