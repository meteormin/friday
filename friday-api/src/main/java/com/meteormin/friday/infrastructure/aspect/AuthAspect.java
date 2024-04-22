package com.meteormin.friday.infrastructure.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.meteormin.friday.auth.domain.Token;
import com.meteormin.friday.infrastructure.persistence.entities.AccessTokenEntity;
import com.meteormin.friday.infrastructure.persistence.entities.LoginHistoryEntity;
import com.meteormin.friday.infrastructure.persistence.entities.UserEntity;
import com.meteormin.friday.infrastructure.persistence.repositories.AccessTokenEntityRepository;
import com.meteormin.friday.infrastructure.persistence.repositories.LoginHistoryEntityRepository;
import com.meteormin.friday.infrastructure.persistence.repositories.UserEntityRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 인증 관련 로깅
 *
 * @author seongminyoo
 * @since 2023/09/15
 */
@Aspect
@Component
@SuppressWarnings("unchecked")
public class AuthAspect extends LoggingAspect {
    /**
     * user entity repository
     */
    private final UserEntityRepository userEntityRepository;

    /**
     * access token entity repository
     */
    private final AccessTokenEntityRepository accessTokenEntityRepository;

    /**
     * login history entity repository
     */
    private final LoginHistoryEntityRepository loginHistoryEntityRepository;

    /**
     * AuthAspect Constructor
     *
     * @param userEntityRepository user entity repository
     * @param accessTokenEntityRepository access token entity repository
     * @param loginHistoryEntityRepository login history entity repository
     * @param objectMapper object mapper
     */
    public AuthAspect(
            UserEntityRepository userEntityRepository,
            AccessTokenEntityRepository accessTokenEntityRepository,
            LoginHistoryEntityRepository loginHistoryEntityRepository,
            ObjectMapper objectMapper) {
        super(
                "Auth",
                LoggerFactory.getLogger(AuthAspect.class),
                objectMapper);
        this.userEntityRepository = userEntityRepository;
        this.accessTokenEntityRepository = accessTokenEntityRepository;
        this.loginHistoryEntityRepository = loginHistoryEntityRepository;
    }

    /**
     * AOP: login end point pointcut
     * <p>
     * If has {@link com.meteormin.friday.infrastructure.aspect.annotation.LoginEndPoint} annotation
     * </p>
     *
     * @see com.meteormin.friday.infrastructure.aspect.annotation.LoginEndPoint
     */
    @Pointcut("@annotation(com.meteormin.friday.infrastructure.aspect.annotation.LoginEndPoint)")
    public void loginEndPoint() {}

