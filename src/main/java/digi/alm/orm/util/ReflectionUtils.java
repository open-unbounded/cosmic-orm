package digi.alm.orm.util;

import com.google.common.base.CaseFormat;
import digi.alm.orm.annotation.Entity;
import digi.alm.orm.annotation.Entry;
import digi.alm.orm.annotation.PrimaryKey;
import digi.alm.orm.annotation.ValueSet;
import kd.bos.exception.KDBizException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.*;
import java.lang.annotation.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * </p>
 * 反射工具类
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/04/20 16:03
 */
public class ReflectionUtils {
    private static final Log log = LogFactory.getLog(ReflectionUtils.class);

    public static Class<?> getClassGenericType(final Class<?> clazz) {
        return getClassGenericType(clazz, 0);
    }

    public static Class<?> getClassGenericType(final Class<?> clazz, final int index) {
        Type genType = clazz.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            return Object.class;
        } else {
            Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
            if (index < params.length && index >= 0) {
                if (!(params[index] instanceof Class)) {
                    log.warn("{} not set the actual class on superclass generic parameter", clazz.getSimpleName());
                    return Object.class;
                } else {
                    return (Class<?>) params[index];
                }
            } else {
                log.warn("Index: {}, Size of {}'s Parameterized Type: {}", index, clazz.getSimpleName(), params.length);
                return Object.class;
            }
        }
    }

    public static Class<?> getFieldGenericType(Field field, int index) {
        final Type genericType = field.getGenericType();
        if (!(genericType instanceof ParameterizedType)) {
            return Object.class;
        } else {
            Type[] params = ((ParameterizedType) genericType).getActualTypeArguments();
            if (index < params.length && index >= 0) {
                if (!(params[index] instanceof Class)) {
                    log.warn("{} not set the actual class on field generic parameter", field.getName());
                    return Object.class;
                } else {
                    return (Class<?>) params[index];
                }
            } else {
                log.warn("Index: {}, Size of {}'s Parameterized Type: {}", index, field.getName(), params.length);
                return Object.class;
            }
        }
    }

    public static Class<?> getFieldGenericType(Field field) {
        return getFieldGenericType(field, 0);
    }

    public static Field makeAccessible(Field field) {
        if (!Modifier.isPublic(field.getModifiers()) || !Modifier.isPublic(field.getDeclaringClass().getModifiers())) {
            field.setAccessible(true);
        }
        return field;
    }

    public static Field getField(Class<?> c, String fieldName) {
        for (Class<?> tempClass = c; tempClass != null; tempClass = tempClass.getSuperclass()) {
            try {
                final Field field = tempClass.getDeclaredField(fieldName);
//                makeAccessible(field);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
        }

        assert c != null;
        log.error(String.format("%sClass不存在%s", c.getName(), fieldName));
        return null;
    }

    public static Optional<Field> getPrimaryKeyFieldOptional(Class<?> c) {
        for (Class<?> tempClass = c; tempClass != null; tempClass = tempClass.getSuperclass()) {
            for (Field field : tempClass.getDeclaredFields()) {
                if (isAnnotationPresent(field, PrimaryKey.class)) {
//                    makeAccessible(field);
                    return Optional.of(field);
                }
            }
        }
        assert c != null;
        log.error(String.format("%sClass不存在@PrimaryKey注解", c.getName()));
        return Optional.empty();
    }

    public static Field[] getAllField(Class<?> clazz) {
        List<Field> fieldList = new ArrayList<>();

        for (Class<?> tempClass = clazz; tempClass != null; tempClass = tempClass.getSuperclass()) {
            fieldList.addAll(Arrays.asList(tempClass.getDeclaredFields()));
        }
        return fieldList.toArray(new Field[0]);
    }

    public static Map<String, Field> getAllFieldMap(Class<?> clazz) {
        Map<String, Field> fieldMap = new LinkedHashMap<>();

        for (Class<?> tempClass = clazz; tempClass != null; tempClass = tempClass.getSuperclass()) {
            final LinkedHashMap<String, Field> map = Arrays.stream(tempClass.getDeclaredFields()).collect(Collectors.toMap(Field::getName, it -> it, (u, v) -> {
                throw new IllegalStateException(String.format("Duplicate key %s", u));
            }, LinkedHashMap::new));
            fieldMap.putAll(map);
        }
        return fieldMap;
    }

    public static Optional<String> getFormFieldNameOptional(Field field) {
        if (ReflectionUtils.isAnnotationPresent(field, PrimaryKey.class)) {
            return Optional.of(field.getAnnotation(PrimaryKey.class).value());
        } else if (isAnnotationPresent(field, digi.alm.orm.annotation.Field.class)) {
            final String formFiledName = field.getAnnotation(digi.alm.orm.annotation.Field.class).value();
            if (StringUtils.isNotBlank(formFiledName)) {
                return Optional.of(formFiledName);
            } else {
                return Optional.of("digi_" + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName()));
            }
        } else if (isAnnotationPresent(field, Entry.class)) {
            final String formFiledName = field.getAnnotation(Entry.class).value();
            if (StringUtils.isNotBlank(formFiledName)) {
                return Optional.of(formFiledName);
            } else {
                return Optional.of("digi_" + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName()));
            }
        }
        return Optional.empty();
    }

    public static Optional<String> getDBFieldNameOptional(Field field) {
        if (isAnnotationPresent(field, digi.alm.orm.annotation.Field.class)) {
            final digi.alm.orm.annotation.Field fieldAnnotation = field.getAnnotation(digi.alm.orm.annotation.Field.class);
            if (!fieldAnnotation.isDBField()) {
                // 非数据表字段则直接返回
                return Optional.empty();
            }
        } else if (isAnnotationPresent(field, Entry.class)) {
            if (!field.getAnnotation(Entry.class).isDBField()) {
                return Optional.empty();
            }
        }
        final Optional<String> formFieldNameOptional = getFormFieldNameOptional(field);
        if (formFieldNameOptional.isPresent()) {
            String formFieldName = formFieldNameOptional.get();
            final String[] split = formFieldName.split("\\.");
            if (split.length == 1) {
                // @Field("name")
                return Optional.of(split[0]);
            } else if (split.length == 2 && split[1].equals("id")) {
                // @Field("didi_asset_id.id")
                return Optional.of(split[0]);
            } else {
                // 错误示例:
                // @Field("didi_asset_id.name")
                // @Field("didi_asset_id.asset_model")
                // 正确示例
                // @Field("didi_asset_id.name", isDBField = false)
                // @Field("didi_asset_id.asset_model", isDBField = false)
                throw new KDBizException(String.format("%s:标识不合法.该字段为当前非数据表字段,请使用@Field(value = \"%s\", isDBField = false)", field.getName(), formFieldName));
            }
        }
        return Optional.empty();
    }

    public static <T> void checkEntity(Class<T> c) {
        for (Class<?> tempClass = c; tempClass != null; tempClass = tempClass.getSuperclass()) {
            if (tempClass.isAnnotationPresent(Entity.class)) {
                return;
            }
        }
        throw new RuntimeException("未添加@Entity注解");
    }

    public static <T> Entity getAnnotationEntity(Class<T> c) {
        for (Class<?> tempClass = c; tempClass != null; tempClass = tempClass.getSuperclass()) {
            if (tempClass.isAnnotationPresent(Entity.class)) {
                return tempClass.getAnnotation(Entity.class);
            }
        }
        throw new RuntimeException("未添加@Entity注解");
    }

    /**
     * 设置字段值
     *
     * @param t     类
     * @param field 字段
     * @param v     值
     * @param <T>   泛型
     * @throws IllegalAccessException
     */
    public static <T> void setValue(T t, Field field, Object v) throws IllegalAccessException {
        makeAccessible(field);
        field.set(t, v);
    }

    /**
     * 获取字段值
     *
     * @param t     类
     * @param field 字段
     * @param <T>   泛型
     * @return 字段值
     * @throws IllegalAccessException
     */
    public static <T> Object getValue(T t, Field field) throws IllegalAccessException {
        makeAccessible(field);
        return field.get(t);
    }

    /**
     * 判断是否存在注释
     *
     * @param field 字段
     * @param c     注解
     * @return boolean
     */
    public static boolean isAnnotationPresent(Field field, Class<? extends Annotation> c) {
        if (field.isAnnotationPresent(c)) {
            // 存在,则立即返回
            return true;
        }
        for (Annotation annotation : field.getAnnotations()) {
            if (annotation.annotationType() != Deprecated.class &&
                    annotation.annotationType() != SuppressWarnings.class &&
                    annotation.annotationType() != Override.class &&
                    annotation.annotationType() != PostConstruct.class &&
                    annotation.annotationType() != PreDestroy.class &&
                    annotation.annotationType() != Resource.class &&
                    annotation.annotationType() != Resources.class &&
                    annotation.annotationType() != Generated.class &&
                    annotation.annotationType() != Target.class &&
                    annotation.annotationType() != Retention.class &&
                    annotation.annotationType() != Documented.class &&
                    annotation.annotationType() != Inherited.class
            ) {
                if (annotation.annotationType() == c) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取值集名称字段
     * 用于值集Value->Name的映射
     *
     * @param valueSetField 值集值字段
     * @param allField      全字段
     * @return 值集名称字段
     */

    public static Optional<Field> getValueSetNameField(Field valueSetField, Field[] allField) {
        final ValueSet annotation = valueSetField.getAnnotation(ValueSet.class);
        if (annotation == null) {
            return Optional.empty();
        }
        String filedName = annotation.value();
        if (filedName.equals("")) {
            // 默认映射字段名
            return Arrays.stream(allField).filter(it -> it.getName().equals(valueSetField.getName() + "Name")).findFirst();
        } else {
            return Arrays.stream(allField).filter(it -> it.getName().equals(filedName)).findFirst();
        }
    }
}
