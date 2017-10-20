package ch.ethz.inf.vs.rsattler.webservices;

import android.support.annotation.Nullable;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class RestResource {

    @Nullable
    static RestResource getResourceFromPath(String path) {
        Pattern home = Pattern.compile("^/$");
        Pattern sensors = Pattern.compile("^/sensors(/?)$");
        Pattern sensorId = Pattern.compile("^/sensors/(\\d+)/?$");

        Matcher m;

        m = home.matcher(path);
        if (m.find()) {
            Log.d("Service", "Requesting Home");
            return new HomeResource();
        }

        m = sensors.matcher(path);
        if (m.find()) {
            return new SensorResource(-1);
        }

        m = sensorId.matcher(path);
        if (m.find()) {
            int id = Integer.parseInt(m.group(1));
            return new SensorResource(id);
        }

        Log.d("Service", "Resource is null");

        return null;
    }

    String getHtmlHeader() {
        return "<!DOCTYPE html>\n<html>\n<head>\n<title>REST Server</title>\n</head>\n<body>\n";
    }

    String getHtmlFooter() {
        return "</body>\n</html>";
    }

    abstract String getHtml();

    static class HomeResource extends RestResource {

        private HomeResource() {

        }

        @Override
        String getHtml() {
            return getHtmlHeader()
                    +"<p>bullshit</p>\n"
                    +getHtmlFooter();
        }
    }

    static class SensorResource extends RestResource {

        private SensorResource(int id) {
            if (id < 0) {
                return;
            }
        }

        @Override
        String getHtml() {
            return null;
        }
    }
}
