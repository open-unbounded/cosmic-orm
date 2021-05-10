package kd.alm.orm;

import kd.bos.dataentity.entity.LocaleString;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * </p>
 * 值集列表
 * NOTE:使用此类时,无需添加注解{@link kd.alm.orm.annotation.ValueSet}
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/05/08 16:49
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MulValueSet extends ArrayList<MulValueSet.ValueSet> {
    @Override
    public String toString() {
        return super.toString();
    }

    /**
     * </p>
     * 值集
     * <p>
     *
     * @author chenquan chenquan.dev@foxmail.com 2021/05/08 16:53
     */
    @Data
    @ToString
    public static class ValueSet implements Serializable {
        private String value;
        private LocaleString name;
    }
}
