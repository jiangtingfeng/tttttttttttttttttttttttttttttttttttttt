package com.jgw.supercodeplatform.trace.common.util;

import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;

import org.hibernate.validator.HibernateValidator;

public class ValidationUtils {

	 
	/**
     * 使用hibernate的注解来进行验证
     */
    private static Validator validator = Validation
            .byProvider(HibernateValidator.class).configure().failFast(true).buildValidatorFactory().getValidator();
 
    /**
     * 功能描述: <br>
     * 〈注解验证参数〉
     *
     * @param obj
     */
    public static <T> void validate(T obj) {
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(obj);
        // 抛出检验异常
        if (constraintViolations.size() > 0) {
        	throw new RuntimeException("错啦");
        }
    }
}
