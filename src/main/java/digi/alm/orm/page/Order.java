package digi.alm.orm.page;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * </p>
 * 排序
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/05/10 12:04
 */
@Data
@ToString
public class Order implements Serializable {

    private static final long serialVersionUID = 3057579551817911983L;

    /**
     * 排序字段
     */
    private String orderBy;
    /**
     * 排序方式
     */
    private Sorter sorter;
}
