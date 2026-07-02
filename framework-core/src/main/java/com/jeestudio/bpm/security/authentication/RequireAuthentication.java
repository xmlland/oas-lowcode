package com.jeestudio.bpm.security.authentication;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @Description: 二次鉴权注解
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireAuthentication {
    RequireAuthenticationType authenticationType() default RequireAuthenticationType.PASSWORD;
    Class<Authenticator> authenticator() default Authenticator.class;
}
