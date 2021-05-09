package com.bigbrotherlee.redis.admin.domain;

public class JsonVO {
	public final static int SUCCESS = 1;
	public final static int FAIL = 0;
	private int status;
	private String msg;
	private Object data;
	
	private JsonVO(int status) {
		this.status=status;
	}
	
	public static JsonVO success() {
		return new JsonVO(SUCCESS);
	}
	public static JsonVO fail() {
		return new JsonVO(FAIL);
	}
	
	
	public int getStatus() {
		return status;
	}
	public String getMsg() {
		return msg;
	}
	public JsonVO setMsg(String msg) {
		this.msg = msg;
		return this;
	}
	public Object getData() {
		return data;
	}
	public JsonVO setData(Object data) {
		this.data = data;
		return this;
	}
	@Override
	public String toString() {
		return "JsonVO [status=" + status + ", msg=" + msg + ", data=" + data + "]";
	}
	
	
}
