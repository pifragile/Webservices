package ch.ethz.inf.vs.a2.solution.http;

import ch.ethz.inf.vs.a2.sensor.AbstractSensor;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

/**
 * Created by pieroguicciardi on 13.10.17.
 */

public class TextSensor extends AbstractSensor {
    @Override
    public String executeRequest() throws Exception {
        URLConnection connection = new URL("http://vslab.inf.ethz.ch:8081/sunspots/Spot1/sensors/temperature").openConnection();
        connection.setRequestProperty("Accept", "text/plain");
        connection.setRequestProperty("Connection", "close");
        InputStream response = connection.getInputStream();
        try (Scanner scanner = new Scanner(response)) {
            String responseBody = scanner.useDelimiter("\\A").next();
            return responseBody;
        }
    }

    @Override
    public double parseResponse(String response) {
        return Double.parseDouble(response);
    }
}
