package com.ttbs.huawei.mpls.discoveryprocessors.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DeviceStatus {


	private Map<String,String> deviceStatus;
	private Map<String,String> failureReason ; 
	private Map<String,Date> startTime; 
	private Map<String,Date> endTime; 
	private int deviceCount;
	private Boolean isDiscoveryCompleted; 
	private Boolean isScanStopped; 
	private Map<Long, Long> entityIdMap;
	
	
	public Map<Long, Long> getEntityIdMap() {
		return entityIdMap;
	}
	public void setEntityIdMap(Map<Long, Long> entityIdMap) {
		this.entityIdMap = entityIdMap;
	}
	/**
     * Default constructor
     */
    public DeviceStatus() {
    	deviceStatus = new HashMap<String, String>();
		failureReason = new HashMap<String, String>(); 
		startTime = new HashMap<String, Date>(); 
		endTime = new HashMap<String, Date>(); 
		isDiscoveryCompleted = false;
		isScanStopped = false;
		deviceCount = 0;
		entityIdMap = new HashMap<Long, Long>();
    }
	public Map<String, String> getDeviceStatus() {
		return deviceStatus;
	}
	public void setDeviceStatus(Map<String, String> deviceStatus) {
		this.deviceStatus = deviceStatus;
	}
	public Map<String, String> getFailureReason() {
		return failureReason;
	}
	public void setFailureReason(Map<String, String> failureReason) {
		this.failureReason = failureReason;
	}
	public Map<String, Date> getStartTime() {
		return startTime;
	}
	public void setStartTime(Map<String, Date> startTime) {
		this.startTime = startTime;
	}
	public Map<String, Date> getEndTime() {
		return endTime;
	}
	public void setEndTime(Map<String, Date> endTime) {
		this.endTime = endTime;
	}
	public int getDeviceCount() {
		return deviceCount;
	}
	public void setDeviceCount(int deviceCount) {
		this.deviceCount = deviceCount;
	}
	public Boolean getIsDiscoveryCompleted() {
		return isDiscoveryCompleted;
	}
	public void setIsDiscoveryCompleted(Boolean isDiscoveryCompleted) {
		this.isDiscoveryCompleted = isDiscoveryCompleted;
	}
	public Boolean getIsScanStopped() {
		return isScanStopped;
	}
	public void setIsScanStopped(Boolean isScanStopped) {
		this.isScanStopped = isScanStopped;
	}
}
