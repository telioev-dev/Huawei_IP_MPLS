package com.ttbs.huawei.mpls.discoveryprocessors.util;

import java.util.Date;

import oracle.communications.integrity.scanCartridges.sdk.ProcessorException;

public class DiscoveryRuntimeException extends ProcessorException {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public DiscoveryRuntimeException() {
		super();
	}
	public DiscoveryRuntimeException(final Throwable cause) throws ProcessorException {
		throw new ProcessorException(cause);
	}	
	
	public DiscoveryRuntimeException(String deviceIp, String errorReason, DeviceStatus deviceStatus) {
		//super(cause);
		    System.out.println("Device with ip:" + deviceIp + " failed with error reason:" + errorReason);
			deviceStatus.getDeviceStatus().put(deviceIp, "FAILED");
			deviceStatus.getFailureReason().put(deviceIp, errorReason);
			deviceStatus.getEndTime().put(deviceIp, new Date());
			deviceStatus.setIsDiscoveryCompleted(false);
	}
	public DiscoveryRuntimeException(String errorReason) {
		super(errorReason);
	}
	
}
