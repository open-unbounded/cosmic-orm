package kd.alm.orm.test;

import kd.alm.orm.MulValueSet;
import kd.alm.orm.annotation.Entity;

import kd.alm.orm.annotation.Field;
import kd.alm.orm.annotation.PrimaryKey;
import kd.alm.orm.annotation.ValueSet;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * </p>
 * description
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/04/19 21:08
 */
@Data
@ToString
@Entity("digi_test_org")
public class TestOrg implements Serializable {
    private static final long serialVersionUID = 3740533878373467645L;
    @PrimaryKey
    private String id;
    @Field("name")
    private String name;
    @Field("createorg.id")
    private String createOrgId;

    @Field("digi_mul_value_set")
    private MulValueSet mulValueSet;
    @Field("digi_asset_list")
    private List<Asset> assetList;

}
