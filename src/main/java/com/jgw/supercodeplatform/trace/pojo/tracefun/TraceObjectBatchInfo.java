package com.jgw.supercodeplatform.trace.pojo.tracefun;

import java.io.Serializable;
import java.util.Date;

public class TraceObjectBatchInfo implements Serializable {

    private static final long serialVersionUID = -6894942493146216744L;

    private String traceBatchInfoId;//唯一id
    private String organizationId;//组织id

    private String traceBatchName;//批次名称
    private String traceBatchId;//批次号

    private String traceTemplateId;//溯源模板号
    private String traceTemplateName;//溯源模板名称
    private String h5TrancePageId;//H5溯源页模板Id号
    private String h5TempleteName;//H5溯源页模板名称
    private String createId;//创建人id
    private String createMan;//创建人
    private Date createTime;//创建时间
    private String updateTime;//修改时间
    private Integer nodeDataCount;
    private String traceBatchPlatformId;
    private String sysId;
    private String productId;//产品id
    private String productName;//产品名称

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSysId() {
        return sysId;
    }

    public void setSysId(String sysId) {
        this.sysId = sysId;
    }

    public TraceObjectBatchInfo(){}

    public TraceObjectBatchInfo(String traceBatchName,String traceBatchId,String traceBatchInfoId,String traceTemplateId,String traceTemplateName,String h5TrancePageId,String h5TempleteName,String createMan,String createId){
        this.traceBatchName=traceBatchName;
        this.traceBatchId=traceBatchId;
        this.traceBatchInfoId=traceBatchInfoId;
        this.traceTemplateId=traceTemplateId;
        this.traceTemplateName=traceTemplateName;
        this.h5TrancePageId=h5TrancePageId;
        this.h5TempleteName=h5TempleteName;
        this.createMan=createMan;
        this.createId=createId;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    private String objectId;
    private String objectName;

    public Long getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(Long serialNumber) {
        this.serialNumber = serialNumber;
    }

    private Long serialNumber;

    public int getBatchType() {
        return batchType;
    }

    public void setBatchType(int batchType) {
        this.batchType = batchType;
    }

    private int batchType;

    public String getTraceBatchInfoId() {
        return traceBatchInfoId;
    }

    public void setTraceBatchInfoId(String traceBatchInfoId) {
        this.traceBatchInfoId = traceBatchInfoId;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getTraceBatchName() {
        return traceBatchName;
    }

    public void setTraceBatchName(String traceBatchName) {
        this.traceBatchName = traceBatchName;
    }

    public String getTraceBatchId() {
        return traceBatchId;
    }

    public void setTraceBatchId(String traceBatchId) {
        this.traceBatchId = traceBatchId;
    }

    public String getTraceTemplateId() {
        return traceTemplateId;
    }

    public void setTraceTemplateId(String traceTemplateId) {
        this.traceTemplateId = traceTemplateId;
    }

    public String getTraceTemplateName() {
        return traceTemplateName;
    }

    public void setTraceTemplateName(String traceTemplateName) {
        this.traceTemplateName = traceTemplateName;
    }

    public String getH5TrancePageId() {
        return h5TrancePageId;
    }

    public void setH5TrancePageId(String h5TrancePageId) {
        this.h5TrancePageId = h5TrancePageId;
    }

    public String getH5TempleteName() {
        return h5TempleteName;
    }

    public void setH5TempleteName(String h5TempleteName) {
        this.h5TempleteName = h5TempleteName;
    }

    public String getCreateId() {
        return createId;
    }

    public void setCreateId(String createId) {
        this.createId = createId;
    }

    public String getCreateMan() {
        return createMan;
    }

    public void setCreateMan(String createMan) {
        this.createMan = createMan;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getNodeDataCount() {
        return nodeDataCount;
    }

    public void setNodeDataCount(Integer nodeDataCount) {
        this.nodeDataCount = nodeDataCount;
    }

    public String getTraceBatchPlatformId() {
        return traceBatchPlatformId;
    }

    public void setTraceBatchPlatformId(String traceBatchPlatformId) {
        this.traceBatchPlatformId = traceBatchPlatformId;
    }
}
