package com.poc.mpt.common;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.io.IOException;
import java.net.*;
import java.util.List;

@Slf4j
@Data
@Configuration
@ConditionalOnProperty(value = "proxy.enabled", havingValue = "true")
@ConfigurationProperties(prefix = "proxy")
public class ProxyConfig {

    private String proxyHost;
    private String proxyPort;

    @PostConstruct
    public void setHttpProxy() {
        ProxySelector.setDefault(new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {

                return List.of(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, Integer.parseInt(proxyPort))));
                //return List.of(Proxy.NO_PROXY);
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
                throw new RuntimeException("Proxy connecting failed");
            }
        });
    }
}
