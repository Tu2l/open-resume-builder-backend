package com.tu2l.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
@ConfigurationProperties(prefix = "gateway")
public class CustomGatewayProperties {
    private List<String> publicRoutes = new ArrayList<>();
    private Map<String, String> serviceUrls = Map.of();
}

