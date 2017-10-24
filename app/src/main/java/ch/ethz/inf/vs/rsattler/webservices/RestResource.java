package ch.ethz.inf.vs.rsattler.webservices;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.InputStream;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.ethz.inf.vs.a2.sensor.Actuator;
import ch.ethz.inf.vs.a2.sensor.SensorType;

/**
 * Represents a Resource offered by the server
 */
public class RestResource implements SensorEventListener {

    /**
     * The HTML header
     */
    private final static String HTML_START = "<!DOCTYPE html>" +
            "<html>" +
            "<head>" +
                "<title>REST Server</title>" +
                "<style>" +
                    "body { padding: 50px 200px; }" +
                    "h1 { font-size: 20px; } " +
                    "li { padding: 3px; }" +
                    "label { padding: 10px 0; } " +
                    "input { margin: 10px; padding: 5px; }" +
                "</style>" +
            "</head>" +
            "<body>" +
            "<h1>REST Server</h1>";
    /**
     * The HTML footer
     */
    private final static String HTML_END = "</body></html>";

    /**
     * Different ResourceTypes
     */
    private enum ResourceType {
        FAVICON, HOME, SENSOR_OVERVIEW, SENSOR, ACTUATOR_OVERVIEW, ACTUATOR
    }

    private ResourceType type;
    private int id;
    private Context context;
    private float[] sensorValues;
    private SensorManager sensorManager;

    private RestResource(Context context, ResourceType type) {
        this.context = context;
        this.type = type;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
    }

    /**
     * Returns the RestResource which corresponds to the given path
     * @param path String pointing to the resource
     * @return RestResource object containing the resource information
     */
    @Nullable
    static RestResource getResourceFromPath(Context context, String path) {
        // Patterns corresponding to the offered resources
        Pattern home = Pattern.compile("^/$");
        Pattern favicon = Pattern.compile("^/favicon.ico$");
        Pattern sensors = Pattern.compile("^/sensors(/?)$");
        Pattern sensorId = Pattern.compile("^/sensors/(\\d+)/?$");
        Pattern actuators = Pattern.compile("^/actuators(/?)$");
        Pattern actuatorId = Pattern.compile("^/actuators/(\\d+)/?$");

        Matcher m;

        // If path matches Home
        m = home.matcher(path);
        if (m.find()) {
            return new RestResource(context, ResourceType.HOME);
        }

        m = favicon.matcher(path);
        if (m.find()) {
            return new RestResource(context, ResourceType.FAVICON);
        }

        // If path matches Sensors
        m = sensors.matcher(path);
        if (m.find()) {
            return new RestResource(context, ResourceType.SENSOR_OVERVIEW);
        }

        // If path matches specific Sensor
        m = sensorId.matcher(path);
        if (m.find()) {
            int id = Integer.parseInt(m.group(1));
            RestResource resource = new RestResource(context, ResourceType.SENSOR);
            resource.id = id;
            return resource;
        }

        // If path matches Actuator
        m = actuators.matcher(path);
        if (m.find()) {
            return new RestResource(context, ResourceType.ACTUATOR_OVERVIEW);
        }

        // If path matches specific Actuator
        m = actuatorId.matcher(path);
        if (m.find()) {
            int id = Integer.parseInt(m.group(1));
            RestResource resource = new RestResource(context, ResourceType.ACTUATOR);
            resource.id = id;
            return resource;
        }

        // If resource nut found return null
        Log.e("Service", "Resource is null for path: "+path);
        return null;
    }

    /**
     * Get the HTML element navigating back to home
     * @return String representing the home link
     */
    private String getHomeNavigation() {
        return "<p><a href=\"/\">Home</a></p>";
    }

    /**
     * Get the HTML element navigating back to the parent
     * @return String representing the parent link
     */
    private String getParentNavigation() {
        String href;
        switch (type) {
            case ACTUATOR_OVERVIEW:
            case SENSOR_OVERVIEW:
                href = "/";
                break;
            case ACTUATOR:
                href = "/actuators";
                break;
            case SENSOR:
                href = "/sensors";
                break;
            default:
                return "";
        }
        return "<p><a href=\""+href+"\">Parent</a></p>";
    }

    /**
     * Get the HTML element containing all the information of this resource
     * @return String containing body
     */
    private String getResourceBody() {
        StringBuilder builder;
        switch (type) {

            case HOME:
                return "<ul><li><a href=\"/actuators\">Actuators</a></li><li><a href=\"/sensors\">Sensors</a></li></ul>";

            case FAVICON:
                InputStream stream = context.getResources().openRawResource(R.raw.favicon);
                Scanner scanner = new Scanner(stream).useDelimiter("\\A");
                return scanner.hasNext() ? scanner.next() : "";

            case SENSOR_OVERVIEW:
                // Get Sensors and add them to the HTML
                List<Sensor> sensors = getSensors();
                builder = new StringBuilder();
                builder.append("<ul>");
                for (int i = 0; i < sensors.size(); i++) {
                    builder.append("<li><a href=\"/sensors/").append(i).append("\">")
                            .append(sensors.get(i).getName()).append("</a></li>");
                }
                builder.append("</ul>");
                return builder.toString();

            case SENSOR:
                // Get Sensor, await values and add it to the HTML
                Sensor sensor = sensorManager.getSensorList(Sensor.TYPE_ALL).get(id);
                SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
                sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);

                long timestamp = System.currentTimeMillis();
                while (System.currentTimeMillis() - timestamp < 1000 && sensorValues == null) { /* spin */ }
                sensorManager.unregisterListener(this);

                builder = new StringBuilder();
                builder.append("<ul>");
                for (int i = 0; i < SensorType.getNumberValues(sensor.getType()); i++) {
                    if (sensorValues == null) {
                        return "<p>0</p>";
                    }

                    builder.append("<li>")
                            .append(sensorValues[i])
                            .append(" ")
                            .append(SensorType.getUnitString(sensor.getType()))
                            .append("</li>");
                }
                builder.append("</ul>");
                return builder.toString();

            case ACTUATOR_OVERVIEW:
                // Get Actuators and add them to the HTML
                List<Actuator> actuators = getActuators();
                builder = new StringBuilder();
                builder.append("<ul>");
                for (int i = 0; i < actuators.size(); i++) {
                    builder.append("<li><a href=\"/actuators/").append(i).append("\">")
                            .append(actuators.get(i).getName()).append("</a></li>");
                }
                builder.append("</ul>");
                return builder.toString();

            case ACTUATOR:
                // Added Actuator form to the HTML
                Actuator actuator = getActuators().get(id);
                return actuator.getHtml();

            default:
                return "";
        }
    }

    public String toHtml() {
        return HTML_START +
                getHomeNavigation() +
                getParentNavigation() +
                getResourceBody() +
                HTML_END;
    }

    public String getContentType() {
        switch (type) {
            case FAVICON:
                return "image/x-icon";
            default:
                return "text/html";
        }
    }

    private List<Sensor> getSensors() {
        return sensorManager.getSensorList(Sensor.TYPE_ALL);
    }

    private List<Actuator> getActuators() {
        return Actuator.getActuators(context);
    }

    public void setFromPost(String body) {
        if (type == ResourceType.ACTUATOR) {
            getActuators().get(id).setFromPost(body);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        this.sensorValues = sensorEvent.values;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        //ignore
    }
}
