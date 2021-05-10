package kd.alm.orm.test;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * </p>
 * description
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/04/22 19:27
 */

@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class DemandApplyTest2 extends DemandApplyTest implements Serializable {

    private static final long serialVersionUID = -4380754228806850000L;
}
