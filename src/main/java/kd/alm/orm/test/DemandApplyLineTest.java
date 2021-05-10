package kd.alm.orm.test;


import kd.alm.orm.annotation.Entity;
import kd.alm.orm.annotation.Field;
import kd.alm.orm.annotation.PrimaryKey;
import lombok.Data;

import java.io.Serializable;

/**
 * </p>
 * description
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/04/22 15:16
 */
@Data
@Entity("digi_demand_apply.digi_apply_line")
public class DemandApplyLineTest implements Serializable {

    private static final long serialVersionUID = -3748960081524292778L;
    /**
     * 主键
     */
    @PrimaryKey
    private String id;
    /**
     * 行类型ID
     */
    @Field("digi_line_type.id")
    private String lineTypeId;
    /**
     * 资产类别ID
     */
    @Field("digi_asset_class.id")
    private String assetClassId;
    /**
     * 物料ID
     */
    @Field("digi_item.id")
    private String itemId;
    /**
     * 物料ID
     */
    @Field("digi_item.number")
    private String itemNumber;
    /**
     * 物料ID
     */
    @Field("digi_item.name")
    private String itemName;
    /**
     * 资产名称
     */
    @Field("digi_asset_name")
    private String assetName;
}
