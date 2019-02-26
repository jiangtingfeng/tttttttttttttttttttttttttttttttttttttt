package com.jgw.supercodeplatform.trace.service.tracebatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jgw.supercodeplatform.exception.SuperCodeException;
import com.jgw.supercodeplatform.pojo.cache.AccountCache;
import com.jgw.supercodeplatform.trace.common.model.RestResult;
import com.jgw.supercodeplatform.trace.common.model.ReturnParamsMap;
import com.jgw.supercodeplatform.trace.common.util.CommonUtil;
import com.jgw.supercodeplatform.trace.common.util.RestTemplateUtil;
import com.jgw.supercodeplatform.trace.dao.mapper1.batchinfo.TraceBatchInfoMapper;
import com.jgw.supercodeplatform.trace.dao.mapper1.template.TraceFuntemplateStatisticalMapper;
import com.jgw.supercodeplatform.trace.exception.SuperCodeTraceException;
import com.jgw.supercodeplatform.trace.pojo.template.TraceFuntemplateStatistical;
import com.jgw.supercodeplatform.trace.pojo.tracebatch.ReturnTraceBatchInfo;
import com.jgw.supercodeplatform.trace.pojo.tracebatch.TraceBatchInfo;
import com.jgw.supercodeplatform.trace.service.template.TraceFunTemplateconfigService;

/**
 * 溯源批次service
 *
 * @author liujianqiang
 * @date 2018年12月12日
 */
@Service
public class TraceBatchInfoService extends CommonUtil {

    @Autowired
    private TraceBatchInfoMapper traceBatchInfoMapper;
    @Autowired
    private TraceFuntemplateStatisticalMapper traceFuntemplateStatisticalMapper;
    @Autowired
    private RestTemplateUtil restTemplateUtil;
    @Autowired
    private TraceFunTemplateconfigService traceFunTemplateconfigService;
    @Autowired
    private CommonUtil commonUtil;

    @Value("${rest.user.url}")
    private String restUserUrl;

    /**
     * 新增溯源批次记录,并将溯源模板统计表的批次数量为原数量加1
     *
     * @param traceBatchInfo
     * @throws Exception
     * @author liujianqiang
     * @data 2018年12月12日
     */
    @Transactional(rollbackFor = Exception.class)
    public String insertTraceBatchInfo(TraceBatchInfo traceBatchInfo) throws Exception {
        //新增溯源批次记录
        AccountCache userAccount = getUserLoginCache();
        String traceBatchInfoId = getUUID();
        String organizationId = getOrganizationId();
        traceBatchInfo.setTraceBatchInfoId(traceBatchInfoId);
        traceBatchInfo.setOrganizationId(organizationId);//组织id
        traceBatchInfo.setCreateId(userAccount.getUserId());
        traceBatchInfo.setNodeDataCount(0);
        traceBatchInfo.setCreateMan(userAccount.getUserName());
        String traceBatchName=traceBatchInfo.getTraceBatchName();
        checkBatchIdAndBatchName(traceBatchInfo.getTraceBatchId(), traceBatchName, organizationId, traceBatchInfoId);
        traceBatchInfo.setTraceBatchName(traceBatchName.replaceAll(" ", ""));
        Integer record = traceBatchInfoMapper.insertTraceBatchInfo(traceBatchInfo);
        if (record != 1) {
            throw new SuperCodeTraceException("新增溯源批次记录失败");
        }
        //修改模板统计表的批次数量为原数量+1
        TraceFuntemplateStatistical traceFuntemplateStatistical = traceFuntemplateStatisticalMapper.selectByTemplateId(traceBatchInfo.getTraceTemplateId());
        if (traceFuntemplateStatistical == null) {
            throw new SuperCodeTraceException("通过溯源模板没有查询出模板记录");
        }
        TraceFuntemplateStatistical traceFuntemplateStatisticalTem = new TraceFuntemplateStatistical();
        traceFuntemplateStatisticalTem.setTraceTemplateId(traceBatchInfo.getTraceTemplateId());
        traceFuntemplateStatisticalTem.setBatchCount(traceFuntemplateStatistical.getBatchCount() + 1);
        Integer updateRecord = traceFuntemplateStatisticalMapper.update(traceFuntemplateStatisticalTem);//修改批次数量为原批次数量加1
        if (updateRecord != 1) {
            throw new SuperCodeTraceException("修改溯源统计表批次数量失败");
        }
        return traceBatchInfoId;
    }

