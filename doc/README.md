# 一. 概述
ORM 基于`digi.alm.utils.db.BaseRepository`和`digi.alm.utils.db.BaseService`接口并搭配四个注解`digi.alm.utils.db.annotation.Entity`,`digi.alm.utils.db.annotation.PrimaryKey`,`digi.alm.utils.db.annotation.Field`,`digi.alm.utils.db.annotation.Entry`构建.
## 1.1 `digi.alm.utils.db.BaseRepository`接口
```java
package digi.alm.utils.db;

import kd.bos.dataentity.OperateOption;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.orm.query.QFilter;

import java.util.List;
import java.util.Optional;

/**
 * </p>
 * 基础资源层
 * TODO 待完善
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/04/20 11:22
 */
public interface BaseRepository<T> {
    /**
     * 保存操作
     * TODO
     *
     * @param t      待保存的数据
     * @param option 操作
     * @return 保存结果
     */
    OperationResult saveOperate(T t, OperateOption option);

    /**
     * 保存操作
     * TODO
     *
     * @param list   待保存的数据列表
     * @param option 操作
     * @return 保存结果
     */
    OperationResult saveOperate(List<T> list, OperateOption option);

    /**
     * 保存
     * TODO
     *
     * @param list 数据
     */
    void save(List<T> list);

    /**
     * 保存
     * TODO
     *
     * @param record 数据
     */
    void save(T record);

    /**
     * 更新
     * TODO
     *
     * @param list 数据
     */
    void update(List<T> list);

    /**
     * 更新
     * TODO
     *
     * @param record 数据
     */
    void update(T record);

    /**
     * 删除
     * NOTE:生成的查询条件字段之间是AND关系
     *
     * @param record 删除条件
     * @return 被删除的条数
     */
    int delete(T record);

    /**
     * 查询
     *
     * @param key   字段
     * @param value 值
     * @return 结果数据
     */
    List<T> select(String key, Object value);

    /**
     * 查询
     *
     * @param key         字段
     * @param value       值
     * @param resultClass 结果类类型
     * @return 结果数据
     */
    <R> List<R> select(String key, Object value, Class<R> resultClass);

    /**
     * 统计满足条件的条数
     * NOTE:生成的查询条件字段之间是AND关系
     *
     * @param record 查询条件
     * @return 条数
     */
    int selectCount(T record);

    /**
     * 查询单条数据
     * NOTE:生成的查询条件字段之间是AND关系
     *
     * @param record 查询条件
     * @return 结果数据
     */
    Optional<T> selectOne(T record);

    /**
     * 查询列表
     * NOTE:生成的查询条件字段之间是AND关系
     *
     * @param record 查询条件
     * @return 结果数据
     */
    List<T> select(T record);

    /**
     * 查询全部数据
     *
     * @return 结果数据
     */
    List<T> selectAll();

    /**
     * 查询全部数据
     *
     * @param resultClass 结果类类型
     * @return 结果数据
     */
    <R> List<R> selectAll(Class<R> resultClass);

    /**
     * 查询列表
     *
     * @param qFilters    条件
     * @param resultClass 结果类类型
     * @return 结果数据
     */
    <R> List<R> select(List<QFilter> qFilters, Class<R> resultClass);

    /**
     * 生成查询条件
     * NOTE:生成的查询条件字段之间是AND关系
     *
     * @param record 实体类
     * @return 条件
     */
    <R> List<QFilter> genQFilter(R record);
}

```
## 1.2 `digi.alm.utils.db.BaseService`接口
目前`digi.alm.utils.db.BaseService`接口均来自`digi.alm.utils.db.BaseRepository`
```java
package digi.alm.utils.db;

/**
 * </p>
 * 基础服务层
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/04/20 11:32
 */
public interface BaseService<T>  extends BaseRepository<T> {

}

```
## 1.3 `digi.alm.utils.db.annotation.Entity`注解
用于针对数据表表单或单据体对应的实体类进行注解,定义代码如下:
```java
package digi.alm.utils.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * </p>
 * 实体注解
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/04/19 17:42
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entity {
    /**
     * 表单或单据体标识
     */
    String value();

    /**
     * 字段描述
     */
    String description() default "";
}

```

