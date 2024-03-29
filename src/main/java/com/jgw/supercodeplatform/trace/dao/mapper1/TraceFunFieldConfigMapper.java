package com.jgw.supercodeplatform.trace.dao.mapper1;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.jgw.supercodeplatform.trace.dao.CommonSql;
import com.jgw.supercodeplatform.trace.dao.sqlbuilder.TraceFunFieldConfigProvider;
import com.jgw.supercodeplatform.trace.dto.template.query.TraceFunTemplateconfigQueryParam;
import com.jgw.supercodeplatform.trace.pojo.TraceFunFieldConfig;

@Mapper
public interface TraceFunFieldConfigMapper extends CommonSql{
    String PARTFields="Id id,FieldType fieldType,ObjectFieldId objectFieldId"
    		+ ",FieldCode fieldCode,ExtraCreate extraCreate,FieldName fieldName,EnTableName enTableName"
    		+ ",ObjectType objectType,MaxSize maxSize,DefaultValue defaultValue,TypeClass typeClass"
    		+ ",FunctionName functionName,FunctionId functionId";
    
    String ALLFields="Id id,FunctionId functionId,ObjectFieldId objectFieldId,ObjectType objectType,FunctionName functionName,ExtraCreate extraCreate,TraceTemplateId traceTemplateId,EnTableName enTableName,FieldType fieldType,FieldName fieldName,FieldWeight fieldWeight,FieldCode fieldCode,TypeClass typeClass,DefaultValue defaultValue,"
    		+ "IsRequired isRequired,ValidateFormat validateFormat,MinSize minSize,MaxSize maxSize,RequiredNumber requiredNumber,MinNumber minNumber,MaxNumber maxNumber,DataValue dataValue,IsRemarkEnable isRemarkEnable,ShowHidden showHidden,CreateBy createBy,DATE_FORMAT(CreateTime,'%Y-%m-%d %H:%i:%S') createTime,LastUpdateBy lastUpdateBy,DATE_FORMAT(LastUpdateTime,'%Y-%m-%d %H:%i:%S') lastUpdateTime,"
			+"ComponentId componentId, filterField, filterSource, readOnly";
    
	@InsertProvider(type = TraceFunFieldConfigProvider.class, method = "batchInsert")
	void batchInsert(@Param("list")List<TraceFunFieldConfig> arg0);
	
	@Select("select f.TableName EnTableName " +
			"from trace_fun_config t " +
			"left join trace_fun f  on t.FUNCTIONid=f.FUNCTIONid " +
			"where f.FunctionId=#{functionId}    AND (f.TraceTemplateId is null or f.TraceTemplateId <>'DELETE')  limit 1")
	String getEnTableNameByFunctionId(@Param("functionId") String functionId);

	@Select("<script>"
			+ "select "+ALLFields+" from trace_fun_config where FunctionId=#{functionId}"
			     + "<if test='businessType !=null '> and TraceTemplateId = #{traceTemplateId} </if> "
				 + "and TypeClass=#{typeClass}"
				 + " order by FieldWeight"
			+"</script>"
			)
	List<TraceFunFieldConfig> selectNodeOrFunAllFields(TraceFunTemplateconfigQueryParam param);

	@Update({
		"<script>"
		   +"update trace_fun_config "
		   +"set"
				+" FieldWeight ="
				+"<foreach collection='list' item='item' index='index' separator=' ' open='case Id' close='end'>"
				+"when #{item.id} then #{item.fieldWeight}"
				+"</foreach>"
				+", DefaultValue ="
				+"<foreach collection='list' item='item' index='index' separator=' ' open='case Id' close='end'>"
				+"when #{item.id} then #{item.defaultValue}"
				+"</foreach>"
			
			+" where Id in"
				+"<foreach collection='list' index='index' item='item' separator=',' open='(' close=')'>"
				+"#{item.id,jdbcType=BIGINT}"
				+"</foreach>"
		+"</script>"
	})
	void batchUpdate(@Param("list")List<TraceFunFieldConfig> update_tffcList);

