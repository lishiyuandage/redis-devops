package com.bigbrotherlee.redis.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PageController {
	@RequestMapping({"/database","/","*"})
	public String index() {
		return "index.html";
	}
}
