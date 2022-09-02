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
            json = new JSONObject()
                    .put("messageName", this.eventId)
                    .put("all", true)
                    .put("resultEnabled", false)
                    .put("processVariablesLocal", new JSONObject()
                            .put("event", new JSONObject()
                                    .put("value", value)
                            )
                    );
        } catch (JSONException e) {
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
}
