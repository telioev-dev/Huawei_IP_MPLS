package com.ttbs.huawei.mpls.discoveryprocessors.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;

import oracle.communications.integrity.scanCartridges.sdk.ProcessorException;
import oracle.communications.inventory.api.entity.DeviceInterface;
import oracle.communications.inventory.api.entity.Equipment;
import oracle.communications.inventory.api.entity.EquipmentHolder;
import oracle.communications.inventory.api.entity.LogicalDevice;
import oracle.communications.inventory.api.entity.MediaInterface;
import oracle.communications.inventory.api.entity.PhysicalDevice;
import oracle.communications.inventory.api.entity.PhysicalPort;
import oracle.communications.inventory.api.entity.common.CharValue;
import oracle.communications.inventory.api.entity.common.CharacteristicExtensible;

public class NIDiscoveryContext {
	private Logger logger;
	private SpecificationHelper specificationHelper = null;
	private SpecificationMapper specificationMapper = null;
	private Properties prop;
	static String materialCodepropertyFile=null;

	/**
	 * Gets the logger
	 * 
	 * @return the logger
	 */
	private Logger getLogger() {
		if (null == logger) {
			logger = Logger.getLogger(this.getClass().getName());
		}
		return logger;
	}

	public NIDiscoveryContext(String env, String propertyFile1, String propertyFile2) throws ProcessorException {
		InputStream inputStream = null;
		prop = new Properties();
		try {
			
			// inputStream = this.getClass().getResourceAsStream("/specmapping.properties");
			if (System.getenv(env) != null) {
				inputStream = new FileInputStream(
						System.getenv(env).concat("/SpecificationMapping/").concat(propertyFile1));
				prop.load(inputStream);
				inputStream = new FileInputStream(
						System.getenv(env).concat("/SpecificationMapping/").concat(propertyFile2));

				prop.load(inputStream);
				specificationMapper = new DefaultSpecificationMapper(this.prop);
				specificationHelper = new SpecificationHelper();
				System.out.println("File found.." + propertyFile1 + " " + propertyFile2);
			}else {
				String hardCode ="/home/niadmin/integrity/NI_CONFIG/";
				inputStream = new FileInputStream(
						hardCode.concat("/SpecificationMapping/").concat(propertyFile1));
				prop.load(inputStream);
				inputStream = new FileInputStream(
						hardCode.concat("/SpecificationMapping/").concat(propertyFile2));

				prop.load(inputStream);
				specificationMapper = new DefaultSpecificationMapper(this.prop);
				specificationHelper = new SpecificationHelper();
				System.out.println("File found.." + propertyFile1 + " " + propertyFile2);
			
			}

		} catch (FileNotFoundException e) {
			getLogger().warning("FileNotFoundException occurred while loading property file:" + propertyFile1);
		} catch (IOException e) {
			getLogger().warning("IOException occurred while loading property file:" + propertyFile1);
		}
	}

	public NIDiscoveryContext(String env, String propertyFile) throws ProcessorException {
		InputStream inputStream = null;
		prop = new Properties();
		try {
			// inputStream = this.getClass().getResourceAsStream("/specmapping.properties");
			if (System.getenv(env) != null) {
				inputStream = new FileInputStream(
						System.getenv(env).concat("/SpecificationMapping/").concat(propertyFile));
				prop.load(inputStream);

				specificationMapper = new DefaultSpecificationMapper(this.prop);
				specificationHelper = new SpecificationHelper();
				System.out.println("File found..");
			}

		} catch (FileNotFoundException e) {
			getLogger().warning("FileNotFoundException occurred while loading property file:" + propertyFile);
		} catch (IOException e) {
			getLogger().warning("IOException occurred while loading property file:" + propertyFile);
		}
	}

	public NIDiscoveryContext() {
	}

