/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.openmessaging.internal;

import io.openmessaging.KeyValue;
import io.openmessaging.MessagingAccessPoint;
import io.openmessaging.PropertyKeys;
import io.openmessaging.exception.OMSRuntimeException;
import java.lang.reflect.Constructor;

/**
 * The {@code MessagingAccessPointAdapter} provides a common implementation to
 * create a specified {@code MessagingAccessPoint} instance, used by OMS internally.
 *
 * @version OMS 1.0
 * @since OMS 1.0
 */
public class MessagingAccessPointAdapter {
    /**
     * The correct OMS driver url is:
     * <p>
     * {@literal openmessaging:<driver_type>://<access_point>[,<access_point>,...]/<namespace>}
     */
    private static final String pattern = "^openmessaging:.+://.+/.*$";

    /**
     * Returns a {@code MessagingAccessPoint} instance from the specified OMS driver url
     * with some preset properties.
     *
     * @param url the driver url
     * @param properties the preset properties
     * @return a {@code MessagingAccessPoint} instance
     */
    public static MessagingAccessPoint getMessagingAccessPoint(String url, KeyValue properties) {
        checkDriverURL(url);
        String driverImpl = parseDriverImpl(url, properties);
        String accessPoints = parseAccessPoints(url);
        String namespace = parseNamespace(url);

        properties.put(PropertyKeys.NAMESPACE, namespace);
        properties.put(PropertyKeys.ACCESS_POINTS, accessPoints);
        properties.put(PropertyKeys.DRIVER_IMPL, driverImpl);

        try {
            Class<?> driverImplClass = Class.forName(driverImpl);
            Constructor constructor = driverImplClass.getConstructor(KeyValue.class);
            return (MessagingAccessPoint) constructor.newInstance(properties);
        } catch (Throwable e) {
            throw new OMSRuntimeException("-1", "The OMS driver url is illegal.", e);
        }
    }

    private static String parseNamespace(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    private static String parseAccessPoints(String url) {
        return url.substring(url.indexOf("//") + 2, url.lastIndexOf("/"));
    }

    private static String parseDriverImpl(String url, KeyValue properties) {
        if (properties.containsKey(PropertyKeys.DRIVER_IMPL)) {
            return properties.getString(PropertyKeys.DRIVER_IMPL);
        }
        return "io.openmessaging." + url.split(":")[1] + ".MessagingAccessPointImpl";
    }

    private static void checkDriverURL(String url) {
        if (!url.matches(pattern)) {
            throw new OMSRuntimeException("-1", "The OMS driver url is illegal.");
        }
    }
}