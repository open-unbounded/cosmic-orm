package kd.alm.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * </p>
 * 值集(下拉框/多类别基础资料类型)注解
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/05/08 11:06
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ValueSet {
    /**
     * 映射字段
     */
    String value() default "";

    /**
     * 描述
     */
    String description() default "";
}
