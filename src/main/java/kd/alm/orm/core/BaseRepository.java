package kd.alm.orm.core;

import kd.alm.orm.page.Page;
import kd.alm.orm.page.PageRequest;
import kd.bos.dataentity.OperateOption;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.orm.query.QFilter;

import java.util.List;
import java.util.Optional;

/**
 * </p>
 * 基础资源层
 * TODO 待完善
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/04/20 11:22
 */
public interface BaseRepository<T> {
    /**
     * 保存操作
     * TODO 此处暂不支持嵌套事务
     *
     * @param t      待保存的数据
     * @param option 操作
     * @return 保存结果
     */
    OperationResult saveOperate(T t, OperateOption option);

    /**
     * 批量保存操作
     * TODO 此处暂不支持嵌套事务
     *
     * @param list   待保存的数据列表
     * @param option 操作
     * @return 保存结果
     */
    OperationResult saveOperate(List<T> list, OperateOption option);

    /**
     * 批量保存
     *
     * @param list 数据
     */
    void save(List<T> list);

    /**
     * 保存
     *
     * @param record 数据
     */
    void save(T record);

    /**
     * 批量更新
     *
     * @param list 数据
     */
    void update(List<T> list);

    /**
     * 更新
     *
     * @param record 数据
     */
    void update(T record);

    /**
     * 删除
     * NOTE:生成的查询条件字段之间是AND关系
     *
     * @param record 删除条件
     * @return 被删除的条数
     */
    int delete(T record);

    /**
     * 查询列表
     *
     * @param fieldName 字段
     * @param value     值
     * @return 结果数据
     */
    List<T> select(String fieldName, Object value);

    /**
     * 查询列表
     *
     * @param key         字段
     * @param value       值
     * @param resultClass 结果类类型
     * @return 结果数据
     */
    <R> List<R> select(String key, Object value, Class<R> resultClass);

    /**
     * 统计满足条件的条数
     * NOTE:生成的查询条件字段之间是AND关系
     *
     * @param record 查询条件
     * @return 条数
     */
    int selectCount(T record);

    /**
     * 统计满足条件的条数
     *
     * @param qFilters   查询条件
     * @param queryClass 查询类
     * @param <R>        泛型
     * @return 条数
     */
    <R> int selectCount(List<QFilter> qFilters, Class<R> queryClass);

    /**
     * 查询单条数据
     * NOTE:生成的查询条件字段之间是AND关系
     *
     * @param record 查询条件
     * @return 结果数据
     */
    Optional<T> selectOne(T record);

    /**
     * 查询满足条件的列表
     * NOTE:生成的查询条件字段之间是AND关系
     *
     * @param record 查询条件
     * @return 结果数据
     */
    List<T> select(T record);

    /**
     * 查询当前表中全部数据
     *
     * @return 结果数据
     */
    List<T> selectAll();

    /**
     * 查询当前表以及关联表中全部数据
     * NOTE:关联系统需要在{@code resultClass}中定义
     *
     * @param resultClass 结果类类型
     * @return 结果数据
     */
    <R> List<R> selectAll(Class<R> resultClass);

    /**
     * 查询列表
     *
     * @param qFilters    条件
     * @param resultClass 结果类类型
     * @return 结果数据
     */
    <R> List<R> select(List<QFilter> qFilters, Class<R> resultClass);

    /**
     * 查询列表
     *
     * @param qFilters    条件
     * @param resultClass 结果类类型
     * @param order       排序
     * @return 结果数据
     */
    <R> List<R> select(List<QFilter> qFilters, Class<R> resultClass, String order);

    /**
     * 查询列表
     *
     * @param qFilters    条件
     * @param resultClass 结果类类型
     * @param order       排序
     * @param top         前top条数据
     * @return 结果数据
     */
    <R> List<R> select(List<QFilter> qFilters, Class<R> resultClass, String order, int top);

    /**
     * 生成查询条件
     * NOTE:生成的查询条件字段之间是AND关系
     *
     * @param record 实体类
     * @return 条件
     */
    <R> List<QFilter> buildQFilter(R record);

    /**
     * 分页查询
     *
     * @param record      实体类
     * @param pageRequest 分页请求
     * @return 结果数据
     */
    Page<T> selectPage(T record, PageRequest pageRequest);

    /**
     * 分页查询
     *
     * @param qFilters    实体类
     * @param pageRequest 分页请求
     * @param resultClass 结果类
     * @return 结果数据
     */
    <R> Page<R> selectPage(List<QFilter> qFilters, PageRequest pageRequest, Class<R> resultClass);

}
