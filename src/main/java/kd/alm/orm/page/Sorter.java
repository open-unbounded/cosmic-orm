package kd.alm.orm.page;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * </p>
 * description
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/05/10 11:54
 */
@ToString
public enum Sorter implements Serializable {
    /**
     * 降序
     */
    DESC,
    /**
     * 升序
     */
    ASC,
}
