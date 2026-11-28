package com.eventhub.config;

import com.eventhub.security.AccountChangeInterceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer
{
    private final AccountChangeInterceptor accountChangeInterceptor;

    public WebMvcConfig(AccountChangeInterceptor accountChangeInterceptor)
    {
        this.accountChangeInterceptor = accountChangeInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry)
    {
        registry.addInterceptor(accountChangeInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/favicon.ico");
    }
}
