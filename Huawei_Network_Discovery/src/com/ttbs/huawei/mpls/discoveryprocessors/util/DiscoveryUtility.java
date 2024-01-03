package com.ttbs.huawei.mpls.discoveryprocessors.util;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import oracle.communications.inventory.api.entity.DeviceInterface;
import oracle.communications.inventory.api.entity.Equipment;
import oracle.communications.inventory.api.entity.EquipmentEquipmentRel;
import oracle.communications.inventory.api.entity.EquipmentHolder;
import oracle.communications.inventory.api.entity.EquipmentHolderEquipmentRel;
import oracle.communications.inventory.api.entity.LogicalDevice;
import oracle.communications.inventory.api.entity.PhysicalDevice;
import oracle.communications.inventory.api.entity.PhysicalDeviceEquipmentRel;
import oracle.communications.inventory.api.entity.PhysicalPort;
import oracle.communications.platform.persistence.PersistenceHelper;
import oracle.communications.platform.util.Utils;


public class DiscoveryUtility {

	public static final String FAILED = "failed";
	public static final String COMPLETED = "completed";
	private static final String BOARDID_PATTERN = "\\d*-\\d*-\\d*";;
	private static final String IPADDRESS_PATTERN = 
			"^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
					"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
					"([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." +
					"([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	
		

	private static Pattern pattern = null;


	private static Logger logger = Logger.getLogger(DiscoveryUtility.class.getName());

	public static void addEquipmentToEquipment(Equipment parentEquipment,
			Equipment childEquipment,NIDiscoveryContext niDiscoveryContext) {
		logger.log(Level.FINEST, "addEquipmentToEquipment started....");
		EquipmentEquipmentRel equipmentEquipmentRel = PersistenceHelper
				.makeEntity(oracle.communications.inventory.api.entity.EquipmentEquipmentRel.class);
//		EquipmentHolder dummyHolder = PersistenceHelper.makeEntity(oracle.communications.inventory.api.entity.EquipmentHolder.class);
//		dummyHolder.setName("Slot "+childEquipment.getName());
//		dummyHolder.setDescription(parentEquipment.getName()+ " Slot "+childEquipment.getName());
//		System.out.println("Add equipment " + parentEquipment.getName() + " " + equipment.getName());
//		if(childEquipment.getName().contains("/")) {
//		dummyHolder.setNativeEMSName(childEquipment.getName().split("/")[1]);
//		}else {
//		dummyHolder.setNativeEMSName(childEquipment.getNativeEmsName());
//		}
//		mapHolderToHolderSpec(dummyHolder.getName(),dummyHolder,niDiscoveryContext);
//		addEquipmentToEquipmentHolder(dummyHolder, childEquipment);
//		parentEquipment.getEquipmentHolders().add(dummyHolder);
		//System.out.println("Add equipment in mapping " + parentEquipment.getName() + " " + childEquipment.getName());
		equipmentEquipmentRel.setParentEquipment(parentEquipment);
		equipmentEquipmentRel.setChildEquipment(childEquipment);
		System.out.println("addEquipmentToEquipment ended....");
		logger.log(Level.FINEST, "addEquipmentToEquipment ended....");

	}

	public static void addPhysicalPortToEquipment(Equipment eq,
			List<PhysicalPort> physicalPortList) {
		logger.log(Level.FINEST, "addPhysicalPortToEquipment started.....");
		eq.setPhysicalPorts(physicalPortList);
		logger.log(Level.FINEST, "addPhysicalPortToEquipment ended.....");
	}
	
	private static void mapHolderToHolderSpec(String holderName,EquipmentHolder currentEquipmentHolder,NIDiscoveryContext niDiscoveryContext) {
		
		String mapperSpecKey="";
		
		if(holderName.contains("CR53LPUF")||
			holderName.contains("CR52LPUN")||
			holderName.contains("CR57LPUF")||
			holderName.contains("CR52MPUD")||
			holderName.contains("Card  slot")||
			holderName.contains("ANG1CXPH slot")||
			holderName.contains("AND2CXPB")||
			holderName.contains("AND2EM8F")||
			holderName.contains("ANK1EX2S")||
			holderName.contains("CR56RPUA")||
			holderName.contains("CR51EAGFD")||
			holderName.contains("CR51L4XFD0")||
			holderName.contains("CFCARD")||
			holderName.contains("CR57")||
			holderName.toUpperCase().contains("SLOT")) {
			mapperSpecKey="CHILD_SLOT";
		}else if(holderName.contains("CR53TCMB")||
				holderName.contains("CR53E8GF")||
				holderName.contains("CR52EEGFN")||
				holderName.contains("CR57L4XX")||
				holderName.contains("CFCARD")||
				holderName.contains("AND2EM8F")||
				holderName.contains("CR57EFGFB")) {
			mapperSpecKey="CHILD_SUBSLOT";
		}
		niDiscoveryContext.applySpecification(currentEquipmentHolder,mapperSpecKey);
		
	}
	
