package com.tu2l.pdf.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.tu2l.common.utils.Util;

@Configuration
public class BeansConfiguration {

   @Bean
   @Scope("prototype")
   public Util util() {
       return new Util();
   }
}
