package digi.alm.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * </p>
 * 字段注解
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/04/19 21:13
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Field {
    /**
     * 字段名称
     */
    String value() default "";

    /**
     * 字段描述
     */
    String description() default "";

    /**
     * 是数据表字段
     */
    boolean isDBField() default true;
}
