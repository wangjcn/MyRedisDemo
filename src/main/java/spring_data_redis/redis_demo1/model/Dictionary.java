package spring_data_redis.redis_demo1.model;

public class Dictionary implements Comparable{

	private String code_id;
	private String code_value;
	private String code_name;
	private String code_type_name;
	private String id;
	public String getCode_id() {
		return code_id;
	}
	public void setCode_id(String code_id) {
		this.code_id = code_id;
	}
	public String getCode_value() {
		return code_value;
	}
	public void setCode_value(String code_value) {
		this.code_value = code_value;
	}
	public String getCode_name() {
		return code_name;
	}
	public void setCode_name(String code_name) {
		this.code_name = code_name;
	}
	public String getCode_type_name() {
		return code_type_name;
	}
	public void setCode_type_name(String code_type_name) {
		this.code_type_name = code_type_name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	public int compareTo(Object o) {
		return this.getCode_value().compareTo(((Dictionary)o).getCode_value());
	}
	@Override
	public String toString() {
		return "Dictionary [code_id=" + code_id + ", code_value=" + code_value
				+ ", code_name=" + code_name + ", code_type_name="
				+ code_type_name + ", id=" + id + "]";
	}
	
}
