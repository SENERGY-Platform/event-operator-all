import org.infai.seits.sepl.operators.Builder;
import org.infai.seits.sepl.operators.Message;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;

public class TestMessageProvider {

    public static Message getTestMessage(Object value) throws IOException {
        Message m;
        Builder builder = new Builder("1", "1");
        JSONObject jsonObject = new JSONObject().put("device_id", "1").put("value", new JSONObject().put("reading", new JSONObject().put("value", value)));
        m = new Message(builder.formatMessage(jsonObject.toString()));
        JSONObject config = getConfig();
        m.setConfig(config.toString());
        return m;
    }

    private static JSONObject getConfig() {
        JSONObject config = new JSONObject().put("inputTopics",new JSONArray().put(new JSONObject().put("Name", "test")
                .put("FilterType", "DeviceId")
                .put("FilterValue", "1")
                .put("Mappings", new JSONArray()
                        .put(new JSONObject().put("Source", "value.reading.value").put("Dest", "value"))
                )));
        return config;
    }
}
