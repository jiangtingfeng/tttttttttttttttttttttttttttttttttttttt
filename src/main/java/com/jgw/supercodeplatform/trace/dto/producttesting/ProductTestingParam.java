package com.jgw.supercodeplatform.trace.dto.producttesting;

import com.jgw.supercodeplatform.trace.pojo.producttesting.ProductTestingItem;
import io.swagger.annotations.ApiModelProperty;

import java.util.Date;
import java.util.List;

public class ProductTestingParam  {
    private Integer id;

    private String productTestingId;

    @ApiModelProperty(value = "企业id",required=true)
    private String organizationId;

    @ApiModelProperty(value = "机构id，第三方检测")
    private String thirdpartyOrganizationId;

    @ApiModelProperty(value = "第三方检测机构名称")
    private String thirdpartyOrganizationName;

    @ApiModelProperty(value = "产品id",required=true)
    private String productID;

    @ApiModelProperty(value = "批次id",required=true)
    private String traceBatchInfoId;

    @ApiModelProperty(value = "计划检测日期")
    private String testingDate;

    @ApiModelProperty(value = "")
    private String testingMan;

    private Date createTime;

    private String createId;

    @ApiModelProperty(value = "检疫证号")
    private String certifyNumber;

    @ApiModelProperty(value = "excel")
    private String excel;

    @ApiModelProperty(value = "批次名称")
    private String traceBatchInfoName;

    public String getTestingManName() {
        return testingManName;
    }

    public void setTestingManName(String testingManName) {
        this.testingManName = testingManName;
    }

    @ApiModelProperty(value = "检测人名称")
    private String testingManName;



    public String getThirdpartyOrganizationName() {
        return thirdpartyOrganizationName;
    }

    public void setThirdpartyOrganizationName(String thirdpartyOrganizationName) {
        this.thirdpartyOrganizationName = thirdpartyOrganizationName;
    }

    public String getTraceBatchInfoName() {
        return traceBatchInfoName;
    }

    public void setTraceBatchInfoName(String traceBatchInfoName) {
        this.traceBatchInfoName = traceBatchInfoName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    @ApiModelProperty(value = "产品名称")
    private  String productName;

    public String getCertifyNumber() {
        return certifyNumber;
    }

    public void setCertifyNumber(String certifyNumber) {
        this.certifyNumber = certifyNumber;
    }

    public String getExcel() {
        return excel;
    }

    public void setExcel(String excel) {
        this.excel = excel;
    }

    @ApiModelProperty(value = "内部检测为1，第三方检测为2")
    private Integer testingType;

    private String organizationName;

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public Integer getTestingType() {
        return testingType;
    }

    public void setTestingType(Integer testingType) {
        this.testingType = testingType;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCreateId() {
        return createId;
    }

    public void setCreateId(String createId) {
        this.createId = createId;
    }

    private List<ProductTestingItem> productTestingItems;



    public List<ProductTestingItem> getProductTestingItems() {
        return productTestingItems;
    }

    public void setProductTestingItems(List<ProductTestingItem> productTestingItems) {
        this.productTestingItems = productTestingItems;
    }



    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getProductTestingId() {
        return productTestingId;
    }

    public void setProductTestingId(String productTestingId) {
        this.productTestingId = productTestingId == null ? null : productTestingId.trim();
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId == null ? null : organizationId.trim();
    }

    public String getThirdpartyOrganizationId() {
        return thirdpartyOrganizationId;
    }

    public void setThirdpartyOrganizationId(String thirdpartyOrganizationId) {
        this.thirdpartyOrganizationId = thirdpartyOrganizationId == null ? null : thirdpartyOrganizationId.trim();
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID == null ? null : productID.trim();
    }

    public String getTraceBatchInfoId() {
        return traceBatchInfoId;
    }

    public void setTraceBatchInfoId(String traceBatchInfoId) {
        this.traceBatchInfoId = traceBatchInfoId == null ? null : traceBatchInfoId.trim();
    }

    public String getTestingDate() {
        return testingDate;
    }

    public void setTestingDate(String testingDate) {
        this.testingDate = testingDate == null ? null : testingDate.trim();
    }

    public String getTestingMan() {
        return testingMan;
    }

    public void setTestingMan(String testingMan) {
        this.testingMan = testingMan == null ? null : testingMan.trim();
    }


}