package com.ankurshala.backend.config;

import com.ankurshala.backend.filter.RateLimitingFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class WebConfig {

    @Autowired
    private RateLimitingFilter rateLimitingFilter;

    @Bean
    public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilterRegistration() {
        FilterRegistrationBean<RateLimitingFilter> registrationBean = new FilterRegistrationBean<>();
        
        registrationBean.setFilter(rateLimitingFilter);
        
        // Apply to API endpoints only
        registrationBean.addUrlPatterns("/user/*", "/student/*", "/teacher/*", "/admin/*");
        
        // Set order to run before security filters
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE + 1);
        registrationBean.setName("rateLimitingFilter");
        
        return registrationBean;
    }
}
