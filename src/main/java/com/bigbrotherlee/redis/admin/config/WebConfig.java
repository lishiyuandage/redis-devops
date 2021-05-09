package com.bigbrotherlee.redis.admin.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class WebConfig {
	private List<String> allowDomains =Arrays.asList("https://bigbrotherlee.com","http://localhost:8080");
	private List<String> allowHeaders =Arrays.asList("Authorization","Cookie","Token","content-type");
	private List<String> exposedHeaders=Arrays.asList("Content-Disposition");
	
	@Bean
	public CorsFilter corsFilter() {
		// 1.添加CORS配置信息
		CorsConfiguration config = new CorsConfiguration();
		// 1) 允许通过的域,不要写*，否则cookie就无法使用了
		allowDomains.forEach(config::addAllowedOrigin);
		// 2) 是否发送Cookie信息
		config.setAllowCredentials(true);
		// 3) 允许的请求方式
		config.addAllowedMethod("*");
		// 4）允许的头信息
		allowHeaders.forEach(config::addAllowedHeader);
		config.setExposedHeaders(exposedHeaders);
		// 2.添加映射路径，我们拦截一切请求
		UrlBasedCorsConfigurationSource configSource = new UrlBasedCorsConfigurationSource();
		configSource.registerCorsConfiguration("/**", config);
		// 3.返回新的CorsFilter.
		return new CorsFilter(configSource);
	}
}