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

import org.infai.seits.sepl.operators.Config;
import org.infai.seits.sepl.operators.Stream;

public class Operator {

    public static void main(String[] args) {
        Stream stream  = new Stream();
        Config config = new Config();
        String converterUrl = config.getConfigValue("converterUrl", "");
        String convertFrom = config.getConfigValue("convertFrom", "");
        String convertTo = config.getConfigValue("convertTo", "");
        String userToken = config.getConfigValue("userToken", "");

        Converter converter = new Converter(converterUrl, convertFrom, convertTo);
        EventAll filter = new EventAll(
                userToken,
                config.getConfigValue("url", ""),
                config.getConfigValue("eventId", ""),
                converter
        );
        stream.start(filter);
    }
}
