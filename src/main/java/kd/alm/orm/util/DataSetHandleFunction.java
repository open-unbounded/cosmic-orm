package kd.alm.orm.util;

import kd.bos.algo.DataSet;

/**
 * </p>
 * DataSet处理
 * <p>
 *
 * @author chenquan chenquan@osai.club 2020/11/23 22:00
 */
@FunctionalInterface
public interface DataSetHandleFunction {
    /**
     * 处理执行
     *
     * @param a DataSet
     * @param b DataSet
     * @return DataSet
     */
    DataSet handle(DataSet a, DataSet b);
}
