package com.tu2l.pdf.config;

import com.tu2l.common.util.CommonUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeansConfiguration {

    @Bean
    public CommonUtil util() {
        return new CommonUtil();
    }
}
