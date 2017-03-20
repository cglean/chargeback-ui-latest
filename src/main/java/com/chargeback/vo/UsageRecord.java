package com.chargeback.vo;

public class UsageRecord {

	private String cpu;
	private String memory;
	private String disk;
	private String orgName;
	private String appname;
	private String spaceName;
	private String summary;
	private String instanceIndex;

		
	public String getCpu() {
		return cpu;
	}

	public void setCpu(String cpu) {
		this.cpu = cpu;
	}

	public String getMemory() {
		return memory;
	}

	public void setMemory(String memory) {
		this.memory = memory;
	}

	public String getDisk() {
		return disk;
	}

	public void setDisk(String disk) {
		this.disk = disk;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getAppname() {
		return appname;
	}

	public void setAppname(String appname) {
		this.appname = appname;
	}

	public String getSpaceName() {
		return spaceName;
	}

	public void setSpaceName(String spaceName) {
		this.spaceName = spaceName;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getInstanceIndex() {
		return instanceIndex;
	}

	public void setInstanceIndex(String instanceIndex) {
		this.instanceIndex = instanceIndex;
	}
	
}
