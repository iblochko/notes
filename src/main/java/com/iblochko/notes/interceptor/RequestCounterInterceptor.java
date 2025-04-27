package com.iblochko.notes.interceptor;

import com.iblochko.notes.service.VisitorCounterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequestCounterInterceptor implements HandlerInterceptor {
    private final VisitorCounterService visitorCounterService;

    @Autowired
    public RequestCounterInterceptor(VisitorCounterService visitorCounterService) {
        this.visitorCounterService = visitorCounterService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             @NonNull HttpServletResponse response, @NonNull Object handler) {
        String requestUri = request.getRequestURI();
        if (!requestUri.startsWith("/stats")) {
            visitorCounterService.registerVisit(requestUri);
        }
        return true;
    }

}
