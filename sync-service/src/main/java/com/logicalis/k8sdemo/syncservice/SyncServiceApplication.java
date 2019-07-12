package com.logicalis.k8sdemo.syncservice;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class SyncServiceApplication {

	private static final Logger logger = LoggerFactory.getLogger(SyncServiceApplication.class);

	@Bean
	JedisConnectionFactory jedisConnectionFactory() {
		
		String redisHost = System.getenv().getOrDefault("CLUSTER_REDIS_HOSTNAME", "localhost");
		String redisPort = System.getenv().getOrDefault("CLUSTER-REDIS_PORT", "6379");
		logger.info("Connecting REDIS at {}:{}", redisHost, redisPort);
		JedisConnectionFactory jedisConFactory = new JedisConnectionFactory();
		jedisConFactory.setHostName(redisHost);
		jedisConFactory.setPort(Integer.parseInt(redisPort));
		return jedisConFactory;
	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate() {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(jedisConnectionFactory());
		return template;
	}

	public static void main(String[] args) {
		SpringApplication.run(SyncServiceApplication.class, args);
	}
}

@RestController()
@RequestMapping("/sync")
class EchoController {

	private static final Logger logger = LoggerFactory.getLogger(EchoController.class);

	@Value("${version:1.0}")
	public String version;

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	@GetMapping("/echo/{text}")
	public Map<String, String> echo(@Value("${message}") String echoTemplate, @PathVariable String text)
			throws UnknownHostException {

		logger.info("Received \"echo\" request for: {}", text);
		Map<String, String> response = new HashMap<>();

		String hostname = InetAddress.getLocalHost().getHostName();
		String message = echoTemplate.replaceAll("\\$name", text).replaceAll("\\$hostname", hostname).replaceAll("\\$version",
				version);

		response.put("message", message);
		response.put("version", version);
		response.put("hostname", hostname);

		return response;
	}

	@GetMapping("/shout/{text}")
	public Map<String, String> shout(@Value("${message}") String echoTemplate, @PathVariable String text)
			throws UnknownHostException {

		logger.info("Received \"shout\" request for: {}", text);
		Map<String, String> response = new HashMap<>();

		String hostname = InetAddress.getLocalHost().getHostName();
		String message = echoTemplate.replaceAll("\\$name", text).replaceAll("\\$hostname", hostname)
				.replaceAll("\\$version", version);

		response.put("message", message);
		response.put("version", version);
		response.put("hostname", hostname);

		redisTemplate.opsForList().rightPush("news", message);
		if (redisTemplate.opsForList().size("news") > 10)
			redisTemplate.opsForList().leftPop("news");

		return response;
	}
}