1. 当前实体类对应表单,例如`digi_pr`,则在类上添加`@digi.alm.utils.db.annotation.Entity("digi_pr")`
1. 当前实体类对应单据体,例如表单`digi_pr`中的单据体`digi_pr_line`,则在类上添加`@digi.alm.utils.db.annotation.Entity("digi_pr.digi_pr_line")`
## 1.4 `digi.alm.utils.db.annotation.PrimaryKey`注解
用于针对数据表表单或单据体对应的实体类中的主键字段进行注解,定义代码如下:
```java
package digi.alm.utils.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * </p>
 * 主键注解
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/04/19 22:52
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PrimaryKey {
    /**
     * 描述
     */
    String description() default "";
}

```
## 1.5 `digi.alm.utils.db.annotation.Field`注解
用于针对数据表表单或单据体对应的实体类中的非主键字段和非当前数据表但需要映射的字段进行注解,定义代码如下:
```java
package digi.alm.utils.db.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * </p>
 * 字段注解
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/04/19 21:13
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Field {
    /**
     * 字段名称
     */
    String value() default "";

    /**
     * 字段描述
     */
    String description() default "";

    /**
     * 是数据表字段
     */
    boolean isDBField() default true;
}

```
# 二 基础代码构建
## 2.1 建立苍穹表单与Java 实体类的对应关系
所有的对应数据表的实体Class需要添加`@Entity`并注明其表示的表单标识,同时使用 `@Field`注解数据表字段
以`digi.alm.entity.Pr`为例:
`@PrimaryKey`:表示`id`对应着`digi_pr`表单的主键
`@Entity("digi_pr")`:表示`Pr`实体类对应着`digi_pr`表单
`@Field("digi_name")`:表示`name`字段对应`digi_pr`表单中`digi_name`字段
`@Entry("digi_pr_line")`表示`prLineList`字段对应`digi_pr`表单中`digi_pr_line`单据体
```java
package digi.alm.entity;

import digi.alm.utils.db.annotation.Entity;
import digi.alm.utils.db.annotation.Entry;
import digi.alm.utils.db.annotation.Field;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * </p>
 * 采购申请单
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/04/14 19:43
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString
@Entity("digi_pr")
public class Pr extends TemplateEntity implements Serializable {

    private static final long serialVersionUID = -7059628468835561723L;
    /**
     * 名称
     */
    @Field("digi_name")
    private String name;
    /**
     * 名称
     */
    @Field("billno")
    private String number;
    /**
     * 申请人ID
     */
    @Field("digi_user_id.id")
    private String userId;
    /**
     * 申请部门ID
     */
    @Field("digi_org_id.id")
    private String orgId;
    /**
     * 申请时间
     */
    @Field
    private Date requestTime;
    /**
     * 采购申请类型
     */
    @Field("digi_pr_type_id.id")
    private String prTypeId;
    /**
     * 处理状态
     */
    @Field
    private String statusPro;
    /**
     * 币别
     */
    @Field("digi_currency_id.id")
    private String currencyId;
    /**
     * 金额
     */
    @Field("digi_total_amount")
    private BigDecimal totalAmount;
    /**
     * 需求组织类型
     */
    @Field("digi_org_type")
    private String orgType;
    /**
     * 需求组织ID
     */
    @Field("digi_demand_org_id.id")
    private String demandOrgId;
    /**
     * 收货位置
     */
    @Field("digi_location.id")
    private String locationId;
    /**
     * 建议供应商
     */
    @Field("digi_supplier_id.id")
    private String supplierId;
    /**
     * 说明
     */
    @Field("digi_description")
    private String description;


    /**
     * 采购申请行
     */
    @Entry("digi_pr_line")
    private List<PrLine> prLineList;

}

```
**注:单据体也是一个独立的数据表因此它也有对应的实体类**
`@Entity("digi_pr.digi_pr_line")`:表示`digi.alm.entity.PrLine`实体类对应着`digi_pr.digi_pr_line`单据体
`@PrimaryKey`:表示`id`实体类对应着`digi_pr.digi_pr_line`单据体的主键
```java
package digi.alm.entity;

import digi.alm.utils.db.annotation.Entity;
import digi.alm.utils.db.annotation.Field;
import digi.alm.utils.db.annotation.PrimaryKey;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * </p>
 * 申请行
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/04/14 19:49
 */
@Data
@ToString
@Entity("digi_pr.digi_pr_line")
public class PrLine implements Serializable {

    private static final long serialVersionUID = 5061854841430031140L;
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
     * 名称
     */
    @Field("digi_asset_name")
    private String assetName;
    /**
     * 品牌/厂商
     */
    @Field("digi_asset_brand")
    private String assetBrand;

    /**
     * 规格型号
     */
    @Field("digi_asset_model")
    private String assetModel;
    /**
     * 数量
     */
    @Field("digi_qty")
    private BigDecimal qty;
    /**
     * 计量单位ID
     */
    @Field("digi_uom_id.id")
    private String uomId;

    /**
     * 单价（含税）
     */
    @Field("digi_tax_unit_price")
    private BigDecimal taxUnitPrice;
    /**
     * 汇率ID
     */
    @Field("digi_tax_rat.id")
    private String taxRatId;
    /**
     * 单价（不含税）
     */
    @Field("digi_unit_price")
    private BigDecimal unitPrice;
    /**
     * 税额
     */
    @Field("digi_tax_amount")
    private BigDecimal taxAmount;
    /**
     * 金额（不含税）
     */
    @Field("digi_notax_amount")
    private BigDecimal noTaxAmount;
    /**
     * 金额（含税）
     */
    @Field("digi_totalamount")
    private BigDecimal totalAmount;
    /**
     * 需求组织类型
     */
    @Field("digi_org_type_l")
    private String orgType;

    /**
     * 需求组织
     */
    @Field("digi_demand_org_id_l.id")
    private String demandOrgId;
    /**
     * 收货位置
     */
    @Field("location_id.id")
    private String locationId;
    /**
     * 建议供应商
     */
    @Field("digi_supplier_id_l.id")
    private String supplierId;
    /**
     * 处理状态
     */
    @Field("digi_line_status_pro")
    private String lineStatusPro;
    /**
     * 说明
     */
    @Field("digi_description_l")
    private String description;
}

```
## 2.2 资源库(Repository)与服务层(BaseService)继承ORM基础接口
以`digi.alm.entity.Pr`实体类对应的资源库(`digi.alm.repository.PrRepository`)和服务层`digi.alm.service.PrService`为例.

