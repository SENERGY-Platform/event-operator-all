/*
 * Copyright 2020 InfAI (CC SES)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.infai.ses.senergy.operators.BaseOperator;
import org.infai.ses.senergy.operators.FlexInput;
import org.infai.ses.senergy.operators.Input;
import org.infai.ses.senergy.operators.Message;
import org.json.JSONObject;
import org.infai.ses.senergy.exceptions.NoValueException;
import org.json.JSONException;
import java.io.StringWriter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Set;
import java.util.HashSet;

import java.io.IOException;


public class EventAll extends BaseOperator {
    private String url;
    private String eventId;
    private Converter converter;
    private String userToken;

    public EventAll(String userToken, String url, String eventId, Converter converter) {
        this.url = url;
        this.eventId = eventId;
        this.converter = converter;
        this.userToken = userToken;
    }

    @Override
    public void run(Message message) {
        try{
            FlexInput input = message.getFlexInput("value");
            Object value = this.getValueOfInput(input);
            this.trigger(value);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private Object getValueOfInput(FlexInput input) throws IOException, NoValueException {
        return this.converter.convert(input, input.getValue(Object.class));
    }

    private void trigger(Object value){
        JSONObject json;
        try {
            if(mustBeMarshalled(value)) {
                value = this.objToJsonStr(value);
            }
            json = new JSONObject()
                    .put("messageName", this.eventId)
                    .put("all", true)
                    .put("resultEnabled", false)
                    .put("processVariables", new JSONObject()
                            .put("event", new JSONObject()
                                    .put("value", value)
                            )
                    );
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10 * 1000).build();
        CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();

        try {
            HttpPost request = new HttpPost(this.url);
            StringEntity params = new StringEntity(json.toString());
            request.addHeader("content-type", "application/json");
            if (!this.userToken.equals("")) {
                request.addHeader("Authorization", userToken);
            }
            request.setEntity(params);
            CloseableHttpResponse resp = httpClient.execute(request);
            resp.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Message configMessage(Message message) {
        message.addFlexInput("value");
        return message;
    }

    private String objToJsonStr(Object in) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, in);
        return writer.toString();
    }

    public static boolean mustBeMarshalled(Object object)
    {
        Class<?> clazz = object.getClass();
        return !(object instanceof String) && !clazz.isPrimitive() && !isWrapperType(clazz);
    }

    public static boolean isWrapperType(Class<?> clazz)
    {
        return getWrapperTypes().contains(clazz);
    }

    private static Set<Class<?>> getWrapperTypes()
    {
        Set<Class<?>> ret = new HashSet<Class<?>>();
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        ret.add(Void.class);
        return ret;
    }
}
