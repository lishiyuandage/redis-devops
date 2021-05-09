package com.bigbrotherlee.redis.admin.config;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.bigbrotherlee.redis.admin.domain.JsonVO;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(Exception.class)
	public JsonVO handleException(Exception exception) {
		return JsonVO.fail().setMsg("请求异常:"+exception.getMessage());
	}
}
