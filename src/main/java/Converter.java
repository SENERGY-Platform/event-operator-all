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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.infai.ses.senergy.operators.FlexInput;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.*;

public class Converter {
    private String url;
    private String extendedUrl;
    private String from;
    private String to;
    private Map<String, List<PathAndCharacteristic>> topicToPathAndCharacteristic;
    private List<CastExtension> castExtensions;

    public Converter(String extendedUrl, String url, String from, String to) {
        this(extendedUrl, url, from, to, "", "");
    }

    public Converter(String extendedUrl,String url, String from, String to, String topicToPathAndCharacteristic) {
        this(extendedUrl, url, from, to, topicToPathAndCharacteristic, "");
    }

    public Converter(String extendedUrl,String url, String from, String to, String topicToPathAndCharacteristic, String castExtensions) {
        this.extendedUrl = extendedUrl;
        this.url = url;
        this.from = from;
        this.to = to;
        this.topicToPathAndCharacteristic = this.parseTopicToPathAndCharacteristic(topicToPathAndCharacteristic);
        this.castExtensions = this.parseCastExtensions(castExtensions);
    }


    public Object convert(Object in) throws IOException {
        return this.convert(this.from, this.to, in);
    }

    public Object convert(FlexInput input, Object value) throws IOException {
        String fromCharacteristic = this.from;
        if (fromCharacteristic.equals("")) {
            String topic = input.getCurrentInputTopic();
            List<PathAndCharacteristic> list = this.topicToPathAndCharacteristic.getOrDefault(topic, new ArrayList<PathAndCharacteristic>());
            if (list.size() > 0) {
                fromCharacteristic = list.get(0).characteristic_id;
            }
        }
        return this.convert(fromCharacteristic, this.to, value);
    }

    public Object convert(String from, String to, Object value) throws IOException {
        if(this.useConverter(from, to)) {
            if(this.useCastExtensions()) {
                return this.convertWithExtension(from, to, value);
            } else {
                return this.convertWithoutExtension(from, to, value);
            }
        }else{
            return value;
        }
    }

    public Object convertWithExtension(String from, String to, Object value) throws IOException {
        Map<String, Object> payload = new HashMap<String, Object>();
        payload.put("input", value);
        payload.put("extensions", this.castExtensions);
        StringEntity entity = new StringEntity(this.objToJsonStr(payload));
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(this.getUrlWithExtension() + "/" + from + "/" + to);
        request.setEntity(entity);
        request.addHeader("content-type", "application/json");
        CloseableHttpResponse resp = httpClient.execute(request);
        String respStr = new BasicResponseHandler().handleResponse(resp);
        return this.jsonStrToObject(respStr);
    }

    public Object convertWithoutExtension(String from, String to, Object value) throws IOException {
        StringEntity entity = new StringEntity(this.objToJsonStr(value));
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(this.getUrlWithoutExtension() + "/" + from + "/" + to);
        request.setEntity(entity);
        request.addHeader("content-type", "application/json");
        CloseableHttpResponse resp = httpClient.execute(request);
        String respStr = new BasicResponseHandler().handleResponse(resp);
        return this.jsonStrToObject(respStr);
    }

    public boolean useConverter(String fromCharacteristic, String toCharacteristic) {
        if (this.url.equals("")) {
            return false;
        }
        if (fromCharacteristic.equals("") || toCharacteristic.equals("")) {
            return false;
        }
        if (fromCharacteristic.equals(toCharacteristic)) {
            return false;
        }
        return true;
    }

    public boolean useCastExtensions() {
        return !this.getUrlWithExtension().equals("") && this.castExtensions != null && this.castExtensions.size() > 0;
    }

    public String getUrlWithExtension() {
        return this.extendedUrl;
    }

    public String getUrlWithoutExtension() {
        return this.url;
    }

    private String objToJsonStr(Object in) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        StringWriter writer = new StringWriter();
        mapper.writeValue(writer, in);
        return writer.toString();
    }

    private Object jsonStrToObject(String in) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(in, Object.class);
    }

    private Map<String,List<PathAndCharacteristic>> parseTopicToPathAndCharacteristic(String topicToPathAndCharacteristic){
        topicToPathAndCharacteristic = topicToPathAndCharacteristic.trim();
        if (topicToPathAndCharacteristic.equals("")) {
            return new HashMap<String,List<PathAndCharacteristic>>();
        }
        Gson g = new Gson();
        Type mapType = new TypeToken<Map<String,List<PathAndCharacteristic>>>() {}.getType();
        return g.fromJson(topicToPathAndCharacteristic, mapType);
    }

    private List<CastExtension> parseCastExtensions(String castExtensions){
        castExtensions = castExtensions.trim();
        if (castExtensions.equals("")) {
            return new LinkedList<CastExtension>();
        }
        Gson g = new Gson();
        Type listType = new TypeToken<List<CastExtension>>() {}.getType();
        return g.fromJson(castExtensions, listType);
    }
}