package kd.alm.orm.core.impl;

import com.google.common.base.CaseFormat;
import kd.alm.orm.MulValueSet;
import kd.alm.orm.annotation.Entity;
import kd.alm.orm.annotation.Entry;
import kd.alm.orm.annotation.PrimaryKey;
import kd.alm.orm.annotation.ValueSet;
import kd.alm.orm.core.BaseRepository;
import kd.alm.orm.exception.OrmRuntimeException;
import kd.alm.orm.page.Page;
import kd.alm.orm.page.PageRequest;
import kd.alm.orm.util.AlmBusinessDataServiceHelper;
import kd.alm.orm.util.AlmSaveServiceHelper;
import kd.alm.orm.util.ReflectionUtils;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.entity.ILocaleString;
import kd.bos.dataentity.entity.LocaleString;
import kd.bos.dataentity.metadata.IDataEntityProperty;
import kd.bos.dataentity.metadata.IMetadata;
import kd.bos.dataentity.metadata.dynamicobject.DynamicObjectType;
import kd.bos.dataentity.metadata.dynamicobject.DynamicProperty;
import kd.bos.entity.ValueMapItem;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.entity.property.*;
import kd.bos.exception.KDBizException;
import kd.bos.id.ID;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.operation.DeleteServiceHelper;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * </p>
 * 基础资源层实现
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/04/20 11:44
 */
public class BaseRepositoryImpl<T> implements BaseRepository<T> {
    private static final Log log = LogFactory.getLog(BaseRepositoryImpl.class);
    private final Class<T> entityClass;
    private final Map<String, IDataEntityProperty> properties = new HashMap<>();

    {
        this.entityClass = getGenericType(this.getClass());
        ReflectionUtils.checkEntity(this.entityClass);
        final Entity annotationEntity = ReflectionUtils.getAnnotationEntity(this.entityClass);
        // 获取当前数据实体属性
        final DynamicObject dynamicObject = AlmBusinessDataServiceHelper.newDynamicObject(annotationEntity.value());
        final Map<String, IDataEntityProperty> collect = dynamicObject.getDynamicObjectType().getProperties().stream().collect(Collectors.toMap(IMetadata::getName, it -> it));
        this.properties.putAll(collect);
        // 单据体
        final Map<String, IDataEntityProperty> entryCollect = new HashMap<>();
        for (IDataEntityProperty property : this.properties.values()) {
            if (!(property instanceof LinkEntryProp) && property instanceof EntryProp) {
                final Map<String, IDataEntityProperty> map = dynamicObject.getDynamicObjectCollection(property.getName())
                        .getDynamicObjectType().getProperties().stream().collect(Collectors.toMap(IMetadata::getName, it -> it));
                entryCollect.putAll(map);
            }
        }
        this.properties.putAll(entryCollect);
    }


