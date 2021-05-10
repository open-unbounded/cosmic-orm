package kd.alm.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * </p>
 * 主键注解
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/04/19 22:52
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PrimaryKey {
    /**
     * 描述
     */
    String description() default "";
}
