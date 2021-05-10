package kd.alm.orm.test;

import kd.alm.orm.annotation.Entity;
import kd.alm.orm.annotation.Field;
import kd.alm.orm.annotation.PrimaryKey;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;

/**
 * </p>
 * description
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/05/10 10:30
 */
@Entity("digi_assets")
@Data
@ToString
public class Asset implements Serializable {

    private static final long serialVersionUID = 8341773365313298653L;
    @PrimaryKey
    private String id;
    @Field("name")
    private String name;
    @Field("number")
    private String number;
}