	public static void addPhysicalPortToEquipment(Equipment eq,
			PhysicalPort physicalport) {
		logger.log(Level.FINEST, "addPhysicalPortToEquipment started.....");
		if(!Utils.checkNull(eq.getPhysicalPorts()))
		{
          eq.getPhysicalPorts().add(physicalport);
		}
else
{
	List <PhysicalPort> portList=new  ArrayList<PhysicalPort>();
	portList.add(physicalport);
	eq.setPhysicalPorts(portList);
}
		logger.log(Level.FINEST, "addPhysicalPortToEquipment ended.....");
	}

	public static void addEquipmentToEquipmentHolder(EquipmentHolder eqh, Equipment eq) {
		logger.log(Level.FINEST, "addEquipmenttoEquipmentHolder started.....");
		EquipmentHolderEquipmentRel equipmentHolderEquipmentRel = PersistenceHelper
				.makeEntity(oracle.communications.inventory.api.entity.EquipmentHolderEquipmentRel.class);
		equipmentHolderEquipmentRel.setEquipmentHolder(eqh);
		equipmentHolderEquipmentRel.setEquipment(eq);
		logger.log(Level.FINEST, "addEquipmenttoEquipmentHolder ended.....");

	}

	public static PhysicalDevice createPhysicalDevice() {
		logger.log(Level.FINEST, "In getPhysicalDevice.....");

		return (PhysicalDevice) PersistenceHelper
				.makeEntity(oracle.communications.inventory.api.entity.PhysicalDevice.class);
	}

	public static Equipment createEquipment() {
		logger.log(Level.FINEST, "In getEquipment.....");

		return (PersistenceHelper
				.makeEntity(oracle.communications.inventory.api.entity.Equipment.class));
	}

	public static PhysicalPort createPhysicalPort() {
		logger.log(Level.FINEST, "In getPhysicalPort....");
		return (PersistenceHelper
				.makeEntity(oracle.communications.inventory.api.entity.PhysicalPort.class));
	}
	public static LogicalDevice createLogicalDevice() {
		logger.log(Level.FINEST, "In createLogicalDevice....");
		return (PersistenceHelper
				.makeEntity(oracle.communications.inventory.api.entity.LogicalDevice.class));
	}
	public static DeviceInterface createDeviceInterface() {
		logger.log(Level.FINEST, "In createLogicalDevice....");
		return (PersistenceHelper
				.makeEntity(oracle.communications.inventory.api.entity.DeviceInterface.class));
	}

	public static void createPhysicalDeviceEquipmentRel(PhysicalDevice pd, Equipment eq) {
		logger.log(Level.FINEST, "getPhysicalDeviceEquipmentRel started.....");
		PhysicalDeviceEquipmentRel physicalDeviceEquipmentRel = PersistenceHelper
				.makeEntity(oracle.communications.inventory.api.entity.PhysicalDeviceEquipmentRel.class);

		physicalDeviceEquipmentRel.setEquipment(eq);
		physicalDeviceEquipmentRel.setPhysicalDevice(pd);
		logger.log(Level.FINEST, "getPhysicalDeviceEquipmentRel ended.....");

	}

	public static void addEquipmentHolderToEquipment(Equipment eq,
			List<EquipmentHolder> equipmentHolderList) {
		logger.log(Level.FINEST, "addEquipmentHolderToEquipment started.....");
		eq.setEquipmentHolders(equipmentHolderList);
		logger.log(Level.FINEST, "addEquipmentHolderToEquipment ended.....");

	}
	
