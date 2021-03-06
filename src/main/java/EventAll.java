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

import java.io.IOException;


public class EventAll extends BaseOperator {
    private String url;
    private String eventId;
    private Converter converter;

    public EventAll(String url, String eventId, Converter converter) {
        this.url = url;
        this.eventId = eventId;
        this.converter = converter;
    }

    @Override
    public void run(Message message) {
        try{
            FlexInput input = message.getFlexInput("value");
            this.trigger(input);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void trigger(FlexInput input){
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(10 * 1000).build();
        CloseableHttpClient httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build();
        try {
            Object value;
            try{
                value = input.getValue();
            }catch (Exception e){
                value = input.getString();
            }
            if(this.converter != null){
                value = this.converter.convert(value);
            }

            JSONObject json = new JSONObject()
                    .put("messageName", this.eventId)
                    .put("all", true)
                    .put("resultEnabled", false)
                    .put("processVariablesLocal", new JSONObject()
                            .put("event", new JSONObject()
                                    .put("value", value)
                            )
                    );

            HttpPost request = new HttpPost(this.url);
            StringEntity params = new StringEntity(json.toString());
            request.addHeader("content-type", "application/json");
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
