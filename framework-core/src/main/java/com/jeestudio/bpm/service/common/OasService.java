package com.jeestudio.bpm.service.common;

import com.jeestudio.bpm.common.entity.common.Zform;
import com.jeestudio.bpm.utils.ResultJson;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: OA公共业务服务
 */
@Service
public class OasService extends ZformService {

  @Transactional(readOnly = false)
  public ResultJson saveZform(Zform zform,
                              String loginName,
                              String businessKey) throws Exception {
    ResultJson resultJson = super.saveZform(zform, loginName, businessKey);
    if ("oas_vehicle_apply".equalsIgnoreCase(zform.getFormNo())) {
      //oas_vehicle_apply 级联更新车辆状态
      Zform applyZform = this.get(zform.getId(), genTableService.getGenTableWithDefination(zform.getFormNo()));
      String status = applyZform.getStatus();
      if ("99".equals(status) || "00".equals(status)) {
        status = "10"; //终止或归还之后车辆借出状态是空闲10
      }
      // 使用参数化查询，避免 SQL 注入
      String sql = "update oas_vehicle set lending_status = #{param.status} where id = #{param.vehicleId}";
      Map<String, Object> param = new HashMap<>();
      param.put("status", status);
      param.put("vehicleId", applyZform.getG01().getId());
      zformDao.updateSqlParm(sql, param);
    }

    return resultJson;
  }
}