	public static void addEquipmentHolderToEquipment(Equipment eq,
			EquipmentHolder equipmentHolder) {
		logger.log(Level.FINEST, "addEquipmentHolderToEquipment started.....");
		if(!Utils.checkNull(eq.getEquipmentHolders()))
				{
		          eq.getEquipmentHolders().add(equipmentHolder);
				}
		else
		{
			List <EquipmentHolder> holderList=new  ArrayList<EquipmentHolder>();
			holderList.add(equipmentHolder);
			eq.setEquipmentHolders(holderList);
		}
		logger.log(Level.FINEST, "addEquipmentHolderToEquipment ended.....");

	}

	public static EquipmentHolder createEquipmentHolder() {
		logger.log(Level.FINEST, "In getEquipmentHolder.........");
		return (PersistenceHelper
				.makeEntity(oracle.communications.inventory.api.entity.EquipmentHolder.class));
	}

	public static HashMap<String,String> splitLineToKeyValue(String line){

		//StringTokenizer tokens = new StringTokenizer(line,",");
		if(line!= null && line.contains("=$")){
			line = line.replace("=$", "=--$");
		}
		HashMap<String,String> attributeMap = new HashMap<String,String>(); 
		if(line != null && !line.trim().isEmpty()){
			String[] tokens = line.split("\\$");
			for (int i = 0; i < tokens.length; i++) {
				String keyVals = tokens[i];
				String[] keyVal = keyVals.split("=");
				String name = keyVal[0];
				String value = keyVal[1];
				attributeMap.put(name, value);
				//System.out.println(name+"="+value);


			}
		}
		return attributeMap;

	}
	public static boolean getBoolean(String value){
		if(value.equalsIgnoreCase("true")){
			return true;
		}
		return false;
	}

	public static String getDeviceName(String hostname, String ipAddr) {
		String deviceName = hostname.concat("(").concat(ipAddr).concat(")");
		return deviceName;
	}
	public static boolean validateBoardId(String bid) {
		pattern  = Pattern.compile(BOARDID_PATTERN);
		if(bid == null)return false;
		Matcher matcher = pattern.matcher(bid);
		return matcher.matches();
	}/**
	 * Converts a string to a long number
	 * @param text the string to convert
	 * @return -1 if fails or the long number if could convert
	 */
	public static long convertStringToLong(String text) {
		long returnValue = -1;
		if (text == null) {
			return returnValue;
		}
		try {
			returnValue = Long.parseLong(text);
		} catch (Exception e) {
			returnValue = -1;
		}
		return returnValue;
	}

	/**
	 * Converts a string to an int number
	 * @param text the string to convert
	 * @return -1 if fails or the int number if could convert
	 */
	public static int convertStringToInt(String text) {
		int returnValue = (int)convertStringToLong(text);
		return returnValue;
	}


	/**
	 * Validate ip address with regular expression
	 * @param ip ip address for validation
	 * @return true valid ip address, false invalid ip address
	 */
	public boolean validateIP(final String ip){	
		Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
		Matcher matcher = pattern.matcher(ip);
		return matcher.matches();	    	    
	}


	public String translatedDeviceModel(String deviceModel) {
		switch (deviceModel) {
		case "AN5006-20":
			return "AN5006_20";
		case "AN5006-30":
			return "AN5006_30";
		case "AN5116-06B":
			return "AN5116_06B";
		default:
			return null;
		}
	}

	public static String getMandatoryParams(String paramName, String paramValue) throws NullPointerException {
		if (paramValue == null)
			throw new NullPointerException("Mandatory parameter  value for parameter : \"" + paramName + "\" missing ");
		else
			return paramValue;
	}

	 public static void addEquipmentToPhysicalDevice(PhysicalDevice pd, Equipment eq) {
			logger.log(Level.FINEST, "getPhysicalDeviceEquipmentRel started.....");
			PhysicalDeviceEquipmentRel physicalDeviceEquipmentRel = PersistenceHelper
			.makeEntity(oracle.communications.inventory.api.entity.PhysicalDeviceEquipmentRel.class);
			physicalDeviceEquipmentRel.setEquipment(eq);
			physicalDeviceEquipmentRel.setPhysicalDevice(pd);
			logger.log(Level.FINEST, "getPhysicalDeviceEquipmentRel ended.....");

		}
	 
}
