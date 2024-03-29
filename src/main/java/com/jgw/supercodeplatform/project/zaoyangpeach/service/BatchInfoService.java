package com.jgw.supercodeplatform.project.zaoyangpeach.service;

import com.jgw.supercodeplatform.exception.SuperCodeException;
import com.jgw.supercodeplatform.pojo.cache.AccountCache;
import com.jgw.supercodeplatform.trace.aware.TraceApplicationContextAware;
import com.jgw.supercodeplatform.trace.common.util.CommonUtil;
import com.jgw.supercodeplatform.trace.dao.DynamicBaseMapper;
import com.jgw.supercodeplatform.trace.dao.mapper1.batchinfo.TraceBatchInfoMapper;
import com.jgw.supercodeplatform.trace.dao.mapper1.zaoyangpeach.BatchInfoMapper;
import com.jgw.supercodeplatform.trace.pojo.TraceFunFieldConfig;
import com.jgw.supercodeplatform.trace.pojo.tracebatch.TraceBatchInfo;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class BatchInfoService extends CommonUtil {

    @Autowired
    private BatchInfoMapper batchInfoMapper;

    @Autowired
    private TraceBatchInfoMapper traceBatchInfoMapper;

    @Autowired
    private TraceApplicationContextAware applicationAware;

    /**
     * 获取批次包装规格数量品级等生产过程信息
     * @param batchName
     * @return
     * @throws Exception
     */
    public Map<String,Object> getBatchInfo(String batchName,Integer functionType) throws Exception{

        //organizationId="5d4010983d914fa7901b389d6ddcd39a";

        TraceBatchInfo traceBatchInfo= traceBatchInfoMapper.selectByBatchName(batchName);
        if(traceBatchInfo==null){
            throw new SuperCodeException("未找到该批次");
        }
        String traceBatchInfoId =traceBatchInfo.getTraceBatchInfoId();

        Map<String,Object> result=new HashMap<String, Object>();
        result.put("traceBatchInfoId",traceBatchInfoId);
        result.put("productId",traceBatchInfo.getProductId());
        result.put("productName",traceBatchInfo.getProductName());
        result.put("traceBatchName",traceBatchInfo.getTraceBatchName());

        switch (functionType){
            case 1:
                selectPurchaseInfo(traceBatchInfoId,result);
                break;
            case 2:
                selectSortingInfo(traceBatchInfoId,result);
                break;
            case 3:
                selectPackingInfo(traceBatchInfoId,result);
                break;
            default:
                selectPackingInfo(traceBatchInfoId,result);
                break;
        }


        return result;
    }

    void selectPackingInfo(String traceBatchInfoId, Map<String,Object> result) throws Exception{
        String organizationId = getOrganizationId();

        TraceFunFieldConfig packingSpecField = batchInfoMapper.selectByNestCompentFieldCode(organizationId,"PackingSpec");
        String enTableName= packingSpecField.getEnTableName();

        DynamicBaseMapper dao=applicationAware.getDynamicMapperByFunctionId(null,packingSpecField.getFunctionId());

        String selectSql = String.format("select * from %s where TraceBatchInfoId = '%s' ",enTableName,traceBatchInfoId);
        List<LinkedHashMap<String, Object>> batchList= dao.select(selectSql);
        if(CollectionUtils.isNotEmpty(batchList)){
            result.put("packingSpec", batchList.get(0).get("PackingSpec"));
            result.put("packingQuantity", batchList.get(0).get("PackingQuantity"));

            TraceFunFieldConfig packingStaff = batchInfoMapper.selectByFieldCode(organizationId,"PackingStaff");
            enTableName=packingStaff.getEnTableName();
            dao=applicationAware.getDynamicMapperByFunctionId(null,packingStaff.getFunctionId());

            Integer parentId=Integer.valueOf(batchList.get(0).get("ParentId").toString());
            selectSql=String.format("SELECT * FROM %s WHERE ID =%s",enTableName, parentId);
            List<LinkedHashMap<String, Object>> packingClassList= dao.select(selectSql);
            if(CollectionUtils.isNotEmpty(packingClassList)){
                result.put("packingClass",packingClassList.get(0).get("PackingClass"));
            }
        }
    }

    void selectPurchaseInfo(String traceBatchInfoId, Map<String,Object> result) throws Exception{
        String organizationId = getOrganizationId();

        TraceFunFieldConfig packingSpecField = batchInfoMapper.selectByFieldCode(organizationId,"PurchaseStaff");
        String enTableName= packingSpecField.getEnTableName();

        DynamicBaseMapper dao=applicationAware.getDynamicMapperByFunctionId(null,packingSpecField.getFunctionId());

        String selectSql = String.format("select * from %s where TraceBatchInfoId = '%s' ",enTableName,traceBatchInfoId);
        List<LinkedHashMap<String, Object>> batchList= dao.select(selectSql);
        if(CollectionUtils.isNotEmpty(batchList)){
            result.put("purchaseQuantity", batchList.get(0).get("Quantity"));
        }
    }

    void selectSortingInfo(String traceBatchInfoId, Map<String,Object> result) throws Exception{
        String organizationId = getOrganizationId();

        TraceFunFieldConfig packingSpecField = batchInfoMapper.selectByNestCompentFieldCode(organizationId,"SortingClass");
        String enTableName= packingSpecField.getEnTableName();

        DynamicBaseMapper dao=applicationAware.getDynamicMapperByFunctionId(null,packingSpecField.getFunctionId());

        String selectSql = String.format("select * from %s where TraceBatchInfoId = '%s' ",enTableName,traceBatchInfoId);
        List<LinkedHashMap<String, Object>> batchList= dao.select(selectSql);
        if(CollectionUtils.isNotEmpty(batchList)){
            result.put("sortingClass", batchList.get(0).get("SortingClass"));
            result.put("sortingQuantity", batchList.get(0).get("SortingQuantity"));

        }
    }

}
