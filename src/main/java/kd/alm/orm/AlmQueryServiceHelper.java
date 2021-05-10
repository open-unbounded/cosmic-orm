package kd.alm.orm;

import kd.bos.algo.DataSet;
import kd.bos.algo.Row;
import kd.bos.orm.ORM;
import kd.bos.orm.query.Distinctable;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * </p>
 * 扩展QueryServiceHelper
 * <p>
 *
 * @author chenquan chenquan@osai.club 2021/01/23 10:05
 */
public class AlmQueryServiceHelper extends QueryServiceHelper {
    /**
     * 主键标识
     */
    private static final String PRIMARY_KEY = "id";

    public static DataSet queryDataSet(String algoKey, String entityName, String selectFields, QFilter[] filters) {
        return queryDataSet(algoKey, entityName, selectFields, filters, (String) null);
    }

    public static DataSet queryDataSet(String algoKey, String entityName, String selectFields, QFilter[] filters, Distinctable distinctable) {
        return queryDataSet(algoKey, entityName, selectFields, filters, null, 0, -1, distinctable);
    }

    public static DataSet queryDataSet(String algoKey, String entityName, String selectFields, QFilter[] filters, String orderBys) {
        return queryDataSet(algoKey, entityName, selectFields, filters, orderBys, 0, -1, null);
    }

    public static DataSet queryDataSet(String algoKey, String entityName, String selectFields, QFilter[] filters, String orderBys, int top) {
        return queryDataSet(algoKey, entityName, selectFields, filters, orderBys, 0, top, null);
    }


    public static DataSet queryDataSet(String algoKey, String entityName, String selectFields, QFilter[] filters, String orderBys, int top, Distinctable distinctable) {
        return queryDataSet(algoKey, entityName, selectFields, filters, orderBys, 0, top, distinctable);
    }

