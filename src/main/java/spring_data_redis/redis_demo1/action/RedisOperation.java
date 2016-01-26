package spring_data_redis.redis_demo1.action;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.BulkMapper;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.query.SortQuery;
import org.springframework.data.redis.core.query.SortQueryBuilder;
import org.springframework.data.redis.hash.DecoratingStringHashMapper;
import org.springframework.data.redis.hash.HashMapper;
import org.springframework.data.redis.hash.JacksonHashMapper;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import spring_data_redis.redis_demo1.model.Dictionary;
import spring_data_redis.redis_demo1.model.Post;
import spring_data_redis.redis_demo1.model.User;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring/*.xml")
public class RedisOperation {

	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	@Autowired
	private JdbcTemplate jdbctemplate;
	private final HashMapper<Post, String, String> entityMapper = new DecoratingStringHashMapper<Post>(
			new JacksonHashMapper<Post>(Post.class));

	@Before
	public void init() throws UnsupportedEncodingException {
		// stringRedisTemplate = ctx.getBean("stringRedisTemplate",
		// StringRedisTemplate.class);
	}

	@Test
	public void testSpringRedis() {
		ConfigurableApplicationContext ctx = null;
		try {
			// String操作
			stringRedisTemplate.delete("myStr");
			stringRedisTemplate.opsForValue().set("myStr",
					"http://yjmyzz.cnblogs.com/");
			System.out.println(stringRedisTemplate.opsForValue().get("myStr"));
			System.out.println("---------------");

			// List操作
			stringRedisTemplate.delete("myList");
			stringRedisTemplate.opsForList().rightPush("myList", "A");
			stringRedisTemplate.opsForList().rightPush("myList", "B");
			stringRedisTemplate.opsForList().leftPush("myList", "0");
			List<String> listCache = stringRedisTemplate.opsForList().range(
					"myList", 0, -1);
			for (String s : listCache) {
				System.out.println(s);
			}
			System.out.println("---------------");

			// Set操作
			stringRedisTemplate.delete("mySet");
			stringRedisTemplate.opsForSet().add("mySet", "A");
			stringRedisTemplate.opsForSet().add("mySet", "B");
			stringRedisTemplate.opsForSet().add("mySet", "C");
			Set<String> setCache = stringRedisTemplate.opsForSet().members(
					"mySet");
			for (String s : setCache) {
				System.out.println(s);
			}
			System.out.println("---------------");

			// Hash操作
			stringRedisTemplate.delete("myHash");
			stringRedisTemplate.opsForHash().put("myHash", "PEK", "PEK");
			stringRedisTemplate.opsForHash().put("myHash", "SHA", "SHA");
			stringRedisTemplate.opsForHash().put("myHash", "PVG", "PVG");
			Map<Object, Object> hashCache = stringRedisTemplate.opsForHash()
					.entries("myHash");
			for (Map.Entry<Object, Object> entry : hashCache.entrySet()) {
				System.out.println(entry.getKey() + " - " + entry.getValue());
			}

			System.out.println("---------------");

		} finally {
			if (ctx != null && ctx.isActive()) {
				ctx.close();
			}
		}

	}

	@Test
	public void testSpringRedis1() {
		ConfigurableApplicationContext ctx = null;
		try {
			// Hash数据
			stringRedisTemplate.delete("myHash");
			stringRedisTemplate.opsForHash().put("sex", "0", "男");
			stringRedisTemplate.opsForHash().put("sex", "1", "女");
		} finally {
			if (ctx != null && ctx.isActive()) {
				ctx.close();
			}
		}

	}

	@Test
	public void testSpringRedis2() {
		ConfigurableApplicationContext ctx = null;
		try {
			List<Dictionary> dicList = jdbctemplate.query(
					"select * from cp_t_at_code",
					new BeanPropertyRowMapper<Dictionary>(Dictionary.class));
			for (Dictionary dic : dicList) {
				System.out.println(dic.toString());
				// Hash操作
				stringRedisTemplate.opsForHash().put(dic.getCode_id(),
						dic.getCode_value(), dic.getCode_name());
			}
			/*
			 * Map<Object, Object> map =
			 * stringRedisTemplate.opsForHash().entries("sex"); for
			 * (Map.Entry<Object, Object> entry : map.entrySet()) {
			 * System.out.println(entry.getKey() + " - " + entry.getValue()); }
			 */

		} finally {
			if (ctx != null && ctx.isActive()) {
				ctx.close();
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testSpringRedis3() {
		ConfigurableApplicationContext ctx = null;
		try {
			Map<Object, Object> map = stringRedisTemplate.opsForHash().entries(
					"payement_type");
			List<Dictionary> dicList = new ArrayList<Dictionary>();
			for (Map.Entry<Object, Object> me : map.entrySet()) {
				System.out.println(me.getKey());
				Dictionary dic = new Dictionary();
				dic.setCode_value(me.getKey().toString());
				dic.setCode_name(me.getValue().toString());
				dicList.add(dic);
				dic.setCode_id("payement_type");
			}
			// stringRedisTemplate.sort(query)
			System.out.println(dicList.toString());
			Collections.sort(dicList);
			System.out.println(dicList.toString());
		} finally {
			if (ctx != null && ctx.isActive()) {
				ctx.close();
			}
		}
	}

	@Test
	public void testSpringRedis4() {
		ConfigurableApplicationContext ctx = null;
		try {
			stringRedisTemplate.opsForHash().delete("payement_type",
					new Object[] { "2", "3" });
		} finally {
			if (ctx != null && ctx.isActive()) {
				ctx.close();
			}
		}
	}

	/**
	 * 
	 * 
	 * @param key
	 * @return
	 */
	public String incrementAndGet(String key) {
		// String key = "a";
		if (StringUtils.isNotEmpty(key)) {
			RedisAtomicLong entityIdCounter = new RedisAtomicLong(key,
					stringRedisTemplate.getConnectionFactory());
			String str = String.valueOf(entityIdCounter.incrementAndGet());
			System.out.println(str);
			return str;
		}
		return null;
	}

	/**
	 *
	 * 
	 * @param uid
	 * @param post
	 */
	@Test
	public void post() {
		// String uid = findKey(username);
		// post.setUid(uid);
		// add post将entity转换成HASH
		int i = 1;
		while (i < 10) {
			i++;
			String pid = String.valueOf(incrementAndGet("a"));
			// redisMap("test-map-"+pid).putAll(entityMapper.toHash((T) post));
			Post post = new Post("helloworld", "1001" + pid, "1001", "1001"
					+ pid);
			stringRedisTemplate.boundHashOps("test-map-" + pid).putAll(
					entityMapper.toHash(post));
			stringRedisTemplate.boundListOps("test-list").leftPush(pid);
		}
		String pid = String.valueOf(incrementAndGet("a"));
		// stringRedisTemplate.boundHashOps("test-user-"+uid).addFirst(pid);//
	}

	/**
	 *
	 * 
	 * @param key
	 * @return
	 */
	@Test
	public void findByList() {
		// test-list中数据 数据结构为key=test-list value=pid (为*) ； test-map-*->uid
		// test-map-*->uid map下的key为uid的值
		// SortQueryBuilder sq=SortQueryBuilder.sort("test-list");
		// SortCriterion sc =sq.noSort();
		// sc.get("");
		SortQuery<String> query = SortQueryBuilder.sort("test-list").by("")
				.get("test-map-*->uid").get("test-map-*->content")
				.get("test-map-*->replyUid").build();
		// SortQueryBuilder.sort("").noSort().
		BulkMapper<Post, String> hm = new BulkMapper<Post, String>() {
			public Post mapBulk(List<String> bulk) {
				Map<String, String> map = new LinkedHashMap<String, String>();
				Iterator<String> iterator = bulk.iterator();
				// String pid=iterator.next();
				map.put("uid", iterator.next());
				map.put("content", iterator.next());
				map.put("replyUid", iterator.next());
				return entityMapper.fromHash(map);// 转型 将map转换成实体
			}
		};
		System.out.println(stringRedisTemplate.boundHashOps("test-map-12")
				.values() + "=====================");// key为test-map-1 的 数据
		// System.out.println(stringRedisTemplate.sort(query));
		System.out.println(stringRedisTemplate.sort(query, hm));
	}

	/**
	 * ===============查询所有的key===============
	 */
	@Test
	public void testForQueryAllKey() {
		Set<String> set = stringRedisTemplate.keys(new String("*"));
		for (String str : set) {
			System.out.println(str + "--" + stringRedisTemplate.type(str));
		}
	}

	/**
	 * ===============列表操作==================
	 */
	// 查询列表
	@Test
	public void testForQueryList() {
		List<String> list = stringRedisTemplate.opsForList().range("test-list",
				0, -1);
		List<String> list1 = stringRedisTemplate.boundListOps("test-list")
				.range(0, -1);
		// stringRedisTemplate.delete("test-list");
		for (String str : list) {
			System.out.println(str);
		}
		for (String str1 : list1) {
			System.out.println(str1);
		}
		// System.out.println("-----------------");
		// System.out.println(new
		// SimpleDateFormat("yyyy-mm-dd HH:mm:ss:SSS").format(new Date()));
		//
		// System.out.println(stringRedisTemplate.opsForList().leftPop("test-list",
		// 60, TimeUnit.SECONDS));
		// System.out.println(new
		// SimpleDateFormat("yyyy-mm-dd HH:mm:ss:SSS").format(new Date()));

	}

	// 列表插入
	@Test
	public void testForAddList() {
		// 1
		// stringRedisTemplate.opsForList().leftPush("test-list", "a");
		// 2
		// stringRedisTemplate.opsForList().rightPush("test-list", "b");
	}

	//
	@Test
	public void testForAddHash() {
		String dicSeq = "";
		// redisMap("test-map-" + pid).putAll(entityMapper.toHash((T) post));
		HashMapper<Dictionary, String, String> mapper = new DecoratingStringHashMapper<Dictionary>(
				new JacksonHashMapper<Dictionary>(Dictionary.class));
		// stringRedisTemplate.opsForHash().putAll("",
		// new HashMap<String, String>());
		List<Dictionary> dicList = jdbctemplate.query(
				"select * from cp_t_at_code",
				new BeanPropertyRowMapper<Dictionary>(Dictionary.class));
		for (Dictionary dic : dicList) {
			dicSeq = String.valueOf(incrementAndGet("dicSeq"));
			System.out.println(dic.toString());
			// Hash操作
			// stringRedisTemplate.opsForHash().put(":"+dic.getCode_id(),
			// dic.getCode_value(), dic.getCode_name());
			stringRedisTemplate.opsForHash().putAll(
					"dictionary:" + dic.getCode_id() + ":" + dicSeq,
					mapper.toHash(dic));
			// redisList("test-list").addFirst(pid);//
		}
	}

	// 根据条件查询所有的hash数据
	@Test
	public void testForQueryHash() {
		Set<String> set = stringRedisTemplate.keys("dictionary*");
		for (String str : set) {
			System.out.println(str);
			Map<Object, Object> map = stringRedisTemplate.opsForHash().entries(
					str);
			System.out.println(map.get("code_id") + "--" + map.get("code_name")
					+ "--" + map.get("code_value"));
		}

	}

	// 查询指定key的hash数据
	@SuppressWarnings("unused")
	@Test
	public void testForQueryHash2() {
		Set<String> set = stringRedisTemplate.keys("dictionary*");
		Map map = stringRedisTemplate.opsForHash().entries(
				"dictionary:card_change_type:71");
		System.out.println(map);
	}

	// 根据条件更新hash数据
	@SuppressWarnings("rawtypes")
	@Test
	public void testForUpdateHash1() {
		Set set = stringRedisTemplate.opsForHash().keys(
				"dictionary:card_change_type:71");
		for (Object obj : set) {
			System.out.println(obj);
		}
		stringRedisTemplate.opsForHash().delete(
				"dictionary:card_change_type:71",
				stringRedisTemplate.opsForHash()
						.keys("dictionary:card_change_type:71").toArray());
		System.out.println();
	}

	// 测试事物
	@Test
	public void testForTransaction() {
		stringRedisTemplate.setEnableTransactionSupport(true);// 奇怪的是一定要再显示开启redistemplate的事务支持
		stringRedisTemplate.multi();
		stringRedisTemplate.boundValueOps("somevkey").increment(1);
		stringRedisTemplate.boundZSetOps("somezkey").add("zvalue", 11);
		stringRedisTemplate.exec();
		System.out.println();
	}

	@Test
	public void testMultiDiscard1() throws Exception {
		stringRedisTemplate.execute(new RedisCallback<Object>() {
			public Object doInRedis(RedisConnection connection)
					throws DataAccessException {
//				connection.openPipeline();
				connection.multi();
				connection.set("a".getBytes(), "b".getBytes());
				connection.set("a".getBytes(), "bbb".getBytes());
				connection.append("a".getBytes(), "ccc".getBytes());
				connection.hSet("a1".getBytes(), "field1".getBytes(),
						"1000".getBytes());
				connection.hIncrBy("a1".getBytes(), "field1".getBytes(), 10);
				Map<byte[], byte[]> args = new HashMap<byte[], byte[]>(3);
				args.put("field2".getBytes(), "valueX".getBytes());
				args.put("field3".getBytes(), "valueY".getBytes());
				args.put("field4".getBytes(), "000".getBytes());
				connection.hMSet("a2".getBytes(), args);
				connection.append("a3".getBytes(), "bunny".getBytes());
				connection.discard();
				connection.exec();
				return null;
			}
		}, true);
	}
	
	  /**
	   * 在连接池环境中，需要借助sessionCallback来绑定connection
	   */
		@SuppressWarnings("unused")
		@Test
	  public void txUsedPoolSample(){
//	    final String dicSeq;
		final List<Dictionary> dicList = jdbctemplate.query(
				"select * from cp_t_at_code",
				new BeanPropertyRowMapper<Dictionary>(Dictionary.class));
		String dicSeq  = String.valueOf(incrementAndGet("dicSeq"));
		final HashMapper<Dictionary, String, String> mapper = new DecoratingStringHashMapper<Dictionary>(
				new JacksonHashMapper<Dictionary>(Dictionary.class));
	    SessionCallback<User> sessionCallback = new SessionCallback<User>() {
	      public User execute(RedisOperations operations) throws DataAccessException {
	        operations.multi();
	        int i = 1;
	        for(Dictionary dic : dicList){
	        	Map<byte[], byte[]> map = new HashMap<byte[], byte[]>();
	        	map.put("code_id".getBytes(), dic.getCode_id().getBytes());
	        	map.put("code_name".getBytes(), dic.getCode_name().getBytes());
	        	operations.opsForHash().putAll("dictionary:"+dic.getCode_id()+":"+(i++), mapper.toHash(dic));
	        	operations.opsForHash();
	        }
//	        oper.expire(60, TimeUnit.MINUTES);//设置过期时间
	        operations.exec();
	        return null;
	      }
	    };
	    stringRedisTemplate.execute(sessionCallback);
	  }

}
