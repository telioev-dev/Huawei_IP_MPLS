package com.ttbs.huawei.mpls.discoveryprocessors.huaweimodelpersister;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;

import com.teliolabs.ttbs.integrity.reporting.util.ReportContext;
import com.ttbs.huawei.mpls.discoveryprocessors.util.DiscoveryConstants;
import com.ttbs.huawei.mpls.discoveryprocessors.util.HuaweiReportContext;
import com.ttbs.huawei.mpls.discoveryprocessors.util.ResponseContext;

import oracle.communications.integrity.scanCartridges.sdk.ProcessorException;
import oracle.communications.integrity.scanCartridges.sdk.context.DiscoveryProcessorContext;
import oracle.communications.inventory.api.entity.PhysicalDevice;
import oracle.communications.inventory.api.manager.NetworkManager;

public class HuaweiModelPersisterProcessorImpl implements HuaweiModelPersisterProcessorInterface {
	private Logger logger;
	private static final String MID = "HuaweiModelPersisterProcessorImpl : ";
	private DiscoveryConstants discoveryConstants;
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

	@Override
	public void invoke(DiscoveryProcessorContext context, HuaweiModelPersisterProcessorRequest request)
			throws ProcessorException {
		PhysicalDevice physicalDevice = request.getPhysicalDevice();
		final String mid = MID + "invoke";
		getLogger().info(discoveryConstants.LOG_ENTER+mid);
		ResponseContext responseContext = request.getResponseContext();
		ReportContext reportContext = request.getReportContext();
		List<PhysicalDevice> physicalDevices = reportContext.getPhysicalDevices();
		try {
			System.out.println("reportContext object============="+reportContext);
			String pdresultGroupName = request.getPhysicalDevice().getName();
			String ldresultGroupName = request.getLogicalDevice().getName();
			HuaweiReportContext instance = HuaweiReportContext.getInstance();
			List<PhysicalDevice> physicalDeviceList = instance.getPhysicalDeviceList();
			System.out.println("physicalDeviceList==========="+physicalDeviceList.size());
			if (null != request.getPhysicalDevice()) {
				
				context.addToResult(pdresultGroupName, DiscoveryConstants.DEVICE_CONST, request.getPhysicalDevice());
				physicalDevices.add(request.getPhysicalDevice());
				physicalDeviceList.add(physicalDevice);
				System.out.println("physical device list================"+physicalDeviceList.size());
				setReportContext(physicalDeviceList,reportContext);
			}
			
			if (null != request.getLogicalDevice()) {
				context.addToResult(ldresultGroupName, DiscoveryConstants.DEVICE_CONST, request.getLogicalDevice());
			}
			
			

		} catch (Exception e) {
			e.printStackTrace();
		}
		getLogger().info(discoveryConstants.LOG_EXIT+mid);
		
	}
	private void setReportContext(List<PhysicalDevice> physicalDevices, ReportContext reportContext) {
		reportContext.setPhysicalDevices(physicalDevices);
		
		String reportDir = System.getenv("NI_CONFIG").concat("/").concat("reports").concat("/").concat("Huawei")
				.concat("/").concat("IP-MPLS");
			String neReportFilePath = reportDir.concat("network_elements.xlsx");
			String inventoryReportFilePath = reportDir.concat("inventory.xlsx");
			String portReportFilePath = reportDir.concat("ports.xlsx");
			reportContext.setNeReportPath(neReportFilePath);
			reportContext.setInventoryReportPath(inventoryReportFilePath);
			reportContext.setPortReportPath(portReportFilePath);
			Set<String> defaultCh = new HashSet<String>();
			defaultCh.add("name");
			defaultCh.add("Description");
			defaultCh.add("physicalAddress");
			defaultCh.add("physicalLocation");
			defaultCh.add("serialNumber");
			defaultCh.add("networkLocationCode");
			Set<String> extendedCh = new HashSet<String>();
			extendedCh.add("ipAddress");
			extendedCh.add("nmsName");
			extendedCh.add("alarmStatus");
			extendedCh.add("deviceDimension");
			extendedCh.add("discoveryStatus");
			extendedCh.add("domain");
			extendedCh.add("emsName");
			extendedCh.add("hardwareVersion");
			extendedCh.add("softwareVersion");
			extendedCh.add("materialCode");
			Set<String> portDefaultCh = new HashSet<String>();
			portDefaultCh.add("name");
			portDefaultCh.add("portNumber");
			portDefaultCh.add("physicalAddress");
			portDefaultCh.add("vendorPortName");
			portDefaultCh.add("customerPortName");
			portDefaultCh.add("description");
			portDefaultCh.add("physicalLocation");
			portDefaultCh.add("serialNumber");
			Set<String> portExtendedCh = new HashSet<String>();
			extendedCh.add("discoveryStatus");
			extendedCh.add("alarmStatus");
			reportContext.setInventoryDefaultCharacteristics(defaultCh);
			reportContext.setInventoryExtendedCharacteristics(extendedCh);
			reportContext.setPdDefaultCharacteristics(defaultCh);
			reportContext.setPdExtendedCharacteristics(extendedCh);
			reportContext.setPortDefaultCharacteristics(portDefaultCh);
			reportContext.setPortExtendedCharacteristics(portExtendedCh);
	}

	
}
