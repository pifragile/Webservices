package ch.ethz.inf.vs.a2.sensor;

//LS
// Write a class SoapSensor that extends AbstractSensor. Use the provided ksoap-2 library and use
// the XML template provided by the Tester, to figure out which parameters to set in the SOAPObject.
public class SoapSensor extends AbstractSensor {

    public double parseResponse(String response) {
        return 1;
    }

    public String executeRequest() {
        return "Hello World";
    }

}