1. 资源库:`digi.alm.repository.PrRepository`继承`digi.alm.utils.db.BaseRepository<T>` 其中T为实体类`digi.alm.entity.Pr`
```java
package digi.alm.repository;

import digi.alm.controller.dto.pr.PrDetailOneResultDTO;
import digi.alm.entity.Pr;
import digi.alm.utils.db.BaseRepository;

import java.util.Optional;

/**
 * </p>
 * 采购申请单资源库
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/04/15 9:32
 */
public interface PrRepository extends BaseRepository<Pr> {
    /**
     * 新增或修改
     *
     * @param pr 数据
     * @return 主键
     */
    String saveOrUpdate(Pr pr);

    /**
     * 查询详情
     *
     * @param id 主键
     * @return 详情
     */
    Optional<PrDetailOneResultDTO> getPrDetailOneById(String id);
}

```

2. 服务层:`digi.alm.service.PrService`继承`digi.alm.utils.db.BaseService<T>` 其中T为实体类`digi.alm.entity.Pr`
```java
package digi.alm.service;

import digi.alm.controller.dto.pr.PrDetailOneResultDTO;
import digi.alm.entity.Pr;
import digi.alm.utils.db.BaseService;
import digi.alm.utils.db.BaseServiceImpl;

import java.util.Optional;

/**
 * </p>
 * 采购申请单服务层
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/04/15 9:28
 */
public interface PrService  extends BaseService<Pr> {
    Optional<PrDetailOneResultDTO> saveOrUpdate(Pr pr);
}

```
## 2.3 资源库实现(RepositoryImpl)与服务层实现(BaseServiceImpl)继承ORM基础接口的默认实现
以`digi.alm.entity.Pr`实体类对应的资源库实现(`digi.alm.repository.impl.PrRepositoryImpl`)和服务层实现`digi.alm.service.PrServiceImpl`为例

