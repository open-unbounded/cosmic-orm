package kd.alm.orm.page;

import lombok.Data;
import lombok.ToString;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.util.Strings;

import java.io.Serializable;
import java.util.List;
import java.util.StringJoiner;

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
    private static final long serialVersionUID = 7909018768787247681L;
    /**
     * 条数
     */
    private int size = 20;
    /**
     * 页码
     */
    private int page = 0;

    @Deprecated
    private String orderBys;

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
        if (CollectionUtils.isEmpty(this.orders) && Strings.isNotEmpty(this.orderBys)) {
            return this.orderBys;
        } else
            // 通过 orderBys 字符串接收排序逻辑
            if (CollectionUtils.isNotEmpty(this.orders)) {
                final StringJoiner stringJoiner = new StringJoiner(", ");
                for (Order order : this.orders) {
                    stringJoiner.add(order.getOrderBy().concat(" ").concat(order.getSorter().name()));
                }

                return stringJoiner.toString();
            }
        return null;
    }

    /**
     * 返回排序
     *
     * @return 排序
     */
    public String[] toOrderByArray() {
        if (CollectionUtils.isNotEmpty(this.orders)) {
            final String[] orderByArray = new String[this.orders.size()];
            for (int i = 0; i < this.orders.size(); i++) {
                orderByArray[i] = this.orders.get(i).getOrderBy().concat(" ").concat(this.orders.get(i).getSorter().toString());
            }
            return orderByArray;
        }
        return null;
    }


}
