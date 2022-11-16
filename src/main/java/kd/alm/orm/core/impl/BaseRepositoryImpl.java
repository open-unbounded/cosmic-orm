package kd.alm.orm.core.impl;

import com.google.common.base.CaseFormat;
import kd.alm.orm.annotation.Entity;
import kd.alm.orm.annotation.Entry;
import kd.alm.orm.annotation.PrimaryKey;
import kd.alm.orm.core.BaseRepository;
import kd.alm.orm.page.Page;
import kd.alm.orm.page.PageRequest;
import kd.alm.orm.util.*;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.exception.KDBizException;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

    {
        this.entityClass = getGenericType(this.getClass());
        ReflectionUtils.checkEntity(this.entityClass);
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
                                if (ReflectionUtils.isAnnotationPresent(field, PrimaryKey.class)) {
                                    qFilters.add(new QFilter(formFiledName, QCP.equals, ConvertUtils.convert(o, Long.class)));
                                    // 主键确定时,直接跳出
                                    break;
                                } else {
                                    qFilters.add(new QFilter(formFiledName, QCP.equals, o));
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
        final DynamicObject[] dynamicObjects = AlmDynamicUtils.mapDynamicObject(list);
        final Entity entity = ReflectionUtils.getAnnotationEntity(this.entityClass);
        String entityName = entity.value();
        return AlmOperationServiceUtils.saveOperate(entityName, dynamicObjects, option == null ? OperateOption.create() : option);
    }

    @Override
    public void save(List<T> list) {
        final DynamicObject[] dynamicObjects = AlmDynamicUtils.mapDynamicObject(list);
        AlmSaveServiceHelper.save(dynamicObjects);
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
        return dynamicObjectOptional.map(dynamicObject -> AlmDynamicUtils.mapJavaObject(c, dynamicObject));
    }

    @Override
    public Optional<T> selectOneByPrimaryKey(Object primaryKey) {
        return this.selectOneByPrimaryKey(primaryKey, this.entityClass);
    }

    @Override
    public <R> Optional<R> selectOneByPrimaryKey(Object primaryKey, Class<R> resultClass) {
        ReflectionUtils.checkEntity(resultClass);
        final String entityName = ReflectionUtils.getAnnotationEntity(resultClass).value();
        final Optional<DynamicObject> dynamicObjectOptional = AlmBusinessDataServiceHelper.loadSingleOptional(primaryKey, entityName);
        return dynamicObjectOptional.map(dynamicObject -> AlmDynamicUtils.mapJavaObject(resultClass, dynamicObject));
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

    @Override
    public <R> List<R> select(List<QFilter> qFilters, Class<R> resultClass) {
        return this.select(qFilters, resultClass, null);
    }

    @Override
    public <R> List<R> select(List<QFilter> qFilters, Class<R> resultClass, String order) {
        return this.select(qFilters, resultClass, order, -1);
    }

    @Override
    public <R> List<R> select(List<QFilter> qFilters, Class<R> resultClass, String order, int top) {
        ReflectionUtils.checkEntity(resultClass);
        final Field[] allField = ReflectionUtils.getAllField(resultClass);
        final List<String> selectFields = getSelectFields(allField);

        final String entityName = ReflectionUtils.getAnnotationEntity(resultClass).value();
        final DynamicObject[] dynamicObjects = AlmBusinessDataServiceHelper.load(entityName, String.join(",", selectFields), qFilters.toArray(new QFilter[0]), order, top);

        return AlmDynamicUtils.mapJavaObject(resultClass, allField, dynamicObjects);
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
        final DynamicObject[] dynamicObjects = AlmDynamicUtils.mapDynamicObject(list);
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

        final List<R> list = AlmDynamicUtils.mapJavaObject(resultClass, allField, dynamicObjects);
        page.setData(list);
        return page;
    }
}
