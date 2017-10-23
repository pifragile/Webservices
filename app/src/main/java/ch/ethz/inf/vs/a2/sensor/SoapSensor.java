package ch.ethz.inf.vs.a2.sensor;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.util.Vector;

public class SoapSensor extends AbstractSensor {

    private final String NAMESPACE      = "http://webservices.vslecture.vs.inf.ethz.ch/";
    private final String METHOD1_NAME   = "getDiscoveredSpots";
    private final String METHOD2_NAME   = "getSpot";
    private final String SOAP_ACTION1   = "http://webservices.vslecture.vs.inf.ethz.ch/SunSPOTWebservice/getDiscoveredSpots";
    private final String SOAP_ACTION2   = "http://webservices.vslecture.vs.inf.ethz.ch/SunSPOTWebservice/getSpot";
    private final String URL            = "http://vslab.inf.ethz.ch:8080/SunSPOTWebServices/SunSPOTWebservice?wsdl";

    private String SPOT;
    private boolean noSpots = false;

    @Override
    public double parseResponse(String response) {
        return Double.parseDouble(response);
    }

    @Override
    public String executeRequest() {

            // 1. CHECK WHICH SPOTS ARE AVAILABLE.
            //----------------------------------------------------------------------------------------------------
            SoapObject requestSpots = new SoapObject(NAMESPACE, METHOD1_NAME);

            // version 12 does not work for the task.
            SoapSerializationEnvelope envReqSpots = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            envReqSpots.setOutputSoapObject(requestSpots);
            HttpTransportSE httpTransport = new HttpTransportSE(URL);

            try {
                httpTransport.call(SOAP_ACTION1, envReqSpots);
                Object response1 = envReqSpots.getResponse();

                // case distinction on how many spots are available.
                if (((Vector) response1).size() == 0) noSpots = true;
                else if (((Vector) response1).size() > 0) SPOT = ((Vector) response1).get(0).toString();

            } catch(Exception e) { e.printStackTrace(); }

            // 2. QUERY THE CHOSEN SPOT.
            //----------------------------------------------------------------------------------------------------
            if (noSpots) {
                // output message, that no spot can be queried.
                sendMessage("No Spot could be contacted.");
                noSpots = false;
                return "0";
            }
            else {
                SoapObject reqData = new SoapObject(NAMESPACE, METHOD2_NAME);
                reqData.addProperty("id", SPOT);

                // version 12 does not work for the task.
                SoapSerializationEnvelope envReqData = new SoapSerializationEnvelope(SoapEnvelope.VER11);
                //envReqData.implicitTypes = true;
                //envReqData.setAddAdornments(false);

                envReqData.setOutputSoapObject(reqData);
                HttpTransportSE httpTransport2 = new HttpTransportSE(URL);
                try {
                    httpTransport2.call(SOAP_ACTION2, envReqData);
                    SoapObject response2 = (SoapObject) envReqData.getResponse();
                    sendMessage("Value retrieved from " + SPOT);
                    //sendValue(parseResponse(response2.getPropertyAsString("temperature")));
                    return response2.getPropertyAsString("temperature");

                } catch (Exception e) { e.printStackTrace(); }
                return "0";
            }
    }
}