	public String getPropValue(String name) {
		if (name == null)
			return null;

		// System.out.println("Prop "+this.prop.entrySet().toString());
		if (this.prop != null) {
			// _CHASSIS_REAR _CHASSIS,_CHASSIS_REAR
			for (Entry<Object, Object> keyvalue : prop.entrySet()) {
				if (name.contains(keyvalue.getKey().toString())) {
					return keyvalue.getValue().toString();
				}
			}

		}
		return null;
	}

	/*
	 * public void setSpecificationHelper(SpecificationHelper specificationHelper) {
	 * this.specificationHelper = specificationHelper; }
	 * 
	 * public void setSpecificationMapper(SpecificationMapper specificationMapper) {
	 * this.specificationMapper = specificationMapper; }
	 * 
	 * public SpecificationHelper getSpecificationHelper() { return
	 * specificationHelper; }
	 * 
	 * public SpecificationMapper getSpecificationMapper() { return
	 * specificationMapper; }
	 */

	public boolean applyCharacteristics(CharacteristicExtensible<? extends CharValue> entity,
			Map<String, String> characteristics) {
		boolean result = true;

		for (Map.Entry<String, String> characteristic : characteristics.entrySet()) {
			String name = characteristic.getKey();
			String value = characteristic.getValue();
			if (specificationHelper.supportsCharacteristic(entity, name)) {
				specificationHelper.setCharacteristic(entity, name, value);
			} else {
				// getLogger().info("Unsupported characteristic=" + name + " with value " +
				// value);
				result = false;
			}
		}
		return result;
	}

	public boolean applySpecification(CharacteristicExtensible<? extends CharValue> entity, String specificationName) {
		String mappedSpecificationName = null;
		if (specificationMapper != null && specificationHelper != null) {
			
//			if(specificationName.contains("_PORT")) {
//				specificationName="E1_PORT";
//				com.oracle.integrity.modelcollections.huaweinetworkdiscovery.port.GigE1GigESFP
//			}
			mappedSpecificationName = specificationMapper.mapSpecificationName(entity, specificationName);
			//System.out.println("mappedSpecificationName==========="+mappedSpecificationName);
			if (mappedSpecificationName != null) {
				return specificationHelper.setSpecificationByName(entity, mappedSpecificationName);
			}
			
			if (mappedSpecificationName == null) {
				System.out.println("specifiaction name ====================="+specificationName);
				if (entity instanceof PhysicalDevice) {
					mappedSpecificationName = specificationMapper.mapSpecificationName(entity, "genericPhysicalDevice");
					//System.out.println("mappedSpecificationName==========="+mappedSpecificationName);
					if (mappedSpecificationName != null) {
						getLogger().warning(
								"Generic specification formed for PhysicalDevice with key:" + specificationName);
						return specificationHelper.setSpecificationByName(entity, mappedSpecificationName);
					
					} else
						//System.out.println("mappedSpecificationName-------------------------------"+mappedSpecificationName);
						return false;
				}

				if (entity instanceof Equipment) {
					mappedSpecificationName = specificationMapper.mapSpecificationName(entity, "genericEquipment");
					
					if (mappedSpecificationName != null) {
						getLogger().warning("Generic specification formed for Equipment with key:" + specificationName);
						return specificationHelper.setSpecificationByName(entity, mappedSpecificationName);
					} else
						return false;
				}

				if (entity instanceof EquipmentHolder) {
					mappedSpecificationName = specificationMapper.mapSpecificationName(entity,
							"genericEquipmentHolder");
					
					if (mappedSpecificationName != null) {
						getLogger().warning(
								"Generic specification formed for EquipmentHolder with key:" + specificationName);
						return specificationHelper.setSpecificationByName(entity, mappedSpecificationName);
					} else
						return false;
				}

				if (entity instanceof PhysicalPort) {
					mappedSpecificationName = specificationMapper.mapSpecificationName(entity, "genericPhysicalPort");
					
					if (mappedSpecificationName != null) {
						getLogger()
								.warning("Generic specification formed for PhysicalPort with key:" + specificationName);
						return specificationHelper.setSpecificationByName(entity, mappedSpecificationName);
					} else
						return false;
				}

				if (entity instanceof LogicalDevice) {
					mappedSpecificationName = specificationMapper.mapSpecificationName(entity, "genericLogicalDevice");
					
					if (mappedSpecificationName != null) {
						getLogger().warning(
								"Generic specification formed for LogicalDevice with key:" + specificationName);
						return specificationHelper.setSpecificationByName(entity, mappedSpecificationName);
					} else
						return false;
				}

				if (entity instanceof MediaInterface) {
					mappedSpecificationName = specificationMapper.mapSpecificationName(entity, "genericMediaInterface");
					
					if (mappedSpecificationName != null) {
						getLogger().warning(
								"Generic specification formed for MediaInterface with key:" + specificationName);
						return specificationHelper.setSpecificationByName(entity, mappedSpecificationName);
					} else
						return false;
				}

				if (entity instanceof DeviceInterface) {
					mappedSpecificationName = specificationMapper.mapSpecificationName(entity, "genericInterface");
					
					if (mappedSpecificationName != null) {
						getLogger().warning(
								"Generic specification formed for DeviceInterface with key:" + specificationName);
						return specificationHelper.setSpecificationByName(entity, mappedSpecificationName);
					} else
						return false;
				}
			}
		}
		return false;
	}

