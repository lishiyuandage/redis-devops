package com.bigbrotherlee.redis.admin.domain;

import java.time.LocalDateTime;

public class Database {
	private String id;
	private LocalDateTime modifytime;
	private String name;
	private String address;
	private String username;
	private String password;
	private String type;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public LocalDateTime getModifytime() {
		return modifytime;
	}

	public void setModifytime(LocalDateTime modifytime) {
		this.modifytime = modifytime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return "Database [id=" + id + ", modifytime=" + modifytime + ", name=" + name + ", address=" + address
				+ ", username=" + username + ", password=" + password + ", type=" + type + "]";
	}

	public static enum Type {
		SINGLON, CLUSTER, SENTINEL
	}

}
