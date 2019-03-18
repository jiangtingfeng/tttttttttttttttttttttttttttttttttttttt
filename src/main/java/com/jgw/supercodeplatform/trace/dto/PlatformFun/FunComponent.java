package com.jgw.supercodeplatform.trace.dto.PlatformFun;


import com.jgw.supercodeplatform.trace.dto.TraceFunFieldConfigParam;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;


public  class FunComponent
{
    @ApiModelProperty(value="组件名称")
    private String componentName;

    @ApiModelProperty(value = "组件类型")
    private String componentType;

    @ApiModelProperty(value = "组件id")
    private String componentId;

    @ApiModelProperty(name = "traceFunFieldConfigModel", value = "功能溯源对象模型数组")
    List<TraceFunFieldConfigParam> traceFunFieldConfigModel;



    public String getComponentId() {
        return componentId;
    }

    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }


    public String getComponentName() {
        return componentName;
    }

    public void setComponentName(String componentName) {
        this.componentName = componentName;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public List<TraceFunFieldConfigParam> getTraceFunFieldConfigModel() {
        return traceFunFieldConfigModel;
    }

    public void setTraceFunFieldConfigModel(List<TraceFunFieldConfigParam> traceFunFieldConfigModel) {
        this.traceFunFieldConfigModel = traceFunFieldConfigModel;
    }
}