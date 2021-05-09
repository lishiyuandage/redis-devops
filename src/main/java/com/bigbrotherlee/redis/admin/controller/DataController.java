package com.bigbrotherlee.redis.admin.controller;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.bigbrotherlee.redis.admin.domain.Database;
import com.bigbrotherlee.redis.admin.domain.JsonVO;
import com.bigbrotherlee.redis.admin.domain.PageVO;

@RestController
@RequestMapping("data")
public class DataController{
	private static final Logger log = LoggerFactory.getLogger(DataController.class);

	private static final String PATH = "database.json";
	@Autowired
	private CommandController commandController;
	private Map<String, Database> cache =  new ConcurrentHashMap<String, Database>();
	
	
	@PostConstruct
	public void init() {
		log.info("start loading cache------------");
		Path path = Paths.get(PATH);
		if(Files.notExists(path)) {
			try {
				Files.createFile(path);
			} catch (IOException e) {
				log.error("create databse file dabase.json fail:"+e.getMessage());
			}
		}
		try {
			byte[] bytes = Files.readAllBytes(path);
			String databse = new String(bytes,Charset.forName("utf-8"));
			List<Database> databases = JSON.parseArray(databse, Database.class);
			if(!CollectionUtils.isEmpty(databases)) {
				cache.putAll(databases.stream().collect(Collectors.toMap(Database::getId, i->i)));
			}			
		} catch (IOException e) {
			log.error("read databse file dabase.json fail:"+e.getMessage());
		}
		
		log.info("load cache end------------");
	}
	
	@EventListener(value = ContextStoppedEvent.class)
	public void stop(ContextStoppedEvent event) {
		Path path = Paths.get(PATH);
		if(Files.notExists(path)) {
			try {
				Files.createFile(path);
			} catch (IOException e) {
				log.error("create databse file dabase.json fail:"+e.getMessage());
			}
		}
		String database = JSON.toJSONString(cache.values());
		try {
			Files.write(path, database.getBytes(Charset.forName("utf-8")));
		} catch (IOException e) {
			log.error("write databse file dabase.json fail:"+e.getMessage());
		}
	}
	
	@GetMapping("/get/{id}")
	public JsonVO getDatabseById(@PathVariable String id) {
		return JsonVO.success().setData(cache.get(id)).setMsg("请求处理成功");
	}
	
	private static final int LENGTH = 9;
	
	@GetMapping("find/{key}/{page}")
	public JsonVO find(@PathVariable("key") String key, @PathVariable("page") int page){
		int start = LENGTH * (page - 1);
		PageVO<Database> pageInfo=new PageVO<>();
		//全部
		if(key.equals("$")) {
			int total = cache.values().size();
			List<Database> list = cache.values().stream().skip(start).limit(LENGTH).collect(Collectors.toList());
			pageInfo.setList(list);
			pageInfo.setTotal(total);
			pageInfo.setPage(page);
			pageInfo.setPages(total % LENGTH == 0 ? (total / LENGTH) : (total / LENGTH + 1));
		}else {
			Long total = cache.values().stream().filter(i -> i.getName().contains(key)).count();
			List<Database> list = cache.values().stream().filter(i -> i.getName().contains(key)).skip(start).limit(LENGTH).collect(Collectors.toList());
			pageInfo.setList(list);
			pageInfo.setTotal(total.intValue());
			pageInfo.setPage(page);
			pageInfo.setPages(total.intValue() % LENGTH == 0 ? (total.intValue() / LENGTH) : (total.intValue() / LENGTH + 1));
		}
		return JsonVO.success().setData(pageInfo).setMsg("请求处理成功");
	}
	
	@PostMapping("/add")
	public JsonVO addDatabase(@RequestBody Database database) {
		Assert.hasText(database.getAddress(),"address must not empty");
		Assert.hasText(database.getType(),"type must not empty");
		Assert.hasText(database.getName(), "name must not empty");
		if(cache.values().stream().anyMatch(i->i.getAddress().equals(database.getAddress()) && i.getType().equals(database.getType()))) {
			throw new IllegalArgumentException("this address and type already have record.");
		}else {
			database.setId(UUID.randomUUID().toString());
			database.setModifytime(LocalDateTime.now());
			cache.put(database.getId(), database);
		}		
		return JsonVO.success().setMsg("添加成功");
	} 
	@GetMapping("/all")
	public JsonVO allDatabase() {
		List<Database> list =cache.values().stream().map(data -> {
			Database target = new Database();
			BeanUtils.copyProperties(data, target);
			target.setAddress(null);
			target.setModifytime(null);
			target.setPassword(null);
			target.setUsername(null);
			return target;
		}).collect(Collectors.toList());
		return JsonVO.success().setMsg("请求成功").setData(list);
	}
	@DeleteMapping("/del/{id}")
	public JsonVO delDatabase(@PathVariable String id) {
		commandController.removeRedisTemplate(id);
		Database data = cache.remove(id);		
		return JsonVO.success().setMsg("已删除").setData(data);
	}
	@PutMapping("/update")
	public JsonVO updateDatabase(@RequestBody Database database) {
		if(database.getId() == null) {
			return addDatabase(database);
		}
		if(!cache.values().stream().anyMatch(i->i.getId().equals(database.getId()))) {
			throw new IllegalArgumentException("databse id not exist");
		}
		commandController.removeRedisTemplate(database.getId());
		database.setModifytime(LocalDateTime.now());
		cache.put(database.getId(), database);
		return JsonVO.success().setMsg("修改成功");
	}
	
}
