package kd.alm.orm.page;

import lombok.Data;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * </p>
 * 分页请求
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/05/10 12:15
 */
@Data
@ToString
public class PageRequest implements Serializable {
    /**
     * 条数
     */
    private int size = 20;
    /**
     * 页码
     */
    private int page = 0;
    /**
     * 排序
     */
    private List<Order> orders;

    /**
     * 返回排序
     *
     * @return 排序
     */
    public String toOrderBys() {
        // 通过 orderBys 字符串接收排序逻辑
        if (CollectionUtils.isNotEmpty(this.orders)) {
            final StringBuilder stringBuffer = new StringBuilder();
            for (Order order : this.orders) {
                stringBuffer.append(order.getOrderBy()).append(" ").append(order.getSorter().toString());
            }
            return stringBuffer.toString();
        }
        return null;
    }

}
