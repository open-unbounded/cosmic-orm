package kd.alm.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * </p>
 * 实体注解
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/04/19 17:42
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entity {
    /**
     * 表单或单据体标识
     */
    String value();

    /**
     * 字段描述
     */
    String description() default "";
}
