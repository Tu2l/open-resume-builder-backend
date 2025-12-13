package com.tu2l.common.constant;

import lombok.Getter;

public enum ServiceIdentifiers {
    USER_SERVICE(CommonConstants.ServiceContext.USER_SERVICE),
    PDF_SERVICE(CommonConstants.ServiceContext.USER_SERVICE);

    @Getter
    private final String serviceName;
    @Getter
    private final String basePath;

    ServiceIdentifiers(String basePath) {
        this.serviceName = this.name().toLowerCase().replace("_", "-");
        this.basePath = basePath;
    }
}
