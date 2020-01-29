import com.sun.net.httpserver.HttpServer;
import org.infai.seits.sepl.operators.Message;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


public class EventAllTest {
    public static boolean called = false;
    private static Object processVariable = null;

    private Object jsonNormalize(Object in) throws ParseException {
        Map<String, Object> wrapper = new HashMap<String, Object>();
        wrapper.put("value", in);
        JSONObject temp = new JSONObject(wrapper);
        Object candidate = ((JSONObject)(new JSONParser().parse(temp.toJSONString()))).get("value");
        if(candidate instanceof Long){
            candidate = Double.valueOf((Long)candidate);
        }
        return candidate;
    }

    private void test(Object messageValue, boolean expectedToTrigger) throws IOException {
        EventAllTest.called = false;
        HttpServer server = TriggerServerMock.create(inputStream -> {
            JSONParser jsonParser = new JSONParser();
            try {
                JSONObject jsonObject = (JSONObject)jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
                if(
                        jsonObject.containsKey("localVariables")
                        && ((JSONObject)jsonObject.get("localVariables")).containsKey("event")
                        && ((JSONObject)((JSONObject)jsonObject.get("localVariables")).get("event")).containsKey("value")
                ){
                    EventAllTest.called = true;
                    EventAllTest.processVariable = ((JSONObject)((JSONObject)jsonObject.get("localVariables")).get("event")).get("value");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        EventAll events = new EventAll("http://localhost:"+server.getAddress().getPort()+"/endpoint", "test");
        Message msg = TestMessageProvider.getTestMessage(messageValue);
        events.config(msg);
        events.run(msg);
        server.stop(0);
        Assert.assertEquals(EventAllTest.called, expectedToTrigger);
        if(expectedToTrigger){
            try {
                Object a = jsonNormalize(EventAllTest.processVariable);
                Object b = jsonNormalize(messageValue);
                Assert.assertEquals(a, b);
            } catch (ParseException e) {
                Assert.fail(e.getMessage());
            }
        }
    }

    @Test
    public void string() throws IOException {
        test("foobar",true);
    }


    @Test
    public void integer() throws IOException {
        test(42,true);
    }

    @Test
    public void floatpoint() throws IOException {
        test(4.2, true);
    }

}