    public static DataSet queryDataSet(String algoKey, String entityName, String selectFields, QFilter[] filters, String orderBys, int from, int length, Distinctable distinctable) {
        ORM orm = ORM.create();
        return orm.queryDataSet(algoKey, entityName, selectFields, filters, orderBys, from, length, distinctable);
    }

//    /**
//     * 分页查询
//     *
//     * @param algoKey      algo标识
//     * @param entityName   表单标识
//     * @param selectFields 查询字段
//     * @param filters      查询条件
//     * @param rowHandle    行处理函数,可对{@link Row}和 T 进行处理
//     * @param pageRequest  分页
//     * @param distinctable 去重
//     * @param calzz        DTO class
//     * @param <T>          泛型DTO
//     * @return 分页数据
//     */
//    public static <T> Page<T> queryPage(String algoKey, String entityName, String selectFields, QFilter[] filters, BiFunction<Row, T, T> rowHandle, PageRequest pageRequest, Distinctable distinctable, Class<T> calzz) {
//        final int size = pageRequest.getSize();
//        final int pageNum = pageRequest.getPage();
//        // 查询总条数
//        final DataSet dataSet = queryDataSet(algoKey, entityName, PRIMARY_KEY, filters, null, 0, -1, distinctable);
//        final int count = dataSet.count(PRIMARY_KEY, false);
//        // 分页查询数据
//        final DataSet pageDataSet = queryDataSet(algoKey, entityName, selectFields, filters, pageRequest.toOrderBys(), pageNum * size, size, distinctable);
//        // 映射数据
//        final List<T> list = AlmDB.query(pageDataSet, rowHandle, calzz);
//        // 生成分页
//        final Page<T> result = PageUtils.genPage(count, pageRequest, calzz);
//        // 存储结果数据
//        result.setData(list);
//        return result;
//    }
//
//    /**
//     * 分页查询
//     *
//     * @param dataSet     全部的dataset
//     * @param pageRequest 分页
//     * @param calzz       DTO class
//     * @param <T>         泛型DTO
//     * @return 分页数据
//     */
//    public static <T> Page<T> queryPageByAllData(DataSet dataSet, String key, PageRequest pageRequest, Class<T> calzz) {
//        return queryPageByAllData(dataSet, key, pageRequest, (row, t) -> t, calzz);
//    }
//
//    /**
//     * 分页查询
//     *
//     * @param dataSet     全部的dataset
//     * @param pageRequest 分页
//     * @param rowHandle   行处理函数,可对{@link Row}和 T 进行处理
//     * @param calzz       DTO class
//     * @param <T>         泛型DTO
//     * @return 分页数据
//     */
//    public static <T> Page<T> queryPageByAllData(DataSet dataSet, String key, PageRequest pageRequest, BiFunction<Row, T, T> rowHandle, Class<T> calzz) {
//        final int size = pageRequest.getSize();
//        final int pageNum = pageRequest.getPage();
//        // 查询总条数
//        final int count = dataSet.copy().count(key, false);
//        // 分页查询数据
//        final DataSet pageDataSet = dataSet.limit(pageNum * size, size);
//        // 映射数据
//        final List<T> list = AlmDB.query(pageDataSet, rowHandle, calzz);
//        // 生成分页
//        final Page<T> result = PageUtils.genPage(count, pageRequest, calzz);
//        // 存储结果数据
//        result.setData(list);
//        return result;
//    }
//
//    /**
//     * 分页查询
//     *
//     * @param algoKey      algo标识
//     * @param entityName   表单标识
//     * @param selectFields 查询字段
//     * @param filters      查询条件
//     * @param pageRequest  分页
//     * @param distinctable 去重
//     * @param calzz        DTO class
//     * @param <T>          泛型DTO
//     * @return 分页数据
//     */
//    public static <T> Page<T> queryPage(String algoKey, String entityName, String selectFields, QFilter[] filters, PageRequest pageRequest, Distinctable distinctable, Class<T> calzz) {
//        return queryPage(algoKey, entityName, selectFields, filters, (row, t) -> t, pageRequest, distinctable, calzz);
//    }
//
//    /**
//     * 分页查询
//     *
//     * @param algoKey      algo标识
//     * @param entityName   表单标识
//     * @param selectFields 查询字段
//     * @param filters      查询条件
//     * @param rowHandle    行处理函数,可对{@link Row}和 T 进行处理
//     * @param pageRequest  分页
//     * @param calzz        DTO class
//     * @param <T>          泛型DTO
//     * @return 分页数据
//     */
//    public static <T> Page<T> queryPage(String algoKey, String entityName, String selectFields, QFilter[] filters, BiFunction<Row, T, T> rowHandle, PageRequest pageRequest, Class<T> calzz) {
//        return queryPage(algoKey, entityName, selectFields, filters, rowHandle, pageRequest, null, calzz);
//    }
//
//    /**
//     * 分页查询
//     *
//     * @param algoKey      algo标识
//     * @param entityName   表单标识
//     * @param selectFields 查询字段
//     * @param filters      查询条件
//     * @param pageRequest  分页
//     * @param calzz        DTO class
//     * @param <T>          泛型DTO
//     * @return 分页数据
//     */
//    public static <T> Page<T> queryPage(String algoKey, String entityName, String selectFields, QFilter[] filters, PageRequest pageRequest, Class<T> calzz) {
//        return queryPage(algoKey, entityName, selectFields, filters, (row, t) -> t, pageRequest, null, calzz);
//    }
//

    /**
     * 列表查询
     *
     * @param algoKey      algo标识
     * @param entityName   表单标识
     * @param selectFields 查询字段
     * @param filters      查询条件
     * @param rowHandle    行处理函数,可对{@link Row}和 T 进行处理
     * @param orderBys     排序
     * @param distinctable 去重
     * @param calzz        DTO class
     * @param <T>          泛型DTO
     * @return 列表数据
     */
    public static <T> List<T> queryList(String algoKey, String entityName, String selectFields, QFilter[] filters, BiFunction<Row, T, T> rowHandle, String orderBys, Distinctable distinctable, Class<T> calzz) {
        DataSet pageDataSet = queryDataSet(algoKey, entityName, selectFields, filters, orderBys, 0, -1, distinctable);
        return AlmDB.query(pageDataSet, rowHandle, calzz);
    }

