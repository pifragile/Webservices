package ch.ethz.inf.vs.a2.solution.http;

import android.util.Log;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import java.net.HttpURLConnection;
import org.json.*;

import ch.ethz.inf.vs.a2.sensor.AbstractSensor;

/**
 * Created by pieroguicciardi on 13.10.17.
 */

public class JsonSensor extends AbstractSensor {
    @Override
    public String executeRequest() throws Exception {
        URLConnection connection = new URL("http://vslab.inf.ethz.ch:8081/sunspots/Spot1/sensors/temperature").openConnection();
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Connection", "close");
        InputStream response = connection.getInputStream();
        try (Scanner scanner = new Scanner(response)) {
            String responseBody = scanner.useDelimiter("\\A").next();
            return responseBody;
        }
    }

    @Override
    public double parseResponse(String response) {
        Log.i("response",response);
        try {
            JSONObject obj = new JSONObject(response);
            return obj.getDouble("value");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return 0;
    }
}
