package com.tu2l.pdf.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.tu2l.common.util.Util;

@Configuration
public class BeansConfiguration {

   @Bean
   public Util util() {
       return new Util();
   }
}
