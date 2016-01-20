package redis_demo1.action;

import java.lang.reflect.ParameterizedType;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.BulkMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.query.SortQuery;
import org.springframework.data.redis.core.query.SortQueryBuilder;
import org.springframework.data.redis.hash.DecoratingStringHashMapper;
import org.springframework.data.redis.hash.HashMapper;
import org.springframework.data.redis.hash.JacksonHashMapper;
import org.springframework.data.redis.support.atomic.RedisAtomicLong;
import org.springframework.data.redis.support.collections.DefaultRedisList;
import org.springframework.data.redis.support.collections.DefaultRedisMap;
import org.springframework.data.redis.support.collections.DefaultRedisSet;
import org.springframework.data.redis.support.collections.RedisList;
import org.springframework.data.redis.support.collections.RedisMap;
import org.springframework.data.redis.support.collections.RedisSet;

import redis_demo1.model.Post;

public class RedisBasic<T> {

//	@Resource
	private StringRedisTemplate template;
	// string 操作
	private ValueOperations<String, String> valueOps;

	private Class<T> entityClass;

	private final HashMapper<T, String, String> entityMapper = new DecoratingStringHashMapper<T>(
			new JacksonHashMapper<T>((Class<T>) entityClass));

	public RedisBasic() {
		entityClass = (Class<T>) ((ParameterizedType) getClass()
				.getGenericSuperclass()).getActualTypeArguments()[0];
	}

	/**
	 * 查询数据根据用户编号
	 * 
	 * @param key
	 * @return
	 */
	public List<T> findByUid(String key) {
		// test-user-1中数据 数据结构为key=test-user-1 value=pid (为*) ；#代表是查询
		// test-user-1 的value test-map-*->uid代表 test-map-*map下的key为uid的值
		SortQuery<String> query = SortQueryBuilder.sort("test-user-1").noSort()
				.get("#").get("test-map-*->uid").get("test-map-*->content")
				.build();
		BulkMapper<T, String> hm = new BulkMapper<T, String>() {
			public T mapBulk(List<String> bulk) {
				Map<String, String> map = new LinkedHashMap<String, String>();
				Iterator<String> iterator = bulk.iterator();
				String pid = iterator.next();
				map.put("uid", iterator.next());
				map.put("content", iterator.next());
				return entityMapper.fromHash(map);// 转型 将map转换成实体
			}
		};
		System.out.println(redisMap("test-map-1").values()
				+ "======================");
		System.out.println(template.sort(query));
		System.out.println(template.sort(query, hm));
		return template.sort(query, hm);
	}

	/**
	 * 查询所有用户添加数据
	 * 
	 * @param key
	 * @return
	 */
	public List<T> findByList() {
		// test-list中数据 数据结构为key=test-list value=pid (为*) ； test-map-*->uid
		// test-map-*->uid map下的key为uid的值
		SortQuery<String> query = SortQueryBuilder.sort("test-list").noSort()
				.get("test-map-*->uid").get("test-map-*->content").build();
		BulkMapper<T, String> hm = new BulkMapper<T, String>() {
			public T mapBulk(List<String> bulk) {
				Map<String, String> map = new LinkedHashMap<String, String>();
				Iterator<String> iterator = bulk.iterator();
				// String pid=iterator.next();
				map.put("uid", iterator.next());
				map.put("content", iterator.next());
				return entityMapper.fromHash(map);// 转型 将map转换成实体
			}
		};
		System.out.println(redisMap("test-map-1").values()
				+ "=====================");// key为test-map-1 的 数据
		System.out.println(template.sort(query));
		System.out.println(template.sort(query, hm));
		return template.sort(query, hm);
	}

	/**
	 * 添加数据
	 * 
	 * @param uid
	 * @param post
	 */
	public void post(String uid, Post post) {
		// String uid = findKey(username);
		// post.setUid(uid);
		// add post将entity转换成HASH
		String pid = String.valueOf(incrementAndGet("test"));
		redisMap("test-map-" + pid).putAll(entityMapper.toHash((T) post));
		redisList("test-user-" + uid).addFirst(pid);// 操作用户Id保存操作的对象
		redisList("test-list").addFirst(pid);// 保存所有的文章对象
	}

	/**
	 * 查找String key
	 * 
	 * @param key
	 * @return
	 */
	public String findKey(String key) {
		return valueOps().get(key);
	}

	public void addListOne(String key, String value) {
		redisList(key).addFirst(value);
	}

	/**
	 * 判断key是否存在
	 * 
	 * @param key
	 * @return
	 */
	public boolean hasKey(String key) {
		return template.hasKey(key);
	}

	/**
	 * 获取自增索引
	 * 
	 * @param key
	 * @return
	 */
	public String incrementAndGet(String key) {
		if (StringUtils.isNotEmpty(key)) {
			RedisAtomicLong entityIdCounter = new RedisAtomicLong(key,
					template.getConnectionFactory());
			return String.valueOf(entityIdCounter.incrementAndGet());
		} else
			return "";

	}

	/**
	 * 获取String操作项
	 * 
	 * @return
	 */
	private ValueOperations<String, String> valueOps() {
		if (valueOps != null) {
			valueOps = template.opsForValue();
		}
		return valueOps;
	}

	/**
	 * 获取Set操作项
	 * 
	 * @return
	 */
	private RedisSet<String> redisSet(String key) {
		return new DefaultRedisSet<String>(key, template);
	}

	/**
	 * 获取MAP操作项
	 * 
	 * @return
	 */
	private RedisMap<String, String> redisMap(String key) {
		return new DefaultRedisMap<String, String>(key, template);
	}

	/**
	 * 获取List操作项
	 * 
	 * @return
	 */
	private RedisList<String> redisList(String key) {
		return new DefaultRedisList<String>(key, template);
	}
}
