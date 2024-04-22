package com.meteormin.friday.aspect;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * logging aspect
 * 
 * @author seongminyoo*
 * @since 2023/09/11
 */
public abstract class LoggingAspect {
    protected String tag;

    public static final String RETUNING = "Returning";

    public static final String THROWING = "Throwing";

    protected final Logger log;

    protected final ObjectMapper objectMapper;

    private static final String DEFAULT_LOG_FORMAT = "[%s%s] {} {}";

    protected LoggingAspect(String tag, Logger log, ObjectMapper objectMapper) {
        this.tag = tag;
        this.log = log;
        this.objectMapper = objectMapper;
    }

    /**
     * Method for logging around a specific method call in a Java application.
     *
     * @param joinPoint the join point representing the specific method call
     * @return the response entity or null if no content
     */
    protected abstract Object aroundLogging(ProceedingJoinPoint joinPoint);

    /**
     * A method that performs some action before logging.
     *
     * @param action the action to be performed
     * @param joinPoint the join point for logging
     */
    protected void beforeLogging(String action, JoinPoint joinPoint) {
        loggingJoinPoint(action, joinPoint);
    }

    /**
     * Logs the action after a method has returned.
     *
     * @param action the action being logged
     * @param joinPoint the join point of the method
     * @param returnValue the return value of the method
     */
    protected void afterReturningLogging(String action, JoinPoint joinPoint, Object returnValue) {
        loggingJoinPoint(action + RETUNING, joinPoint, returnValue);
    }

    /**
     * Logs the action, join point, and exception after an exception is thrown.
     *
     * @param action the action that was being performed
     * @param joinPoint the join point at which the exception was thrown
     * @param e the exception that was thrown
     */
    protected void afterThrowingLogging(String action, JoinPoint joinPoint, Exception e) {
        loggingJoinPoint(action + THROWING, joinPoint, e);
    }

    /**
     * Logs the action and details of the join point before proceeding.
     *
     * @param action the action to be logged
     * @param joinPoint the join point to be logged
     */
    protected void loggingJoinPoint(String action, JoinPoint joinPoint) {
        String format = String.format(DEFAULT_LOG_FORMAT, "before", action);

        var signature = parseSignature(joinPoint);
        log.info(format, tag, signature);

        var details = parseDetails(joinPoint);
        for (var detail : details) {
            log.info(format, tag, detail);
        }
    }

    /**
     * A method for logging the join point with the specified action.
     *
     * @param action the action being performed
     * @param joinPoint the join point
     * @param returnValue the return value of the join point
     */
    protected void loggingJoinPoint(
            String action,
            JoinPoint joinPoint,
            Object returnValue) {
        String format = String.format(DEFAULT_LOG_FORMAT, "after", action);
        var signature = parseSignature(joinPoint);
        log.info(format, tag, signature);
        if (returnValue == null) {
            return;
        }

        var returnJson = toJson(returnValue);
        log.info("ReturnValue {}", returnJson);
    }

    /**
     * loggingJoinPoint method for logging the join point with action and exception.
     *
     * @param action the action being performed
     * @param joinPoint the join point being logged
     * @param e the exception that occurred
     */
    protected void loggingJoinPoint(
            String action,
            JoinPoint joinPoint,
            Exception e) {

        String format = String.format(DEFAULT_LOG_FORMAT, "after", action);
        var signature = parseSignature(joinPoint);
        log.error(format, tag, signature);
        log.error("Message {}", e.getMessage(), e);
    }

    /**
     * Parse the signature of the given JoinPoint and create a log message
     *
     * @param joinPoint the JoinPoint to parse
     * @return the formatted log message
     */
    protected String parseSignature(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String className = joinPoint.getTarget().getClass().getName();
        String methodName = methodSignature.getName();
        String[] names = methodSignature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        // 로깅 형식에 맞게 출력
        StringBuilder logMessage = new StringBuilder();
        logMessage.append(className).append(".")
                .append(methodName).append("(");

        // 메소드 파라미터 추가
        for (int i = 0; i < args.length; i++) {
            logMessage.append(names[i])
                    .append("=")
                    .append(args[i]);
            if (i < args.length - 1) {
                logMessage.append(", ");
            }
        }

        logMessage.append(")");

        return logMessage.toString();
    }

    /**
     * A method to parse details from the JoinPoint.
     *
     * @param joinPoint the join point containing the details to parse
     * @return an array of strings containing the parsed details
     */
    protected String[] parseDetails(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = methodSignature.getName();
        String[] names = methodSignature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        String[] logMessages = new String[args.length + 1];
        logMessages[0] = "[" + className + "." + methodName + "] Details ";

        // 로깅 형식에 맞게 출력
        for (int i = 0; i < args.length; i++) {
            StringBuilder logMessage = new StringBuilder();
            var arg = toJson(args[i]);
            logMessages[i + 1] = logMessage
                    .append("[").append(className).append(".").append(methodName).append("] ")
                    .append("Parameter(").append(i).append("): ")
                    .append(names[i]).append(" ")
                    .append(arg).toString();
        }

        return logMessages;
    }

    /**
     * Converts the given object to its JSON representation.
     *
     * @param obj the object to be converted to JSON
     * @return the JSON representation of the object
     */
    protected String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonMappingException e) {
            log.debug("failed mapping object: {}", e.getOriginalMessage());
            log.info("{}", obj);
        } catch (IOException e) {
            log.debug("failed mapping object: {}", e.getMessage());
            log.info("{}", obj);
        }

        return obj.toString();
    }

    /**
     * Converts a Map of String arrays to a String representation.
     *
     * @param paramMap the Map of String arrays to be converted
     * @return the String representation of the paramMap
     */
    protected String paramMapToString(Map<String, String[]> paramMap) {
        return paramMap.entrySet().stream()
                .map(entry -> {
                    var values = String.join(", ", entry.getValue());
                    return String.format("%s -> (%s)",
                            entry.getKey(), values);
                }).collect(Collectors.joining(", "));
    }

}
