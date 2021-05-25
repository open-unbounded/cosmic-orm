package kd.alm.orm.util;

import com.google.common.collect.Sets;
import kd.alm.utils.AlmBusinessDataServiceHelper;
import kd.alm.utils.Page;
import kd.alm.utils.PageRequest;
import kd.bos.algo.DataSet;
import kd.bos.algo.DataType;
import kd.bos.algo.Row;
import kd.bos.algo.RowMeta;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.metadata.IDataEntityProperty;
import kd.bos.dataentity.metadata.IDataEntityType;
import kd.bos.dataentity.metadata.clr.DataEntityPropertyCollection;
import kd.bos.dataentity.metadata.dynamicobject.DynamicObjectType;
import kd.bos.db.DB;
import kd.bos.db.DBRoute;
import kd.bos.db.SqlBuilder;
import kd.bos.entity.property.BasedataProp;
import kd.bos.entity.property.ItemClassProp;
import kd.bos.exception.KDBizException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.SetUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * </p>
 * 数据查询工具
 * <p>
 *
 * @author chenquan chenquan@osai.club 2020/08/08 12:10
 */
public class AlmDB {

    private final static Log log = LogFactory.getLog(AlmDB.class);

    /**
     * 数据库查询工具
     *
     * @param dbRoute 数据库路由
     * @param sql     SQL
     * @param calzz   数据库映射的DTO类
     * @param <T>
     * @return 查询结果
     */
    public static <T> List<T> query(DBRoute dbRoute, String sql, Class<T> calzz) {
        return DB.query(dbRoute, sql, resultSet -> query(resultSet, calzz));
    }

