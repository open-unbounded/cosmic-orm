package kd.alm.controller.v1;


import com.alibaba.fastjson.JSONObject;
import kd.alm.orm.test.*;
import kd.bos.algo.DataSet;
import kd.bos.bill.IBillWebApiPlugin;
import kd.bos.entity.api.ApiResult;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;

import java.util.List;
import java.util.Map;

/**
 * </p>
 * 陈权-测试接口-勿用
 * <p>
 *
 * @author chenquan chenquan@osai.club 2020/08/20 15:27
 */
public class CqTestApi implements IBillWebApiPlugin {
    final DemandApplyRepositoryTestImplTest demandApplyRepositoryTestImplTest = new DemandApplyRepositoryTestImplTest();

    final TestOrgRepository testOrgRepository = new TestOrgRepositoryImpl();
    @Override
    public ApiResult doCustomService(Map<String, Object> params) {
        final List<DemandApplyTest> select = demandApplyRepositoryTestImplTest.select(new DemandApplyTest());
        final List<TestOrg> testOrgs = testOrgRepository.selectAll();
        return ApiResult.success(JSONObject.toJSON(testOrgs));

    }
}