    /**
     * Executes the code before the login endpoint is called.
     *
     * @param joinPoint the join point at which the advice is applied
     */
    @Before("loginEndPoint()")
    public void beforeLogin(JoinPoint joinPoint) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes())
                        .getRequest();
        log.info("[Before login] {} {} < {}",
                request.getMethod(),
                request.getRequestURI(),
                request.getRemoteHost());
        beforeLogging("BeforeLogin", joinPoint);
    }

    /**
     * This method is executed after an exception is thrown in the loginEndPoint() pointcut.
     *
     * @param joinPoint the join point at which the exception was thrown
     * @param e the exception that was thrown
     */
    @AfterThrowing(pointcut = "loginEndPoint()", throwing = "e")
    public void afterLoginThrowing(JoinPoint joinPoint, Exception e) {
        log.error("[After login Throwing] {}({})",
                e.getMessage(),
                e.getClass().getSimpleName());

        afterThrowingLogging("AfterLogin", joinPoint, e);
    }

    /**
     * After returning advice executed after a method returns successfully.
     *
     * @param joinPoint the join point at which the advice is being invoked
     * @param returnValue the value returned by the method
     */
    @AfterReturning(pointcut = "loginEndPoint()", returning = "returnValue")
    public void afterLoginRetuning(JoinPoint joinPoint, Object returnValue) {
        ResponseEntity<Token> token = (ResponseEntity<Token>) returnValue;
        UserEntity user;
        if (returnValue != null) {
            String json = toJson(returnValue);
            log.info("[After login Returning] {}", json);
        } else {
            log.info("[After login Returning] null");
        }

        var loginHistoryEntity = createFromRequest();

        loginHistoryEntity.setUser(null);
        if (token != null) {
            var tokenBody = token.getBody();
            if (tokenBody != null && tokenBody.accessToken() != null) {
                user = accessTokenEntityRepository
                        .findByToken(tokenBody.accessToken())
                        .flatMap((AccessTokenEntity tokenEntity) -> userEntityRepository
                                .findById(Long.valueOf(tokenEntity.getUserId())))
                        .orElse(null);
                loginHistoryEntity.setUser(user);
            }
        }

        loginHistoryEntityRepository.save(loginHistoryEntity);

        afterReturningLogging("AfterLogin", joinPoint, returnValue);
    }

    /**
     * Auth controller point
     */
    @Pointcut("within(com.meteormin.friday.auth.adapter.in.rest.AuthController)")
    public void authControllerPoint() {}

    /**
     * AuthController executing around request
     *
     * @param joinPoint join point.
     * @return return value of AuthController
     * @throws Throwable if an error occurs during the execution of the advice
     */
    @Around("authControllerPoint()")
    public Object aroundRequest(ProceedingJoinPoint joinPoint) throws Throwable {
        return aroundLogging(joinPoint);
    }

    /**
     * This function is executed before the execution of any method in the AuthController class that
     * is annotated with the authControllerPoint() pointcut.
     *
     * @param joinPoint the join point at which this advice is being applied
     */
    @Before("authControllerPoint()")
    public void beforeAuth(JoinPoint joinPoint) {
        beforeLogging("Auth", joinPoint);
    }

    /**
     * AuthController executing after request
     *
     * @param joinPoint the join point at which the advice is applied
     * @param returnValue the return value of the method
     */
    @AfterReturning(pointcut = "authControllerPoint()", returning = "returnValue")
    public void afterAuthRetuning(JoinPoint joinPoint, Object returnValue) {
        afterReturningLogging("Auth", joinPoint, returnValue);
    }

    /**
     * AuthController executing after throwing request
     *
     * @param joinPoint the join point at which the advice is applied
     * @param e the exception
     */
    @AfterThrowing(pointcut = "authControllerPoint()", throwing = "e")
    public void afterAuthThrowing(JoinPoint joinPoint, Exception e) {
        afterThrowingLogging("Auth", joinPoint, e);
    }

    /**
     * Auth Persistence point
     */
    @Pointcut("within(com.meteormin.friday.auth.adapter.out.persistence.AuthAdapter)")
    public void authPersistencePoint() {}

    /**
     * AuthPersistence executing around request
     *
     * @param joinPoint the join point at which the advice is applied
     */
    @Before("authPersistencePoint()")
    public void beforeAuthPersistence(JoinPoint joinPoint) {
        beforeLogging("Auth", joinPoint);
    }

    /**
     * AuthPersistence executing after request
     *
     * @param joinPoint the join point at which the advice is applied
     * @param returnValue the return value of the method
     */
    @AfterReturning(pointcut = "authPersistencePoint()", returning = "returnValue")
    public void afterAuthPersistenceRetuning(JoinPoint joinPoint, Object returnValue) {
        afterReturningLogging("Auth", joinPoint, returnValue);
    }

    /**
     * AuthPersistence executing after throwing request
     *
     * @param joinPoint the join point at which the advice is applied
     * @param e the exception
     */
    @AfterThrowing(pointcut = "authPersistencePoint()", throwing = "e")
    public void afterAuthPersistenceThrowing(JoinPoint joinPoint, Exception e) {
        afterThrowingLogging("Auth", joinPoint, e);
    }

    /**
     * Create a new LoginHistoryEntity object based on the current HttpServletRequest.
     *
     * @return The newly created LoginHistoryEntity object.
     */
    private LoginHistoryEntity createFromRequest() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder
                .currentRequestAttributes()).getRequest();

        String ip;
        if (request.getHeader("X-Forwarded-For") == null) {
            ip = request.getRemoteAddr();
        } else {
            ip = request.getHeader("X-Forwarded-For");
        }

        return LoginHistoryEntity.create(null, ip);
    }
}
