package com.meteormin.friday.aspect;

import org.aspectj.lang.JoinPoint;

/**
 * Hexagonal Aspect
 * <ul>
 * <li>RestAdapters</li>
 * <li>PersistenceAdapters</li>
 * <li>Application</li>
 * <li>Domain</li>
 * </ul>
 *
 * @author seongminyoo
 * @since 2023/09/11
 */
public interface HexagonalAspect {
    String REQUEST = "Request";
    String PERSISTENCE = "Persistence";
    String APPLICATION = "Application";
    String DOMAIN = "Domain";

    // Adapter layer

    /**
     * <p>
     * RestAdapters pointcut.
     * </p>
     * <br>
     * <p>
     * RestAdapter equals RestController
     * </p>
     * <br>
     * <strong>Related current interface methods.</strong>
     * <ul>
     * <li>{@link #beforeRequest(JoinPoint)}</li>
     * <li>{@link #afterRequestReturning(JoinPoint, Object)}</li>
     * <li>{@link #afterRequestThrowing(JoinPoint, Exception)}</li>
     * </ul>
     */
    void restAdapterPoint();

    /**
     * before request
     *
     * @param joinPoint the join point at which the advice is applied
     */
    void beforeRequest(JoinPoint joinPoint);

    /**
     * after request returning
     *
     * @param joinPoint the join point at which the advice is applied
     * @param returnValue the return value of the method
     */
    void afterRequestReturning(JoinPoint joinPoint, Object returnValue);

    /**
     * after request throwing
     *
     * @param joinPoint the join point at which the advice is applied
     * @param e the exception
     */
    void afterRequestThrowing(JoinPoint joinPoint, Exception e);

    // Persistence layer

    /**
     * <p>
     * Persistence layer pointcut
     * </p>
     * <br>
     * <p>
     * Persistence Layer is a sets of repository
     * </p>
     * <br>
     * <strong>Related current interface methods.</strong>
     * <ul>
     * <li>{@link #beforePersistence(JoinPoint)}</li>
     * <li>{@link #afterPersistenceReturning(JoinPoint, Object)}</li>
     * <li>{@link #afterPersistenceThrowing(JoinPoint, Exception)}</li>
     * </ul>
     */
    void persistencePoint();

    /**
     * before persistence
     *
     * @param joinPoint the join point at which the advice is applied
     */
    void beforePersistence(JoinPoint joinPoint);

    /**
     * after persistence returning
     *
     * @param joinPoint the join point at which the advice is applied
     * @param returnValue the return value of the method
     */
    void afterPersistenceReturning(JoinPoint joinPoint, Object returnValue);

    /**
     * after persistence throwing
     *
     * @param joinPoint the join point at which the advice is applied
     * @param e the exception
     */
    void afterPersistenceThrowing(JoinPoint joinPoint, Exception e);

    // Application layer

    /**
     * <p>
     * Application layer pointcut
     * </p>
     * <br>
     * <p>
     * Application Layer equals Service's subset
     * </p>
     * <p>
     * Called by Usecase in hexagonal arch.
     * </p>
     * <p>
     * just controlling logic flow
     * </p>
     * <br>
     * <strong>Related current interface methods.</strong>
     * <ul>
     * <li>{@link #beforeService(JoinPoint) }</li>
     * <li>{@link #afterServiceReturning(JoinPoint, Object)}</li>
     * <li>{@link #afterServiceThrowing(JoinPoint, Exception)}</li>
     * </ul>
     */
    void applicationPoint();

    /**
     * before service
     *
     * @param joinPoint the join point at which the advice is applied
     */
    void beforeService(JoinPoint joinPoint);

    /**
     * after service returning
     *
     * @param joinPoint the join point at which the advice is applied
     * @param returnValue the return value of the method
     */
    void afterServiceReturning(JoinPoint joinPoint, Object returnValue);

    /**
     * after service throwing
     *
     * @param joinPoint the join point at which the advice is applied
     * @param e the exception
     */
    void afterServiceThrowing(JoinPoint joinPoint, Exception e);

    // Domain layer

    /**
     * <p>
     * Domain layer pointcut
     * </p>
     * <p>
     * * Domain is a set of aggregate.
     * </p>
     * <p>
     * * Domain has business logic
     * </p>
     * <p>
     * Related current interface methods.
     * </p>
     * <ul>
     * <li>{@link #beforeDomain(JoinPoint)}</li>
     * <li>{@link #afterDomainReturning(JoinPoint, Object)}</li>
     * <li>{@link #afterDomainThrowing(JoinPoint, Exception)}</li>
     * </ul>
     */
    void domainPoint();

    /**
     * before domain
     *
     * @param joinPoint the join point at which the advice is applied
     */
    void beforeDomain(JoinPoint joinPoint);

    /**
     * after domain returning
     *
     * @param joinPoint the join point at which the advice is applied
     * @param returnValue the return value of the method
     */
    void afterDomainReturning(JoinPoint joinPoint, Object returnValue);

    /**
     * after domain throwing
     *
     * @param joinPoint the join point at which the advice is applied
     * @param e the exception
     */
    void afterDomainThrowing(JoinPoint joinPoint, Exception e);
}