	public String propertiesKeyValue(String keyName) {
		// TODO Auto-generated method stub
		String value = null;
		if (specificationMapper != null && specificationHelper != null) {
			value = specificationMapper.mapSpecificationName(null, keyName);
			
		}
		//System.out.println("value is =========="+value);
		return value;
	}
	
	public BufferedWriter  createfilewithTimeStamp(String hostName) {
		// TODO Auto-generated method stub
	
		BufferedWriter  writer=null;
		  SimpleDateFormat sdfDate = new SimpleDateFormat(
	                "yyyy-MM-dd HH:mm:ss.SSS");// dd/MM/yyyy/ss/SSS
	        Date now = new Date();
	        String strDate = sdfDate.format(now);
		try {
			String filename=System.getenv("NI_CONFIG").concat("/Report/").concat("Huawei_").concat(hostName).concat("_").concat(strDate).concat(".csv").replaceAll("\\s+","");
			System.out.println("filename:: "+filename);
			FileWriter fw = new FileWriter(filename);
			  writer = new BufferedWriter (fw); 
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
    	    	return writer;
	}
	
	public static Properties getNetworkLocationMappings() {
		Properties locProps = new Properties();
		try {
			locProps.load(new FileInputStream(System.getenv("NI_CONFIG") +"/locationMapping/AMSLocation.properties"));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return locProps;
		
	}
	public static Properties getDeviceDimensionMappings() {
		Properties locProps = new Properties();
		try {
			locProps.load(new FileInputStream(System.getenv("NI_CONFIG") +"/SpecificationMapping/DeviceDimension.properties"));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return locProps;
		
	}
	public static Properties getMaterialCodeMappings() {
		Properties matProps = new Properties();
		try {
			matProps.load(new FileInputStream(System.getenv("NI_CONFIG")+ "/SpecificationMapping/materialCodepropertyFile.properties"));
			//locProps.load(new FileInputStream(System.getProperty("domain.home") + File.separator + "config/huaweiConfig/huaweiMplsnetworkLoc.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return matProps;
		
	}
	public static Properties getSlotPosition() {
		Properties slotProps = new Properties();
		try {
			slotProps.load(new FileInputStream(System.getenv("NI_CONFIG")+ "/SpecificationMapping/slotPosition.properties"));
			//locProps.load(new FileInputStream(System.getProperty("domain.home") + File.separator + "config/huaweiConfig/huaweiMplsnetworkLoc.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return slotProps;
		
	}

}
