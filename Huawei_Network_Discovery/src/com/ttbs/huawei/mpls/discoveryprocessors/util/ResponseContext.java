package com.ttbs.huawei.mpls.discoveryprocessors.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oracle.communications.integrity.scanCartridges.sdk.ProcessorException;
import oracle.communications.inventory.api.entity.PhysicalDevice;

public class ResponseContext {

	private Map<String, String> deviceStatus;
	private Map<String, String> failureReason;
	private Map<String, Date> startTime;
	private Map<String, Date> endTime;
	private int deviceCount;
	private Boolean isDiscoveryCompleted;
	private Boolean isScanStopped;
	private NIDiscoveryContext niDiscoveryContext;
	private List<PhysicalDevice> physicalDevices = new ArrayList<>();
	public List<PhysicalDevice> getPhysicalDevices() {
		return physicalDevices;
	}

	/**
	 * Default constructor
	 * 
	 * @throws ProcessorException
	 */
	public ResponseContext() throws ProcessorException {
		deviceStatus = new HashMap<String, String>();
		failureReason = new HashMap<String, String>();
		startTime = new HashMap<String, Date>();
		endTime = new HashMap<String, Date>();
		isDiscoveryCompleted = false;
		isScanStopped = false;
		deviceCount = 0;
		niDiscoveryContext = new NIDiscoveryContext("NI_CONFIG", "huawei_specmapping.properties",
			"huawei_slotmapping.properties");
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

	public int getDeviceCount() {
		return deviceCount;
	}

	public void setDeviceCount(int deviceCount) {
		this.deviceCount = deviceCount;
	}

	public NIDiscoveryContext getNiDiscoveryContext() {
		return niDiscoveryContext;
	}

	public void setNiDiscoveryContext(NIDiscoveryContext niDiscoveryContext) {
		this.niDiscoveryContext = niDiscoveryContext;
	}

}
