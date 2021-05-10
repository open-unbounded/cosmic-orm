package kd.alm.orm.test;

import kd.alm.orm.annotation.*;

import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * </p>
 * description
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/04/22 15:08
 */
@Data
@ToString
@Entity("digi_demand_apply")
public class DemandApplyTest implements Serializable {

    private static final long serialVersionUID = -8846604729649798608L;
    @PrimaryKey
    private String id;
    @Field("billno")
    private String number;
    @Field("digi_name")
    private String name;
    @Entry("digi_apply_line")
    private List<DemandApplyLineTest> demandApplyLineTestList;
    @ValueSet
    @Field
    private String requestOrgType;
    private Object requestOrgTypeName;
}
