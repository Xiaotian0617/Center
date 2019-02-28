package com.al.exchange.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * No need login annotation for controller.
 * 使用此注解的方法不会被拦截器拦截
 *
 * @author Asin Liu
 * @version 0.0.1
 * @since 0.0.1
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NoNeedLogin {
}
