package kd.alm.orm.util;

import kd.alm.orm.MulValueSet;
import kd.alm.orm.annotation.Entity;
import kd.alm.orm.annotation.Entry;
import kd.alm.orm.annotation.PrimaryKey;
import kd.alm.orm.annotation.ValueSet;
import kd.alm.orm.exception.OrmRuntimeException;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.ILocaleString;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.dataentity.metadata.IDataEntityProperty;
import kd.bos.dataentity.metadata.IMetadata;
import kd.bos.dataentity.metadata.dynamicobject.DynamicObjectType;
import kd.bos.dataentity.metadata.dynamicobject.DynamicProperty;
import kd.bos.entity.ValueMapItem;
import kd.bos.entity.property.*;
import kd.bos.exception.KDBizException;
import kd.bos.id.ID;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 用于对象与DTO的映射
 * 将原本BaseRepository对应操作拆分，准备尝试使用此类，并准备做移除BaseRepositoryImpl中功能处理
 *
 * </p>
 *
 * @author wentao.liu01@foxmail.com 2022/05/13 10:44
 */
public class AlmDynamicUtils {

    private static final Log log = LogFactory.getLog(AlmDynamicUtils.class);

    private static final Long LONG_ZERO = 0L;


    /**
     * 将数据映射为DynamicObject[]
     *
     * @param list 数据
     * @return DynamicObject[]
     */
    public static <T> DynamicObject[] mapDynamicObject(List<T> list) {
        final int size = list.size();
        assert size != 0;
        final DynamicObject[] dynamicObjects = new DynamicObject[size];
        for (int i = 0; i < list.size(); i++) {
            dynamicObjects[i] = mapDynamicObject(list.get(i));
        }
        return dynamicObjects;
    }

