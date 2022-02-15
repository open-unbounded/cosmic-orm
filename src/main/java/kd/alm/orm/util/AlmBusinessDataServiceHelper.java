package kd.alm.orm.util;

import kd.alm.orm.page.Page;
import kd.alm.orm.page.PageRequest;
import kd.bos.algo.DataSet;
import kd.bos.cache.CacheFactory;
import kd.bos.cache.DistributeSessionlessCache;
import kd.bos.data.BusinessDataReader;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.metadata.IDataEntityProperty;
import kd.bos.dataentity.metadata.dynamicobject.DynamicObjectType;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.entity.property.EntryProp;
import kd.bos.entity.property.LinkEntryProp;
import kd.bos.exception.ErrorCode;
import kd.bos.exception.KDException;
import kd.bos.id.ID;
import kd.bos.orm.ORM;
import kd.bos.orm.query.QFilter;
import kd.bos.orm.query.WithEntityEntryDistinctable;
import kd.bos.servicehelper.BusinessDataServiceHelper;

import java.util.*;
import java.util.function.Supplier;

/**
 * </p>
 * 查询工具类
 *
 * <p>
 *
 * @author chenquan chenquan@osai.club 2020/07/14 14:46
 */
public class AlmBusinessDataServiceHelper extends BusinessDataServiceHelper {

    /**
     * 缓存
     */
    private static final DistributeSessionlessCache cache = CacheFactory.getCommonCacheFactory().getDistributeSessionlessCache();


    /**
     * 分页和排序查询
     *
     * @param entityName  标识名
     * @param filters     过滤条件
     * @param pageRequest 分页
     * @return 分页的查询内容
     */
    public static Page doPageAndSortFromCache(String entityName, QFilter[] filters, PageRequest pageRequest) {

        return doPageAndSortFromCache(entityName, filters, null, pageRequest);
    }

    /**
     * 分页和排序查询
     *
     * @param entityName       标识名
     * @param filters          过滤条件
     * @param selectProperties 字段
     * @param pageRequest      分页
     * @return 分页的查询内容
     */
    public static Page doPageAndSortFromCache(String entityName, QFilter[] filters, String selectProperties, PageRequest pageRequest) {
        DataSet ds = ORM.create().queryDataSet("AlmBusinessDataServiceHelper.doPageAndSortFromCache", entityName, "id", filters, pageRequest.toOrderBys(), -1, WithEntityEntryDistinctable.get());
        // 主键列表
        List<Long> idList = new ArrayList<>();
        ds.forEach((row) -> {
            idList.add(row.getLong(0));
        });
        // 关闭DataSet
        ds.close();
        return doPageFromCache(idList, entityName, selectProperties, pageRequest);
    }

    /**
     * 分页
     *
     * @param pkList      主键ID列表
     * @param entityName  标识名
     * @param pageRequest 分页
     * @return 分页的查询内容
     */
    public static Page doPageFromCache(List<Long> pkList, String entityName, PageRequest pageRequest) {
        return doPageFromCache(pkList, entityName, null, pageRequest);
    }

