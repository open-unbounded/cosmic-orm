package kd.alm.orm.test;

import kd.alm.orm.annotation.Field;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * </p>
 * description
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/04/20 22:23
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class TestOrg2 extends TestOrg {

    private static final long serialVersionUID = 1804081192100220070L;

    @Field("createorg.name")
    private String createOrgName;
    @Field("createorg.number")
    private String createOrgNumber;
}
