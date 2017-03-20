package com.chargeback.vo;

import java.util.Date;



public class Usage {

	
	private long usageId;
	private double cpu;
	private long memory;
	private long disk;
	private Date createdTime;
	private String orgName;
	private String appname;
	private String spaceName;
	private int instanceIndex;

	public long getUsageId() {
		return usageId;
	}

	public void setUsageId(long usageId) {
		this.usageId = usageId;
	}


	public double getCpu() {
		return cpu;
	}


	public void setCpu(double cpu) {
		this.cpu = cpu;
	}


	public long getMemory() {
		return memory;
	}


	public void setMemory(long memory) {
		this.memory = memory;
	}


	public long getDisk() {
		return disk;
	}


	public void setDisk(long disk) {
		this.disk = disk;
	}


	
	public Date getCreatedTime() {
		return createdTime;
	}


	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
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


	public int getInstanceIndex() {
		return instanceIndex;
	}


	public void setInstanceIndex(int instanceIndex) {
		this.instanceIndex = instanceIndex;
	}


	@Override
	public String toString() {
		return "Usage [id=" + usageId + ", cpu=" + cpu + ", memory=" + memory + ", disk=" + disk + ", time=" + createdTime
				+ ", orgName=" + orgName + ", appname=" + appname + ", spaceName=" + spaceName + ", instanceIndex="
				+ instanceIndex + "]";
	}

}
