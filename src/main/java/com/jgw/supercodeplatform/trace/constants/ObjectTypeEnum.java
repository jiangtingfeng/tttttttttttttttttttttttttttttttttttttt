package com.jgw.supercodeplatform.trace.constants;

import com.jgw.supercodeplatform.trace.exception.SuperCodeTraceException;

/**
 * 对象类型
 *
 * @author wzq
 * @date: 2019-03-28
 */
public enum ObjectTypeEnum {
	Default(0,"",""),

	USER(13001,"UserId", "员工"),
	PRODUCT(13002, "ProductId","产品"),
	TRACE_BATCH(13003,"TraceBatchInfoId", "产品批次"),

	StoragePlace(13004,"StoragePlaceId","存放地点"),
	FarmOperation(13005,"FarmOperation","农事操作"),
	ProductGrade(13006,"ProductGradeId","产品品级"),
	ProductSpecific(13007,"ProductSpecificId","产品规格"),
	TransportMethod(13008,"TransportMethodId","运输方式"),
	Delivery(13009,"DeliveryId","发货方式"),

	MassifInfo(13012,"MassIfId","地块"),
	MassifBatch(13013,"TraceBatchId","地块批次"),

	PlantingBatch(13014,"TraceBatchInfoId", "种植批次"),

	Device(13018,"DeviceId","设备"),
	Material(13019,"MaterialId","物料"),
	MaterialSpecific(13020,"MaterialSpecificId","物料规格"),
	MaterialBatch(13021,"MaterialBatchId","物料批次"),

	CodeAssociate(13010,"AssociateType","码关联方式");


	public static ObjectTypeEnum getType(Integer codeTypeId) throws SuperCodeTraceException {
		if (null==codeTypeId) {
			throw new SuperCodeTraceException("ObjectTypeEnum.getType参数codeTypeId不能为空", 500);
		}
		switch (codeTypeId) {
		case 13001:
			return USER;
		case 13002:
			return PRODUCT;
		case 13003:
			return TRACE_BATCH;
		case 13004:
			return StoragePlace;
		case 13005:
			return FarmOperation;
		case  13006:
			return ProductGrade;
		case 13007:
			return ProductSpecific;
		case 13008:
			return TransportMethod;
		case 13009:
			return Delivery;

		case 13012:
			return MassifInfo;
		case 13013:
			return MassifBatch;
		case 13014:
			return PlantingBatch;
		case 13018:
			return Device;
		case 13019:
			return Material;

		case 13020:
			return MaterialBatch;
		case 13021:
			return MaterialSpecific;

		case 13010:
			return CodeAssociate;
		default:
			return Default;
			//throw new SuperCodeTraceException("无法根据对象类型codeTypeId="+codeTypeId+"获取到对象", 500);
		}
	}
	private int code;
    private String fieldCode;
	private String desc;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getFieldCode() {
		return fieldCode;
	}

	public void setFieldCode(String fieldCode) {
		this.fieldCode = fieldCode;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	private ObjectTypeEnum(int code, String fieldCode, String desc) {
		this.code = code;
		this.fieldCode = fieldCode;
		this.desc = desc;
	}


}