    /**
     * 将数据映射为DynamicObject
     *
     * @param t 对象
     * @return DynamicObject[]
     */
    public static <T> DynamicObject mapDynamicObject(T t) {

        Class<?> entityClass = t.getClass();
        final Entity entity = ReflectionUtils.getAnnotationEntity(entityClass);
        String entityName = entity.value();

        final Optional<Field> primaryKeyFieldOptional = ReflectionUtils.getPrimaryKeyFieldOptional(entityClass);

        DynamicObject dynamicObject = null;
        if (primaryKeyFieldOptional.isPresent()) {
            // 主键
            final Field field = primaryKeyFieldOptional.get();
            try {
                // 主键数据
                final Object id = ReflectionUtils.getValue(t, field);
                // 查询数据
                final Optional<DynamicObject> objectOptional = AlmBusinessDataServiceHelper.loadSingleOptional(id, entityName);
                if (objectOptional.isPresent()) {
                    dynamicObject = objectOptional.get();
                }
            } catch (IllegalAccessException ignored) {
            }
        }
        // 数据不存在新建
        if (dynamicObject == null) {
            dynamicObject = AlmBusinessDataServiceHelper.newDynamicObject(entityName);
        }
        final Field[] allField = ReflectionUtils.getAllField(entityClass);
        try {
            // 向dynamicObject设置数据
            mapDynamicObjectValue(t, allField, dynamicObject);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return dynamicObject;
    }

    /**
     * 把{@link DynamicObject}生成对象{@link R}
     *
     * @param c              对象{@link R}的Class
     * @param dynamicObjects DynamicObject数据集
     * @param <R>            泛型
     * @return 对象{@link T}
     */
    public static <R> List<R> mapJavaObject(Class<R> c, DynamicObject... dynamicObjects) {
        final Field[] allField = ReflectionUtils.getAllField(c);
        return mapJavaObject(c, allField, dynamicObjects);
    }

    /**
     * 把{@link DynamicObject}生成对象{@link R}
     *
     * @param c              对象{@link R}的Class
     * @param allField       所有的字段
     * @param dynamicObjects DynamicObject数据集
     * @param <R>            泛型
     * @return 对象{@link T}
     */
    public static <R> List<R> mapJavaObject(Class<R> c, Field[] allField, DynamicObject... dynamicObjects) {
        final ArrayList<R> result = new ArrayList<>();
        for (DynamicObject dynamicObject : dynamicObjects) {
            final R t = mapJavaObject(c, allField, dynamicObject);
            result.add(t);
        }
        return result;
    }

    /**
     * 把{@link DynamicObject}生成对象{@link R}
     *
     * @param c             对象{@link R}的Class
     * @param dynamicObject DynamicObject数据
     * @param <R>           泛型
     * @return 对象{@link T}
     */
    public static <R> R mapJavaObject(Class<R> c, DynamicObject dynamicObject) {
        final Field[] allField = ReflectionUtils.getAllField(c);
        return mapJavaObject(c, allField, dynamicObject);
    }

    /**
     * 把{@link DynamicObject}生成对象{@link R}
     *
     * @param c             对象{@link R}的Class
     * @param allField      所有的字段
     * @param dynamicObject DynamicObject数据
     * @param <R>           泛型
     * @return 对象{@link T}
     */
    public static <R> R mapJavaObject(Class<R> c, Field[] allField, DynamicObject dynamicObject) {
        final R t;
        try {
            t = c.newInstance();

            for (Field field : allField) {
                // 获取当前字段在苍穹中的字段属性
                final Optional<String> fieldNameOptional = ReflectionUtils.getFormFieldNameOptional(field);
                if (fieldNameOptional.isPresent()) {
                    final String formFieldName = fieldNameOptional.get().split("\\.")[0];
                    // 获得字段属性
                    final IDataEntityProperty iDataEntityProperty = getProperties(dynamicObject).get(formFieldName);
                    if (iDataEntityProperty == null) {
                        throw new OrmRuntimeException("苍穹中不存在此字段");
                    }
                    if (iDataEntityProperty instanceof MulComboProp && field.getType().equals(MulValueSet.class)) {
                        // 设置多值集
                        setMutValueSetFieldValue(t, field, dynamicObject);
                    } else if (iDataEntityProperty instanceof MulBasedataProp) {
                        // 多选基础资料
                        setMutBaseDataFieldValue(t, field, dynamicObject);

                    } else {
                        // 设置当前字段的值
                        setFieldValue(t, field, dynamicObject);
                    }

                    final Object valueSetValue = ReflectionUtils.getValue(t, field);

                    if (valueSetValue == null) {
                        // 跳过
                        continue;
                    }
                    handleSpecialType(valueSetValue, iDataEntityProperty, field, allField, t);
                }
            }
            return t;
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("对象转换异常：", e);
            throw new KDBizException("对象转换异常！");
        }
    }


    /**
     * 将实体对象数据{@link R}映射到{@link DynamicObject}中
     *
     * @param t             数据
     * @param fields        实体对象的成员变量
     * @param dynamicObject DynamicObject数据
     * @param <R>           泛型
     * @throws IllegalAccessException
     */
    public static <R> void mapDynamicObjectValue(R t, Field[] fields, DynamicObject dynamicObject) throws IllegalAccessException {
        for (Field field : fields) {
            mapDynamicObjectValue(t, field, dynamicObject);
        }
    }

    /**
     * 设置DynamicObject的值
     *
     * @param t             数据
     * @param field         字段
     * @param dynamicObject DynamicObject对象
     * @param <R>           泛型
     * @throws IllegalAccessException 非法访问异常
     */
    public static <R> void mapDynamicObjectValue(R t, Field field, DynamicObject dynamicObject) throws IllegalAccessException {
        // 获取表单标识(数据表中)
        final Optional<String> formFieldNameOptional = ReflectionUtils.getDBFieldNameOptional(field);
        if (formFieldNameOptional.isPresent()) {
            final DynamicObjectType dynamicObjectType = dynamicObject.getDynamicObjectType();
            // 获取数据
            Object v = ReflectionUtils.getValue(t, field);
            final String formFieldName = formFieldNameOptional.get();

            if (field.isAnnotationPresent(PrimaryKey.class)) {
                // 主键
                if ("0".equals(v) || LONG_ZERO.equals(v)) {
                    // 创建ID
                    v = ID.genLongId();
                }
                dynamicObject.set(field.getAnnotation(PrimaryKey.class).value(), v);
            } else if (field.isAnnotationPresent(kd.alm.orm.annotation.Field.class)) {
                final DynamicProperty property = dynamicObjectType.getProperty(formFieldName);
                if (property instanceof BasedataProp) {
                    // 是基础资料

                    // 必录校验
                    if (((BasedataProp) property).isMustInput() && (v == null || "0".equals(v))) {
                        throw new KDBizException(String.format("[%s]字段对应的基础资料数据不存在,该字段为必录字段", field.getName()));
                    }
                    // 基础资料字段对应的表单标识
                    String entityName;
                    if (property instanceof ItemClassProp) {
                        // 是多类别基础资料
                        final String typePropName = ((ItemClassProp) property).getTypePropName();
                        entityName = dynamicObject.getString(typePropName);
                        if (StringUtils.isBlank(entityName)) {
                            throw new KDBizException(String.format("[%s]字段对应的多类别资料对应的多类别基础资料类型未被赋值", field.getName()));
                        }
                    } else {
                        // 普通基础资料
                        entityName = ((BasedataProp) property).getBaseEntityId();
                    }
                    final Optional<DynamicObject> vOptional = AlmBusinessDataServiceHelper.loadSingleOptional(v, entityName);
                    if (vOptional.isPresent()) {
                        // 数据存在
                        v = vOptional.get();
                        dynamicObject.set(formFieldName, v);
                    } else {
                        // 必录校验
                        if (((BasedataProp) property).isMustInput()) {
                            throw new KDBizException(String.format("[%s]字段对应的基础资料数据不存在,该字段为必录字段", field.getName()));
                        }
                    }
                } else {
                    dynamicObject.set(formFieldName, v);
                }

            } else if (Iterable.class.isAssignableFrom(field.getType()) && field.isAnnotationPresent(Entry.class)) {
                // 单据体
                if (v != null) {
                    final Iterable<?> entryList = (Iterable<?>) field.get(t);
                    final Class<?> c = ReflectionUtils.getFieldGenericType(field);
                    final Field[] entryAllField = ReflectionUtils.getAllField(c);
                    final DynamicObjectCollection dynamicObjectCollection = dynamicObject.getDynamicObjectCollection(formFieldName);

                    //
                    // 获取全部的行主键
                    // ------------------------------------------------------------------------------
                    // 主键字段
                    final Field primaryKeyField = ReflectionUtils.getPrimaryKeyFieldOptional(c)
                            .orElseThrow(() -> {
                                // 不存在主键注解,抛出异常
                                final Entry entry = field.getAnnotation(Entry.class);
                                return new KDBizException("单据体:".concat(entry.value()).concat("不存在主键注解@PrimaryKey"));
                            });

                    // 主键
                    Set<String> keys = new HashSet<>();
                    for (Object o : entryList) {
                        // 获取对应的主键
                        String value = (String) ReflectionUtils.getValue(o, primaryKeyField);
                        if (value != null) {
                            keys.add(value);
                        }
                    }

                    // 删除不存在的数据
                    dynamicObjectCollection.removeIf(next -> !keys.contains(next.getString(primaryKeyField.getAnnotation(PrimaryKey.class).value())));
                    // 建立映射关系
                    final Map<String/*主键*/, DynamicObject/*数据*/> dynamicObjectMap = dynamicObjectCollection.stream()
                            .collect(Collectors.toMap(it -> it.getString(primaryKeyField.getAnnotation(PrimaryKey.class).value()), it -> it));
                    for (Object o : entryList) {
                        String id = (String) ReflectionUtils.getValue(o, primaryKeyField);
                        // 存在则直接使用,不存在则新增
                        DynamicObject entryDo = dynamicObjectMap.get(id);
                        if (entryDo == null) {
                            entryDo = dynamicObjectCollection.addNew();
                        }
                        // 映射对象
                        mapDynamicObjectValue(o, entryAllField, entryDo);
                    }

                }
            }
        }

    }


    /**
     * 动态获取属性
     *
     * @return 属性
     */
    private static <R> Map<String, IDataEntityProperty> getProperties(DynamicObject dynamicObject) {
        final Map<String, IDataEntityProperty> properties = dynamicObject.getDynamicObjectType().getProperties().stream().collect(Collectors.toMap(IMetadata::getName, it -> it));
        // 单据体
        final Map<String, IDataEntityProperty> entryCollect = new HashMap<>();
        for (IDataEntityProperty property : properties.values()) {
            if (!(property instanceof LinkEntryProp) && property instanceof EntryProp) {
                final Map<String, IDataEntityProperty> map = dynamicObject.getDynamicObjectCollection(property.getName())
                        .getDynamicObjectType().getProperties().stream().collect(Collectors.toMap(IMetadata::getName, it -> it));
                entryCollect.putAll(map);
            }
        }
        properties.putAll(entryCollect);
        return properties;
    }

    /**
     * 设置多选值集字段数据
     *
     * @param t             实例对象
     * @param field         字段
     * @param dynamicObject DynamicObject
     * @param <R>           泛型
     * @throws IllegalAccessException
     */
    private static <R> void setMutValueSetFieldValue(R t, Field field, DynamicObject dynamicObject) throws IllegalAccessException {
        // 多值集
        final Optional<String> formFieldNameOptional = ReflectionUtils.getFormFieldNameOptional(field);
        final String formFieldName = formFieldNameOptional.orElseThrow(() -> new OrmRuntimeException("值集名称映射字段不存在"));

        final Object o = dynamicObject.get(formFieldName);
        if (o == null) {
            return;
        }
        final MulComboProp iDataEntityProperty = (MulComboProp) dynamicObject.getDynamicObjectType().getProperties().get(formFieldName);
        final Map<String, LocaleString> valueSetMap = iDataEntityProperty.getComboItems().stream().collect(Collectors.toMap(it -> it.getValue(), it -> it.getName()));
        final String mutValueSet = (String) o;

        // 构建值集
        final MulValueSet mulValueSet = new MulValueSet();
        for (String v : Arrays.stream(mutValueSet.split(",")).filter(StringUtils::isNoneBlank).toArray(String[]::new)) {
            final MulValueSet.ValueSet valueSet = new MulValueSet.ValueSet();
            final LocaleString localeString = valueSetMap.get(v);
            if (localeString == null) {
                // 为null,则跳过
                continue;
            }
            valueSet.setValue(v);
            valueSet.setName(localeString);
            mulValueSet.add(valueSet);
        }
        // 赋值
        ReflectionUtils.setValue(t, field, mulValueSet);
    }

    /**
     * 设置多选基础资料数据
     *
     * @param t             实例对象
     * @param field         字段
     * @param dynamicObject DynamicObject
     * @param <R>           泛型
     */
    private static <R> void setMutBaseDataFieldValue(R t, Field field, DynamicObject dynamicObject) throws IllegalAccessException {
        // 选基础资料数
        final Optional<String> formFieldNameOptional = ReflectionUtils.getFormFieldNameOptional(field);
        final String formFieldName = formFieldNameOptional.orElseThrow(() -> new OrmRuntimeException("值集名称映射字段不存在"));
        Object o = dynamicObject.get(formFieldName);
        if (o == null) {
            return;
        }
        final Class<?> c = ReflectionUtils.getFieldGenericType(field);
        // 取出关联的基础资料
        final DynamicObject[] dynamicObjects = ((DynamicObjectCollection) o).toArray(new DynamicObject[0]);

        // 返回一个List
        o = mapJavaObject(c, dynamicObjects);
        ReflectionUtils.setValue(t, field, o);

    }

    private static <R> void handleSpecialType(Object valueSetValue, IDataEntityProperty iDataEntityProperty, Field field, Field[] allField, R t) throws IllegalAccessException, InstantiationException {
        // 非多选,单选
        if (!(iDataEntityProperty instanceof MulComboProp) && iDataEntityProperty instanceof ComboProp && field.isAnnotationPresent(ValueSet.class)) {
            // 下拉框
            final Optional<Field> valueSetNameFieldOptional = ReflectionUtils.getValueSetNameField(field, allField);
            final Field valueSetNameField = valueSetNameFieldOptional.orElseThrow(() -> new OrmRuntimeException("值集名称映射字段不存在"));

            // 普通下拉框
            for (ValueMapItem comboItem : ((ComboProp) iDataEntityProperty).getComboItems()) {
                if (comboItem.getValue().equals(valueSetValue)) {
                    if (valueSetNameField.getType().equals(String.class)) {
                        // 字符串
                        ReflectionUtils.setValue(t, valueSetNameField, comboItem.getName().getLocaleValue());
                    } else {
                        // 非字符串
                        ReflectionUtils.setValue(t, valueSetNameField, comboItem.getName());

                    }

                }
            }

        }
    }

    /**
     * 设置字段值
     *
     * @param t             数据
     * @param field         字段
     * @param dynamicObject DynamicObject对象
     * @param <R>           泛型
     * @throws IllegalAccessException 非法访问异常
     */
    private static <R> void setFieldValue(R t, Field field, DynamicObject dynamicObject) throws IllegalAccessException {
        final Optional<String> fieldNameOptional = ReflectionUtils.getFormFieldNameOptional(field);
        if (fieldNameOptional.isPresent()) {
            // 表单字段名称
            final String formFieldName = fieldNameOptional.get();
            Object o = dynamicObject.get(formFieldName);
            if (o == null) {
                // 数据为空直接返回
                return;
            }
            if (o instanceof ILocaleString) {
                o = ((ILocaleString) o).getLocaleValue();

            }
            //
            // 保留原有逻辑，以适配旧代码
            // ------------------------------------------------------------------------------
            else if (o instanceof Long && field.getName().toLowerCase().endsWith("id") && field.getType().equals(String.class)) {
                // ID字段
                o = ConvertUtils.convert(o, String.class);
            }
            // ------------------------------------------------------------------------------
            else if (o instanceof DynamicObjectCollection && ReflectionUtils.isAnnotationPresent(field, Entry.class)) {
                // 单据体
                // 泛型的Class
                final Entry fieldAnnotation = field.getAnnotation(Entry.class);
                // 获取自定义的数据映射类型
                Class<?> c = fieldAnnotation.mapClass();
                final Class<?> fileGenericClassType = ReflectionUtils.getFieldGenericType(field);
                // 如果为Void.class则使用字段自身的,否则使用自定义的
                if (c.equals(Void.class)) {
                    // 用字段自身的
                    c = ReflectionUtils.getFieldGenericType(field);
                } else {
                    // 检查自定义的映射类型是否为字段映射类型的子类
                    if (!fileGenericClassType.isAssignableFrom(c)) {
                        throw new KDBizException(String.format("自定义数据映射类型%s不是%s的子类", c.getName(), fileGenericClassType.getName()));
                    }
                }
                final DynamicObject[] dynamicObjects = ((DynamicObjectCollection) o).toArray(new DynamicObject[0]);
                // 返回一个List
                o = mapJavaObject(c, dynamicObjects);
            }
            ReflectionUtils.setValue(t, field, o);
        }
    }


}
