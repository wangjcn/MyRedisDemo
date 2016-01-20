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
	// string ����
	private ValueOperations<String, String> valueOps;

	private Class<T> entityClass;

	private final HashMapper<T, String, String> entityMapper = new DecoratingStringHashMapper<T>(
			new JacksonHashMapper<T>((Class<T>) entityClass));

	public RedisBasic() {
		entityClass = (Class<T>) ((ParameterizedType) getClass()
				.getGenericSuperclass()).getActualTypeArguments()[0];
	}

	/**
	 * ��ѯ���ݸ����û����
	 * 
	 * @param key
	 * @return
	 */
	public List<T> findByUid(String key) {
		// test-user-1������ ���ݽṹΪkey=test-user-1 value=pid (Ϊ*) ��#�����ǲ�ѯ
		// test-user-1 ��value test-map-*->uid���� test-map-*map�µ�keyΪuid��ֵ
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
				return entityMapper.fromHash(map);// ת�� ��mapת����ʵ��
			}
		};
		System.out.println(redisMap("test-map-1").values()
				+ "======================");
		System.out.println(template.sort(query));
		System.out.println(template.sort(query, hm));
		return template.sort(query, hm);
	}

	/**
	 * ��ѯ�����û��������
	 * 
	 * @param key
	 * @return
	 */
	public List<T> findByList() {
		// test-list������ ���ݽṹΪkey=test-list value=pid (Ϊ*) �� test-map-*->uid
		// test-map-*->uid map�µ�keyΪuid��ֵ
		SortQuery<String> query = SortQueryBuilder.sort("test-list").noSort()
				.get("test-map-*->uid").get("test-map-*->content").build();
		BulkMapper<T, String> hm = new BulkMapper<T, String>() {
			public T mapBulk(List<String> bulk) {
				Map<String, String> map = new LinkedHashMap<String, String>();
				Iterator<String> iterator = bulk.iterator();
				// String pid=iterator.next();
				map.put("uid", iterator.next());
				map.put("content", iterator.next());
				return entityMapper.fromHash(map);// ת�� ��mapת����ʵ��
			}
		};
		System.out.println(redisMap("test-map-1").values()
				+ "=====================");// keyΪtest-map-1 �� ����
		System.out.println(template.sort(query));
		System.out.println(template.sort(query, hm));
		return template.sort(query, hm);
	}

	/**
	 * �������
	 * 
	 * @param uid
	 * @param post
	 */
	public void post(String uid, Post post) {
		// String uid = findKey(username);
		// post.setUid(uid);
		// add post��entityת����HASH
		String pid = String.valueOf(incrementAndGet("test"));
		redisMap("test-map-" + pid).putAll(entityMapper.toHash((T) post));
		redisList("test-user-" + uid).addFirst(pid);// �����û�Id��������Ķ���
		redisList("test-list").addFirst(pid);// �������е����¶���
	}

	/**
	 * ����String key
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
	 * �ж�key�Ƿ����
	 * 
	 * @param key
	 * @return
	 */
	public boolean hasKey(String key) {
		return template.hasKey(key);
	}

	/**
	 * ��ȡ��������
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
	 * ��ȡString������
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
	 * ��ȡSet������
	 * 
	 * @return
	 */
	private RedisSet<String> redisSet(String key) {
		return new DefaultRedisSet<String>(key, template);
	}

	/**
	 * ��ȡMAP������
	 * 
	 * @return
	 */
	private RedisMap<String, String> redisMap(String key) {
		return new DefaultRedisMap<String, String>(key, template);
	}

	/**
	 * ��ȡList������
	 * 
	 * @return
	 */
	private RedisList<String> redisList(String key) {
		return new DefaultRedisList<String>(key, template);
	}
}