    /**
     * 数据库查询工具
     *
     * @param resultSet ResultSet结果集
     * @param calzz     数据库映射的DTO类
     * @param <T>
     * @return 查询结果
     */
    public static <T> List<T> query(ResultSet resultSet, Class<T> calzz) {
        ArrayList<T> rs = new ArrayList<>();
        try {
            final ResultSetMetaData metaData = resultSet.getMetaData();
            final int columnCount = metaData.getColumnCount();
            // 列名:Java类型
            HashMap<String, Class<?>> typeMap = new HashMap<>(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                String columnClassName = metaData.getColumnClassName(i);
                typeMap.put(metaData.getColumnLabel(i), Class.forName(columnClassName));
            }
            // 类成员名称:set方法
            Map<String, Method> methodHashMap = getMethod(calzz);
            while (resultSet.next()) {
                T t = calzz.newInstance();
                typeMap.forEach((k, v) -> {
                    try {
                        String field = k.toLowerCase();
                        Object object = resultSet.getObject(field);
                        System.out.println(object);
                        String name = formatFieldName(field);
                        Method method = methodHashMap.get(name);
                        if (object instanceof Long && field.endsWith("id")) {
                            object = ConvertUtils.convert(object, String.class);
                        }
                        if (method != null) {
                            method.invoke(t, object);
                        }
                    } catch (SQLException | IllegalAccessException | InvocationTargetException throwables) {
                        throwables.printStackTrace();
                    }

                });
                rs.add(t);
            }
            return rs;
        } catch (InstantiationException | SQLException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return rs;
    }

    /**
     * 数据库查询工具
     *
     * @param dataSet 数据集
     * @param calzz   数据库映射的DTO类
     * @param <T>
     * @return 查询结果
     */
    public static <T> List<T> query(DataSet dataSet, Class<T> calzz) {
        return query(dataSet, (ignore, t) -> t, calzz);
    }

    /**
     * 数据库查询工具
     *
     * @param dataSet 数据集
     * @param calzz   数据库映射的DTO类
     * @param <T>
     * @return 查询结果
     */
    public static <T> List<T> query(DataSet dataSet, BiFunction<Row, T, T> rowHandle, Class<T> calzz) {
        ArrayList<T> rs = new ArrayList<>();
        if (dataSet == null || dataSet.isEmpty()) {
            return rs;
        }
        final RowMeta rowMeta = dataSet.getRowMeta();
        final int fieldCount = rowMeta.getFieldCount();
        // 列名:Java类型
        HashMap<String, Class<?>> typeMap = new HashMap<>(fieldCount);
        for (int i = 0; i < fieldCount; i++) {
            String fieldName = rowMeta.getFieldName(i);
            DataType dataType = rowMeta.getDataType(i);
            Class<?> javaType = dataType.getJavaType();
            typeMap.put(fieldName, javaType);
        }

        // 类成员名称:set方法
        Map<String, Method> methodHashMap = getMethod(calzz);

        try {
            while (dataSet.hasNext()) {
                Row data = dataSet.next();

                T t = calzz.newInstance();
                typeMap.forEach((k, v) -> {
                    try {
                        String field = k.toLowerCase();
                        Object object = data.get(field);
                        String name = formatFieldName(field);
                        Method method = methodHashMap.get(name);
                        if (object instanceof Long && field.endsWith("id")) {
                            object = ConvertUtils.convert(object, String.class);
                        }
                        if (method != null) {

                            if (object instanceof Integer) {
                                object = ConvertUtils.convert(object, Long.class);
                            }
                            method.invoke(t, object);
                        }
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        throw new KDBizException(String.format("字段类型匹配错误!字段:%s  期待类型:%s", k, v));
                    }

                });
                // 处理每一个数据
                final T apply = rowHandle.apply(data, t);
                rs.add(apply);

            }
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return rs;
    }

    public static <T> T queryOne(DataSet dataSet, Class<T> calzz) {
        List<T> result = query(dataSet, calzz);
        if (CollectionUtils.isNotEmpty(result)) {
            return result.get(0);
        }
        return null;

    }

    /**
     * @param dbRoute
     * @param sqlBuilder
     * @param calzz
     * @param <T>
     * @return
     */
    public static <T> List<T> query(DBRoute dbRoute, SqlBuilder sqlBuilder, Class<T> calzz) {
        return DB.query(dbRoute, sqlBuilder, resultSet -> query(resultSet, calzz));
    }

    private static String formatMethodName(String name) {
        if (name.startsWith("set") && name.length() > 4) {
            return name.substring(3, 4).toLowerCase() + name.substring(4);
        } else {
            return name;
        }
    }

    /**
     * 格式化字段名
     *
     * @param name 字段名
     * @return 格式化后的字段名
     */
    private static String formatFieldName(String name) {
        String[] strings = name.split("_");
        StringBuilder stringBuffer = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            if (i == 0) {
                stringBuffer.append(strings[i]);
            } else {
                stringBuffer
                        .append(strings[i].substring(0, 1).toUpperCase())
                        .append(strings[i].substring(1));
            }
        }
        return stringBuffer.toString();
    }

    /**
     * 获取当前类下所有set方法
     *
     * @param calzz 类
     * @param <T>
     * @return 所有set方法
     */
    private static <T> Map<String, Method> getMethod(Class<T> calzz) {
        // 类成员名称:set方法
        Method[] methods = calzz.getMethods();
        HashMap<String, Method> methodHashMap = new HashMap<>(methods.length);
        for (Method method : methods) {
            String name = method.getName();
            if (name.startsWith("set") && name.length() > 4) {
                // setName -> name
                String key = name.substring(3, 4).toLowerCase() + name.substring(4);
                // name -> setName
                methodHashMap.put(key, method);
            }
        }
        return methodHashMap;
    }


    /**
     * 根据ID分页
     *
     * @param pkList      主键
     * @param function    根据输入的已分页ID查询数据
     * @param pageRequest 分页
     * @param <T>
     * @return 分页数据
     */
    public static <T> Page<T> doPageByIds(List<Long> pkList, Function<List<Long>, Collection<T>> function, PageRequest pageRequest) {
        // 计算查询的个数
        int total = pkList.size();
        Page<T> tPage = new Page<>();
        // 计算总页数
        double totalPage = Math.ceil((double) total / pageRequest.getSize());

        tPage.setPage(pageRequest.getPage());
        tPage.setSize(pageRequest.getSize());
        tPage.setTotalPage((int) totalPage);
        tPage.setTotalSize(total);

        int nextPage;
        int pervPage;
        Map<Object, DynamicObject> resultMap = new HashMap<>(16);
        if (pageRequest.getPage() > totalPage) {
            nextPage = -1;
            pervPage = -1;

        } else {
            int fromIndex = pageRequest.getPage() * pageRequest.getSize();
            int toIndex = fromIndex + pageRequest.getSize();
            if (toIndex > total) {
                toIndex = total;
            }

            // 待查询的主键列表
            List<Long> pageIdList = pkList.subList(fromIndex, toIndex);
            // 获取分页结果
            Collection<T> result = function.apply(pageIdList);
            tPage.setData(result);
            // 计算分页前/后页码
            nextPage = pageRequest.getPage() - 1;
            pervPage = pageRequest.getPage() + 1;
            if (pervPage == totalPage) {
                pervPage = -1;
            }
        }
        tPage.setNextPage(nextPage);
        tPage.setPervPage(pervPage);
        return tPage;
    }

    /**
     * 最大可能的合并两个DataSet
     *
     * @param left  DataSet left
     * @param right DataSet right
     * @return DataSet
     */
    public static DataSet mustUnion(DataSet left, DataSet right) {
        HashSet<String> aSet = Sets.newHashSet(left.getRowMeta().getFieldNames());
        HashSet<String> bSet = Sets.newHashSet(right.getRowMeta().getFieldNames());
        Set<String> allSet = SetUtils.union(aSet, bSet).toSet();
        Set<String> aMiss = SetUtils.difference(allSet, aSet).toSet();
        Set<String> bMiss = SetUtils.difference(allSet, bSet).toSet();
        left = left.addNullField(aMiss.toArray(new String[0]));
        right = right.addNullField(bMiss.toArray(new String[0]));
        right = right.select(left.getRowMeta().getFieldNames());
        return left.union(right);
    }

    /**
     * DataSet 连接处理
     *
     * @param left   DataSet
     * @param right  DataSet left
     * @param handle DataSet right
     * @return
     */
    public static DataSet join(DataSet left, DataSet right, DataSetHandleFunction handle) {
        HashSet<String> aSet = Sets.newHashSet(left.getRowMeta().getFieldNames());
        HashSet<String> bSet = Sets.newHashSet(right.getRowMeta().getFieldNames());
        Set<String> cSet = SetUtils.intersection(aSet, bSet).toSet();
        aSet.removeAll(cSet);
        Set<String> bSetIntersection = cSet.stream().map(it -> it + " AS b_" + it).collect(Collectors.toSet());
        aSet.addAll(bSetIntersection);
        left = left.select(aSet.toArray(new String[0]));
        right = right.select(bSet.toArray(new String[0]));

        return handle.handle(left, right);
    }

    /**
     * 内连接
     *
     * @param left             DataSet
     * @param right            DataSet
     * @param leftOnFieldName  left DataSet的连接字段
     * @param rightOnFieldName right DataSet的连接字段
     * @return DataSet
     */
    public static DataSet join(DataSet left, DataSet right, String leftOnFieldName, String rightOnFieldName) {
        return join(left, right, (a, b) -> a.join(b).on(leftOnFieldName, rightOnFieldName).select(a.getRowMeta().getFieldNames(), b.getRowMeta().getFieldNames()).finish());
    }

    /**
     * 左连接
     *
     * @param left             DataSet
     * @param right            DataSet
     * @param leftOnFieldName  left DataSet的连接字段
     * @param rightOnFieldName right DataSet的连接字段
     * @return DataSet
     */
    public static DataSet leftJoin(DataSet left, DataSet right, String leftOnFieldName, String rightOnFieldName) {
        return join(left, right, (a, b) -> a.leftJoin(b).on(leftOnFieldName, rightOnFieldName).select(a.getRowMeta().getFieldNames(), b.getRowMeta().getFieldNames()).finish());

    }

    /**
     * 右连接
     *
     * @param left             DataSet
     * @param right            DataSet
     * @param leftOnFieldName  left DataSet的连接字段
     * @param rightOnFieldName right DataSet的连接字段
     * @return DataSet
     */
    public static DataSet rightJoin(DataSet left, DataSet right, String leftOnFieldName, String rightOnFieldName) {
        return join(left, right, (a, b) -> a.rightJoin(b).on(leftOnFieldName, rightOnFieldName).select(a.getRowMeta().getFieldNames(), b.getRowMeta().getFieldNames()).finish());
    }

    /**
     * 生成单据体select field 语句
     *
     * @param entryFiled   单据体标识
     * @param selectFields 待查询的单据体内字段
     * @return 语句
     */
    public static String genEntrySelectFields(String entryFiled, String... selectFields) {
        return Arrays.stream(selectFields).map(it -> String.format("%s.%s", entryFiled, it)).collect(Collectors.joining(", "));
    }

    /**
     * 将ID转为对应的 DynamicObject类型
     *
     * @param id         主键
     * @param entityName 表单标识
     * @return DynamicObject
     */
    public static DynamicObject idToDynamicObject(Object id, String entityName) {
        if (id == null) {
            return null;
        }
        try {
            return AlmBusinessDataServiceHelper.loadSingle(id, entityName);
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * 获取当前类下所有get方法
     *
     * @param calzz 类
     * @param <T>   泛型
     * @return 所有get方法
     */
    public static <T> Map<String, Method> getAllGetMethod(Class<T> calzz) {
        // 类成员名称:set方法
        Method[] methods = calzz.getMethods();
        HashMap<String, Method> methodHashMap = new HashMap<>(methods.length);
        for (Method method : methods) {
            String name = method.getName();
            if (name.startsWith("get") && name.length() > 4) {
                // 分解getName -> name
                String key = name.substring(3, 4).toLowerCase() + name.substring(4);
                // name -> setName
                methodHashMap.put(key, method);
            }
        }
        return methodHashMap;
    }

    /**
     * 对象自动映射为动态对象
     *
     * @param obj          对象
     * @param relationMap  映射关系
     * @param entityName   表单标识
     * @param allowNullMap 允许字段null映射
     * @param <T>          担心
     * @return 动态对象
     */
    public static <T> DynamicObject objectAutoMapDynamicObject(final T obj, final Map<String/*实体字段名*/, String/*表单字段标识名*/> relationMap, final String entityName, boolean allowNullMap) {
        // 类成员名称:set方法
        final DynamicObject dynamicObject = AlmBusinessDataServiceHelper.newDynamicObject(entityName);
        return objectAutoMapDynamicObject(obj, relationMap, dynamicObject, allowNullMap, false);
    }

    /**
     * 对象自动映射为动态对象(允许字段数据null更新)
     *
     * @param obj         对象
     * @param relationMap 映射关系
     * @param entityName  表单标识
     * @param <T>         担心
     * @return 动态对象
     */
    public static <T> DynamicObject objectAutoMapDynamicObject(final T obj, final Map<String/*实体字段名*/, String/*表单字段标识名*/> relationMap, final String entityName) {
        return objectAutoMapDynamicObject(obj, relationMap, entityName, true);
    }

    /**
     * 对象自动映射为动态对象
     *
     * @param obj               对象
     * @param relationMap       映射关系
     * @param entityDynamicType 动态对象类型
     * @param allowNullMap      允许字段null映射
     * @param <T>               担心
     * @return 动态对象
     */
    public static <T> DynamicObject objectAutoMapDynamicObject(final T obj, final Map<String/*实体字段名*/, String/*表单字段标识名*/> relationMap, final DynamicObjectType entityDynamicType, boolean allowNullMap) {
        final DynamicObject dynamicObject = new DynamicObject(entityDynamicType);
        return objectAutoMapDynamicObject(obj, relationMap, dynamicObject, allowNullMap, false);
    }

    /**
     * 对象自动映射为动态对象
     *
     * @param obj               对象
     * @param relationMap       映射关系
     * @param entityDynamicType 动态对象类型
     * @param <T>               担心
     * @return 动态对象
     */
    public static <T> DynamicObject objectAutoMapDynamicObject(final T obj, final Map<String/*实体字段名*/, String/*表单字段标识名*/> relationMap, final DynamicObjectType entityDynamicType) {
        final DynamicObject dynamicObject = new DynamicObject(entityDynamicType);
        return objectAutoMapDynamicObject(obj, relationMap, dynamicObject, true, false);
    }

    /**
     * 对象自动映射为动态对象
     *
     * @param obj           对象
     * @param relationMap   映射关系
     * @param dynamicObject 动态对象
     * @param <T>           担心
     * @return 动态对象
     */
    public static <T> DynamicObject objectAutoMapDynamicObject(final T obj, final Map<String/*实体字段名*/, String/*表单字段标识名*/> relationMap, DynamicObject dynamicObject) {
        return objectAutoMapDynamicObject(obj, relationMap, dynamicObject, true, false);
    }

    /**
     * 对象自动映射为动态对象
     *
     * @param obj           对象
     * @param relationMap   映射关系
     * @param dynamicObject 动态对象
     * @param allowNullMap  允许字段null映射
     * @param <T>           担心
     * @return 动态对象
     */
    public static <T> DynamicObject objectAutoMapDynamicObject(final T obj, final Map<String/*实体字段名*/, String/*表单字段标识名*/> relationMap, DynamicObject dynamicObject, boolean allowNullMap, boolean isIgnoreMustInput) {
        // 类成员名称:get方法
        Map<String, Method> methodHashMap = getAllGetMethod(obj.getClass());

        final IDataEntityType dataEntityType = dynamicObject.getDataEntityType();
        final DataEntityPropertyCollection properties = dataEntityType.getProperties();
//        String fieldName;
//        String markFieldName;
//        Method method;
//        for (IDataEntityProperty property : properties) {
//
//            markFieldName = property.getName();
//            fieldName = formatFieldName(markFieldName);
//            // 优先从关系表
//            method = methodHashMap.get(relationMap.get(fieldName));
//            //
//            if (method == null) {
//                method = methodHashMap.get(fieldName);
//            }
//            if (method == null && markFieldName.startsWith("digi_")) {
//                fieldName = markFieldName.substring(5);
//                method = methodHashMap.get(fieldName);
//            }
//            if (method == null) {
//                // 跳过这个字段
//                continue;
//            }
//            Object value;
//            try {
//                value = method.invoke(obj);
////                final IDataEntityProperty entityProperty = properties.get(markFieldName);
//                if (value != null && property instanceof BasedataProp) {
//                    // 如果是基础资料则转换
//                    try {
//                        // 获取对应的DynamicObject
//                        value = AlmBusinessDataServiceHelper.loadSingle(value, ((BasedataProp) property).getBaseEntityId());
//                    } catch (Exception e) {
//                        // 数据不存在
//                        throw new KDBizException(String.format("[%s]字段对应的基础资料数据不存在", fieldName));
//                    }
//                }
//                if (allowNullMap) {
//                    // 允许null映射
//                    dynamicObject.set(markFieldName, value);
//                } else {
//                    // 不允许null映射
//                    if (value != null) {
//                        // 不为空才更新
//                        dynamicObject.set(markFieldName, value);
//                    }
//                }
//            } catch (IllegalAccessException | InvocationTargetException ignored) {
//            }
//        }

        String fieldName;
        String markFieldName;
        Method method;

        for (Map.Entry<String, String> entry : relationMap.entrySet()) {
            // 实体字段名
            fieldName = entry.getKey();
            // 标识名
            markFieldName = entry.getValue();
            // 获取对应get方法
            method = methodHashMap.get(fieldName);
            if (method == null) {
                throw new RuntimeException(fieldName + "get方法不存在");
            }
            Object value;
            try {

                value = method.invoke(obj);
                final IDataEntityProperty entityProperty = properties.get(markFieldName);
                if (value != null && entityProperty instanceof BasedataProp) {
                    String entityId;
                    if (entityProperty instanceof ItemClassProp) {
                        final String typePropName = ((ItemClassProp) entityProperty).getTypePropName();
                        entityId = dynamicObject.getString(typePropName);
                        if (StringUtils.isBlank(entityId)) {
                            throw new KDBizException("多类别资产资料为被赋值");
                        }
                    } else {
                        entityId = ((BasedataProp) entityProperty).getBaseEntityId();
                    }
                    // 如果是基础资料则转换
                    final Object tmp = value;
                    try {
                        // 获取对应的DynamicObject
                        value = AlmBusinessDataServiceHelper.loadSingle(value, entityId);
                    } catch (Exception e) {
                        value = null;
                        // 数据不存在
                        if (!isIgnoreMustInput && ((BasedataProp) entityProperty).isMustInput() && !tmp.equals(0L)) {

                            throw new KDBizException(String.format("[%s]字段对应的基础资料数据不存在", fieldName));
                        }
                    }
                }
                if (allowNullMap) {
                    // 允许null映射
                    dynamicObject.set(markFieldName, value);
                } else {
                    // 不允许null映射
                    if (value != null) {
                        // 不为空才更新
                        dynamicObject.set(markFieldName, value);
                    }
                }
            } catch (IllegalAccessException | InvocationTargetException ignored) {
            }
        }
        return dynamicObject;
    }

//    public static <T> List<DynamicObject> mapDynamicObject(Iterable<T> list) {
//        assert list != null;
//        final ArrayList<DynamicObject> dynamicObjectArrayList = new ArrayList<>();
//        final Iterator<T> iterator = list.iterator();
//        // get方法
//        Map<String, Method> methodMap = null;
//        // 表单标识
//        String entityName = null;
//        Map<String/*标识*/, Field/*实体字段名*/> fieldMap = new HashMap<>();
//        if (iterator.hasNext()) {
//            final T t = iterator.next();
//            final Class<?> c = t.getClass();
//            final Entity entity = c.getAnnotation(Entity.class);
//            entityName = entity.value();
//            methodMap = AlmDB.getAllGetMethod(c);
//
//            Class<?> tmpClass = c;
//            while (c != null && !tmpClass.getName().toLowerCase().equals("java.lang.object")) {
//                Map<String, Field> map = Arrays.stream(tmpClass.getDeclaredFields())
//                        .filter(it -> it.isAnnotationPresent(kd.alm.utils.db.annotation.Field.class))
//                        .collect(Collectors.toMap(it -> {
//                            final String formFieldName = it.getAnnotation(kd.alm.utils.db.annotation.Field.class).value();
//                            if (StringUtils.isBlank(formFieldName)) {
//                                // 转下划线
//                                return "digi_" + CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, it.getName());
//                            }
//                            return formFieldName;
//                        }, it -> it));
//                fieldMap.putAll(map);
//                // 父级
//                tmpClass = tmpClass.getSuperclass();
//            }
//            dynamicObjectArrayList.add(setDynamicObject(methodMap, entityName, fieldMap, t));
//
//        }
//        while (iterator.hasNext()) {
//            final T t = iterator.next();
//            dynamicObjectArrayList.add(setDynamicObject(methodMap, entityName, fieldMap, t));
//        }
//        return dynamicObjectArrayList;
//    }

//    private static <T> DynamicObject setDynamicObject(Map<String, Method> methodMap, String entityName, Map<String, Field> fieldMap, T t) {
//        final DynamicObject dynamicObject = AlmBusinessDataServiceHelper.newDynamicObject(entityName);
//        final DynamicObjectType dynamicObjectType = dynamicObject.getDynamicObjectType();
//        for (Map.Entry<String, Field> entry : fieldMap.entrySet()) {
//            Field field = entry.getValue();
//            final String formName = entry.getKey();
//            // 获取get方法
//            final Method method = methodMap.get(field.getName());
//
//            try {
//                Object v = method.invoke(t);
//                if (field.isAnnotationPresent(PrimaryKey.class)) {
//                    if (v == null || v.equals(0L)) {
//                        // 创建ID
//                        v = ID.genLongId();
//                    }
//                    dynamicObject.set(formName, v);
//
//                } else if (field.isAnnotationPresent(kd.alm.utils.db.annotation.Field.class)) {
//                    // 数据不为空
//                    if (v != null) {
//                        final DynamicProperty property = dynamicObjectType.getProperty(formName);
//                        if (property instanceof BasedataProp) {
//                            // 是基础资料
//                            String entityId;
//                            if (property instanceof ItemClassProp) {
//                                // 是多类别基础资料
//                                final String typePropName = ((ItemClassProp) property).getTypePropName();
//                                entityId = dynamicObject.getString(typePropName);
//                                if (StringUtils.isBlank(formName)) {
//                                    throw new KDBizException(String.format("[%s]字段对应的多类别资产资料未被赋值", field.getName()));
//                                }
//                            } else {
//                                entityId = ((BasedataProp) property).getBaseEntityId();
//                            }
//
//                            final Optional<DynamicObject> vOptional = AlmBusinessDataServiceHelper.loadSingleOptional(v, entityId);
//                            if (vOptional.isPresent()) {
//                                v = vOptional.get();
//                            } else {
//                                if (((BasedataProp) property).isMustInput() && !v.equals(0L)) {
//                                    throw new KDBizException(String.format("[%s]字段对应的基础资料数据不存在", field.getName()));
//                                }
//                            }
//
//                        }
//                    }
//                    dynamicObject.set(formName, v);
//                } else if (field.isAnnotationPresent(Entry.class) && Iterable.class.isAssignableFrom(field.getType())) {
//                    mapDynamicObject((Iterable) v);
//                }
//
//
//            } catch (IllegalAccessException | InvocationTargetException e) {
//                log.error("数据映射失败");
//                e.printStackTrace();
//            }
//        }
//        return dynamicObject;
//    }

}
