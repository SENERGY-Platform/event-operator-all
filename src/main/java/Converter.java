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
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.StringWriter;

public class Converter {
    private String url;
    private String from;
    private String to;
    private boolean used;

    public Converter(String url, String from, String to){
        this.url = url;
        this.from = from;
        this.to = to;
        if(this.url.equals("") || this.from.equals("") || this.to.equals("")){
            this.used = false;
        }else{
            this.used = true;
        }
    }

    public Object convert(Object in) throws IOException {
        if(this.used) {
            StringEntity entity = new StringEntity(this.objToJsonStr(in));
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();
            HttpPost request = new HttpPost(this.url + "/" + this.from + "/" + this.to);
            request.setEntity(entity);
            request.addHeader("content-type", "application/json");
            CloseableHttpResponse resp = httpClient.execute(request);
            String respStr = new BasicResponseHandler().handleResponse(resp);
            return this.jsonStrToObject(respStr);
        }else{
            return in;
        }
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


    public boolean isUsed() {
        return used;
    }
}
