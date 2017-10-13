package ch.ethz.inf.vs.a2.solution.http;

import android.util.Log;

import ch.ethz.inf.vs.a2.sensor.AbstractSensor;
import ch.ethz.inf.vs.a2.solution.http.HttpRawRequestImpl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;


/**
 * Created by pieroguicciardi on 13.10.17.
 */

public class RawHttpSensor extends AbstractSensor{
    @Override
    public String executeRequest() throws Exception {
        HttpRawRequestImpl r = new HttpRawRequestImpl();
        String req = r.generateRequest("vslab.inf.ethz.ch",8081,"/sunspots/Spot1/sensors/temperature");
        Socket s = new Socket(InetAddress.getByName("129.132.130.223"), 8081);
        PrintWriter pw = new PrintWriter(s.getOutputStream());
        pw.print(req);
        pw.flush();
        BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        String t,q;
        q = "";
        while((t = br.readLine()) != null) {q+=t;Log.i("Test",t);}
        Log.i("Response",q);
        br.close();
        return q;
    }

    @Override
    public double parseResponse(String response) {
        String s = response.substring(response.lastIndexOf("<li class = \"getter\">Temperature Getter: <span class=\"getterValue\">") + 67).substring(0,4);
        return Double.parseDouble(s);
    }

}


