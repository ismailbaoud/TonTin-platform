package com.tontin.platform.aspect;

import java.time.LocalDateTime;
import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.tontin.platform.domain.Loggin;
import com.tontin.platform.dto.auth.login.request.LoginRequest;
import com.tontin.platform.dto.auth.register.request.RegisterRequest;
import com.tontin.platform.repository.LogRepository;

import lombok.RequiredArgsConstructor;

@Aspect
@Component
@RequiredArgsConstructor
public class AuthloggingAspect {
    
    private final LogRepository logRepository;
    @Value("${env}")
    private String env;

    @Around("execution(* com.tontin.platform.service.AuthService.*(..))")
    public Object logAuthMethods(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = null;
        String status = "SUCCESS";
        String event = joinPoint.getSignature().getName();
        LocalDateTime timestamp = LocalDateTime.now();

        try{
            result = joinPoint.proceed();
        }catch(Exception ex) {
            status = "FAILURE";
            throw ex;
        }finally{
            String userEmail = null;
            Object[] args = joinPoint.getArgs();
            for (Object arg : args) {
                if (arg instanceof LoginRequest login) {
                    userEmail = login.getEmail();
                } else if (arg instanceof RegisterRequest register) {
                    userEmail = register.getEmail();
                }
            }

            Loggin log = Loggin.builder()
                    .timestamp(timestamp)
                    .event(event)
                    .path(joinPoint.getTarget().getClass().getSimpleName())
                    .service("platform-service")
                    .status(status)
                    .requestId(UUID.randomUUID().toString())
                    .env(env.toUpperCase())
                    .userEmail(userEmail)
                    .build();

            logRepository.save(log);

        }
        return result;
    }
}
