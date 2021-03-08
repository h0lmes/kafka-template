package com.kafkatemplate.configuration;

import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import com.kafkatemplate.model.message.Message;

@Aspect
@Component
public class MdcAdvice {

    public static final String MDC_TOKEN_KEY = "HDC.traceId";

    @Pointcut("execution(public * com.kafkatemplate.service.KafkaConsumer.*(..))")
    private void injectMdcInfo() {
        // No body needed
    }

    @Around("injectMdcInfo()")
    public Object aroundAdvice(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            Message message = null;
            Object[] args = joinPoint.getArgs();
            for (Object obj : args) {
                if (obj instanceof Message) {
                    message = (Message) obj;
                    break;
                }
            }
            String traceId = extractToken(message);
            MDC.put(MDC_TOKEN_KEY, traceId);
            return joinPoint.proceed();
        } finally {
            MDC.remove(MDC_TOKEN_KEY);
        }
    }

    private String extractToken(Message message) {
        if (message != null && message.getTraceId() != null) {
            return message.getTraceId().toString();
        }
        return UUID.randomUUID().toString().replace("-", "");
    }
}