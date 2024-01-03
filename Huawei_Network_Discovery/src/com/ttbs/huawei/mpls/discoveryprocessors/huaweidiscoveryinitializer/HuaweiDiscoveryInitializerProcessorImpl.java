package com.ttbs.huawei.mpls.discoveryprocessors.huaweidiscoveryinitializer;

import java.util.Objects;
import java.util.logging.Logger;

import com.ttbs.huawei.mpls.discoveryprocessors.huaweidiscoveryinitializer.HuaweiDiscoveryInitializerProcessorInterface;
import com.ttbs.huawei.mpls.discoveryprocessors.huaweidiscoveryinitializer.HuaweiDiscoveryInitializerProcessorRequest;
import com.ttbs.huawei.mpls.discoveryprocessors.huaweidiscoveryinitializer.HuaweiDiscoveryInitializerProcessorResponse;
import com.ttbs.huawei.mpls.discoveryprocessors.util.DeviceStatus;
import com.ttbs.huawei.mpls.discoveryprocessors.util.DiscoveryRuntimeException;
import com.ttbs.huawei.mpls.discoveryprocessors.util.ResponseContext;

import oracle.communications.integrity.scanCartridges.sdk.ProcessorException;
import oracle.communications.integrity.scanCartridges.sdk.context.DiscoveryProcessorContext;

public class HuaweiDiscoveryInitializerProcessorImpl implements HuaweiDiscoveryInitializerProcessorInterface {

	private Logger logger;

	/**
	 * Gets the logger
	 * 
	 * @return the logger
	 */
	private Logger getLogger() {
		if (Objects.isNull(logger)) {
			logger = Logger.getLogger(this.getClass().getName());
		}
		return logger;
	}

	public HuaweiDiscoveryInitializerProcessorImpl() {
		// Do not remove
	}

	@Override
	public HuaweiDiscoveryInitializerProcessorResponse invoke(DiscoveryProcessorContext context,
			HuaweiDiscoveryInitializerProcessorRequest request) throws ProcessorException {
		HuaweiDiscoveryInitializerProcessorResponse response = null;
		ResponseContext responseContext = null;

		getLogger().info("invoke: enter");
		response = new HuaweiDiscoveryInitializerProcessorResponse();
		responseContext = new ResponseContext();
		response.setResponseContext(responseContext);
		getLogger().info("invoke: exit");
		try{
			//getLogger().info("invoke: start"); 
			DeviceStatus deviceStatus = new DeviceStatus();	
			response.setDeviceStatus(deviceStatus);//setDeviceStatus(DeviceStatus);
		}
		catch (Exception exception) {
			exception.printStackTrace();
			//getLogger().error("Exception in DeviceStatusInitializerProcessorImpl invoke method:" + exception.getMessage());
			throw new DiscoveryRuntimeException(exception);
		}
		return response;
	}

}
