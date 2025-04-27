package com.iblochko.notes.config;

import com.iblochko.notes.interceptor.RequestCounterInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final RequestCounterInterceptor requestCounterInterceptor;

    @Autowired
    public WebMvcConfig(RequestCounterInterceptor requestCounterInterceptor) {
        this.requestCounterInterceptor = requestCounterInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestCounterInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/stats/**");
    }
}
