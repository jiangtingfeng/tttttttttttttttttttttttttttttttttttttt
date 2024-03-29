package com.jgw.supercodeplatform.trace.service.tracebatch;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import com.alibaba.fastjson.JSONObject;


import com.fasterxml.jackson.databind.node.NullNode;
import com.jgw.supercodeplatform.trace.common.model.Field;
import com.jgw.supercodeplatform.trace.common.model.page.AbstractPageService;
import com.jgw.supercodeplatform.trace.common.model.page.Page;
import com.jgw.supercodeplatform.trace.dao.mapper1.template.TraceFunTemplateconfigMapper;
import com.jgw.supercodeplatform.trace.dao.mapper1.tracefun.TraceObjectBatchInfoMapper;
import com.jgw.supercodeplatform.trace.dto.tracebatch.ProductBatchRelationSysView;
import com.jgw.supercodeplatform.trace.enums.BatchTableType;
import com.jgw.supercodeplatform.trace.pojo.tracefun.TraceBatchRelation;
import com.jgw.supercodeplatform.trace.pojo.tracefun.TraceObjectBatchInfo;
import com.jgw.supercodeplatform.trace.service.producttesting.ProductTestingService;
import com.jgw.supercodeplatform.trace.service.template.TraceFunFieldConfigDelegate;
import com.jgw.supercodeplatform.trace.service.tracefun.TraceBatchRelationService;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static Logger logger = LoggerFactory.getLogger(TraceBatchInfoService.class);

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

    @Autowired
    private TraceObjectBatchInfoMapper traceObjectBatchInfoMapper;

    @Autowired
    private TraceBatchRelationService traceBatchRelationService;

    @Autowired
    private ProductTestingService productTestingService;

    @Value("${rest.user.url}")
    private String restUserUrl;

    @Value("${trace.h5page.url}")
    private String h5PageUrl;


    @Autowired
    private TraceFunTemplateconfigMapper traceFunTemplateconfigMapper;


    private void setDefaultField(TraceBatchInfo traceBatchInfo) throws Exception {
        AccountCache userAccount = getUserLoginCache();
        String organizationId = getOrganizationId();

        traceBatchInfo.setSysId(getSysId());
        traceBatchInfo.setOrganizationId(organizationId);//组织id
        traceBatchInfo.setCreateId(userAccount.getUserId());
        if(traceBatchInfo.getNodeDataCount()==null){
            traceBatchInfo.setNodeDataCount(0);
        }
        traceBatchInfo.setCreateMan(userAccount.getUserName());
    }

    public Integer selectDefaultNodeCount(String traceTemplateId)
    {
        Integer defaultNodeCount=0;
        defaultNodeCount= traceFunTemplateconfigMapper.selectDefaultNodeCount(traceTemplateId);
        return defaultNodeCount;
    }

    /**
     * 新增溯源批次记录,并将溯源模板统计表的批次数量为原数量加1
     *
     * @param traceBatchInfo
     * @throws Exception
     * @author liujianqiang
     * @data 2018年12月12日
     */
    //@Transactional(rollbackFor = Exception.class)
    public String insertTraceBatchInfo(TraceBatchInfo traceBatchInfo) throws Exception {
        //新增溯源批次记录

        setDefaultField(traceBatchInfo);
        String traceBatchName=traceBatchInfo.getTraceBatchName();

        String traceBatchInfoId= insertTraceBatchInfoToPlatform(traceBatchInfo);
        if(StringUtils.isEmpty(traceBatchInfoId)){
            throw new SuperCodeTraceException("新增溯源批次记录失败");
        } else {
            traceBatchInfo.setTraceBatchPlatformId(traceBatchInfoId);
            traceBatchInfo.setTraceBatchInfoId(traceBatchInfoId);
        }

        checkBatchIdAndBatchName(traceBatchInfo.getTraceBatchId(), traceBatchName, traceBatchInfo.getOrganizationId(), traceBatchInfoId);
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
            setDefaultField(traceBatchInfo);
            traceBatchInfo.setTraceBatchPlatformId(traceBatchInfo.getTraceBatchInfoId());
            record = traceBatchInfoMapper.insertTraceBatchInfo(traceBatchInfo);
            //throw new SuperCodeTraceException("修改溯源批次记录失败");
        }

        try{
            traceBatchInfo= traceBatchInfoMapper.selectByTraceBatchInfoId(traceBatchInfo.getTraceBatchInfoId());
            updateTraceBatchInfoToPlatform(traceBatchInfo);
        }catch (Exception e){
            e.printStackTrace();
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
        TraceBatchInfo traceBatchInfo= traceBatchInfoMapper.selectByTraceBatchInfoId(traceBatchInfoId);
        Integer record = traceBatchInfoMapper.deleteTraceBatchInfo(traceBatchInfoId);//删除溯源批次信息
        if (record != 1) {
            throw new SuperCodeTraceException("删除溯源批次数据失败");
        }
        deleteTraceBatchInfoToPlatform(traceBatchInfo);
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

    public JsonNode deleteTraceBatchInfoToPlatform(TraceBatchInfo traceBatchInfo) {

        Map<String, Object> batchModel=new HashMap<String, Object>();
        batchModel.put("id",traceBatchInfo.getTraceBatchPlatformId());

        Map<String, String> headerMap = new HashMap<String, String>();
        try {
            headerMap.put("super-token", getSuperToken());
            headerMap.put(getSysAuthHeaderKey(),getSecretKeyForUser());
            ResponseEntity<String> rest = restTemplateUtil.deleteJsonDataAndReturnJosn(restUserUrl + "/product-batch",batchModel, headerMap);

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

        return null;
    }

    public JsonNode updateTraceBatchInfoToPlatform(TraceBatchInfo traceBatchInfo) {

        Map<String, Object> batchModel=new HashMap<String, Object>();
        batchModel.put("batchName",traceBatchInfo.getTraceBatchName());
        batchModel.put("batchId",traceBatchInfo.getTraceBatchId());
        batchModel.put("marketDate",traceBatchInfo.getListedTime());
        batchModel.put("productId",traceBatchInfo.getProductId());
        batchModel.put("productName",traceBatchInfo.getProductName());
        batchModel.put("traceBatchInfoId",traceBatchInfo.getTraceBatchPlatformId());

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("addProductBatchModel", batchModel);
        Map<String, String> headerMap = new HashMap<String, String>();
        try {
            headerMap.put("super-token", getSuperToken());
            headerMap.put(getSysAuthHeaderKey(),getSecretKeyForUser());
            ResponseEntity<String> rest = restTemplateUtil.putJsonDataAndReturnJosn(restUserUrl + "/product-batch/v2",JSONObject.toJSONString( batchModel), headerMap);

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

        return null;
    }

    public String insertTraceBatchInfoToPlatform(TraceBatchInfo traceBatchInfo) throws Exception {
        String traceBatchInfoId=null;

        Map<String, Object> batchModel=new HashMap<String, Object>();
        batchModel.put("batchName",traceBatchInfo.getTraceBatchName());
        batchModel.put("batchId",traceBatchInfo.getTraceBatchId());
        batchModel.put("marketDate",traceBatchInfo.getListedTime());
        batchModel.put("productId",traceBatchInfo.getProductId());
        batchModel.put("productName",traceBatchInfo.getProductName());

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("addProductBatchModel", batchModel);
        Map<String, String> headerMap = new HashMap<String, String>();
        try {
            headerMap.put("super-token", getSuperToken());
            headerMap.put(getSysAuthHeaderKey(),getSecretKeyForUser());
            ResponseEntity<String> rest = restTemplateUtil.postJsonDataAndReturnJosn(restUserUrl + "/product-batch",JSONObject.toJSONString( batchModel), headerMap);

            if (rest.getStatusCode().value() == 200) {
                String body = rest.getBody();
                JsonNode node = new ObjectMapper().readTree(body);
                if (200 == node.get("state").asInt()) {
                    traceBatchInfoId= node.get("results").asText();
                }else {
                    String msg= node.get("msg").asText();
                    throw new SuperCodeTraceException(msg);
                }
            }
        } catch (SuperCodeTraceException | IOException | SuperCodeException e) {
            e.printStackTrace();
            throw e;
        }

        return traceBatchInfoId;
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
        Integer total=null;
        ReturnParamsMap returnParamsMap=null;
        StringBuilder idsBuilder = new StringBuilder();
        Map<String, Object> dataMap=null;

        if (map.get("batchType")!=null && map.get("batchType").toString().equals("2")){
            total = traceObjectBatchInfoMapper.getCountByCondition(map);//获取总记录数
            returnParamsMap = getPageAndRetuanMap(map, total);
            List<TraceObjectBatchInfo> traceBatchInfoList = traceObjectBatchInfoMapper.getTraceBatchInfo(returnParamsMap.getParamsMap());
            for (TraceObjectBatchInfo returnTraceBatchInfo : traceBatchInfoList) {
                idsBuilder.append(returnTraceBatchInfo.getTraceTemplateId()).append(",");
            }
            dataMap = returnParamsMap.getReturnMap();
            getRetunMap(dataMap, traceBatchInfoList);
        }else {
            total = traceBatchInfoMapper.getCountByCondition(map);//获取总记录数
            returnParamsMap = getPageAndRetuanMap(map, total);
            List<ReturnTraceBatchInfo> traceBatchInfoList = traceBatchInfoMapper.getTraceBatchInfo(returnParamsMap.getParamsMap());
            for (ReturnTraceBatchInfo returnTraceBatchInfo : traceBatchInfoList) {
                idsBuilder.append(returnTraceBatchInfo.getTraceTemplateId()).append(",");
            }
            dataMap = returnParamsMap.getReturnMap();
            getRetunMap(dataMap, traceBatchInfoList);
        }

        JsonNode node;
        if (idsBuilder.length() > 0) {
            String ids = idsBuilder.substring(0, idsBuilder.length() - 1);
            node = getAddressUrl(ids);
            dataMap.put("addressWithTemplate", node);
        }else {
            dataMap.put("addressWithTemplate", new ArrayList<>());
        }
        return dataMap;
    }

    public String toString(Object param){
        String result=null;
        if(param!=null)
            result=String.valueOf(param);
        return  result;
    }

    public Map<String, Object> listProductBatchInfo(Map<String, Object> params) throws Exception{

        AbstractPageService.PageResults<List<TraceBatchInfo>> pageResults=null;
        Map<String, String> headerMap = new HashMap<String, String>();
        Map<String, Object> dataMap=new HashMap<String, Object>();
        try {
            headerMap.put("super-token", getSuperToken());
            headerMap.put(getSysAuthHeaderKey(),getSecretKeyForUser());
            ResponseEntity<String> rest = restTemplateUtil.getRequestAndReturnJosn(restUserUrl + "/product-batch/list", params, headerMap);
            if (rest.getStatusCode().value() == 200) {
                String body = rest.getBody();
                JsonNode resultsNode=new ObjectMapper().readTree(body).get("results");
                List<JSONObject> productBatchRelationSysViews= (List<JSONObject>)JSONObject.parseObject(resultsNode.get("list").toString(), ArrayList.class);
                Page page= (Page)JSONObject.parseObject(resultsNode.get("pagination").toString(), Page.class);

                StringBuilder idsBuilder = new StringBuilder();

                List<TraceBatchInfo> traceBatchInfos=null;
                if (productBatchRelationSysViews!=null&& productBatchRelationSysViews.size()>0){

                    traceBatchInfos= productBatchRelationSysViews.stream().map(e->new TraceBatchInfo(
                            e.get("batchName").toString(),e.get("productId").toString(),e.get("productName").toString(),e.get("batchId").toString(),toString(e.get("marketDate")),String.valueOf(e.get("founder")),String.valueOf(e.get("createTime")),String.valueOf(e.get("traceBatchInfoId"))
                    )).collect(Collectors.toList());

                    List<String> traceBatchInfoIds= traceBatchInfos.stream().map(e->String.format("'%s'",e.getTraceBatchInfoId())).collect(Collectors.toList());
                    String ids=StringUtils.join(traceBatchInfoIds,",");
                    List<TraceBatchInfo> localBatchs=traceBatchInfoMapper.selectByTraceBatchInfoIds(ids);
                    for(TraceBatchInfo traceBatchInfo: traceBatchInfos){
                        List<TraceBatchInfo> currBatchs= localBatchs.stream().filter(e->e.getTraceBatchInfoId().equals(traceBatchInfo.getTraceBatchInfoId())).collect(Collectors.toList());
                        if(currBatchs!=null && currBatchs.size()>0){
                            TraceBatchInfo currBatch= currBatchs.get(0);
                            traceBatchInfo.setTraceTemplateId(currBatch.getTraceTemplateId());
                            traceBatchInfo.setTraceTemplateName(currBatch.getTraceTemplateName());
                            traceBatchInfo.setNodeDataCount(currBatch.getNodeDataCount());
                            traceBatchInfo.setH5TempleteName(currBatch.getH5TempleteName());
                            traceBatchInfo.setH5TrancePageId(currBatch.getH5TrancePageId());
                        }
                    }

                    for (TraceBatchInfo returnTraceBatchInfo : traceBatchInfos) {
                        idsBuilder.append(returnTraceBatchInfo.getTraceTemplateId()).append(",");
                    }
                }
                dataMap.put("list",traceBatchInfos);
                dataMap.put("pagination",page);

                JsonNode node;
                if (idsBuilder.length() > 0) {
                    String ids = idsBuilder.substring(0, idsBuilder.length() - 1);
                    node = getAddressUrl(ids);
                    dataMap.put("addressWithTemplate", node);
                }else {
                    dataMap.put("addressWithTemplate", new ArrayList<>());
                }
                //pageResults=new AbstractPageService.PageResults<List<TraceBatchInfo>>(traceBatchInfos,page);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return dataMap;
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
                headerMap.put(getSysAuthHeaderKey(),getSecretKeyForUser());
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
			TraceObjectBatchInfo traceObjectBatchInfo= traceObjectBatchInfoMapper.selectByTraceBatchInfoId(traceBatchInfoId);
            traceBatchInfo= commonUtil.convert(traceObjectBatchInfo,TraceBatchInfo.class);
			if(traceObjectBatchInfo==null){
                throw new SuperCodeTraceException("无此批次号记录", 500);
            }
        }
        String orgnizationId = commonUtil.getOrganizationId();
        RestResult<List<Map<String, Object>>> nodeDataResult=null;
        nodeDataResult= traceFunTemplateconfigService.queryNodeInfo(traceBatchInfo.getTraceBatchInfoId(), traceBatchInfo.getTraceTemplateId(), false, orgnizationId, null,null);
        List<Map<String, Object>> batchDatas = nodeDataResult.getResults();

        Date nodeEndTime=null;
        if(StringUtils.isNotEmpty(traceBatchInfo.getCreateTime())){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            nodeEndTime =sdf.parse( traceBatchInfo.getCreateTime());
            Calendar cal = Calendar.getInstance();
            cal.setTime(nodeEndTime);
            cal.add(Calendar.HOUR, 1);
            nodeEndTime = cal.getTime();
        }

        getParentNodeInfo(traceBatchInfoId,batchDatas,false, null, nodeEndTime);
        return nodeDataResult;
    }

    public TraceBatchInfo getOneByUnkonwnOneField(String plainSql) {

        return traceBatchInfoMapper.getOneByUnkonwnOneField(plainSql);
    }

    public TraceBatchInfo selectByTraceBatchInfoId(String traceBatchInfoId) {
        return traceBatchInfoMapper.selectByTraceBatchInfoId(traceBatchInfoId);
    }

    /**
     * 查询父级批次对应的溯源信息数据并返回
     * @param traceBatchRelations
     * @param nodeDatas
     * @throws Exception
     */
    private void GetParentBatchNodeInfo(List<TraceBatchRelation> traceBatchRelations, List<Map<String, Object>> nodeDatas)  throws Exception {
        for(TraceBatchRelation traceBatchRelation:traceBatchRelations){
            String traceBatchInfoId=traceBatchRelation.getParentBatchId();
            String traceTemplateId=null;
            if(!StringUtils.isEmpty(traceBatchInfoId)){
                if(traceBatchRelation.getParentBatchType()== BatchTableType.ObjectBatch.getKey()){
                    TraceObjectBatchInfo traceBatchInfo=traceObjectBatchInfoMapper.selectByTraceBatchInfoId(traceBatchInfoId);
                    traceTemplateId=traceBatchInfo.getTraceTemplateId();
                }else {
                    TraceBatchInfo traceBatchInfo = traceBatchInfoMapper.selectByTraceBatchInfoId(traceBatchInfoId);
                    traceTemplateId=traceBatchInfo.getTraceTemplateId();
                }
                RestResult<List<Map<String, Object>>> nodeDataResult= traceFunTemplateconfigService.queryNodeInfo(traceBatchInfoId, traceTemplateId, true, null, null,null);
                List<Map<String,Object>> parentNodeData=nodeDataResult.getResults();
                if(parentNodeData!=null && parentNodeData.size()>0) {
                    nodeDatas.addAll(0,nodeDataResult.getResults());
                }
            }
        }
    }

    private List<Map<String,Object>> getBatchNodeInfo(TraceBatchRelation traceBatchRelation, boolean fromH5, Date start, Date end) throws Exception{
        String traceBatchInfoId=traceBatchRelation.getParentBatchId();
        String traceTemplateId=null;
        List<Map<String,Object>> parentNodeData=null;
        Object batchInfo=null;
        if(!StringUtils.isEmpty(traceBatchInfoId)){
            if(traceBatchRelation.getParentBatchType()== BatchTableType.ObjectBatch.getKey()){
                TraceObjectBatchInfo traceBatchInfo=traceObjectBatchInfoMapper.selectByTraceBatchInfoId(traceBatchInfoId);
                traceTemplateId=traceBatchInfo.getTraceTemplateId();
                batchInfo=traceBatchInfo;
            }else {
                TraceBatchInfo traceBatchInfo = traceBatchInfoMapper.selectByTraceBatchInfoId(traceBatchInfoId);
                traceTemplateId=traceBatchInfo.getTraceTemplateId();
                batchInfo=traceBatchInfo;
            }
            RestResult<List<Map<String, Object>>> nodeDataResult= traceFunTemplateconfigService.queryNodeInfo(traceBatchInfoId, traceTemplateId, fromH5, null,start, end);
            parentNodeData=nodeDataResult.getResults();


            /*if(parentNodeData!=null && parentNodeData.size()>0){
                currentDataMap=new HashMap<String, Object>();
                currentDataMap.put("nodeInfo",parentNodeData);
                currentDataMap.put("productInfo", batchInfo);
            }*/
        }
        return parentNodeData;
    }

    /**
     * h5页面数据查询接口
     *
     * @param traceBatchInfoId
     * @return
     * @throws Exception
     */
    public RestResult<HashMap<String, Object>> h5PageData(String traceBatchInfoId, Integer traceBatchType, Date start, Date end) throws Exception {
        RestResult<HashMap<String, Object>> backResult = new RestResult<HashMap<String, Object>>();
        HashMap<String, Object> dataMap = new HashMap<String, Object>();
        List<Map<String, Object>> batchDatas = null;

        RestResult<List<Map<String, Object>>> nodeDataResult=null;

        List<JSONObject> productTestings= productTestingService.getProductTesting(traceBatchInfoId);
        dataMap.put("testingInfo",productTestings);

        Date nodeEndTime =null;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if(traceBatchType !=null && traceBatchType.intValue()==2){
            TraceObjectBatchInfo traceBatchInfo=traceObjectBatchInfoMapper.selectByTraceBatchInfoId(traceBatchInfoId);
            if (null == traceBatchInfo) {
                throw new SuperCodeTraceException("无此批次号记录", 500);
            }
            nodeDataResult = traceFunTemplateconfigService.queryNodeInfo(traceBatchInfo.getTraceBatchInfoId(), traceBatchInfo.getTraceTemplateId(), true, null,null,null);

            dataMap.put("productInfo", traceBatchInfo);
            nodeEndTime=traceBatchInfo.getCreateTime();
        } else {
            TraceBatchInfo traceBatchInfo = traceBatchInfoMapper.selectByTraceBatchInfoId(traceBatchInfoId);
            if (null == traceBatchInfo) {
                throw new SuperCodeTraceException("无此批次号记录", 500);
            }
            //h5查询不需要登陆 没有组织id
            nodeDataResult = traceFunTemplateconfigService.queryNodeInfo(traceBatchInfo.getTraceBatchInfoId(), traceBatchInfo.getTraceTemplateId(), true, null,start,end);

            dataMap.put("productInfo", traceBatchInfo);
            nodeEndTime=sdf.parse( traceBatchInfo.getCreateTime());
        }

        if(nodeEndTime!=null){
            Calendar cal = Calendar.getInstance();
            cal.setTime(nodeEndTime);
            cal.add(Calendar.HOUR, 1);
            nodeEndTime = cal.getTime();
        }

        dataMap.put("nodeInfo", nodeDataResult.getResults());
        batchDatas =nodeDataResult.getResults();

        //递归查询所有父级批次，查询所有父级批次对应的溯源信息数据并返回
        getParentNodeInfo(traceBatchInfoId,batchDatas,true, null, nodeEndTime);

        if (nodeDataResult.getState() != 200) {
            throw new SuperCodeTraceException(nodeDataResult.getMsg(), 500);
        }

        backResult.setState(200);
        backResult.setResults(dataMap);
        return backResult;
    }

    public void getParentNodeInfo(String traceBatchInfoId,List<Map<String, Object>> batchDatas, boolean fromH5, Date start, Date end) throws Exception
    {
        List<TraceBatchRelation> traceBatchRelations= traceBatchRelationService.selectByBatchId(traceBatchInfoId);
        if (traceBatchRelations!=null && traceBatchRelations.size()>0){

            List<Map<String, Object>> currentDataMap = null;
            List<TraceBatchRelation> parentBatchs= traceBatchRelations.stream().filter(e->e.getCurrentBatchId().equals(traceBatchInfoId)).collect(Collectors.toList());
            while (parentBatchs!=null && parentBatchs.size()>0){
                if(parentBatchs.size()==1){
                    currentDataMap = getBatchNodeInfo(parentBatchs.get(0),fromH5, start, end);
                    if(currentDataMap!=null){
                        currentDataMap= currentDataMap.stream().filter(e->!e.get("businessType").equals("3")).collect(Collectors.toList());
                    }
                    if(currentDataMap!=null){
                        batchDatas.addAll(currentDataMap);
                    }

                    String parentBatchId= parentBatchs.get(0).getParentBatchId();
                    parentBatchs= traceBatchRelations.stream().filter(e->e.getCurrentBatchId().equals(parentBatchId)).collect(Collectors.toList());
                } else {
                    Map<String, Object> currentMap=new HashMap<String, Object>();
                    currentMap.put("relations",parentBatchs);
                    batchDatas.add(currentMap);
                    parentBatchs=null;
                }
            }
        }
        Collections.sort(batchDatas,new Comparator(){
            public int compare(Object a, Object b){
                int ret = 0;
                Map<String, Object> m1= (Map<String, Object>)a;
                Map<String, Object> m2= (Map<String, Object>)b;
                List<Field> lines1= (List<Field>)m1.get("defaultLineData");
                List<Field> lines2= (List<Field>)m2.get("defaultLineData");
                List<Field> sortDates1= lines1.stream().filter(e->e.getFieldCode().equals("SortDateTime")).collect(Collectors.toList());
                List<Field> sortDates2= lines2.stream().filter(e->e.getFieldCode().equals("SortDateTime")).collect(Collectors.toList());
                String sortDateTime1=sortDates1.get(0).getFieldValue().toString();
                String sortDateTime2=sortDates2.get(0).getFieldValue().toString();
                ret=sortDateTime1.compareTo(sortDateTime2);
                return ret;
            }
        });
    }

    public String getH5PageUrlByTraceBatchId(String batchId) throws Exception{
        TraceBatchInfo traceBatchInfo= traceBatchInfoMapper.selectBatchInfoByTraceBatchId(batchId);
        if(traceBatchInfo==null){
            throw new Exception("未找到该批次号对应的批次信息");
        }
//        String url= getOrgDomainUrl(traceBatchInfo.getOrganizationId());
//        url=String.format(url,traceBatchInfo.getTraceBatchInfoId());
        String url=String.format("%s?traceBatchInfoId=%s",h5PageUrl,traceBatchInfo.getTraceBatchInfoId());
        return url;
    }

    public String getOrgDomainUrl(String organizationId) throws Exception{
        String url=String.format("%s?traceBatchInfoId=",h5PageUrl )+"%s";
        Map<String, String> headerMap=getSuperCodeToken();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("organizationId", organizationId);

        try{
            ResponseEntity<String> rest =  restTemplateUtil.getRequestAndReturnJosn(restUserUrl + "/orgdomain/selectByOrgId", params, headerMap);
            if (rest.getStatusCode().value() == 200) {
                String body = rest.getBody();
                JsonNode node = new ObjectMapper().readTree(body);
                if (200 == node.get("state").asInt()) {
                    if(  node.get("results").getClass()!= NullNode.class){

                        url= node.get("results").get("url").asText();
                    }
                }
            }
        }catch (Exception e){
            logger.error(e.getMessage(),e);
        }
        return url;
    }


}
