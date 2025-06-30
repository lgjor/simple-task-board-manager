package org.desviante.util;

import com.google.api.client.json.jackson2.JacksonFactory;

public class JacksonFactoryKeepAlive {
    public static void ensureUsed() {
        JacksonFactory factory = JacksonFactory.getDefaultInstance();
        factory.toString(); // uso simbólico para garantir referência
    }
}