1. 资源库实现:`digi.alm.repository.impl.PrRepositoryImpl`实现`digi.alm.repository.PrRepository`接口并继承`digi.alm.utils.db.BaseRepositoryImpl<T>`其中T为实体类`digi.alm.entity.Pr`
```java
package digi.alm.repository.impl;

import digi.alm.constants.EntityConstants;
import digi.alm.controller.dto.pr.PrDetailOneResultDTO;
import digi.alm.entity.Pr;
import digi.alm.entity.PrLine;
import digi.alm.repository.PrRepository;
import digi.alm.service.impl.DemandApplyServiceImpl;
import digi.alm.utils.AlmBusinessDataServiceHelper;
import digi.alm.utils.ResponseUtils;
import digi.alm.utils.db.BaseRepositoryImpl;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.id.ID;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * </p>
 * 采购申请单资源库实现
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/04/15 9:32
 */
@Repository
public class PrRepositoryImpl extends BaseRepositoryImpl<Pr> implements PrRepository {
    private final String className = this.getClass().getName();

    @Override
    public String saveOrUpdate(Pr pr) {
        final DynamicObject dynamicObject = toDynamicObject(pr);
        final OperationResult operationResult = SaveServiceHelper.saveOperate(EntityConstants.PR, new DynamicObject[]{dynamicObject}, OperateOption.create());
        // 捕获异常
        ResponseUtils.handleOperationResult(operationResult);
        return dynamicObject.getString("id");
    }

    @Override
    public Optional<PrDetailOneResultDTO> getPrDetailOneById(String id) {
        try {
            final DynamicObject prDo = AlmBusinessDataServiceHelper.loadSingle(id, EntityConstants.PR);
            return Optional.of(DemandApplyServiceImpl.doToPrDetailOneResultDTO(prDo));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private DynamicObject toDynamicObject(Pr pr) {
        // 是新增
        DynamicObject dynamicObject;
        if (StringUtils.isBlank(pr.getId())) {
            // 主键不存在
            dynamicObject = AlmBusinessDataServiceHelper.newDynamicObject(EntityConstants.PR);
            // 采购申请单头关系映射
            dynamicObject.set("id", Objects.toString(ID.genLongId()));
            dynamicObject.set("creator", RequestContext.get().getUserId());
            dynamicObject.set("createtime", new Date());

        } else {
            final Optional<DynamicObject> optional = AlmBusinessDataServiceHelper.loadSingleOptional(pr.getId(), EntityConstants.PR);
            dynamicObject = optional.orElse(AlmBusinessDataServiceHelper.newDynamicObject(EntityConstants.PR));
            dynamicObject.set("id", pr.getId());
        }
        dynamicObject.set("billno", pr.getNumber());
        dynamicObject.set("digi_name", pr.getName());
        dynamicObject.set("digi_user_id", pr.getUserId());
        dynamicObject.set("digi_org_id", pr.getOrgId());
        dynamicObject.set("digi_request_time", pr.getRequestTime());
        dynamicObject.set("digi_pr_type_id", pr.getPrTypeId());
        dynamicObject.set("billstatus", pr.getStatus());
        dynamicObject.set("digi_status_pro", pr.getStatusPro());
        dynamicObject.set("digi_currency_id", pr.getCurrencyId());
        dynamicObject.set("digi_total_amount", pr.getTotalAmount());
        dynamicObject.set("digi_org_type", pr.getOrgType());
        dynamicObject.set("digi_demand_org_id", pr.getDemandOrgId());
        dynamicObject.set("digi_location", pr.getLocationId());
        dynamicObject.set("digi_supplier_id", pr.getSupplierId());
        dynamicObject.set("digi_description", pr.getDescription());
        dynamicObject.set("modifier", RequestContext.get().getUserId());
        dynamicObject.set("modifytime", new Date());

        // 采购申请单行关系映射
        if (CollectionUtils.isNotEmpty(pr.getPrLineList())) {
            // 单据体
            final DynamicObjectCollection lineDoc = dynamicObject.getDynamicObjectCollection("digi_pr_line");
            final Map<String, DynamicObject> lineDocMap = lineDoc.stream().collect(Collectors.toMap(it -> it.getString("id"), it -> it));
            for (PrLine prLine : pr.getPrLineList()) {
                final String prLineId = prLine.getId();
                DynamicObject lineDo;
                if (StringUtils.isBlank(prLineId)) {
                    lineDo = lineDoc.addNew();
                    lineDo.set("id", ID.genLongId());
                } else {
                    lineDo = lineDocMap.get(prLineId);
                    if (lineDo == null) {
                        lineDo = lineDoc.addNew();
                        lineDo.set("id", prLineId);
                    }
                }
                // 设置值
                lineDo.set("digi_line_type", prLine.getLineTypeId());
                lineDo.set("digi_asset_class", prLine.getAssetClassId());
                lineDo.set("digi_item", prLine.getItemId());
                lineDo.set("digi_asset_name", prLine.getAssetName());
                lineDo.set("digi_asset_brand", prLine.getAssetBrand());
                lineDo.set("digi_asset_model", prLine.getAssetModel());
                lineDo.set("digi_qty", prLine.getQty());
                lineDo.set("digi_uom_id", prLine.getUomId());
                lineDo.set("digi_tax_unit_price", prLine.getTaxUnitPrice());
                lineDo.set("digi_tax_rat", prLine.getTaxRatId());
                lineDo.set("digi_unit_price", prLine.getUnitPrice());
                lineDo.set("digi_tax_amount", prLine.getTaxAmount());
                lineDo.set("digi_notax_amount", prLine.getNoTaxAmount());
                lineDo.set("digi_totalamount", prLine.getTotalAmount());
                lineDo.set("digi_org_type_l", prLine.getOrgType());
                lineDo.set("digi_demand_org_id_l", prLine.getDemandOrgId());
                lineDo.set("location_id", prLine.getLocationId());
                lineDo.set("digi_supplier_id_l", prLine.getSupplierId());
                lineDo.set("digi_line_status_pro", prLine.getLineStatusPro());
                lineDo.set("digi_description_l", prLine.getDescription());
            }
        }

        return dynamicObject;
    }
}

```

