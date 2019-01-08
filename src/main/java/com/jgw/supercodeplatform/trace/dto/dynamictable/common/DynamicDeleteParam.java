package com.jgw.supercodeplatform.trace.dto.dynamictable.common;

import java.util.List;

import javax.validation.constraints.NotEmpty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "动态表删除model")
public class DynamicDeleteParam {
	@NotEmpty
	@ApiModelProperty(name = "ids", value = "删除的id集合", example = "", required = false)
	private List<Long> ids;
	
	@ApiModelProperty(name = "functionId", value = "功能id，表示操作的功能，由请求头传", example = "", required = false,hidden=true)
	private String functionId;

	@ApiModelProperty(value = "溯源模板id，如果删除的是节点业务数据必传，否则不传",required=false)
	private String traceTemplateId;      //溯源模板号
	
	public List<Long> getIds() {
		return ids;
	}

	public void setIds(List<Long> ids) {
		this.ids = ids;
	}

	public String getFunctionId() {
		return functionId;
	}

	public void setFunctionId(String functionId) {
		this.functionId = functionId;
	}

	public String getTraceTemplateId() {
		return traceTemplateId;
	}

	public void setTraceTemplateId(String traceTemplateId) {
		this.traceTemplateId = traceTemplateId;
	}
	
	
}
