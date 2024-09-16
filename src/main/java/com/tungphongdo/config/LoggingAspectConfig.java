package com.tungphongdo.config;
/*
docs: https://viblo.asia/p/su-dung-aop-trong-spring-boot-va-aspectj-vyDZOkbaZwj
 */

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Aspect
@Component
@Slf4j
public class LoggingAspectConfig {

    @Before("execution(public * com.tungphongdo.controller..*(..))")
    public void logRequest(JoinPoint joinPoint) {
        // Extract request details
        HttpServletRequest request = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        String url = request.getRequestURI();
        String method = request.getMethod();
        log.info("Incoming request: {} {}", method, url);
        logHeaders(request);
        log.info("Method name: {}", joinPoint.getSignature().getName());
        logRequestBody(joinPoint);
    }

//    // Logging after a method returns successfully
//    @AfterReturning(pointcut = "execution(* com.tungphongdo.controller..*(..))", returning = "result")
//    public void logAfterReturning(JoinPoint joinPoint, Object result) {
//        String methodName = joinPoint.getSignature().getName();
//        log.info("Method {} executed successfully", methodName);
//
//        // Optionally, log the returned object
//        log.info("Returned value: {}", result);
//    }
//
//    // Exception Logging using @AfterThrowing advice
//    @AfterThrowing(pointcut = "execution(* com.tungphongdo..*(..))", throwing = "exception")
//    public void logAfterThrowing(JoinPoint joinPoint, Throwable exception) {
//        String methodName = joinPoint.getSignature().getName();
//        String className = joinPoint.getSignature().getDeclaringTypeName();
//
//        log.error("Exception in {}.{}() with cause = '{}'", className, methodName, exception.getCause() != null ? exception.getCause() : "NULL");
//        log.error("Exception Message: {}", exception.getMessage(), exception);
//
//        // Optionally log stack trace, or any custom logic on exception
//        for (StackTraceElement stackTraceElement : exception.getStackTrace()) {
//            log.error(stackTraceElement.toString());
//        }
//    }

    // Around advice to log the execution of methods
    @Around("execution(* com.yourpackage..*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getSignature().getDeclaringTypeName();
        long startTime = System.currentTimeMillis();

        // Log method entry details (before the method is executed)
        log.info("Entering method {}.{} with arguments {}", className, methodName, joinPoint.getArgs());

        Object result = null;
        try {
            // Proceed with method execution
            result = joinPoint.proceed();
        } catch (Exception e) {
            // Log exception if any during method execution
            log.error("Exception in method {}.{}: {}", className, methodName, e.getMessage());
            throw e; // rethrow the exception after logging it
        }

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Log method exit details (after the method has executed)
        log.info("Exiting method {}.{}; returned value = {} ; Execution time = {} ms", className, methodName, result, duration);

        return result; // return the method result
    }

    private void logHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        log.info("Request Headers: {}", headers);
    }

    private void logRequestBody(JoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof RequestBody) {
                log.info("Request Body: {}", arg);
            }
        }
    }
}