    /**
     * 分页
     *
     * @param pkList           主键ID列表
     * @param entityName       标识名
     * @param selectProperties 字段
     * @param pageRequest      分页
     * @return 分页的查询内容
     */
    public static Page<DynamicObject> doPageFromCache(List<Long> pkList, String entityName, String selectProperties, PageRequest pageRequest) {
        DynamicObjectType type;
        if (selectProperties == null) {
            type = EntityMetadataCache.getDataEntityType(entityName);
        } else {
            type = getSubEntityType(entityName, selectProperties);
        }
        // 计算查询的个数
        int total = pkList.size();

        // 计算总页数
        double totalPage = Math.ceil((double) total / pageRequest.getSize());

        int nextPage;
        int pervPage;
        Map<Object, DynamicObject> resultMap = new HashMap<Object, DynamicObject>(16);
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
            resultMap = loadFromCache(pageIdList.toArray(), type);
            // 计算分页前/后页码
            nextPage = pageRequest.getPage() - 1;
            pervPage = pageRequest.getPage() + 1;
            if (pervPage == totalPage) {
                pervPage = -1;
            }
        }
        // 结果
        Page<DynamicObject> page = new Page<>();
        page.setNextPage(nextPage);
        page.setPervPage(pervPage);
        page.setSize(pageRequest.getSize());
        page.setPage(pageRequest.getPage());
        page.setTotalPage((int) totalPage);
        page.setTotalSize(total);
        page.setData(new ArrayList<>(resultMap.values()));
        return page;
    }

    /**
     * 字段
     *
     * @param entityName       标识名
     * @param selectProperties 字段
     */
    private static DynamicObjectType getSubEntityType(String entityName, String selectProperties) {
        String[] properties = selectProperties.split(",");
        Set<String> select = new HashSet<>(properties.length);
        for (String prop : properties) {
            select.add(prop.trim());
        }
        return EntityMetadataCache.getSubDataEntityType(entityName, select);
    }

    /**
     * 通过查询实体动态对象
     *
     * @param pk         主键
     * @param entityName 标识
     * @return Optional<DynamicObject>
     */
    public static Optional<DynamicObject> loadSingleOptional(Object pk, String entityName) {
        if (pk == null) {
            return Optional.empty();
        }

        return loadSingleOptional(() -> {
            final DynamicObject dynamicObject = loadSingle(pk, entityName);
            return Optional.of(dynamicObject);
        });
    }

    /**
     * 通过查询实体动态对象
     *
     * @param pk   主键
     * @param type 类型
     * @return Optional<DynamicObject>
     */
    public static Optional<DynamicObject> loadSingleOptional(Object pk, DynamicObjectType type) {
        if (pk == null) {
            return Optional.empty();
        }

        return loadSingleOptional(() -> {
            final DynamicObject dynamicObject = loadSingle(pk, type);
            return Optional.of(dynamicObject);
        });
    }


    private static Optional<DynamicObject> loadSingleOptional(Supplier<Optional<DynamicObject>> handle) {

        try {
            return handle.get();
        } catch (RuntimeException e) {
            Throwable cause = e;
            while (cause != null) {
                if (cause instanceof KDException) {
                    final ErrorCode errorCode = ((KDException) cause).getErrorCode();
                    if (errorCode != null && "bos.orm.read.dataNotExist".equals(errorCode.getCode())) {
                        return Optional.empty();
                    }
                }
                if (cause.getCause() != cause) {
                    cause = cause.getCause();
                }
            }

            throw e;
        }


    }


    /**
     * @param entityName 表单标识
     * @param filters
     * @return
     */
    public static Optional<DynamicObject> loadSingleOptional(String entityName, QFilter[] filters) {
        final DynamicObject dynamicObject = loadSingle(entityName, "id", filters);
        if (dynamicObject == null) {
            return Optional.empty();
        }
        return loadSingleOptional(() -> loadSingleOptional(dynamicObject.getPkValue(), entityName));
    }


    /**
     * 查询多条DynamicObject
     *
     * @param entityName 表单标识
     * @param filters
     * @return
     */
    public static Optional<DynamicObject[]> loadOptional(String entityName, QFilter[] filters) {

        final DynamicObject[] dynamicObjectCollection = load(entityName, "id", filters);
        Object[] pks = Arrays.stream(dynamicObjectCollection).map(e -> e.get("id")).toArray();

        DynamicObjectType type = EntityMetadataCache.getDataEntityType(entityName);
        DynamicObject[] list = BusinessDataServiceHelper.load(pks, type);

        if (list.length > 0) {
            return Optional.of(list);
        }
        return Optional.empty();
    }

    /**
     * 查询多条DynamicObject
     * @param entityName 表单标识
     * @param filters 过滤条件
     * @param orderBy 排序条件
     * @return
     */
    public static Optional<DynamicObject[]> loadOptional(String entityName, QFilter[] filters, String orderBy) {

        final DynamicObject[] dynamicObjectCollection = AlmBusinessDataServiceHelper.load(entityName, "id", filters, orderBy);
        Object[] pks = Arrays.stream(dynamicObjectCollection).map(e -> e.get("id")).toArray();
        DynamicObjectType type = EntityMetadataCache.getDataEntityType(entityName);

        List<Object> ids = new ArrayList<>();
        Collections.addAll(ids, pks);
        DynamicObject[] objs = BusinessDataReader.load(ids.toArray(), type, Boolean.TRUE);

        DynamicObject[] list = orderBy(objs, ids, orderBy);

        if (list.length > 0) {
            return Optional.of(list);
        }
        return Optional.empty();
    }

    private static DynamicObject[] orderBy(DynamicObject[] dynamicObjects, List<Object> idList, String orderBy) {
        if (!kd.bos.dataentity.utils.StringUtils.isBlank(orderBy) && idList.size() > 1) {
            Map<Object, DynamicObject> maps = new HashMap<>();

            for (DynamicObject dynamicObject : dynamicObjects) {
                maps.put(dynamicObject.getPkValue(), dynamicObject);
            }

            List<DynamicObject> listDyn = new ArrayList<>();

            for (Object id : idList) {
                if (maps.get(id) != null) {
                    listDyn.add(maps.get(id));
                }
            }

            return listDyn.toArray(new DynamicObject[0]);
        } else {
            return dynamicObjects;
        }
    }

    /**
     * 获取单据体DynamicObjectType
     *
     * @param entityName 表单标识
     * @param entryName  单据体标识
     * @return DynamicObjectType
     */
    public static DynamicObjectType getEntryDynamicObjectType(String entityName, String entryName) {
        final DynamicObject dynamicObject = newDynamicObject(entityName);
        return dynamicObject.getDynamicObjectCollection(entryName).getDynamicObjectType();
    }


    /**
     * 为主键数据无效的数据设置主键
     *
     * @param dynamicObject
     */
    public static void setPrimaryKey(DynamicObject dynamicObject) {
        final String id = dynamicObject.getString("id");
        if (id == null || "0".equals(id)) {
            dynamicObject.set("id", ID.genLongId());
        }
        final DynamicObjectType dynamicObjectType = dynamicObject.getDynamicObjectType();
        for (IDataEntityProperty property : dynamicObjectType.getProperties()) {
            if (!(property instanceof LinkEntryProp) && property instanceof EntryProp) {
                // 单据体
                final DynamicObjectCollection dynamicObjectCollection = dynamicObject.getDynamicObjectCollection(property.getName());
                setPrimaryKey(dynamicObjectCollection);
            }
        }
    }

    /**
     * 为主键数据无效的数据设置主键
     *
     * @param dynamicObjectCollection
     */
    public static void setPrimaryKey(DynamicObjectCollection dynamicObjectCollection) {
        for (DynamicObject dynamicObject : dynamicObjectCollection) {
            setPrimaryKey(dynamicObject);
        }
    }
}