	@Update({
			"<script>"
					+"update trace_fun_config "
					+"set"
					+" FieldWeight ="
					+"<foreach collection='list' item='item' index='index' separator=' ' open='case Id' close='end'>"
					+"when #{item.id} then #{item.fieldWeight}"
					+"</foreach>"

					+" where Id in"
					+"<foreach collection='list' index='index' item='item' separator=',' open='(' close=')'>"
					+"#{item.id,jdbcType=BIGINT}"
					+"</foreach>"
					+"</script>"
	})
	void batchUpdateSort(@Param("list")List<TraceFunFieldConfig> update_tffcList);

	@Update(startScript + " UPDATE trace_fun_config "
			+ " <set>"
			+ " <if test='showHidden !=null  '>  ShowHidden = #{showHidden} ,</if> "
			+ " <if test='isRequired !=null  '>  IsRequired  = #{isRequired} ,</if> "
			+ " <if test='defaultValue !=null  '>  DefaultValue  = #{defaultValue} ,</if> "
			+ " <if test='readOnly !=null  '>  ReadOnly  = #{readOnly} ,</if> "
			+ " </set>"
			+ " WHERE Id = #{id} "
			+ endScript
	)
	void updateField(TraceFunFieldConfig update_tffcList);

    
	@Select("select "+ALLFields+" from trace_fun_config where FunctionId=#{functionId} and TypeClass=1 order by FieldWeight ")
	List<TraceFunFieldConfig> selectDZFPartFieldsByFunctionId(@Param("functionId")String functionId);
    
	@Select("<script>"
			+ "select "+PARTFields+" from trace_fun_config where FunctionId=#{functionId}"
			     + "<if test='businessType !=null'> and TraceTemplateId = #{traceTemplateId} </if> "
				 + "and TypeClass=#{typeClass}"
				 + " order by FieldWeight"
			+"</script>"
			)
	List<TraceFunFieldConfig> selectPartNodeOrFunFields(TraceFunTemplateconfigQueryParam param);
    
	@Delete("delete from trace_fun_config where TraceTemplateId = #{traceTemplateId} and FunctionId=#{functionId}")
	void deleteByTraceTemplateIdAndFunctionId(@Param("traceTemplateId")String traceTemplateId, @Param("functionId")String functionId);
    
	@Delete("delete from trace_fun_config where TraceTemplateId = #{traceTemplateId} and FunctionId in (${strFunctionIds})")
	void deleteByTraceTemplateIdAndFunctionIds(@Param("traceTemplateId")String templateConfigId, @Param("strFunctionIds")String strFunctionIds);

	@Delete("delete from trace_fun_config where FunctionId=#{functionId} and TypeClass=1")
	void deleteDzFieldsByFunctionId(@Param("functionId")String functionId);

	@Select("select "+ALLFields+" from trace_fun_config where TraceTemplateId = #{traceTemplateId} and FunctionId=#{functionId} order by FieldWeight")
	List<TraceFunFieldConfig> selectPartTraceTemplateIdAndNodeFunctionId(@Param("traceTemplateId")String traceTemplateId, @Param("functionId")String functionId);

	@Update("update trace_fun_config set FunctionName=#{functionName} where FunctionId=#{functionId} and TypeClass=1")
	void updateDzGnFunctionNameByFunctionId(@Param("functionName")String functionName, @Param("functionId")String functionId);

	@Select("SELECT * FROM trace_fun_config WHERE TraceTemplateId=#{traceTemplateId} AND showHidden=1 ")
	List<TraceFunFieldConfig> selectByTraceTemplateId(@Param("traceTemplateId")String traceTemplateId);

	@Delete("delete from trace_fun_config where TraceTemplateId = #{traceTemplateId}")
	void deleteByTraceTemplateId(@Param("traceTemplateId")String traceTemplateId);

}
