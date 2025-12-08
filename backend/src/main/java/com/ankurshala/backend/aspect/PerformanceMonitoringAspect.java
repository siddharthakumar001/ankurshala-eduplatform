package com.ankurshala.backend.aspect;

import com.ankurshala.backend.service.MetricsService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;

/**
 * Aspect for automatic API performance monitoring.
 * Tracks latency and error rates for all REST endpoints.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class PerformanceMonitoringAspect {

    private final MeterRegistry meterRegistry;
    private final MetricsService metricsService;

    /**
     * Monitor all controller methods (REST endpoints).
     */
    @Around("@within(org.springframework.web.bind.annotation.RestController) && " +
            "execution(public * com.ankurshala.backend.controller..*(..))")
    public Object monitorApiPerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String endpoint = getEndpointName(joinPoint);
        String httpMethod = getHttpMethod(joinPoint);
        
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            Object result = joinPoint.proceed();
            
            // Record success metrics
            long duration = System.currentTimeMillis() - startTime;
            sample.stop(Timer.builder("api.request.duration")
                    .tag("endpoint", endpoint)
                    .tag("method", httpMethod)
                    .tag("status", "success")
                    .register(meterRegistry));
            
            meterRegistry.counter("api.request.count",
                    "endpoint", endpoint,
                    "method", httpMethod,
                    "status", "success").increment();

            if (duration > 1000) {
                log.warn("Slow API call detected: {} {} took {}ms", httpMethod, endpoint, duration);
            }

            return result;
            
        } catch (Exception e) {
            // Record error metrics
            sample.stop(Timer.builder("api.request.duration")
                    .tag("endpoint", endpoint)
                    .tag("method", httpMethod)
                    .tag("status", "error")
                    .register(meterRegistry));
            
            meterRegistry.counter("api.request.count",
                    "endpoint", endpoint,
                    "method", httpMethod,
                    "status", "error").increment();

            metricsService.recordApiError(endpoint, e.getClass().getSimpleName());
            
            log.error("API error: {} {} - {}", httpMethod, endpoint, e.getMessage());
            throw e;
        }
    }

    /**
     * Monitor all service methods annotated with @Timed.
     */
    @Around("@annotation(timed)")
    public Object monitorServicePerformance(ProceedingJoinPoint joinPoint, Timed timed) throws Throwable {
        long startTime = System.currentTimeMillis();
        String serviceName = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            Object result = joinPoint.proceed();
            
            long duration = System.currentTimeMillis() - startTime;
            sample.stop(Timer.builder(timed.value())
                    .tag("class", serviceName)
                    .tag("method", methodName)
                    .register(meterRegistry));

            if (duration > 500) {
                log.warn("Slow service method detected: {}.{} took {}ms", 
                        serviceName, methodName, duration);
            }

            return result;
            
        } catch (Exception e) {
            sample.stop(Timer.builder(timed.value())
                    .tag("class", serviceName)
                    .tag("method", methodName)
                    .tag("exception", e.getClass().getSimpleName())
                    .register(meterRegistry));
            
            log.error("Service error: {}.{} - {}", serviceName, methodName, e.getMessage());
            throw e;
        }
    }

    /**
     * Monitor database operations.
     */
    @Around("execution(* com.ankurshala.backend.repository..*(..))")
    public Object monitorDatabasePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String repository = joinPoint.getSignature().getDeclaringTypeName();
        String method = joinPoint.getSignature().getName();
        
        Timer.Sample sample = Timer.start(meterRegistry);

        try {
            Object result = joinPoint.proceed();
            
            long duration = System.currentTimeMillis() - startTime;
            sample.stop(Timer.builder("db.query.duration")
                    .tag("repository", repository)
                    .tag("method", method)
                    .register(meterRegistry));

            if (duration > 100) {
                log.warn("Slow database query detected: {}.{} took {}ms", 
                        repository, method, duration);
            }

            return result;
            
        } catch (Exception e) {
            metricsService.recordDatabaseError(method, e.getClass().getSimpleName());
            log.error("Database error: {}.{} - {}", repository, method, e.getMessage());
            throw e;
        }
    }

    private String getEndpointName(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        // Try to get the mapping from method annotations
        if (method.isAnnotationPresent(GetMapping.class)) {
            String[] paths = method.getAnnotation(GetMapping.class).value();
            return paths.length > 0 ? paths[0] : method.getName();
        } else if (method.isAnnotationPresent(PostMapping.class)) {
            String[] paths = method.getAnnotation(PostMapping.class).value();
            return paths.length > 0 ? paths[0] : method.getName();
        } else if (method.isAnnotationPresent(PutMapping.class)) {
            String[] paths = method.getAnnotation(PutMapping.class).value();
            return paths.length > 0 ? paths[0] : method.getName();
        } else if (method.isAnnotationPresent(DeleteMapping.class)) {
            String[] paths = method.getAnnotation(DeleteMapping.class).value();
            return paths.length > 0 ? paths[0] : method.getName();
        } else if (method.isAnnotationPresent(PatchMapping.class)) {
            String[] paths = method.getAnnotation(PatchMapping.class).value();
            return paths.length > 0 ? paths[0] : method.getName();
        } else if (method.isAnnotationPresent(RequestMapping.class)) {
            String[] paths = method.getAnnotation(RequestMapping.class).value();
            return paths.length > 0 ? paths[0] : method.getName();
        }
        
        return method.getName();
    }

    private String getHttpMethod(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        
        if (method.isAnnotationPresent(GetMapping.class)) return "GET";
        if (method.isAnnotationPresent(PostMapping.class)) return "POST";
        if (method.isAnnotationPresent(PutMapping.class)) return "PUT";
        if (method.isAnnotationPresent(DeleteMapping.class)) return "DELETE";
        if (method.isAnnotationPresent(PatchMapping.class)) return "PATCH";
        if (method.isAnnotationPresent(RequestMapping.class)) {
            RequestMethod[] methods = method.getAnnotation(RequestMapping.class).method();
            return methods.length > 0 ? methods[0].name() : "UNKNOWN";
        }
        
        return "UNKNOWN";
    }
}