2. 服务层实现:`digi.alm.service.impl.PrServiceImpl`实现`digi.alm.service.PrService`接口继承`digi.alm.utils.db.BaseServiceImpl<T>`其中T为实体类`digi.alm.entity.Pr`
```java
package digi.alm.service.impl;

import digi.alm.controller.dto.pr.PrDetailOneResultDTO;
import digi.alm.entity.Pr;
import digi.alm.repository.PrRepository;
import digi.alm.service.PrService;
import digi.alm.utils.db.BaseServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * </p>
 * 采购申请单服务层实现
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/04/15 9:28
 */
@Service
public class PrServiceImpl extends BaseServiceImpl<Pr> implements PrService {
    @Autowired
    private PrRepository prRepository;

    @Override
    public Optional<PrDetailOneResultDTO> saveOrUpdate(Pr pr) {
        // 新增或修改数据
        String id = prRepository.saveOrUpdate(pr);
        return prRepository.getPrDetailOneById(id);
    }
}

```
# 三 使用ORM
## 3.1 查询
### 3.1.1 根据指定的字段查询:`List<T> select(String fieldName, Object value)`
示例:
```java
// 等价于SELECT A.FId "id" FROM tk_digi_pr A WHERE A.fbillno = 'PR-000141'
// SELECT T1.fk_digi_status_pro,T1.fk_digi_pr_type_id,T1.fk_digi_description,T1.fk_digi_supplier_id,T1.fk_digi_location,T1.fk_digi_demand_org_id,T1.fk_digi_org_type,T1.fk_digi_total_amount,T1.fk_digi_currency_id,T1.fk_digi_datetimefield,T1.fk_digi_org_id,T1.fk_digi_user_id,T1.fk_digi_name,T1.fbillno,T1.FId FROM tk_digi_pr T1 WHERE  T1.FId= ? 
final List<Pr> list = prRepository.select("number", "PR-000141");

```
### 3.1.2 根据指定的字段查询并指定返回类泛型`R`: `<R> List<R> select(String key, Object value, Class<R> resultClass)`
示例:
`digi.alm.controller.dto.pr.PrDetailOneResultDTO`继承于`digi.alm.entity.Pr`因此在ORM中会继承`digi.alm.entity.Pr`中的`@Entity("digi_pr")`注解

