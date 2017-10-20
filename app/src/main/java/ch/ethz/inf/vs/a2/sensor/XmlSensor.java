package ch.ethz.inf.vs.a2.sensor;


//LS
public class XmlSensor extends AbstractSensor {

    // for the implementation of the parseResponse(String response) method use the XmlPullParser,
    // which is also delivered by Android.
    public double parseResponse(String response) {
        //setDoOutput(true);
        return 1;
    }

    public String executeRequest() {
        return "Hello World";
    }
}
