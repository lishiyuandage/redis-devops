package com.bigbrotherlee.redis.admin.controller;

import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bigbrotherlee.redis.admin.domain.Command;
import com.bigbrotherlee.redis.admin.domain.Database;
import com.bigbrotherlee.redis.admin.domain.JsonVO;

@RestController
@RequestMapping("command")
public class CommandController {
	private static final Logger log = LoggerFactory.getLogger(CommandController.class);

	@Autowired
	private DataController dataController;
	private Map<String, RedisTemplate<String, Serializable>> cache = new ConcurrentHashMap<String, RedisTemplate<String, Serializable>>();

	@Value("${redis.defaultExpTime:2592000}")
	private int defaultExpTime;
	
	@PostMapping("run")
	public JsonVO runCommand(@RequestBody Command command) {
		Assert.hasText(command.getHostId(),"host must not empty");
		Assert.hasText(command.getType(),"type must not empty");
		Assert.hasText(command.getCommand(), "command must not empty");
		Database database = (Database) dataController.getDatabseById(command.getHostId()).getData();
		if (database == null) {
			throw new IllegalArgumentException("this host not exist");
		}
		Command.Type type = null;
		try {
			type = Command.Type.valueOf(command.getType());
		} catch (Exception e) {
			throw new IllegalArgumentException("this command type not exist");
		}
		log.info("start run command :"+command.getCommand());
		RedisTemplate<String, Serializable> redisTemplate = getRedisTemplate(database);
		//
		Object result = null;
		switch (type) {
		case DEL:
			result = redisTemplate.delete(command.getCommand());
			break;
		case GET:
			result = redisTemplate.opsForValue().get(command.getCommand());
			break;
		case KEYS:
			result = redisTemplate.keys(command.getCommand());
			break;
		case SET:
			String[] commands =command.getCommand().split("\\s+");
			if(commands.length < 2) {
				throw new IllegalArgumentException("you must set key and value");
			}
			long time = commands.length > 2 ? time = Integer.valueOf(commands[2]) :defaultExpTime;
			redisTemplate.opsForValue().set(commands[0],commands[1],time, TimeUnit.SECONDS);
			break;
		case OTHER:
			String[] commandAndArgs =command.getCommand().split("\\s+");
			byte[][] args = Arrays.asList(Arrays.copyOfRange(commandAndArgs, 1, commandAndArgs.length)).stream().map(String::getBytes).toArray(byte[][]::new);
			
			result = redisTemplate.execute(new RedisCallback<Object>() {
				@Override
				public Object doInRedis(RedisConnection connection) throws DataAccessException {
					return connection.execute(commandAndArgs[0], args);
				}
			});
			break;
		}
		log.info("finish run command :"+command.getCommand());
		return JsonVO.success().setMsg("命令执行成功").setData(result);
	}
	public RedisTemplate<String, Serializable> getRedisTemplate(Database database) {
		RedisTemplate<String, Serializable> template = cache.get(database.getId());
		if (template == null) {
			LettuceConnectionFactory connectionFactory = createConnectionFactory(database);
			RedisTemplate<String, Serializable> redisTemplate = new RedisTemplate<>();
			redisTemplate.setKeySerializer(new StringRedisSerializer(Charset.forName("utf-8")));
			redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
			redisTemplate.setConnectionFactory(connectionFactory);
			redisTemplate.afterPropertiesSet();
			cache.put(database.getId(), redisTemplate);
			template = redisTemplate;
			log.info("create new redisTemplate:"+database.getId());
		}
		return template;
	}

	private LettuceConnectionFactory createConnectionFactory(Database database) {
		Database.Type type = null;
		try {
			type = Database.Type.valueOf(database.getType());
		} catch (Exception e) {
			throw new RuntimeException("database type error : " + database.getType());
		}
		LettuceConnectionFactory connectionFactory = null;
		// 连接池
		LettucePoolingClientConfiguration clientConfiguration = LettucePoolingClientConfiguration.defaultConfiguration();

		switch (type) {
		case CLUSTER:
			RedisClusterConfiguration redisClusterConfiguration = new RedisClusterConfiguration(Arrays.asList(database.getAddress().split(",")));
			redisClusterConfiguration.setPassword(database.getPassword());
			redisClusterConfiguration.setUsername(database.getUsername());
			redisClusterConfiguration.setMaxRedirects(3);
			connectionFactory = new LettuceConnectionFactory(redisClusterConfiguration,clientConfiguration);
			break;
		case SENTINEL:
			String[] address = database.getAddress().split(",");
			String master = address[0];
			String[] sentinel = Arrays.copyOfRange(address, 0, address.length);
			RedisSentinelConfiguration redisSentinelConfiguration = new RedisSentinelConfiguration(master,Arrays.asList(sentinel).stream().collect(Collectors.toSet()));
			redisSentinelConfiguration.setMaster(database.getName());
			redisSentinelConfiguration.setUsername(database.getUsername());
			redisSentinelConfiguration.setPassword(database.getPassword());
			connectionFactory = new LettuceConnectionFactory(redisSentinelConfiguration, clientConfiguration);
			break;
		case SINGLON:
			String[] hostAndPort=database.getAddress().split(":");
			RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration(hostAndPort[0], Integer.valueOf(hostAndPort[1]));
			redisStandaloneConfiguration.setUsername(database.getUsername()); 
			redisStandaloneConfiguration.setUsername(database.getPassword()); 
			connectionFactory = new LettuceConnectionFactory(redisStandaloneConfiguration,clientConfiguration);
			break;
		}
		connectionFactory.afterPropertiesSet();
		return connectionFactory;
	}

	public void removeRedisTemplate(String id) {
		RedisTemplate<String, Serializable> template = cache.get(id);
		if (template != null) {
			// TODO 做销毁操作
			cache.remove(id);
		}
	}

}