1. `@Field(value = "digi_user_id.name", isDBField = false)`表示`userName`字段是非当前数据表字段,目前在查询基础资料非主键字段可用,通过`digi_user_id.xxx`(其中`xxx`为基础资料字段标识)查询到该基础资料中`xxx`字段的数据
1. `@Entry(value = "digi_pr_line", mapClass = PrLineDetailOneResultDTO.class)`:表示`prLineList`字段对应`digi_pr`表单中`digi_pr_line`单据体,并且将`List<PrLine> prLineList`中的`PrLine`最终映射为`PrLineDetailOneResultDTO`类(注意:`mapClass`所指向类型必须是List<T> 中T的子类或同类)
```java
package digi.alm.controller.dto.pr;

import digi.alm.entity.Pr;
import digi.alm.entity.PrLine;
import digi.alm.utils.db.annotation.Entry;
import digi.alm.utils.db.annotation.Field;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

/**
 * </p>
 *
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/04/14 19:43
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class PrDetailOneResultDTO extends Pr {

    private static final long serialVersionUID = 532906140578609107L;
    /**
     * 申请人名称
     */
    @Field(value = "digi_user_id.name", isDBField = false)
    private String userName;
    /**
     * 申请部门名称
     */
    @Field(value = "digi_org_id.name", isDBField = false)
    private String orgName;
    /**
     * 单据状态名称
     */
    private Object statusName;
    /**
     * 采购申请类型名称
     */
    @Field(value = "digi_pr_type_id.name", isDBField = false)
    private String prTypeName;
    /**
     * 处理状态名称
     */
    private Object statusProName;
    /**
     * 币别名称
     */
    @Field(value = "digi_currency_id.name", isDBField = false)
    private String currencyName;
    /**
     * 需求组织类型名称
     */
    private Object orgTypeName;
    /**
     * 需求组织名称
     */
    @Field(value = "digi_demand_org_id.name", isDBField = false)
    private String demandOrgName;
    /**
     * 收货位置名称
     */
    @Field(value = "digi_location.name", isDBField = false)
    private String locationName;
    /**
     * 建议供应商名称
     */
    @Field(value = "digi_supplier_id.name", isDBField = false)
    private String supplierName;
    /**
     * 采购申请行
     * 覆盖
     */
    @Entry(value = "digi_pr_line", mapClass = PrLineDetailOneResultDTO.class)
    private List<PrLine> prLineList;

}

```
`**PrLineDetailOneResultDTO**`**Class:**
**由于**`PrLineDetailOneResultDTO`继承
```java
package digi.alm.controller.dto.pr;

import digi.alm.entity.PrLine;
import digi.alm.utils.db.annotation.Field;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.io.Serializable;

/**
 * </p>
 * 申请行详情
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/04/14 20:07
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ToString
public class PrLineDetailOneResultDTO extends PrLine implements Serializable {

    private static final long serialVersionUID = -1685892508747794760L;
    /**
     * 行类型
     */
    @Field("digi_line_type.name")
    private String lineTypeName;
    /**
     * 资产类别
     */
    @Field("digi_asset_class.name")
    private String assetClassName;
    /**
     * 物料
     */
    @Field("digi_item.name")
    private String itemName;
    /**
     * 计量单位名称
     */
    private String uomName;
    /**
     * 税率名称
     */
    private String taxRatName;
    /**
     * 需求组织类型名称
     */
    private Object orgTypeName;
    /**
     * 需求组织名称
     */
    private String demandOrgName;
    /**
     * 收货位置名称
     */
    private String locationName;
    /**
     * 建议供应商名称
     */
    private String supplierName;
    /**
     * 处理状态名称
     */
    private Object lineStatusProName;


}

```
### 3.1.3 统计满足条件的条数(NOTE:生成的查询条件字段之间是AND关系):`int selectCount(T record)`
`record`可为`T`本类以及其子类
示例:
查询状态为`APPROVED`的数据条数
```java
final PrDetailOneResultDTO prDetailOneResultDTO1 = new PrDetailOneResultDTO();
prDetailOneResultDTO1.setStatusPro("APPROVED");
// 
final int i = prRepository.selectCount(prDetailOneResultDTO1);
```
### 3.1.4 查询单条数据(NOTE:生成的查询条件字段之间是AND关系):`Optional_<_T_> _selectOne_(_T record_)_`
`record`可为`T`本类以及其子类
示例:

