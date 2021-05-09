package com.bigbrotherlee.redis.admin.domain;


public class Command {
	private String hostId;
	private String type;
	private String command;
	
	public String getHostId() {
		return hostId;
	}

	public void setHostId(String hostId) {
		this.hostId = hostId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	@Override
	public String toString() {
		return "Command [hostId=" + hostId + ", type=" + type + ", command=" + command + "]";
	}

	public static enum Type{
		DEL,KEYS,SET,GET,OTHER,
	}
	
}
