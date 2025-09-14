package com.itzixi;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Component
@Slf4j
@Aspect
public class ServiceLogAspect {

    @Around("execution(* com.itzixi.service.impl..*.*(..))")
    public Object recordTimesLog(ProceedingJoinPoint joinPoint) throws Throwable {

//        long begin = System.currentTimeMillis();

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        Object proceed = joinPoint.proceed();

        String point = joinPoint.getTarget().getClass().getName()
                        + "."
                        +  joinPoint.getSignature().getName();

//        long end = System.currentTimeMillis();
        stopWatch.stop();

//        long takeTime = end - begin;

        long takeTime = stopWatch.getTotalTimeMillis();

        if (takeTime > 3000) {
            log.error("{} 耗时偏长 {} 毫秒", point, takeTime);
        } else if (takeTime > 2000) {
            log.warn("{} 耗时中等 {} 毫秒", point, takeTime);
        } else {
            log.info("{} 耗时 {} 毫秒", point, takeTime);
        }

        return proceed;
    }

}