1. 查询编号为`1`的采购申请单数据
```java
final Pr pr = new Pr();
pr.setNumber("1");
final Optional<Pr> pr1 = prRepository.selectOne(pr)

```

2. 查询需求组织为`无边界`的数据
```java
// 使用子类进行查询
final PrDetailOneResultDTO pr = new PrDetailOneResultDTO();
pr.setDemandOrgName("无边界");
final Optional<Pr> pr1 = prRepository.selectOne(pr);
```
### 3.1.5 查询满足条件的列表(NOTE:生成的查询条件字段之间是AND关系):`List<T> select(T record)`
`record`可为`T`本类以及其子类
示例:

1. 查询币别ID为`1`的数据并且返回的数据中不包含其他关联表数据(即只有该表本身的数据),因此`record`需要使用`Pr`类(该类为当前表的实体)
```java
final Pr pr = new PrDetailOneResultDTO();
pr.setCurrencyId("1");
final List<Pr> select = prRepository.select(pr);
```

2. 查询币别ID为`1`的数据并且返回的数据中包含其他关联表数据,因此`record`需要使用`Pr`类中使用了`@Field`注解关联其他表字段的子类`PrDetailOneResultDTO`
```java
final PrDetailOneResultDTO pr = new PrDetailOneResultDTO();
pr.setCurrencyId("1");
final List<Pr> select = prRepository.select(pr);
```
### 3.1.6 查询当前表中全部数据:`List<T> selectAll()`
示例:
查询出当前表中所有数据(不包含关联表中的字段数据)
```java
final List<Pr> select = prRepository.selectAll();
```
### 3.1.7 查询当前表以及关联表中全部数据(NOTE:关联系统需要在{@code resultClass}中定义):`<R> List<R> selectAll(Class<R> resultClas s)`
`resultClass`:可为任意添加了`@Entity`以及`@Field`注解(`其中@ENtr`可选)的类或其的子类
示例:

1. `Pr`使用了`@Entity`以及`@Field`注解,因此满足条件
```java
final List<Pr> select = prRepository.selectAll(Pr.class);
// 此时这两种方式等价
final List<Pr> select = prRepository.selectAll();
```

2. `PrDetailOneResultDTO`是`Pr`的子类,`Pr`添加了`@Entity`以及`@Field`注解,因此可以使用`PrDetailOneResultDTO`
```java
final List<PrDetailOneResultDTO> select = prRepository.selectAll(PrDetailOneResultDTO.class);
```
### 3.1.8 查询列表:`<R> List<R> select(List<QFilter> qFilters, Class<R> resultClass)`
`qFilters`:查询条件
`resultClass`:可为任意添加了`@Entity`以及`@Field`注解(`其中@Entry`可选)的类或其的子类
示例:

1. 模糊查询当前数据表中`名称(digi_name)`包含`陈`的全部数据(不包含关联表字段的数据)
```java
final List<QFilter> qFilters = new ArrayList<>();
qFilters.add(QFilter.ftlike("陈", "digi_name"));
final List<Pr> select = prRepository.select(qFilters,Pr.class);
```

2. 模糊查询当前数据表中`名称(digi_name)`包含`陈`的全部数据(包含关联表字段的数据)
```java
final List<QFilter> qFilters = new ArrayList<>();
qFilters.add(QFilter.ftlike("陈", "digi_name"));
final List<PrDetailOneResultDTO> select = prRepository.select(qFilters,PrDetailOneResultDTO.class);
```
### 3.1.9 生成查询条件(NOTE:生成的查询条件字段之间是AND关系):`<R> List<QFilter> genQFilter(R record)`
`record`:可为任意添加了`@Entity`以及`@Field`注解(`其中@ENtr`可选)的类或其的子类
示例:

1. 生成查询币别ID为`1`数据查询条件
```java
final Pr pr = new Pr();
pr.setCurrencyId("1");
final List<QFilter> qFilters = prRepository.genQFilter(pr);
```
## 3.2 删除
### 3.2.1 删除:`int delete(T record)`
`record`:可为任意添加了`@Entity`以及`@Field`注解(`其中@ENtr`可选)的类或其的子类
示例:

1. 删除编号为`1`的数据
```java
final Pr pr = new Pr();
pr.setNumber("1");
final int delete = prRepository.delete(pr);
```


