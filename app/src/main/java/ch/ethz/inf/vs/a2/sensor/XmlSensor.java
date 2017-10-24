package ch.ethz.inf.vs.a2.sensor;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;

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
    private String spot = "";

    String temp;

    @Override
    public double parseResponse(String response) {

        String temperature = "0";
        Boolean rightPlace = false;
        try {
            XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
            //parserFactory.setNamespaceAware(true);
            XmlPullParser pullParser = parserFactory.newPullParser();
            pullParser.setInput(new StringReader(response));

            int eventType = pullParser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG && pullParser.getName().equals("temperature")) rightPlace = true;
                if (eventType == XmlPullParser.TEXT && rightPlace) {
                    temperature = pullParser.getText();
                    break;
                }
                eventType = pullParser.next();
            }
        }
        catch (XmlPullParserException e) { e.printStackTrace(); }
        catch (IOException e) { e.printStackTrace();}

        return Double.parseDouble(temperature);
    }

    @Override
    public String executeRequest() throws Exception {

        // get the xml queries.
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
        //} catch (Exception e) {}

        // check which spots are available.
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
        sendMessage("Value retrieved from " + spot + " (XMLSensor)");
        return response2;

        /*
        // version without xml pull parser.
        int x;
        for (x=0; x<response2.length(); x++) { if (response2.regionMatches(x,"<temperature>",0,13)) break; }
        response22 = response2.substring(x+13);
        Log.wtf("tag", response22);
        for (x=0; x<response22.length(); x++) { if (response22.regionMatches(x,"</temperature>",0,14)) break; }
        response23 = response22.substring(0,x);
        Log.wtf("tag", response23);

        sendMessage("Value retrieved from " + spot + " (XMLSensor)");
        return response23;
        */
    }

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
