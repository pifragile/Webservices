package ch.ethz.inf.vs.a2.sensor;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Container for the Actuators
 */
public class Actuator {

    public static List<Actuator> getActuators(Context context) {
        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        List<Actuator> list = new ArrayList<>();
        if (vibrator.hasVibrator()) {
            list.add(new Actuator(context, Type.VIBRATOR));
        }
        return list;
    }

    private enum Type {
        VIBRATOR
    }

    private Type type;
    private Context context;

    private Actuator(Context context, Type type) {
        this.context = context;
        this.type = type;
    }

    public String getName() {
        switch (type) {
            case VIBRATOR:
                return "Vibrator";
            default:
                return "";
        }
    }

    public String getHtml() {
        return getHtmlForm();
    }

    private String getHtmlForm() {
        switch (type) {
            case VIBRATOR:
                return "<form method=\"post\" autocomplete=\"off\">" +
                        "<p>" +
                            "<input type=\"radio\" name=\"type\" id=\"one_shot\" value=\"one_shot\" checked/><label for=\"one_shot\">One-shot</label><br/>" +
                            "<input type=\"radio\" name=\"type\" id=\"wave\" value=\"wave\"/><label for=\"wave\">Waveform</label>" +
                        "</p>" +
                        "<p>" +
                            "<label for=\"duration\">Duration for One-shot pattern</label><br/>" +
                            "<input required type=\"number\" id=\"duration\" name=\"duration\" value=\"200\" step=\"1\" min=\"100\" max=\"5000\"/>" +
                        "</p>" +
                        "<p>" +
                            "<label for=\"wave\">Wave pattern</label><br/>" +
                            "<input required type=\"text\" id=\"wave\" name=\"wave\" value=\"0,200,100,200\" pattern=\"(\\d{1,4},{0,1})+\" " +
                                "title=\"(\\d{1,4},{0,1})+\"/>" +
                        "</p>" +
                        "<input type=\"submit\" value=\"Submit\"/>" +
                        "</form>";
            default:
                return "";
        }
    }

    /**
     * Extract parameters from POST Request body
     * @param post POST Request body
     */
    public void setFromPost(String post) {
        switch (type) {
            case VIBRATOR:
                Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
                if (vibrator.hasVibrator()) {
                    Pattern pattern = Pattern.compile("^type=(one_shot|wave)|duration=(\\d+)|wave=(((\\d{1,4})(%2C){0,1})*)$");
                    Matcher m = pattern.matcher(post);

                    boolean type = false;
                    int duration = 0;
                    String waveString = "";

                    while (m.find()) {
                        if (m.group(1) != null) {
                            type = ("one_shot".equals(m.group(1)));
                        }
                        if (m.group(2) != null) {
                            duration = Integer.parseInt(m.group(2));
                        }
                        if (m.group(3) != null) {
                            waveString = m.group(3);
                        }
                    }

                    if (type) {
                        vibrator.vibrate(duration);
                    } else {

                        String[] waveValues = waveString.split("%2C");

                        for (String v : waveValues) {
                            Log.d("TAG", "Value: "+v);
                        }

                        long[] wave = new long[waveValues.length];
                        for (int i = 0; i < waveValues.length; i++) {
                            wave[i] = Integer.parseInt(waveValues[i]);
                        }

                        vibrator.vibrate(wave, -1);
                    }
                }
        }
    }
}