    /**
     * 修改溯源批次
     *
     * @param traceBatchInfo
     * @throws SuperCodeException
     * @throws Exception
     * @author liujianqiang
     * @data 2018年12月18日
     */
    public void updateTraceBatchInfo(TraceBatchInfo traceBatchInfo) throws Exception {
    	
    	String traceBatchName=traceBatchInfo.getTraceBatchName();
        checkBatchIdAndBatchName(traceBatchInfo.getTraceBatchId(), traceBatchName, getOrganizationId(), traceBatchInfo.getTraceBatchInfoId());
        traceBatchInfo.setTraceBatchName(traceBatchName.replaceAll(" ", ""));
        Integer record = traceBatchInfoMapper.updateTraceBatchInfo(traceBatchInfo);
        if (record != 1) {
            throw new SuperCodeTraceException("修改溯源批次记录失败");
        }
    }

    /**
     * 删除批次信息,并将模板统计表的批次数量减1
     *
     * @param traceBatchInfoId
     * @param traceTemplateId
     * @throws SuperCodeTraceException
     * @author liujianqiang
     * @data 2018年12月21日
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTraceBatchInfo(String traceBatchInfoId, String traceTemplateId) throws SuperCodeTraceException {
        Integer record = traceBatchInfoMapper.deleteTraceBatchInfo(traceBatchInfoId);//删除溯源批次信息
        if (record != 1) {
            throw new SuperCodeTraceException("删除溯源批次数据失败");
        }
        //修改溯源模板统计表的批次数据为原批次数据减1
        TraceFuntemplateStatistical traceFuntemplateStatistical = traceFuntemplateStatisticalMapper.selectByTemplateId(traceTemplateId);
        if (traceFuntemplateStatistical == null) {
            throw new SuperCodeTraceException("通过溯源模板没有查询出模板记录");
        }
        TraceFuntemplateStatistical traceFuntemplateStatisticalTem = new TraceFuntemplateStatistical();
        Integer batchCount = traceFuntemplateStatistical.getBatchCount();
        if (batchCount <= 0) {
            throw new SuperCodeTraceException("该模板的批次数量小于等于0,不允许删除");
        }
        traceFuntemplateStatisticalTem.setBatchCount(batchCount - 1);
        traceFuntemplateStatisticalTem.setTraceTemplateId(traceTemplateId);
        Integer updateRecord = traceFuntemplateStatisticalMapper.update(traceFuntemplateStatisticalTem);//修改批次数量为原批次数量-1
        if (updateRecord != 1) {
            throw new SuperCodeTraceException("修改溯源统计表批次数量失败");
        }
    }

    public void deleteTraceBatch(String traceBatchInfoId, String traceTemplateId) throws Exception
    {
        RestResult<List<Map<String, Object>>> result= listBusinessNodeData(traceBatchInfoId);
        List<Map<String, Object>> nodeList =result.getResults();
        boolean delete=true;
        for(Map<String, Object> node:nodeList){
            if (Integer.parseInt(node.get("businessType").toString()) == 1){
                delete=false;
            }
        }
        if(!delete){
            throw  new SuperCodeTraceException("该批次在生产中已被使用（定制功能中使用），不能删除");
        }

        deleteTraceBatchInfo(traceBatchInfoId,traceTemplateId);
    }

    /**
     * 根据条件获取分页溯源批次信息
     *
     * @param map
     * @return
     * @throws SuperCodeTraceException
     * @throws SuperCodeException
     * @author liujianqiang
     * @data 2018年12月19日
     */
    public Map<String, Object> listTraceBatchInfoByOrgPage(Map<String, Object> map) throws Exception {
        map.put("organizationId", getOrganizationId());
        Integer total = traceBatchInfoMapper.getCountByCondition(map);//获取总记录数
        ReturnParamsMap returnParamsMap = getPageAndRetuanMap(map, total);
        List<ReturnTraceBatchInfo> traceBatchInfoList = traceBatchInfoMapper.getTraceBatchInfo(returnParamsMap.getParamsMap());
        Map<String, Object> dataMap = returnParamsMap.getReturnMap();

        StringBuilder idsBuilder = new StringBuilder();
        for (ReturnTraceBatchInfo returnTraceBatchInfo : traceBatchInfoList) {
            idsBuilder.append(returnTraceBatchInfo.getTraceTemplateId()).append(",");
        }
        JsonNode node;
        if (idsBuilder.length() > 0) {
            String ids = idsBuilder.substring(0, idsBuilder.length() - 1);
            node = getAddressUrl(ids);
            dataMap.put("addressWithTemplate", node);
        }else {
            dataMap.put("addressWithTemplate", new ArrayList<>());
        }
        return getRetunMap(dataMap, traceBatchInfoList);
    }

