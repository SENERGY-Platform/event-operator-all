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

import org.infai.ses.senergy.operators.Config;
import org.infai.ses.senergy.operators.Stream;
import org.infai.ses.senergy.utils.ConfigProvider;

public class Operator {

    public static void main(String[] args) {
        Stream stream  = new Stream();
        Config config = ConfigProvider.getConfig();

        String userToken = config.getConfigValue("userToken", "");

        String converterUrl = config.getConfigValue("converterUrl", "");
        String convertFrom = config.getConfigValue("convertFrom", "");
        String convertTo = config.getConfigValue("convertTo", "");
        String castExtension = config.getConfigValue("castExtensions", "");
        String extendedConverterUrl = config.getConfigValue("extendedConverterUrl", "");
        String topicToPathAndCharacteristic = config.getConfigValue("topicToPathAndCharacteristic", "");

        String marshallerUrl = config.getConfigValue("marshallerUrl", "");
        String path = config.getConfigValue("path", "");
        String functionId = config.getConfigValue("functionId", "");
        String aspectNodeId = config.getConfigValue("aspectNodeId", "");
        String targetCharacteristicId = config.getConfigValue("targetCharacteristicId", "");
        String topicToServiceId = config.getConfigValue("topicToServiceId", "");


        ConverterInterface converter;
        if(marshallerUrl.equals("")) {
            converter = new Converter(extendedConverterUrl, converterUrl, convertFrom, convertTo, topicToPathAndCharacteristic, castExtension);
        } else {
            converter = new Marshaller(marshallerUrl, userToken, functionId, aspectNodeId, path, targetCharacteristicId, topicToServiceId);
        }

        EventAll filter = new EventAll(
                userToken,
                config.getConfigValue("url", ""),
                config.getConfigValue("eventId", ""),
                converter
        );
        stream.start(filter);
    }
}