    /**
     * 获取泛型类型
     *
     * @param c 包含泛型的Class类型
     * @return 泛型Class
     */
    private Class<T> getGenericType(Class<?> c) {
        Type genType = c.getGenericSuperclass();
        if (!(genType instanceof ParameterizedType)) {
            return (Class<T>) Object.class;
        } else {
            Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
            if (0 < params.length) {
                if (!(params[0] instanceof Class)) {
                    return (Class<T>) Object.class;
                } else {
                    return (Class<T>) params[0];
                }
            } else {
                return (Class<T>) Object.class;
            }
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
                if ("0".equals(v)) {
                    // 创建ID
                    v = ID.genLongId();
                }
                dynamicObject.set("id", v);
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
                    final Optional<DynamicObject> vOptional = kd.alm.utils.AlmBusinessDataServiceHelper.loadSingleOptional(v, entityName);
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
                    dynamicObjectCollection.removeIf(next -> !keys.contains(next.getString("id")));
                    // 建立映射关系
                    final Map<String/*主键*/, DynamicObject/*数据*/> dynamicObjectMap = dynamicObjectCollection.stream()
                            .collect(Collectors.toMap(it -> it.getString("id"), it -> it));
                    for (Object o : entryList) {
                        String id = (String) ReflectionUtils.getValue(o, primaryKeyField);
                        // 存在则直接使用,不存在则新增
                        DynamicObject entryDo = dynamicObjectMap.computeIfAbsent(id, x -> dynamicObjectCollection.addNew());
                        // 映射对象
                        mapDynamicObjectValue(o, entryAllField, entryDo);
                    }

                }
            }
        }

    }

    /**
     * 把{@link DynamicObject}生成对象{@link R}
     *
     * @param c              对象{@link R}的Class
     * @param allField       对象{@link R}的成员变量
     * @param dynamicObjects DynamicObject数据集
     * @param <R>            泛型
     * @return 对象{@link T}
     * @throws IllegalAccessException
     * @throws InstantiationException
     */

    public <R> List<R> mapObject(Class<R> c, Field[] allField, DynamicObject... dynamicObjects) {
        final ArrayList<R> result = new ArrayList<>();
        for (DynamicObject dynamicObject : dynamicObjects) {
            try {
                final R t = this.mapObject(c, allField, dynamicObject);
                result.add(t);
            } catch (IllegalAccessException | InstantiationException e) {
                log.error("mapObject error", e);
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 把{@link DynamicObject}生成对象{@link R}
     *
     * @param c             对象{@link R}的Class
     * @param allField      对象{@link R}的成员变量
     * @param dynamicObject DynamicObject数据
     * @param <R>           泛型
     * @return 对象{@link T}
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public <R> R mapObject(Class<R> c, Field[] allField, DynamicObject dynamicObject) throws IllegalAccessException, InstantiationException {
        final R t = c.newInstance();
        for (Field field : allField) {
            // 获取当前字段在苍穹中的字段属性
            final Optional<String> fieldNameOptional = ReflectionUtils.getFormFieldNameOptional(field);
            if (fieldNameOptional.isPresent()) {
                final String formFieldName = fieldNameOptional.get().split("\\.")[0];
                // 获得字段属性
                final IDataEntityProperty iDataEntityProperty = properties.get(formFieldName);
                if (iDataEntityProperty == null) {
                    throw new OrmRuntimeException("苍穹中不存在此字段");
                }
                if (iDataEntityProperty instanceof MulComboProp && field.getType().equals(MulValueSet.class)) {
                    // 设置多值集
                    this.setMutValueSetFieldValue(t, field, dynamicObject);
                } else if (iDataEntityProperty instanceof MulBasedataProp) {
                    // 多选基础资料
                    this.setMutBaseDataFieldValue(t, field, dynamicObject);

                } else {
                    // 设置当前字段的值
                    this.setFieldValue(t, field, dynamicObject);
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

    }

    /**
     * 设置多选基础资料数据
     *
     * @param t             实例对象
     * @param field         字段
     * @param dynamicObject DynamicObject
     * @param <R>           泛型
     */
    private <R> void setMutBaseDataFieldValue(R t, Field field, DynamicObject dynamicObject) throws IllegalAccessException {
        // 选基础资料数
        final Optional<String> formFieldNameOptional = ReflectionUtils.getFormFieldNameOptional(field);
        final String formFieldName = formFieldNameOptional.orElseThrow(() -> new OrmRuntimeException("值集名称映射字段不存在"));
        Object o = dynamicObject.get(formFieldName);
        if (o == null) {
            return;
        }
        final Class<?> c = ReflectionUtils.getFieldGenericType(field);

        final Field[] allField = ReflectionUtils.getAllField(c);
        // 取出关联的基础资料
        final DynamicObject[] dynamicObjects = ((DynamicObjectCollection) o).stream().map(it -> it.getDynamicObject("fbasedataid")).toArray(DynamicObject[]::new);

        // 返回一个List
        o = mapObject(c, allField, dynamicObjects);
        ReflectionUtils.setValue(t, field, o);

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
    private <R> void setMutValueSetFieldValue(R t, Field field, DynamicObject dynamicObject) throws IllegalAccessException {
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

    private <R> void handleSpecialType(Object valueSetValue, IDataEntityProperty iDataEntityProperty, Field field, Field[] allField, R t) throws IllegalAccessException, InstantiationException {
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
    public <R> void setFieldValue(R t, Field field, DynamicObject dynamicObject) throws IllegalAccessException {
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
            } else if (o instanceof Long && field.getName().toLowerCase().endsWith("id")) {
                // ID字段
                o = ConvertUtils.convert(o, String.class);
            } else if (o instanceof DynamicObjectCollection && ReflectionUtils.isAnnotationPresent(field, Entry.class)) {
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
                final Field[] allField = ReflectionUtils.getAllField(c);
                final DynamicObject[] dynamicObjects = ((DynamicObjectCollection) o).toArray(new DynamicObject[0]);
                // 返回一个List
                o = mapObject(c, allField, dynamicObjects);
            }
            ReflectionUtils.setValue(t, field, o);
        }
    }

    @Override
    public <R> List<QFilter> buildQFilter(R record) {
        final Class<R> c = (Class<R>) record.getClass();
        final Field[] allField = ReflectionUtils.getAllField(c);
        final ArrayList<QFilter> qFilters = new ArrayList<>();
        for (Field field : allField) {
            ReflectionUtils.makeAccessible(field);
            try {
                final Object o = field.get(record);
                if (o != null) {
                    // 非单据体字段才能生成
                    if (!ReflectionUtils.isAnnotationPresent(field, Entry.class)) {
                        // 获取表单标识
                        final Optional<String> formFiledNameOptional = ReflectionUtils.getFormFieldNameOptional(field);

                        if (formFiledNameOptional.isPresent()) {
                            final String formFiledName = formFiledNameOptional.get();
                            if (o instanceof Iterable) {
                                // 可迭代的数据
                                final ArrayList<Object> ids = new ArrayList<>();
                                ((Iterable<?>) o).forEach(ids::add);
                                qFilters.add(new QFilter(formFiledName, QCP.in, ids));
                            } else {
                                qFilters.add(new QFilter(formFiledName, QCP.equals, o));
                                if (ReflectionUtils.isAnnotationPresent(field, PrimaryKey.class)) {
                                    // 主键确定时,直接跳出
                                    break;
                                }
                            }
                        }
                    }

                }

            } catch (IllegalAccessException e) {
                log.error("generate select error", e);
            }
        }
        return qFilters;
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
     * 生成查询的Select字段
     *
     * @param allField 字段
     * @return
     */
    public static List<String> getSelectFields(Field[] allField) {
        final List<String> selectFields = new ArrayList<>();

        for (Field field : allField) {
            if (ReflectionUtils.isAnnotationPresent(field, PrimaryKey.class)) {
                selectFields.add("id");
            } else if (ReflectionUtils.isAnnotationPresent(field, kd.alm.orm.annotation.Field.class)) {
                String formFiledName = field.getAnnotation(kd.alm.orm.annotation.Field.class).value();
                if (StringUtils.isBlank(formFiledName)) {
                    formFiledName = "digi_" + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());
                }
                selectFields.add(formFiledName);
            } else if (Iterable.class.isAssignableFrom(field.getType()) && ReflectionUtils.isAnnotationPresent(field, Entry.class)) {

                final String formFiledName = field.getAnnotation(Entry.class).value();
                final Class<?> c = ReflectionUtils.getFieldGenericType(field);
                final Field[] innerAllField = ReflectionUtils.getAllField(c);
                // 获取单据体内字段
                final List<String> innerSelectFields = getSelectFields(innerAllField);
                // 添加上单据体标识
                selectFields.addAll(innerSelectFields.stream().map(it -> it.split("\\.")[0]).map(it -> formFiledName + "." + it).collect(Collectors.toList()));
            }
        }
        return selectFields;

    }

    @Override
    public void save(T record) {
        this.save(Collections.singletonList(record));
    }

    @Override
    public OperationResult saveOperate(List<T> list, OperateOption option) {
        assert list.size() != 0;
        final DynamicObject[] dynamicObjects = this.mapDynamicObject(list);
        final Entity entity = ReflectionUtils.getAnnotationEntity(this.entityClass);
        String entityName = entity.value();
        return AlmSaveServiceHelper.saveOperate(entityName, dynamicObjects, OperateOption.create());
    }

    @Override
    public void save(List<T> list) {
        final DynamicObject[] dynamicObjects = mapDynamicObject(list);
        AlmSaveServiceHelper.save(dynamicObjects);
    }

    /**
     * 将数据映射为DynamicObject[]
     *
     * @param list 数据
     * @return DynamicObject[]
     */
    private DynamicObject[] mapDynamicObject(List<T> list) {
        final int size = list.size();
        assert size != 0;
        final Entity entity = ReflectionUtils.getAnnotationEntity(this.entityClass);
        String entityName = entity.value();
        final DynamicObject[] dynamicObjects = new DynamicObject[size];

        for (int i = 0; i < list.size(); i++) {
            final T t = list.get(i);
            // 主键字段
            final Optional<Field> primaryKeyFieldOptional = ReflectionUtils.getPrimaryKeyFieldOptional(this.entityClass);

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
            final Field[] allField = ReflectionUtils.getAllField(this.entityClass);
            try {
                // 向dynamicObject设置数据
                mapDynamicObjectValue(t, allField, dynamicObject);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            dynamicObjects[i] = dynamicObject;
        }
        return dynamicObjects;
    }

    @Override
    public OperationResult saveOperate(T t, OperateOption option) {
        return this.saveOperate(Collections.singletonList(t), option);
    }

    @Override
    public List<T> select(String key, Object value) {
        return this.select(key, value, this.entityClass);

    }

    @Override
    public <R> List<R> select(String key, Object value, Class<R> c) {
        return this.select(Collections.singletonList(new QFilter(key, QCP.equals, value)), c);
    }

    @Override
    public int delete(T record) {
        final Class<T> c = (Class<T>) record.getClass();
        ReflectionUtils.checkEntity(c);
        final List<QFilter> qFilters = buildQFilter(record);
        final String entityName = ReflectionUtils.getAnnotationEntity(c).value();
        return DeleteServiceHelper.delete(entityName, qFilters.toArray(new QFilter[0]));
    }

    @Override
    public int selectCount(T record) {
        final Class<T> c = (Class<T>) record.getClass();
        return this.selectCount(buildQFilter(record), c);
    }

    @Override
    public <R> int selectCount(List<QFilter> qFilters, Class<R> queryClass) {
        ReflectionUtils.checkEntity(queryClass);
        final String entityName = ReflectionUtils.getAnnotationEntity(queryClass).value();
        final DynamicObject[] dynamicObjects = AlmBusinessDataServiceHelper.load(entityName, "id", qFilters.toArray(new QFilter[0]));
        return dynamicObjects != null ? dynamicObjects.length : 0;
    }

    @Override
    public Optional<T> selectOne(T record) {
        final Class<T> c = (Class<T>) record.getClass();
        final List<QFilter> qFilters = buildQFilter(record);
        final String entityName = ReflectionUtils.getAnnotationEntity(c).value();
        final Optional<DynamicObject> dynamicObjectOptional = AlmBusinessDataServiceHelper.loadSingleOptional(entityName, qFilters.toArray(new QFilter[0]));
        if (dynamicObjectOptional.isPresent()) {
            final Field[] allField = ReflectionUtils.getAllField(c);
            try {
                return Optional.of(mapObject(c, allField, dynamicObjectOptional.get()));
            } catch (IllegalAccessException | InstantiationException e) {
                log.error("mapObject error", e);
                e.printStackTrace();
            }
        }
        return Optional.empty();
    }

    @Override
    public List<T> select(T record) {
        final Class<T> c = (Class<T>) record.getClass();
        final List<QFilter> qFilters = this.buildQFilter(record);
        return this.select(qFilters, c);
    }

    @Override
    public List<T> selectAll() {
        return this.selectAll(this.entityClass);
    }

    @Override
    public <R> List<R> selectAll(Class<R> c) {
        return this.select(new ArrayList<>(), c);
    }

//    /**
//     * 根据实体成员变量名生成实体对象
//     *
//     * @param fieldName  成名变量名称
//     * @param fieldValue 成员变量值
//     * @param c          实体类类型
//     * @return 实体对象
//     */
//    private <R> R injectEntityValue(String fieldName, Object fieldValue, Class<R> c) {
//        try {
//            R entity = c.newInstance();
//            Field field = getField(c, fieldName);
//            assert field != null;
//            field.set(entity, fieldValue);
//            return entity;
//        } catch (IllegalAccessException | InstantiationException e) {
//            log.error(e.getMessage());
//            throw new KDBizException(e.getMessage());
//        }
//    }

    @Override
    public <R> List<R> select(List<QFilter> qFilters, Class<R> resultClass) {
        ReflectionUtils.checkEntity(resultClass);
        final Field[] allField = ReflectionUtils.getAllField(resultClass);
        final List<String> selectFields = getSelectFields(allField);

        final String entityName = ReflectionUtils.getAnnotationEntity(resultClass).value();
        final DynamicObject[] dynamicObjects = AlmBusinessDataServiceHelper.load(entityName, String.join(",", selectFields), qFilters.toArray(new QFilter[0]));

        return mapObject(resultClass, allField, dynamicObjects);
    }

    @Override
    public void update(List<T> list) {
        // 判断数据中是否存在无主键的数据
        final boolean isExistNotIdData = list.stream().anyMatch(t -> {
            final Optional<Field> primaryKeyFieldOptional = ReflectionUtils.getPrimaryKeyFieldOptional(t.getClass());
            if (primaryKeyFieldOptional.isPresent()) {
                final Field field = primaryKeyFieldOptional.get();
                try {
                    final Object id = field.get(t);
                    if (id == null || "0".equals(id)) {
                        return true;
                    }
                } catch (IllegalAccessException ignored) {
                }
            }
            return false;
        });
        if (isExistNotIdData) {
            throw new KDBizException("存在无主键的数据");
        }
        final DynamicObject[] dynamicObjects = this.mapDynamicObject(list);
        AlmSaveServiceHelper.update(dynamicObjects);
    }

    @Override
    public void update(T record) {
        this.update(Collections.singletonList(record));
    }

    @Override
    public Page<T> selectPage(T record, PageRequest pageRequest) {
        final Class<T> c = (Class<T>) record.getClass();

        final List<QFilter> qFilters = this.buildQFilter(record);

        return this.selectPage(qFilters, pageRequest, c);
    }

    @Override
    public <R> Page<R> selectPage(List<QFilter> qFilters, PageRequest pageRequest, Class<R> resultClass) {

        final int selectCount = this.selectCount(qFilters, resultClass);

        final Page<R> page = Page.buildPage(selectCount, pageRequest);

        final Field[] allField = ReflectionUtils.getAllField(resultClass);
        final List<String> selectFields = getSelectFields(allField);
        final String entityName = ReflectionUtils.getAnnotationEntity(resultClass).value();

        final DynamicObject[] dynamicObjects = AlmBusinessDataServiceHelper.load(entityName, String.join(",", selectFields), qFilters.toArray(new QFilter[0]), pageRequest.toOrderBys(), pageRequest.getPage(), pageRequest.getSize());

        final List<R> list = mapObject(resultClass, allField, dynamicObjects);
        page.getData().addAll(list);
        return null;
    }
}