    /**
     * 列表查询
     *
     * @param algoKey      algo标识
     * @param entityName   表单标识
     * @param selectFields 查询字段
     * @param filters      查询条件
     * @param calzz        DTO class
     * @param <T>          泛型DTO
     * @return 列表数据
     */
    public static <T> List<T> queryList(String algoKey, String entityName, String selectFields, QFilter[] filters, String orderBys, Distinctable distinctable, Class<T> calzz) {
        return queryList(algoKey, entityName, selectFields, filters, (row, t) -> t, orderBys, distinctable, calzz);
    }

    /**
     * 列表查询
     *
     * @param algoKey      algo标识
     * @param entityName   表单标识
     * @param selectFields 查询字段
     * @param filters      查询条件
     * @param calzz        DTO class
     * @param <T>          泛型DTO
     * @return 列表数据
     */
    public static <T> List<T> queryList(String algoKey, String entityName, String selectFields, QFilter[] filters, String orderBys, Class<T> calzz) {
        return queryList(algoKey, entityName, selectFields, filters, (row, t) -> t, orderBys, null, calzz);
    }

    /**
     * 列表查询
     *
     * @param algoKey      algo标识
     * @param entityName   表单标识
     * @param selectFields 查询字段
     * @param filters      查询条件
     * @param rowHandle    行处理函数,可对{@link Row}和 T 进行处理
     * @param calzz        DTO class
     * @param <T>          泛型DTO
     * @return 列表数据
     */
    public static <T> List<T> queryList(String algoKey, String entityName, String selectFields, QFilter[] filters, BiFunction<Row, T, T> rowHandle, String orderBys, Class<T> calzz) {
        return queryList(algoKey, entityName, selectFields, filters, rowHandle, orderBys, null, calzz);
    }

    /**
     * 单对象查询
     *
     * @param algoKey      algo标识
     * @param entityName   表单标识
     * @param selectFields 查询字段
     * @param filters      查询条件
     * @param calzz        DTO class
     * @param <T>          泛型DTO
     * @return 单对象
     */
    public static <T> Optional<T> queryOne(String algoKey, String entityName, String selectFields, QFilter[] filters, String orderBys, Class<T> calzz) {
        DataSet pageDataSet = queryDataSet(algoKey, entityName, selectFields, filters, orderBys, 0, 1, null);
        return Optional.ofNullable(AlmDB.queryOne(pageDataSet, calzz));
    }

    /**
     * 单对象查询
     *
     * @param algoKey      algo标识
     * @param entityName   表单标识
     * @param selectFields 查询字段
     * @param filters      查询条件
     * @param calzz        DTO class
     * @param <T>          泛型DTO
     * @return 单对象
     */
    public static <T> Optional<T> queryOne(String algoKey, String entityName, String selectFields, QFilter[] filters, Class<T> calzz) {
        DataSet pageDataSet = queryDataSet(algoKey, entityName, selectFields, filters, null, 0, 1, null);
        return Optional.ofNullable(AlmDB.queryOne(pageDataSet, calzz));
    }


    public static Optional<DataSet> queryAndUnion(Map<String, Set<String>> map) {
        DataSet resultDs = null;
        for (Map.Entry<String, Set<String>> entry : map.entrySet()) {
            final String entityName = entry.getKey();
            final Set<String> ids = entry.getValue();
            String selectFields = "id,number,name";
            if ("bd_customerlinkman".equals(entityName)) {
                selectFields = "id,phone as number,contactperson As name";
            } else if ("bd_supplierlinkman".equals(entityName)) {
                selectFields = "id,mobile as number,contactperson As name";
            }
            DataSet ds = QueryServiceHelper.queryDataSet("AlmQueryServiceHelper.getMulBaseMapByAssetPage",
                    entityName,
                    selectFields, new QFilter[]{new QFilter("id", QCP.in, ids)}, null);
            if (resultDs == null) {
                resultDs = ds;
            } else {
                resultDs = resultDs.union(ds);
            }
        }
        return Optional.ofNullable(resultDs);
    }
}
