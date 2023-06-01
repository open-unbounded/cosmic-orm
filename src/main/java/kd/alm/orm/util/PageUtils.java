package kd.alm.orm.util;

import kd.alm.orm.page.Page;
import kd.alm.orm.page.PageRequest;
import kd.bos.algo.DataSet;
import kd.bos.algo.Row;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * </p>
 * 分页
 * <p>
 *
 * @author chenquan chenquan@osai.club 2020/08/22 17:33
 */
public class PageUtils {
    /**
     * 生成Page对象,但不会进行分页
     *
     * @param total       总计
     * @param pageRequest 分页
     * @return
     */
    public static <T> Page<T> genPage(int total, PageRequest pageRequest, Class<T> clzz) {


        //page = -2，不分页
        if (pageRequest.getPage() == -2){
            pageRequest.setSize(total);
            pageRequest.setPage(0);
        }

        // 计算总页数
        double totalPage = Math.ceil((double) total / pageRequest.getSize());
        // 下一页
        int nextPage;
        // 上一页
        int pervPage;

        if (pageRequest.getPage() >= totalPage) {
            nextPage = -1;
            pervPage = -1;
        } else {
            // 计算分页前/后页码
            pervPage = pageRequest.getPage() - 1;
            nextPage = pageRequest.getPage() + 1;
            if (nextPage == totalPage) {
                nextPage = -1;
            }
        }
        // 结果
        Page<T> page = new Page<>();
        page.setNextPage(nextPage);
        page.setPervPage(pervPage);
        page.setSize(pageRequest.getSize());
        page.setPage(pageRequest.getPage());
        page.setTotalPage((int) totalPage);
        page.setTotalSize(total);
        page.setData(new ArrayList<>());
        return page;
    }

    /**
     * 生成Page对象,但不会进行分页
     *
     * @param total       总计
     * @param pageRequest 分页
     * @return
     */
    public static <T> Page<T> genPage(int total, PageRequest pageRequest) {


        // 计算总页数
        double totalPage = Math.ceil((double) total / pageRequest.getSize());
        // 下一页
        int nextPage;
        // 上一页
        int pervPage;

        if (pageRequest.getPage() >= totalPage) {
            nextPage = -1;
            pervPage = -1;
        } else {
            // 计算分页前/后页码
            pervPage = pageRequest.getPage() - 1;
            nextPage = pageRequest.getPage() + 1;
            if (nextPage == totalPage) {
                nextPage = -1;
            }
        }
        // 结果
        Page<T> page = new Page<>();
        page.setNextPage(nextPage);
        page.setPervPage(pervPage);
        page.setSize(pageRequest.getSize());
        page.setPage(pageRequest.getPage());
        page.setTotalPage((int) totalPage);
        page.setTotalSize(total);
        page.setData(new ArrayList<>());
        return page;
    }

    /**
     * 分页
     *
     * @param total       数据总条数
     * @param ds          数据源
     * @param pageRequest 分页参数
     * @param clzz        数据类型
     * @param <T>         泛型
     * @return 分页结果
     */
    public static <T> Page<T> genPage(int total, DataSet ds, BiFunction<Row, T, T> handle, PageRequest pageRequest, Class<T> clzz) {
        Page<T> result = genPage(total, pageRequest, clzz);
        final int page = pageRequest.getPage();
        final int size = pageRequest.getSize();
        DataSet limit = ds.limit(page * size, size);
        List<T> pageData = AlmDB.query(limit, handle, clzz);
        result.setData(pageData);

        return result;
    }

    /**
     * 分页
     *
     * @param total       数据总条数
     * @param ds          数据源
     * @param pageRequest 分页参数
     * @param clzz        数据类型
     * @param <T>         泛型
     * @return 分页结果
     */
    public static <T> Page<T> genPage(int total, DataSet ds, PageRequest pageRequest, Class<T> clzz) {
        Page<T> result = genPage(total, pageRequest, clzz);
        final int page = pageRequest.getPage();
        final int size = pageRequest.getSize();
        DataSet limit = ds.limit(page * size, size);
        List<T> pageData = AlmDB.query(limit, (row, t) -> t, clzz);
        result.setData(pageData);

        return result;
    }

    /**
     * 分页
     *
     * @param ds          数据源
     * @param pageRequest 分页参数
     * @param clzz        数据类型
     * @param <T>         泛型
     * @return 分页结果
     */
    public static <T> Page<T> genPage(DataSet ds, BiFunction<Row, T, T> handle, PageRequest pageRequest, Class<T> clzz) {
        return genPage(ds, "id", handle, pageRequest, clzz);
    }

    /**
     * 分页
     *
     * @param ds          数据源
     * @param pageRequest 分页参数
     * @param clzz        数据类型
     * @param <T>         泛型
     * @return 分页结果
     */
    public static <T> Page<T> genPage(DataSet ds, PageRequest pageRequest, Class<T> clzz) {
        return genPage(ds, "id", (row, t) -> t, pageRequest, clzz);
    }


    /**
     * 分页
     *
     * @param ds          数据源
     * @param countField  统计字段
     * @param pageRequest 分页参数
     * @param clzz        数据类型
     * @param handle      处理行函数
     * @param <T>         泛型
     * @return 分页结果
     */
    public static <T> Page<T> genPage(DataSet ds, String countField, BiFunction<Row, T, T> handle, PageRequest pageRequest, Class<T> clzz) {
        int total = ds.copy().count(countField, false);
        return genPage(total, ds, handle, pageRequest, clzz);
    }

    /**
     * 分页
     *
     * @param ds          数据源
     * @param countField  统计字段
     * @param pageRequest 分页参数
     * @param clzz        数据类型
     * @param <T>         泛型
     * @return 分页结果
     */
    public static <T> Page<T> genPage(DataSet ds, String countField, PageRequest pageRequest, Class<T> clzz) {
        int total = ds.copy().count(countField, false);
        return genPage(total, ds, (row, t) -> t, pageRequest, clzz);
    }
}
