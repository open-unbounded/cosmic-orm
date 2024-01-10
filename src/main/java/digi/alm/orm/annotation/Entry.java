package digi.alm.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * </p>
 * 单据体注解
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/04/19 17:42
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Entry {
    /**
     * 单据体
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

    /**
     * 当前单据体数据映射的Class类型
     * 默认为{@code Void.class}时
     */
    Class<?> mapClass() default Void.class;
}