    /**
     * 获取追溯模板与h5页面对应关系记录
     *
     * @param traceBatchInfoList
     * @return
     */
    public JsonNode getAddressUrl(String ids) {
        if (StringUtils.isNotBlank(ids)) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("traceTemplateIds", ids);
            Map<String, String> headerMap = new HashMap<String, String>();
            try {
                headerMap.put("super-token", getSuperToken());
                ResponseEntity<String> rest = restTemplateUtil.getRequestAndReturnJosn(restUserUrl + "/platform/h5/Ids", params, headerMap);
                if (rest.getStatusCode().value() == 200) {
                    String body = rest.getBody();
                    JsonNode node = new ObjectMapper().readTree(body);
                    if (200 == node.get("state").asInt()) {
                        return node.get("results");
                    }
                }
            } catch (SuperCodeTraceException | IOException | SuperCodeException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 获取组织下的所有溯源批次信息
     *
     * @param map
     * @return
     * @throws SuperCodeTraceException
     * @throws SuperCodeException
     * @author liujianqiang
     * @data 2018年12月19日
     */
    public List<ReturnTraceBatchInfo> listTraceBatchInfo() throws SuperCodeException {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("organizationId", getOrganizationId());
        return traceBatchInfoMapper.getTraceBatchInfo(map);
    }

    //验证该组织下的批次号和批次名称
    private void checkBatchIdAndBatchName(String traceBatchId, String traceBatchName, String organizationId, String traceBatchInfoId) throws SuperCodeTraceException {
        checkCountByOrgAndBatchId(traceBatchId, organizationId, traceBatchInfoId);
        checkCountByOrgAndBatchName(traceBatchName, organizationId, traceBatchInfoId);
    }

    //验证该组织下的溯源批次号是否存在
    private void checkCountByOrgAndBatchId(String traceBatchId, String organizationId, String traceBatchInfoId) throws SuperCodeTraceException {
        if ("".equals(traceBatchId)) {//假如批次号为空,不需要验证
        } else {
            Integer record = traceBatchInfoMapper.getCountByOrgAndBatchId(traceBatchId, organizationId, traceBatchInfoId);
            if (record > 0) {
                throw new SuperCodeTraceException("该组织下,该溯源批次号已经存在");
            }
        }
    }

    //验证该组织下的溯源批次名称是否存在,已经名称是否为空验证
    private void checkCountByOrgAndBatchName(String traceBatchName, String organizationId, String traceBatchInfoId) throws SuperCodeTraceException {
        if (traceBatchName == null || "".equals(traceBatchName) || traceBatchName.isEmpty()) {
            throw new SuperCodeTraceException("批次名称不能为空");
        }
        String nospacetraceBatchName=traceBatchName.replaceAll(" ", "");
        Integer record = traceBatchInfoMapper.getCountByOrgAndBatchName(nospacetraceBatchName, organizationId, traceBatchInfoId);
        if (record > 0) {
            throw new SuperCodeTraceException("该组织下,该溯源批次名称已经存在");
        }
    }

    /**
     * 查询溯源记录
     *
     * @param traceBatchInfoId
     * @return
     * @throws Exception
     */
    public RestResult<List<Map<String, Object>>> listBusinessNodeData(String traceBatchInfoId) throws Exception {
        TraceBatchInfo traceBatchInfo = traceBatchInfoMapper.selectByTraceBatchInfoId(traceBatchInfoId);
        if (null == traceBatchInfo) {
            throw new SuperCodeTraceException("无此批次号记录", 500);
        }
        String orgnizationId = commonUtil.getOrganizationId();
        return traceFunTemplateconfigService.queryNodeInfo(traceBatchInfo.getTraceBatchInfoId(), traceBatchInfo.getTraceTemplateId(), false, orgnizationId);
    }

    public TraceBatchInfo getOneByUnkonwnOneField(String plainSql) {

        return traceBatchInfoMapper.getOneByUnkonwnOneField(plainSql);
    }

    public TraceBatchInfo selectByTraceBatchInfoId(String traceBatchInfoId) {
        return traceBatchInfoMapper.selectByTraceBatchInfoId(traceBatchInfoId);
    }

    /**
     * h5页面数据查询接口
     *
     * @param traceBatchInfoId
     * @return
     * @throws Exception
     */
    public RestResult<Map<String, Object>> h5PageData(String traceBatchInfoId) throws Exception {
        RestResult<Map<String, Object>> backResult = new RestResult<Map<String, Object>>();
        TraceBatchInfo traceBatchInfo = traceBatchInfoMapper.selectByTraceBatchInfoId(traceBatchInfoId);
        if (null == traceBatchInfo) {
            throw new SuperCodeTraceException("无此批次号记录", 500);
        }
        //h5查询不需要登陆 没有组织id
        RestResult<List<Map<String, Object>>> nodeDataResult = traceFunTemplateconfigService.queryNodeInfo(traceBatchInfo.getTraceBatchInfoId(), traceBatchInfo.getTraceTemplateId(), true, null);
        if (nodeDataResult.getState() != 200) {
            throw new SuperCodeTraceException(nodeDataResult.getMsg(), 500);
        }
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("productInfo", traceBatchInfo);
        dataMap.put("nodeInfo", nodeDataResult.getResults());
        backResult.setState(200);
        backResult.setResults(dataMap);
        return backResult;
    }
}
