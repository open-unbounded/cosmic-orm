package kd.alm.orm.util;

import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.serialization.DataEntitySerializer;
import kd.bos.dataentity.utils.OrmUtils;
import kd.bos.entity.EntityMetadataCache;
import kd.bos.entity.operate.Operations;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.service.operation.OperationService;
import kd.bos.service.operation.OperationServiceImpl;

/**
 * </p>
 * 操作工具类
 * <p>
 *
 * @author chenquan chenquan.dev@foxmail.com 2021/05/21 16:21
 */
public class AlmOperationServiceUtils {
    private static final OperationService INSTANCE = new OperationServiceImpl();

    /**
     * 保存操作
     *
     * @param entityNumber 表单标识
     * @param dataEntity   数据
     * @return 操作结果
     */
    public static OperationResult saveOperate(String entityNumber, DynamicObject dataEntity) {
        return saveOperate(entityNumber, new DynamicObject[]{dataEntity}, OperateOption.create());
    }

    /**
     * 保存操作
     *
     * @param entityNumber 表单标识
     * @param dataEntities 数据
     * @return 操作结果
     */
    public static OperationResult saveOperate(String entityNumber, DynamicObject[] dataEntities) {
        return saveOperate(entityNumber, dataEntities, OperateOption.create());
    }

    /**
     * 保存操作
     *
     * @param entityNumber 表单标识
     * @param dataEntities 数据
     * @param option       操作参数
     * @return 操作结果
     */
    public static OperationResult saveOperate(String entityNumber, DynamicObject[] dataEntities, OperateOption option) {
        Operations operations = EntityMetadataCache.getDataEntityOperations(entityNumber);
        return invokeOperation(operations.getSave() == null ? "save" : operations.getSave(), dataEntities, option);
    }

    /**
     * 直接操作
     *
     * @param operationKey 操作标识
     * @param entityNumber 表单标识
     * @param ids          主键列表
     * @param option       操作参数
     * @return 操作结果
     */
    public static OperationResult invokeOperation(String operationKey, String entityNumber, Object[] ids, OperateOption option) {
        final String str = INSTANCE.invokeOperation(operationKey, entityNumber, ids, option);
        return (OperationResult) DataEntitySerializer.deSerializerFromString(str, OrmUtils.getDataEntityType(OperationResult.class));
    }

    /**
     * 直接操作
     *
     * @param operationKey 操作标识
     * @param dataEntities 数据
     * @param option       操作参数
     * @return 操作结果
     */
    public static OperationResult invokeOperation(String operationKey, DynamicObject[] dataEntities, OperateOption option) {
        final String str = INSTANCE.invokeOperation(operationKey, dataEntities, option);
        return (OperationResult) DataEntitySerializer.deSerializerFromString(str, OrmUtils.getDataEntityType(OperationResult.class));

    }

}
