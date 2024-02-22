package com.ttbs.huawei.mpls.discoveryprocessors.huaweiphysicaldevicemodeler;

import java.io.BufferedWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.teliolabs.ttbs.integrity.reporting.util.ReportContext;
import com.ttbs.huawei.mpls.discoveryprocessors.huaweiphysicaldevicemodeler.HuaweiPhysicalDeviceModelerProcessorInterface;
import com.ttbs.huawei.mpls.discoveryprocessors.huaweiphysicaldevicemodeler.HuaweiPhysicalDeviceModelerProcessorRequest;
import com.ttbs.huawei.mpls.discoveryprocessors.huaweiphysicaldevicemodeler.HuaweiPhysicalDeviceModelerProcessorResponse;
import com.ttbs.huawei.mpls.discoveryprocessors.util.DefaultModelHelper;
import com.ttbs.huawei.mpls.discoveryprocessors.util.DiscoveryConstants;
import com.ttbs.huawei.mpls.discoveryprocessors.util.DiscoveryRuntimeException;
import com.ttbs.huawei.mpls.discoveryprocessors.util.DiscoveryUtility;
import com.ttbs.huawei.mpls.discoveryprocessors.util.NIDiscoveryContext;
import com.ttbs.huawei.mpls.discoveryprocessors.util.ResponseContext;

import oracle.communications.integrity.scanCartridges.sdk.BaseDiscoveryController;
import oracle.communications.integrity.scanCartridges.sdk.ProcessorException;
import oracle.communications.integrity.scanCartridges.sdk.context.DiscoveryProcessorContext;
import oracle.communications.inventory.api.entity.Equipment;
import oracle.communications.inventory.api.entity.EquipmentEquipmentRel;
import oracle.communications.inventory.api.entity.EquipmentHolder;
import oracle.communications.inventory.api.entity.EquipmentHolderEquipmentRel;
import oracle.communications.inventory.api.entity.EquipmentSpecification;
import oracle.communications.inventory.api.entity.PhysicalDevice;
import oracle.communications.inventory.api.entity.PhysicalDeviceEquipmentRel;
import oracle.communications.inventory.api.entity.PhysicalPort;
import oracle.communications.inventory.api.manager.NetworkManager;
import oracle.communications.platform.logging.Log;
import oracle.communications.platform.logging.LogFactory;
import oracle.communications.platform.nds.snmp.huaweiphysicalcollector.HuaweiPhysicalCollectorResponseType;
import oracle.communications.platform.nds.snmp.huaweiphysicalcollector_entity_mib.EntPhysicalClassType;
import oracle.communications.platform.nds.snmp.huaweiphysicalcollector_entity_mib.EntPhysicalEntryType;
import oracle.communications.platform.nds.snmp.huaweiphysicalcollector_entity_mib.EntityMibMib;
import oracle.communications.platform.nds.snmp.huaweiphysicalcollector_if_mib.IfMibMib;
import oracle.communications.platform.nds.snmp.huaweiphysicalcollector_rfc1213_mib.Rfc1213MibMib;
import oracle.communications.platform.persistence.PersistenceHelper;
import oracle.communications.platform.util.Utils;

public class HuaweiPhysicalDeviceModelerProcessorImpl implements HuaweiPhysicalDeviceModelerProcessorInterface {

	private static final String MID = "HuaweiPhysicalDeviceModelerProcessorImpl : ";

	private Log logger;
	private HuaweiPhysicalDeviceModelerProcessorResponse response;
	private ResponseContext responseContext;
	private DiscoveryConstants discoveryConstants;
	private DiscoveryUtility util;
	private NIDiscoveryContext niDiscoveryContext;
	private HuaweiPhysicalCollectorResponseType snmpDataResponseType;
	private Rfc1213MibMib rfc1213Mib;
	private IfMibMib ifMibMib;
	private EntityMibMib entityMibMib;
	private Map<String, EntPhysicalEntryType> equipTypeIndexMap;
	private Map<String, EntPhysicalEntryType> equipHolderTypeIndexMap;
	private Map<String, EntPhysicalEntryType> portTypeIndexMap;
	private EntPhysicalEntryType entPhysicalEntryType[];
	private Map<EntPhysicalEntryType, List<EntPhysicalEntryType>> parentChildEntityMap;
	private int chassisIndexInArray;
	private Map<String, Equipment> createdEquipmentMap;
	private Map<String, EquipmentHolder> createdEquipmentHolderMap;
	private Map<String, PhysicalPort> createdPhysicalPortMap;
	private String ipAddress;
	private int incrementvalue = 0;
	private PhysicalDevice physicalDevice;
//	private static String physicalDeviceName;
//	private static String networkLocationCode;
	private Map<String, Equipment> globalEquipMap;
	private Map<String, EquipmentHolder> globalEquipHolderMap;
	private Map<String, PhysicalPort> globalPortMap;
	private Map<String, PhysicalPort> mappedPhysicalToLogicalPortMap;
	// private Equipment chassis=null;
	private List<BigInteger> managementCard = null;
	private String chassisType = null;
	private String hwVersion = null;
	private Map<String, EntPhysicalEntryType> portSerialMap;
	// private String incrementvalue = "0";
	private boolean portTypeFromCard = false;
	private BufferedWriter bw = null;
	private String materialCode = null;
	private String location=null;
	private String devDim = null;
	private EquipmentHolder dummyslot = null;
	private Properties slotPosition = null;
	private String chassisName = null;
	private String physicalLoc = null;
	private static final String NA = "NA";

	/**
	 * Default constructor
	 * 
	 * @throws ProcessorException
	 */
	public HuaweiPhysicalDeviceModelerProcessorImpl() throws ProcessorException {
		response = new HuaweiPhysicalDeviceModelerProcessorResponse();
		responseContext = new ResponseContext();
		util = new DiscoveryUtility();
		mappedPhysicalToLogicalPortMap = new HashMap<String, PhysicalPort>();
		managementCard = new ArrayList<BigInteger>();
	}

	/**
	 * Gets the logger
	 * 
	 * @return the logger
	 */
	private Log getLogger() {
		if (null == logger) {
			logger = LogFactory.getLog(this.getClass().getName());

		}
		return logger;
	}

	@Override
	public HuaweiPhysicalDeviceModelerProcessorResponse invoke(DiscoveryProcessorContext context,
			HuaweiPhysicalDeviceModelerProcessorRequest request) throws ProcessorException {

		final String mid = MID + "invoke";
		getLogger().debug(DiscoveryConstants.LOG_ENTER + mid);
		String deviceName;

		try {
			
			responseContext = request.getResponseContext();
			niDiscoveryContext = responseContext.getNiDiscoveryContext();
			snmpDataResponseType = request.getHuaweiPhysicalCollectorResponseDocument();
			ipAddress = request.getScopeAddress();
			rfc1213Mib = snmpDataResponseType.getDiscoveryResult().getRfc1213MibResults();
			entityMibMib = snmpDataResponseType.getDiscoveryResult().getEntityMibResults();
			ifMibMib = snmpDataResponseType.getDiscoveryResult().getIfMibResults();
			//slotPosition = niDiscoveryContext.getSlotPosition();
			//
			// Properties locationMapProps =
			// NIDiscoveryContext.getNetworkLocationMappings();
			//
			List<PhysicalDevice> physicalDevices = request.getResponseContext().getPhysicalDevices();
			Properties materialcodeMapProps = NIDiscoveryContext.getMaterialCodeMappings();
			Properties networkLocationMappings = NIDiscoveryContext.getNetworkLocationMappings();
			slotPosition = NIDiscoveryContext.getSlotPosition();

			entPhysicalEntryType = entityMibMib.getEntPhysicalTable().getEntPhysicalEntryArray();
			deviceName = DiscoveryUtility.getMandatoryParams("Device Name ", rfc1213Mib.getSysName().trim());
			deviceName = deviceName.toUpperCase().split("\\.")[0];
			// logger.debug("entPhysicalEntryType[0].getEntPhysicalModelName()==="+entPhysicalEntryType[0].getEntPhysicalModelName());

			physicalDevice = DiscoveryUtility.createPhysicalDevice();
			physicalDevice.setName((deviceName == null || deviceName.isEmpty()) ? NA : deviceName);
			physicalLoc = rfc1213Mib.getSysLocation();
			if (physicalLoc != null) {
				physicalLoc = physicalLoc.replace(" ", "").replace("\"", "");
				// physicalLoc = locationMapProps.getProperty(physicalDevice.getName(),
				// physicalLoc);
			}
			//physicalDevice.setNativeEMSName(physicalDevice.getName());

			 location = networkLocationMappings.getProperty(physicalDevice.getName());
			physicalDevice.setSerialNumber((entPhysicalEntryType[0].getEntPhysicalSerialNum() == null
					|| entPhysicalEntryType[0].getEntPhysicalSerialNum().isEmpty()) ? NA
							: entPhysicalEntryType[0].getEntPhysicalSerialNum());
			// physicalDevice.setDescription(rfc1213Mib.getSysDescr());
			String phyDescr = rfc1213Mib.getSysDescr();
			if (phyDescr.length() > 50) {
				String phyDescription = phyDescr.substring(0, 49);
				physicalDevice
						.setDescription((phyDescription == null || phyDescription.isEmpty()) ? NA : phyDescription);
			} else {
				physicalDevice
						.setDescription((rfc1213Mib.getSysDescr() == null || rfc1213Mib.getSysDescr().isEmpty()) ? NA
								: rfc1213Mib.getSysDescr());
			}

			physicalDevice.setPhysicalLocation(physicalLoc);
			physicalDevice.setNetworkLocationCode(location);
			physicalDevice.setPhysicalAddress(snmpDataResponseType.getManagementIP());
			physicalDevice.setAlias(request.getScopeAddress());
			getLogger().debug("rfc1213Mib.getSysLocation() ::::: ", rfc1213Mib.getSysLocation());

			// Intializing Maps for storing created entities data
			portSerialMap = new TreeMap<String, EntPhysicalEntryType>();
			globalEquipMap = new TreeMap<String, Equipment>();
			globalEquipHolderMap = new TreeMap<String, EquipmentHolder>();
			globalPortMap = new TreeMap<String, PhysicalPort>();

			// Populating Map for Equipment/Holder/Port
			populateMapForEntityType(entPhysicalEntryType);
			physicalDevice.setPhysicalLocation(rfc1213Mib.getSysLocation());

			logger.debug("equipTypeIndexMap size" + equipTypeIndexMap.size());
			logger.debug("equipTypeIndexMap map  " + equipTypeIndexMap.toString());
			logger.debug("Equipment Holder Map size " + equipHolderTypeIndexMap.size());
			logger.debug("Equipment Holder Map " + equipHolderTypeIndexMap);

			if (((BaseDiscoveryController) context).isScanStopped()) {
				logger.info("Discovery scan is stopped, interrupting data collection");
				request.getDeviceStatus().setIsScanStopped(true);
				// logger.log(Level.WARNING, "Scan is stopped, interrupting data collection");
				context.getScanRun().setFailureReason("Scan is stopped, interrupting data collection");
//				throw new ProcessorException("Scan is stopped, interrupting data collection");
				// return;
			}

			// Creating Equipment including Chassis
			for (Map.Entry<String, EntPhysicalEntryType> equipentry : equipTypeIndexMap.entrySet()) {
				logger.debug("Going to Model equipment::" + equipentry.getValue().getEntPhysicalName());
				modelEquipment(equipentry.getKey(), equipentry.getValue(), rfc1213Mib);
			}

			logger.debug("Equipment map size " + globalEquipMap.size());
			logger.debug("Equipment map  " + globalEquipMap.toString());

			// Creating EquipemtHolder
			for (Map.Entry<String, EntPhysicalEntryType> equipholderentry : equipHolderTypeIndexMap.entrySet()) {
				logger.debug("equipholderentry.getkey==========" + equipholderentry.getKey());
				logger.debug("equipholderentry.getValue()===========" + equipholderentry.getValue());
				logger.debug("equipholderentry.getkey==========" + equipholderentry.getKey());
				logger.debug("equipholderentry.getValue()===========" + equipholderentry.getValue());
				modelEquipmentHolder(equipholderentry.getKey(), equipholderentry.getValue());
			}

			logger.debug("EquipmentHolder map size " + globalEquipHolderMap.size());

			for (Map.Entry<String, EntPhysicalEntryType> portentry : portTypeIndexMap.entrySet()) {
				modelPort(portentry.getKey(), portentry.getValue());
			}

			logger.debug("Port map size " + globalPortMap.size());
			logger.debug("Port map " + globalPortMap.toString());

			// Sort holder and remove blank card
			for (Entry<String, Equipment> modeledEquipment : globalEquipMap.entrySet()) {

				if (!Utils.checkNull(modeledEquipment.getValue().getEquipmentHolders())
						&& !modeledEquipment.getValue().getEquipmentHolders().isEmpty()) {

					Map<Integer, EquipmentHolder> equipHolderTreeMap = new TreeMap<Integer, EquipmentHolder>();
					int i = 0;
//						int i = modeledEquipment.getValue().getEquipmentHolders().size();
					logger.debug("Value of initial i " + i);
					for (EquipmentHolder eh : modeledEquipment.getValue().getEquipmentHolders()) {
						logger.debug("Value of EquipmentHolder : " + eh);
						i++;
						if (Utils.checkNull(eh.getNativeEMSName())) {
							logger.debug("Value of i inside loop : " + i);
//								i++;
							eh.setNativeEmsName(Integer.toString(i));
							equipHolderTreeMap.put(Integer.valueOf(eh.getNativeEMSName()), eh);
						} else {
							/*
							 * 
							 * To handle special Scenarios of Fan and Power card where eh.getNativeEMSName()
							 * is not null
							 */
							logger.debug("Value of i inside loop : " + i);
//								i++;
//								eh.setNativeEmsName(Integer.toString(i));
							equipHolderTreeMap.put(Integer.valueOf(eh.getNativeEMSName()), eh);

						}

					}

					modeledEquipment.getValue().getEquipmentHolders().clear();
					modeledEquipment.getValue().getEquipmentHolders().addAll(equipHolderTreeMap.values());

				}

				removeBlankEquipment(modeledEquipment.getValue());
			}

			// Apply custom physical device specification
			logger.debug("chassis  before PD set" + chassisType.toUpperCase().trim().replaceAll("\\s+", "") + "_PD");
			chassisName = chassisType.toUpperCase().trim().replaceAll("\\s+", "");
			System.out.println("Chassis name =========" + chassisName);
			logger.debug("Chassis name =========" + chassisName);
			if (physicalDevice.getName().contains("S5328C") || physicalDevice.getName().contains("S5328F")
					|| physicalDevice.getName().contains("S5328E")) {
				chassisName = "Shelf-S5328C-EI-24S";
			} else if (physicalDevice.getName().contains("S5328P")) {
				chassisName = "Shelf-S5328C-EI";
			} else if (physicalDevice.getName().contains("S5352P")) {
				chassisName = "Shelf-S5352C-EI";
			} else if (rfc1213Mib.getSysDescr().contains("S5320-56C-EI-48S-DC")) {
				chassisName = "Shelf-S5320-56C-EI-48S-DC";
			} else if (rfc1213Mib.getSysDescr().contains("S5732-H48S6Q")) {
				chassisName = "Shelf-S5732-H48S6Q";
			} else if (rfc1213Mib.getSysDescr().contains("S6348-EI")) {
				chassisName = "Shelf-S6348-EI";
			} else if (physicalDevice.getName().contains("5000")) {
				chassisName = "Shelf-5000";
			} else {
				chassisName = chassisType.toUpperCase().trim().replaceAll("\\s+", "");
			}
			boolean hasPDSpecification = false;
			hasPDSpecification = niDiscoveryContext.applySpecification(physicalDevice,
					physicalDevice.getName().trim().replaceAll("\\s+", "") + "_PD");
			logger.debug("IP-----------------------" + snmpDataResponseType.getManagementIP());
			
			materialCode = materialcodeMapProps.getProperty(chassisName);
			devDim = niDiscoveryContext.getDeviceDimensionMappings().getProperty(chassisName);

			logger.debug("materialCode	=======" + materialCode);
			// System.out.println("materialCode=======" + materialCode);
			if (hasPDSpecification) {
				// logger.debug("In spec ===========================");
				logger.debug("spec  found");
				// apply custom physical device characteristics
				Map<String, String> physicalDeviceCharMap = new HashMap<String, String>();
				physicalDeviceCharMap.put("ipAddress",
						((snmpDataResponseType.getManagementIP() == null
								|| snmpDataResponseType.getManagementIP().isEmpty()) ? NA
										: snmpDataResponseType.getManagementIP()));
				physicalDeviceCharMap.put("neId", (deviceName == null || deviceName.isEmpty()) ? NA : deviceName);
				physicalDeviceCharMap.put("materialCode",
						(materialCode == null || materialCode.isEmpty()) ? NA : materialCode);
				physicalDeviceCharMap.put("alarmStatus", "ok");
				physicalDeviceCharMap.put("deviceDimension", checkforNull(devDim));
				physicalDeviceCharMap.put("emsName", "NCE");
				physicalDeviceCharMap.put("nmsName", "NCE");
				physicalDeviceCharMap.put("discoveryStatus", "Active");
				physicalDeviceCharMap.put("domain", "IP-MPLS");

				// physicalDeviceCharMap.put("NetworkLocationCode", location);
				if (entPhysicalEntryType[0].getEntPhysicalClass().toString().contains("chassis")) {
					physicalDeviceCharMap.put("hardwareVersion",
							checkforNull(entPhysicalEntryType[0].getEntPhysicalHardwareRev()));
					if (entPhysicalEntryType[0].getEntPhysicalSoftwareRev().length() > 40) {
						String softwareRev = entPhysicalEntryType[0].getEntPhysicalSoftwareRev();
						softwareRev = softwareRev.substring(0, 39);
						physicalDeviceCharMap.put("softwareVersion", checkforNull(softwareRev));
					} else {
						physicalDeviceCharMap.put("softwareVersion",
								checkforNull(entPhysicalEntryType[0].getEntPhysicalSoftwareRev()));
					}

				} else {
					physicalDeviceCharMap.put("softwareVersion", NA);
					physicalDeviceCharMap.put("hardwareVersion", NA);
				}
				/*
				 * else if (rfc1213Mib.getSysDescr() != null &&
				 * rfc1213Mib.getSysDescr().toLowerCase().contains("version")) { String ver =
				 * rfc1213Mib.getSysDescr().toLowerCase();
				 * 
				 * physicalDeviceCharMap .put("softwareVersion",
				 * rfc1213Mib.getSysDescr().substring(ver.indexOf("version") + 7,
				 * nthOccurrence(ver,"(",2)-1)); }
				 */

				niDiscoveryContext.applyCharacteristics(physicalDevice, physicalDeviceCharMap);
			} else {
				logger.debug("spec not found");
				getLogger().debug("Specification not found for physical device against sdevice:",
						physicalDevice.getName());
			}
			
//			logger.debug( "  Final "+physicalDevice.getEquipment());
			if(physicalDevice !=null ) {
				System.out.println("pd is not null");
				List<PhysicalDeviceEquipmentRel> pdEquipRel = physicalDevice.getEquipment();
				if (pdEquipRel.size()>=1) {
				    System.out.println("size of pdEquipRel.size()" + pdEquipRel.size());

					    for (PhysicalDeviceEquipmentRel physicalDeviceEquipmentRel : pdEquipRel) {
					        if (physicalDeviceEquipmentRel != null) {
	
					        System.out.println("physicalDeviceEquipmentRel is not null=========");
					        Equipment equipment = physicalDeviceEquipmentRel.getEquipment();
					        List<EquipmentHolder> equipmentHolders = equipment.getEquipmentHolders();
					        EquipmentSpecification specification = equipment.getSpecification();
					        System.out.println("shelf specification name====================" + specification.getName().replaceAll("\\s+", ""));
					        String noOfslots = slotPosition.getProperty(specification.getName().replaceAll("\\s+", ""));
					        System.out.println("noOfslots=============" + noOfslots);
					        if (noOfslots !=null && !noOfslots.isEmpty()) {
					        List<EquipmentHolder> newEquipmentHolders = new ArrayList<>();
					        Set<String> existingNativeEmsNames = new HashSet<>();
					        for (EquipmentHolder slot : equipmentHolders) {
					            System.out.println("Existing slots: " + slot.getName());
					            //list of native ems
					           existingNativeEmsNames.add(slot.getNativeEmsName()) ;
					        
					        }
					        String finalSpecName=null;
					        if (existingNativeEmsNames.contains("0")) {
					        	for (int i = 0; i < Integer.parseInt(noOfslots); i++) {
					                String newNativeEmsName = String.valueOf(i);
					               // System.out.println("value of i============"+i);
					                //newNativeEmsName=23 existingNativeEmsNames=no 23
					                if (!existingNativeEmsNames.contains(newNativeEmsName)) {
					                    EquipmentHolder holder = DiscoveryUtility.createEquipmentHolder();
					                    holder.setName("Slot-" + i); //23
					                    holder.setNativeEmsName(String.valueOf(i));
					                    holder.setDescription("EmptySlots");
					                   // holder.setSerialNumber("NA");
					                   // holder.setPhysicalLocation(physicalLoc);
					                   // System.out.println("new slot name ==============="+holder.getName());
					                    String holderName = chassisType.toUpperCase().trim().replaceAll("\\s+", "") + "_"
					            				+ holder.getName().toUpperCase().trim().replaceAll("\\s+", "_");
					                    
					            		String sysDescr = rfc1213Mib.getSysDescr();
					            		 if (physicalDevice.getName().contains("5000")) {
					            			finalSpecName = "5000_" + holderName;
					            		} else if (physicalDevice.getName().contains("S5328C") || physicalDevice.getName().contains("S5328F")
					            				|| physicalDevice.getName().contains("S5328E")) {
					            			finalSpecName = "S5328C-EI-24S_" + holderName;
					            		} else if (physicalDevice.getName().contains("S5328P")&& !sysDescr.contains("S5328C-EI-24S")) {
					            			finalSpecName = "S5328C-EI_" + holderName;
					            		} else if (physicalDevice.getName().contains("S5352P")) {
					            			finalSpecName = "S5352C-EI_" + holderName;
					            		}else if (sysDescr.contains("S5328C-EI-24S")) {
					            			finalSpecName = "S5328C-EI-24S_" + holderName;
					            		} else {
					            			finalSpecName = holderName;
					            		}

					                    boolean hasSlotSpecification=niDiscoveryContext.applySpecification(holder, finalSpecName);
					                    newEquipmentHolders.add(holder);
					            }
					            
					        }
					        	equipmentHolders.addAll(newEquipmentHolders); 
							} else {
								for (int i = 1; i <= Integer.parseInt(noOfslots); i++) {
					                String newNativeEmsName = String.valueOf(i);
					                //newNativeEmsName=23 existingNativeEmsNames=no 23
					                if (!existingNativeEmsNames.contains(newNativeEmsName)) {
					                    EquipmentHolder holder = DiscoveryUtility.createEquipmentHolder();
					                    holder.setName("Slot-" + i); //23
					                    holder.setNativeEmsName(String.valueOf(i));
					                    holder.setDescription("EmptySlots");
					                    //holder.setSerialNumber(NA);
					                    holder.setPhysicalLocation(physicalLoc);
					                    String holderName = chassisType.toUpperCase().trim().replaceAll("\\s+", "") + "_"
					            				+ holder.getName().toUpperCase().trim().replaceAll("\\s+", "_");
					                    boolean hasSlotSpecification=niDiscoveryContext.applySpecification(holder, holderName);
					                    newEquipmentHolders.add(holder);
					            }
					            
					        }
								equipmentHolders.addAll(newEquipmentHolders); 
							}
					            
					            //add the sorting logic
					            //equipmentHolders.addAll(newEquipmentHolders); 
					            equipmentHolders.sort(Comparator.comparing(EquipmentHolder::getNativeEmsName)); 
					            customSort(equipmentHolders);
					            // Add new equipment holders to the original list after the iteration
					         //   equipmentHolders.addAll(newEquipmentHolders);
					            /*if (specification.getName().contains("EquipmentHolder")) {
					            	equipment.getEquipmentHolders().clear();
								}*/
					        }}
					   
					        
					    }
				    //adding sorting logic
				    List<PhysicalDeviceEquipmentRel> newEquipmentList = new ArrayList<>();
		            
					for (PhysicalDeviceEquipmentRel pdEqRel : physicalDevice.getEquipment()) {
				    

					    if (pdEqRel.getEquipment() != null) {

					        Equipment shelf = sortChildHolderForEquipment(pdEqRel.getEquipment());
 
					        // Create a new PhysicalDeviceEquipmentRel for the sorted equipment

					        PhysicalDeviceEquipmentRel newPdEqRel = PersistenceHelper.makeEntity(PhysicalDeviceEquipmentRel.class);

					        newPdEqRel.setEquipment(shelf);
 
					        // Add the new PhysicalDeviceEquipmentRel to the list

					        newEquipmentList.add(newPdEqRel);
 
					        // Iterate through equipment holders in the sorted equipment

					        for (EquipmentHolder eh : newPdEqRel.getEquipment().getEquipmentHolders()) {

					            // Perform actions with each EquipmentHolder

//					            System.out.println("Processing EquipmentHolder: " + eh.getName());

					        }

					    }

					
						}
					physicalDevice.getEquipment().clear();
 
					// Add the sorted equipment list to the physical device
					physicalDevice.getEquipment().addAll(newEquipmentList);
				}
				
			
			response.setPhysicalDevice(physicalDevice);
			response.setPortNameAndEntity(mappedPhysicalToLogicalPortMap);

			getLogger().info("invoke: exit");
			getLogger().info(DiscoveryConstants.LOG_EXIT + mid);
			// context.addToResult(physicalDevice.getName(), "HuaweiDevice",
			// physicalDevice);
			}} catch (Exception e) {
			e.printStackTrace();
			getLogger().error(e.getMessage());
			throw new DiscoveryRuntimeException(ipAddress,
					"Exception in HuaweiPhysicalDeviceModelerProcessorImpl:invoke", request.getDeviceStatus());
		}

		return response;

	}
	private static void customSort(List<EquipmentHolder> equipmentHolders) {
	    equipmentHolders.sort(new Comparator<EquipmentHolder>() {
	        @Override
	        public int compare(EquipmentHolder holder1, EquipmentHolder holder2) {
	            return holder1.getNativeEmsName().compareTo(holder2.getNativeEmsName());
	        }
	    });
	}
	public Equipment sortChildHolderForEquipment(Equipment parent) {

	    if (parent.getEquipmentHolders() != null) {

	        List<EquipmentHolder> holderList = new LinkedList<>();

	        Map<Integer, EquipmentHolder> sortedEhMap = new TreeMap<>();
 
	        // Sort EquipmentHolders based on the specified criteria

	        for (EquipmentHolder eq : parent.getEquipmentHolders()) {

	        	if(eq!=null && eq.getSpecification()!=null && eq.getSpecification().getName()!=null) {

	        	System.out.println("eq.getSpecification().getName()===="+eq.getSpecification().getName());

	        			sortedEhMap.put(Integer.parseInt(eq.getNativeEmsName()), eq);
	        	}

	        }
 
	        parent.getEquipmentHolders().clear();
 
	        // Add sorted EquipmentHolders back to the parent

	        for (Map.Entry<Integer, EquipmentHolder> entrySet : sortedEhMap.entrySet()) {

	            holderList.add(entrySet.getValue());

	        }
 
	        parent.setEquipmentHolders(holderList);

	    }
 
	    return parent;

	}
	private void removeBlankEquipment(Equipment equipment) {

		final String mid = MID + "removeBlankEquipment";
		getLogger().info(DiscoveryConstants.LOG_ENTER + mid);

		if ((equipment.getChildEquipment() == null
				|| (equipment.getChildEquipment() != null && equipment.getChildEquipment().isEmpty()))
				&& equipment.getEquipmentHolders() != null && equipment.getEquipmentHolders().isEmpty()
				&& equipment.getPhysicalPorts() != null && equipment.getPhysicalPorts().isEmpty()) {

			if (equipment.getParentEquipment() != null && equipment.getParentEquipment().getParentEquipment() != null) {

				EquipmentEquipmentRel equipEquipRel = equipment.getParentEquipment();
				Equipment parentEquipment = equipEquipRel.getParentEquipment();
				equipEquipRel.getParentEquipment().getChildEquipment().remove(equipEquipRel);
				removeBlankEquipment(parentEquipment);
			}
		}

		getLogger().info(DiscoveryConstants.LOG_EXIT + mid);
	}

	private PhysicalPort modelPort(String key, EntPhysicalEntryType portdata) {

		boolean getPortBandFromCard = false;

		final String mid = MID + "modelPort";
		getLogger().debug(DiscoveryConstants.LOG_ENTER + mid);

		if (!globalPortMap.containsKey(key)) {

			PhysicalPort port = DiscoveryUtility.createPhysicalPort();
			// getPhysicalDeviceEquipmentRel(pd, equipment);
			port.setName(portdata.getEntPhysicalName());
			//port.setId(portdata.getEntPhysicalName());
			port.setDescription(
					(portdata.getEntPhysicalDescr() == null || portdata.getEntPhysicalDescr().isEmpty()) ? NA
							: portdata.getEntPhysicalDescr());
			port.setPortNumber((portdata.getEntPhysicalParentRelPos().intValue()));
			if (portdata.getEntPhysicalSerialNum() != null && !portdata.getEntPhysicalSerialNum().trim().isEmpty()) {
				port.setSerialNumber(portdata.getEntPhysicalSerialNum());
			} else {
				port.setSerialNumber(populateSerialNumber(portdata.getIndex()));
			}
			port.setPhysicalLocation(checkforNull(physicalLoc));
			port.setSerialNumber(NA);
			port.setCustomerPortName(NA);
			port.setPhysicalAddress(ipAddress);
			port.setVendorPortName(NA);
			String portName = portdata.getEntPhysicalName().toUpperCase().trim();
			boolean hasPORTSpecification = false;
			String portDiscreption = portdata.getEntPhysicalDescr().toUpperCase();
			if (portDiscreption != null && !portDiscreption.isEmpty() && !portName.contains("XGIGABITETHERNET")
					&& !portName.contains("METH") && !portName.equals("PORT") && !portName.contains("40GE")
					&& !portDiscreption.equals("PORT")) {
				String[] portRate = portDiscreption.split("/");

				hasPORTSpecification = niDiscoveryContext.applySpecification(port,
						portRate[0].trim().replaceAll("\\s+", "") + "_PORT");
			} else {
				// GigabitEthernet8/0/0
				String name = portdata.getEntPhysicalName().toUpperCase();
				if (name.contains("XGIGABITETHERNET")) {

					String portname = name.substring(0, 3);
					// System.out.println("in x gig e port=========" + portname);
					hasPORTSpecification = niDiscoveryContext.applySpecification(port,
							portname.trim().replaceAll("\\s+", "") + "_PORT");
				} else if (name.contains("METH")) {

					String portname = name.substring(0, 3);
					// System.out.println("in meth----------" + portname);
					hasPORTSpecification = niDiscoveryContext.applySpecification(port,
							portname.trim().replaceAll("\\s+", "") + "_PORT");
				} else if (name.contains("40GE")) {

					String portname = "40GE";
					// System.out.println("in 40GE----yyyyyyyyyyyyyyyyyyyyyyy------" + portname);
					hasPORTSpecification = niDiscoveryContext.applySpecification(port,
							portname.trim().replaceAll("\\s+", "") + "_PORT");

				} else {
					String portname = name.substring(0, 3);
					// System.out.println("in gigabite----------==========" + portname);
					hasPORTSpecification = niDiscoveryContext.applySpecification(port,
							portname.trim().replaceAll("\\s+", "") + "_PORT");
				}

			}
			// hasPORTSpecification =
			// niDiscoveryContext.applySpecification(port,portdata.getEntPhysicalDescr().toUpperCase().trim().replaceAll("\\s+"));
			logger.debug("Port Key::" + portdata.getEntPhysicalName().toUpperCase().trim().replaceAll("\\s+", ""));

			String parentindex = portdata.getEntPhysicalContainedIn().toString();
			Equipment parentEquipment = null;

			if (!Utils.checkNull(parentindex) && globalEquipMap.containsKey(parentindex)) {
				parentEquipment = globalEquipMap.get(parentindex);
			}
			// portName =
			// DefaultModelHelper.getAppropriatePortBandwidth(portdata,parentEquipment,getPortBandFromCard);

			// Setting this attribute for mapping DI bandwidth

			// hasPORTSpecification =
			// niDiscoveryContext.applySpecification(port,portName+"_PORT");*/

			if (hasPORTSpecification) {
				// logger.debug("port spec found");
				Map<String, String> portCharMap = new HashMap<String, String>();
				portCharMap.put("discoveryStatus", "Active");
				portCharMap.put("alarmStatus", "ok");

				niDiscoveryContext.applyCharacteristics(port, portCharMap);
			} else {

				getLogger().warn("Specification not found for Port against Port type:"
						+ portdata.getEntPhysicalDescr().toUpperCase().trim().replaceAll("\\s+", ""));
			}

			globalPortMap.put(key, port);
			mappedPhysicalToLogicalPortMap.put(portdata.getEntPhysicalName().toLowerCase(), port);

			if (!Utils.checkNull(parentindex) && globalEquipMap.containsKey(parentindex)) {
				Equipment parenteuipment = globalEquipMap.get(parentindex);
				if (!port.getName().contains("Cascade")&&!port.getName().contains("Fsp")) {
					DiscoveryUtility.addPhysicalPortToEquipment(parenteuipment, port);
				}
				
				logger.debug("Add child equipmentHolder " + parenteuipment.getName() + " " + port.getName());
			}

		}
		getLogger().info(DiscoveryConstants.LOG_EXIT + mid);
		return globalPortMap.get(key);

	}

	private Equipment modelEquipment(String key, EntPhysicalEntryType equipData, Rfc1213MibMib rfc1213Mib) {
		final String mid = MID + "modelEquipment";
		getLogger().info(DiscoveryConstants.LOG_ENTER + mid);
		String parentindex = equipData.getEntPhysicalContainedIn().toString();
		
		Equipment rearEquipment = null;
//		String networkLocationCode="";
		String chassisName = chassisType.toUpperCase().trim().replaceAll("\\s+", "");
		logger.debug("parentindex inside modelEquipment ::::::" + parentindex);
		String sysDescr = rfc1213Mib.getSysDescr();
		String shelfName = null;
		if (!globalEquipMap.containsKey(key)) {
//			if(rfc1213Mib.getSysLocation().length()<25) {
//				networkLocationCode = rfc1213Mib.getSysLocation();
//			}else {
//				networkLocationCode = rfc1213Mib.getSysName();
//			}
			if (physicalDevice.getName().contains("S5328C") || physicalDevice.getName().contains("S5328F")
					|| physicalDevice.getName().contains("S5328E")) {
				shelfName = "Shelf-S5328C-EI-24S frame";
			} else if (physicalDevice.getName().contains("S5328P") && !sysDescr.contains("S5328C-EI-24S")) {
				shelfName = "Shelf-S5328C-EI frame";
			} else if (physicalDevice.getName().contains("S5352P")) {
				shelfName = "Shelf-S5352C-EI frame";
			} else if (sysDescr.contains("S5320-56C-EI-48S-DC")) {
				shelfName = "Shelf-S5320-56C-EI-48S-DC frame";
			} else if (sysDescr.contains("S5732-H48S6Q")) {
				shelfName = "Shelf-S5732-H48S6Q frame";
			} else if (sysDescr.contains("S6348-EI")) {
				shelfName = "Shelf-S6348-EI frame";
			} else if (sysDescr.contains("S6730-H48X6C")) {
				shelfName = "Shelf-S6730-H48X6C frame";
			} else if (sysDescr.contains("S5352C-EI")) {
				shelfName = "Shelf-S5352C-EI frame";
			} else if (physicalDevice.getName().contains("5000")) {
				shelfName = chassisType + "5000";
			}else if (sysDescr.contains("S5328C-EI-24S")) {
				shelfName = "Shelf-S5328C-EI-24S frame";
			}
			else {
				shelfName = equipData.getEntPhysicalName();
			}
			Properties networkLocationMappings = NIDiscoveryContext.getNetworkLocationMappings();
			//location = networkLocationMappings.getProperty(shelfName);
			Equipment equipment = DiscoveryUtility.createEquipment();
			if (equipData.getEntPhysicalClass().toString().toUpperCase().contains("CHASSIS")
					|| equipData.getEntPhysicalClass().toString().toUpperCase().contains("FRAME")) {
				// System.out.println("shelfName.toUpperCase()===="+shelfName.toUpperCase());
				if (shelfName.toUpperCase().contains("NE40E-X8 FRAME")
						|| shelfName.toUpperCase().contains("NE40E-X16 FRAME")) {
					equipment.setName(shelfName + "_front");
				} else {
					equipment.setName(shelfName);
				}

			} else {
				equipment.setName(checkforNull(equipData.getEntPhysicalName()));
			}
			devDim = niDiscoveryContext.getDeviceDimensionMappings().getProperty(shelfName);
			String shelfmatcode=niDiscoveryContext.getMaterialCodeMappings().getProperty(shelfName+"_MAT");
			String shelfdevDimension=devDim+"_DIMENSION";
			//equipment.setPhysicalLocation(checkforNull(physicalLoc));
			equipment.setDescription(checkforNull(equipData.getEntPhysicalDescr()));
			equipment.setSerialNumber(
					(equipData.getEntPhysicalSerialNum() == null || equipData.getEntPhysicalSerialNum().isEmpty())
							? NA
							: equipData.getEntPhysicalSerialNum());
			// equipment.set
			//equipment.setNetworkLocationCode(location);
			//equipment.setNativeEMSName(checkforNull(rfc1213Mib.getSysName()));
			equipment.setNetworkLocationCode(location);
			boolean hasCARDSpecification = false;
			// materialCode = materialcodeMapProps.getProperty(chassisName);
			getLogger().info("About to instantiate Equipment object" + equipment);
			if (parentindex.equals("0")) {

				hasCARDSpecification = niDiscoveryContext.applySpecification(equipment,
						shelfName.toUpperCase().trim().replaceAll("\\s+", "") + "_SHELF");
				// Rear Equipment creation if exist in property file
				String keyChassis = chassisType.toUpperCase().trim().replaceAll("\\s+", "") + "_CHASSIS_REAR";
				globalEquipMap.put(key, equipment);
				if (niDiscoveryContext.propertiesKeyValue(
						chassisType.toUpperCase().trim().replaceAll("\\s+", "") + "_CHASSIS_REAR") != null) {

					rearEquipment = DiscoveryUtility.createEquipment();
					rearEquipment.setName(checkforNull(shelfName + "_back"));
					rearEquipment.setDescription(checkforNull(equipData.getEntPhysicalDescr()));
					rearEquipment.setSerialNumber((equipData.getEntPhysicalSerialNum() == null
							|| equipData.getEntPhysicalSerialNum().isEmpty()) ? NA
									: equipData.getEntPhysicalSerialNum());
					//rearEquipment.setPhysicalLocation(checkforNull(physicalLoc));
					rearEquipment.setNetworkLocationCode(location);
//					rearEquipment.setPhysicalLocation(networkLocationCode);
					//rearEquipment.setNetworkLocationCode(location);
//					rearEquipment.setNativeEMSName(rfc1213Mib.getSysName());
					boolean hasREARCARDSpecification = niDiscoveryContext.applySpecification(rearEquipment,
							chassisType.toUpperCase().trim().replaceAll("\\s+", "") + "_CHASSIS_REAR");

					if (hasREARCARDSpecification) {
						// logger.debug("hasREARCARDSpecification-----------------");
						Map<String, String> cardCharMap = new HashMap<String, String>();
						cardCharMap.put("partnumber", equipData.getEntPhysicalModelName().toUpperCase().trim());
						cardCharMap.put("discoveryStatus", "Active");
						cardCharMap.put("materialCode", checkforNull(shelfmatcode));
						cardCharMap.put("alarmStatus", "ok");
						cardCharMap.put("category", NA);
						cardCharMap.put("subCategory", NA);
						cardCharMap.put("deviceDimension", NA);
						cardCharMap.put("HardwareVersion",
								((equipData.getEntPhysicalHardwareRev() == null
										|| equipData.getEntPhysicalHardwareRev().isEmpty()) ? NA
												: equipData.getEntPhysicalHardwareRev()));
						cardCharMap.put("SoftwareVersion",
								((equipData.getEntPhysicalSoftwareRev() == null
										|| equipData.getEntPhysicalSoftwareRev().isEmpty()) ? NA
												: equipData.getEntPhysicalSoftwareRev()));

						niDiscoveryContext.applyCharacteristics(rearEquipment, cardCharMap);
					}

					key = key + "_REAR";
					globalEquipMap.put(key, rearEquipment);
				}
			} else if (parentindex.equals("1") && chassisName.contains("S5624")) {
				hasCARDSpecification = niDiscoveryContext.applySpecification(equipment,
						chassisType.toUpperCase().trim().replaceAll("\\s+", "") + "_SHELF");
				globalEquipMap.put(key, equipment);

				if (niDiscoveryContext.propertiesKeyValue(
						chassisType.toUpperCase().trim().replaceAll("\\s+", "") + "_CHASSIS_REAR") != null) {

					rearEquipment = DiscoveryUtility.createEquipment();
					rearEquipment.setName(shelfName + "back");
					rearEquipment.setDescription(equipData.getEntPhysicalDescr());
					rearEquipment.setSerialNumber((equipData.getEntPhysicalSerialNum() == null
							|| equipData.getEntPhysicalSerialNum().isEmpty()) ? NA
									: equipData.getEntPhysicalSerialNum());
					//rearEquipment.setPhysicalLocation(physicalLoc);
					rearEquipment.setNetworkLocationCode(location);
					//rearEquipment.setNetworkLocationCode(location);
					
//					rearEquipment.setNativeEMSName(rfc1213Mib.getSysName());
					boolean hasREARCARDSpecification = niDiscoveryContext.applySpecification(rearEquipment,
							chassisType.toUpperCase().trim().replaceAll("\\s+", "") + "_CHASSIS_REAR");

					if (hasREARCARDSpecification) {
						// logger.debug("hasREARCARDSpecification-----------------");
						Map<String, String> cardCharMap = new HashMap<String, String>();
						cardCharMap.put("discoveryStatus", "Active");
						cardCharMap.put("materialCode", NA);
						cardCharMap.put("alarmStatus", "ok");
						cardCharMap.put("category", NA);
						cardCharMap.put("subCategory", NA);
						cardCharMap.put("deviceDimension", NA);
						cardCharMap.put("HardwareVersion",
								((equipData.getEntPhysicalHardwareRev() == null
										|| equipData.getEntPhysicalHardwareRev().isEmpty()) ? NA
												: equipData.getEntPhysicalHardwareRev()));
						cardCharMap.put("SoftwareVersion",
								((equipData.getEntPhysicalSoftwareRev() == null
										|| equipData.getEntPhysicalSoftwareRev().isEmpty()) ? NA
												: equipData.getEntPhysicalSoftwareRev()));

						niDiscoveryContext.applyCharacteristics(rearEquipment, cardCharMap);
					}

					key = key + "_REAR";
					globalEquipMap.put(key, rearEquipment);
				}

			} else if (!Utils.checkNull(equipData.getEntPhysicalName())
					&& !Utils.checkBlank(equipData.getEntPhysicalName()) && !hasCARDSpecification) {
				hasCARDSpecification = niDiscoveryContext.applySpecification(equipment,
						equipData.getEntPhysicalName().toUpperCase().trim().replaceAll("\\s+", "") + "_CARD");
				// hasCARDSpecification = applyCardSpecFromPhyName(equipData, equipment,
				// hasCARDSpecification);
			} else if (!Utils.checkNull(equipData.getEntPhysicalModelName())
					&& !Utils.checkBlank(equipData.getEntPhysicalModelName())
					&& !equipData.getEntPhysicalModelName().trim().equalsIgnoreCase(NA) && !hasCARDSpecification) {
				hasCARDSpecification = niDiscoveryContext.applySpecification(equipment,
						equipData.getEntPhysicalModelName().toUpperCase().trim().replaceAll("\\s+", "") + "_CARD");
			} else {

				String partNumber = DefaultModelHelper.getPartNumber(chassisType, equipData.getEntPhysicalName());
				hasCARDSpecification = niDiscoveryContext.applySpecification(equipment, partNumber + "_CARD");

				if (!Utils.checkNull(partNumber))
					equipData.setEntPhysicalModelName(partNumber);
			}

			if (hasCARDSpecification) {
				// apply custom Card characteristics
				// logger.debug("hasCARDSpecification-----------in
				// card------------------------");
				Map<String, String> cardCharMap = new HashMap<String, String>();
				if (equipData.getEntPhysicalName().toLowerCase().contains("power")) {
					cardCharMap.put("partnumber", "POWER");
				} else if (equipData.getEntPhysicalName().toLowerCase().contains("fan")) {
					cardCharMap.put("partnumber", "FAN");
				} else {
					cardCharMap.put("partnumber", equipData.getEntPhysicalModelName().toUpperCase().trim());
				}

				cardCharMap.put("discoveryStatus", "Active");
				cardCharMap.put("materialCode", NA);
				cardCharMap.put("alarmStatus", "ok");
				cardCharMap.put("category", NA);
				cardCharMap.put("subCategory", NA);
				cardCharMap.put("deviceDimension", NA);
				cardCharMap.put("hardwareVersion",
						((equipData.getEntPhysicalHardwareRev() == null
								|| equipData.getEntPhysicalHardwareRev().isEmpty()) ? NA
										: equipData.getEntPhysicalHardwareRev()));
				cardCharMap.put("softwareVersion",
						((equipData.getEntPhysicalSoftwareRev() == null
								|| equipData.getEntPhysicalSoftwareRev().isEmpty()) ? NA
										: equipData.getEntPhysicalSoftwareRev()));
				cardCharMap.put("SubBroadType", equipData.getEntPhysicalModelName().toUpperCase().trim());
				cardCharMap.put("BoardType", equipData.getEntPhysicalModelName().toUpperCase().trim());

				niDiscoveryContext.applyCharacteristics(equipment, cardCharMap);
			} else {
				logger.debug("False hasCARDSpecification exists so spec is " + null);
				getLogger().warn("Specification not found for Card against PartNumber:"
						+ equipData.getEntPhysicalModelName().toUpperCase());
			}

			// Chassis need to check
			if (parentindex.equals("0")) {
				DiscoveryUtility.addEquipmentToPhysicalDevice(physicalDevice, equipment);
				// chassis=equipment;
				logger.debug("Chassis added to physicaldevice" + physicalDevice.getEquipment());
				if (rearEquipment != null)
					DiscoveryUtility.addEquipmentToPhysicalDevice(physicalDevice, rearEquipment);

			} else if (parentindex.equals("1") && chassisName.contains("S5624")) {
				DiscoveryUtility.addEquipmentToPhysicalDevice(physicalDevice, equipment);
				// chassis=equipment;
				logger.debug("Chassis added to physicaldevice" + physicalDevice.getEquipment());
				if (rearEquipment != null)
					DiscoveryUtility.addEquipmentToPhysicalDevice(physicalDevice, rearEquipment);

			} else if (equipTypeIndexMap.containsKey(parentindex)) {
				globalEquipMap.put(key, equipment);
				// Adding Equipment to equipment
				Equipment parentEquipment = null;

				if (!globalEquipMap.containsKey(parentindex)) {
					EquipmentHolder dummyslot = createDummyEquipmentHolder(parentEquipment, equipment,
							niDiscoveryContext);
					parentEquipment = modelEquipment(parentindex, equipTypeIndexMap.get(parentindex), rfc1213Mib);

				} else {
					parentEquipment = globalEquipMap.get(parentindex);
				}
				logger.debug("parentEquipment-----------" + parentEquipment.getName());
				logger.debug("Equipment-----------" + equipment.getName());

				DiscoveryUtility.addEquipmentToEquipment(parentEquipment, equipment, niDiscoveryContext);
				// System.out.println("Add equipment " + parentEquipment.getName() + " " +
				// equipment.getName());

			} // Adding Equipment to equipmentHolder
			else if (equipHolderTypeIndexMap.containsKey(parentindex)) {
				globalEquipMap.put(key, equipment);
				EquipmentHolder parentEquipHolder = null;

				if (!globalEquipHolderMap.containsKey(parentindex)) {
					parentEquipHolder = modelEquipmentHolder(parentindex, equipHolderTypeIndexMap.get(parentindex));
				} else {
					parentEquipHolder = globalEquipHolderMap.get(parentindex);
				}
				DiscoveryUtility.addEquipmentToEquipmentHolder(parentEquipHolder, equipment);
				logger.debug("Add equipment to holder " + equipment.getName() + " " + parentEquipHolder.getName());
			}
		}
		getLogger().info(DiscoveryConstants.LOG_EXIT + mid);
		return globalEquipMap.get(key);
	}

	private EquipmentHolder createDummyEquipmentHolder(Equipment parentEquipment, Equipment equipment,
			NIDiscoveryContext niDiscoveryContext2) {
		EquipmentHolder dummyslot = DiscoveryUtility.createEquipmentHolder();
		dummyslot.setName("subslot");
		logger.debug("created dummy slot====" + dummyslot.getName());
		// TODO Auto-generated method stub
		return dummyslot;
	}

	/**
	 * @param equipData
	 * @param equipment
	 * @param hasCARDSpecification
	 * @return
	 */

	/**
	 * @param equipData
	 * @param equipment
	 * @param hasCARDSpecification
	 * @return
	 */

	/**
	 * @param key
	 * @param equipHolderdata
	 * @return
	 */
	@SuppressWarnings("unlikely-arg-type")
	private EquipmentHolder modelEquipmentHolder(String key, EntPhysicalEntryType equipHolderdata) {
		final String mid = MID + "modelEquipmentHolder";
		getLogger().info(DiscoveryConstants.LOG_ENTER + mid);
		EquipmentHolder equipHolder = null;
		String slotindex = null;
		String finalSpecName = "generic";
		String slotindexKey = null;
		Set<Entry<String, EquipmentHolder>> entrySet = globalEquipHolderMap.entrySet();
		for (Entry<String, EquipmentHolder> entry : entrySet) {
			String key2 = entry.getKey();
			logger.debug("key2===========" + key2);
			EquipmentHolder value = entry.getValue();
			String name = value.getName();
			logger.debug("name is------------------" + name);
		}
		if (!globalEquipHolderMap.containsKey(key)) {

			equipHolder = DiscoveryUtility.createEquipmentHolder();
			 equipHolder.setName(equipHolderdata.getEntPhysicalName());
			// equipHolderdata.getEntPhysicalParentRelPos().toString()
			String slotRelPosition = equipHolderdata.getEntPhysicalParentRelPos().toString();
			//System.out.println("chassis type=========================="+chassisType);
			String shelfType = chassisType.replaceAll("\\s+", "");
			//System.out.println("chassis type=========================="+shelfType);
			//String slotValue = slotPosition.getProperty(shelfType);
			
			//System.out.println("slotValue============="+slotValue);
			
			/*if (slotValue=="true" && !equipHolder.getEntityType().equals("HUAWEI_SUBSLOT")) {
				incrementvalue=1;
				
				} else {
					incrementvalue=0;
			}*/
			/*int slotNo = Integer.parseInt(slotRelPosition);
			int slotNumberFinal=slotNo+incrementvalue;*/
			//equipHolder.setName("Slot-" + slotNumberFinal);
			equipHolder.setName("Slot-" + equipHolderdata.getEntPhysicalParentRelPos().toString());
			logger.debug("slot name =========" + equipHolder.getName());
			equipHolder.setDescription(equipHolderdata.getEntPhysicalDescr());
			//equipHolder.setSerialNumber("N/A");
			//equipHolder.setPhysicalLocation(checkforNull(physicalLoc));
			//equipHolder.setId("Slot-" + equipHolderdata.getEntPhysicalParentRelPos().toString());
			finalSpecName = getAppropriateHolderEntityKey(equipHolderdata, equipHolder, finalSpecName);
			// Need to Add chassisType as prefix...........
			slotindexKey = chassisType.toUpperCase().trim().replaceAll("\\s+", "") + "_" + finalSpecName + "_REAR";
			// System.out.println("slotindexKey=============="+slotindexKey);
//			slotindex = niDiscoveryContext.getPropValue(slotindexKey);propertiesKeyValue
			slotindex = niDiscoveryContext.propertiesKeyValue(slotindexKey);
			logger.debug("Value of slotindex >>>>>>>>" + slotindex + " returned by slotindexKey >>>>>" + slotindexKey);
			logger.debug("setting setNativeEmsName " + equipHolderdata.getEntPhysicalParentRelPos().toString()
					+ " for equipment holder " + equipHolder.getName());
			equipHolder.setNativeEmsName(equipHolderdata.getEntPhysicalParentRelPos().toString());
			globalEquipHolderMap.put(key, equipHolder);
		}

		equipHolder = globalEquipHolderMap.get(key);
		String parentindex = equipHolderdata.getEntPhysicalContainedIn().toString();

		if (!Utils.checkNull(slotindex)) {

			logger.debug("Adding slot " + slotindex);

			if (slotindex.contains("_REAR")) {
				parentindex = parentindex + "_REAR";
			}

//			equipHolder.setNativeEMSName(slotindex.replaceAll("_REAR", "").trim());
			logger.debug("setting setNativeEmsName " + equipHolderdata.getEntPhysicalParentRelPos().toString()
					+ " for equipment holder " + equipHolder.getName());
			equipHolder.setNativeEmsName(equipHolderdata.getEntPhysicalParentRelPos().toString());

		}
		if (!Utils.checkNull(parentindex) && globalEquipMap.containsKey(parentindex)) {
			Equipment parentequipment = globalEquipMap.get(parentindex);
			if (!Utils.checkNull(parentequipment.getEquipmentHolders())
					&& !parentequipment.getEquipmentHolders().contains(equipHolder)) {
				DiscoveryUtility.addEquipmentHolderToEquipment(parentequipment, equipHolder);

				logger.debug("Add equipmentHolder " + parentequipment.getName() + " " + equipHolder.getName());
			}
		}
		getLogger().info(DiscoveryConstants.LOG_EXIT + mid);
		return globalEquipHolderMap.get(key);
	}

	private String getAppropriateHolderEntityKey(EntPhysicalEntryType equipHolderdata, EquipmentHolder equipHolder,
			String finalSpecName) {
		boolean hasHolderSpecification = false;

		String holderName = chassisType.toUpperCase().trim().replaceAll("\\s+", "") + "_"
				+ equipHolderdata.getEntPhysicalName().toUpperCase().trim().replaceAll("\\s+", "_");
		String sysDescr = rfc1213Mib.getSysDescr();
		if (holderName.contains("CFCARDSLOT") || (holderName.contains("CARD_SLOT") && holderName.contains("/"))) {
			finalSpecName = "CHILD_SLOT";
		} else if (physicalDevice.getName().contains("5000")) {
			finalSpecName = "5000_" + holderName;
		} else if (physicalDevice.getName().contains("S5328C") || physicalDevice.getName().contains("S5328F")
				|| physicalDevice.getName().contains("S5328E")) {
			finalSpecName = "S5328C-EI-24S_" + holderName;
		} else if (physicalDevice.getName().contains("S5328P")&& !sysDescr.contains("S5328C-EI-24S")) {
			finalSpecName = "S5328C-EI_" + holderName;
		} else if (physicalDevice.getName().contains("S5352P")) {
			finalSpecName = "S5352C-EI_" + holderName;
		}else if (sysDescr.contains("S5328C-EI-24S")) {
			finalSpecName = "S5328C-EI-24S_" + holderName;
		} else {
			finalSpecName = holderName;
		}

		hasHolderSpecification = niDiscoveryContext.applySpecification(equipHolder, finalSpecName);

		return finalSpecName;
	}

	/**
	 * @param entPhysicalEntryTypes
	 * @throws ProcessorException
	 */
	private void populateMapForEntityType(EntPhysicalEntryType[] entPhysicalEntryTypes) throws ProcessorException {
		final String mid = MID + "populateMapForEntityType";
		getLogger().info(DiscoveryConstants.LOG_ENTER + mid);
		try {
			getLogger().debug("addEntryTypeToMap Started...");
			logger.debug("length of entPhysicalEntryType " + entPhysicalEntryType.length);
			equipTypeIndexMap = new TreeMap<String, EntPhysicalEntryType>();
			equipHolderTypeIndexMap = new TreeMap<String, EntPhysicalEntryType>();
			portTypeIndexMap = new TreeMap<String, EntPhysicalEntryType>();

			for (EntPhysicalEntryType entPhysicalEntryType : entPhysicalEntryTypes) {
				logger.debug("Inside first for loop of addEntryTypeToMap....");
				logger.debug("Inside first for loop of addEntryTypeToMap...." + entPhysicalEntryType);
				logger.debug(
						"entPhysicalEntryType.getEntPhysicalClass()...." + entPhysicalEntryType.getEntPhysicalClass());
				String physicalIndex = entPhysicalEntryType.getIndex();
				if (entPhysicalEntryType.getEntPhysicalClass().equals(EntPhysicalClassType.MODULE)
						|| entPhysicalEntryType.getEntPhysicalClass().equals(EntPhysicalClassType.CHASSIS)
						|| entPhysicalEntryType.getEntPhysicalClass().equals(EntPhysicalClassType.POWER_SUPPLY)
						|| entPhysicalEntryType.getEntPhysicalClass().equals(EntPhysicalClassType.FAN)) {

					if (entPhysicalEntryType.getEntPhysicalClass().equals(EntPhysicalClassType.CHASSIS)) {
						logger.debug("entPhysicalEntryType.getEntPhysicalDescr()>>>>>>>>>>>>>>"
								+ entPhysicalEntryType.getEntPhysicalDescr());

						if (!Utils.checkNull(entPhysicalEntryType.getEntPhysicalModelName())
								&& !Utils.checkBlank(entPhysicalEntryType.getEntPhysicalModelName())) {
							logger.debug("entPhysicalEntryType.getEntPhysicalModelName()>>>>>>>>>>>>>>"
									+ entPhysicalEntryType.getEntPhysicalModelName());
							chassisType = entPhysicalEntryType.getEntPhysicalModelName();
						} else {
//							chassisType = entPhysicalEntryType.getEntPhysicalDescr().replaceAll("Huawei Systems, Inc.","");
							logger.debug("entPhysicalEntryType.getEntPhysicalName()>>>>>>>>>>>>>>"
									+ entPhysicalEntryType.getEntPhysicalName());
							chassisType = entPhysicalEntryType.getEntPhysicalName();
						}
						entPhysicalEntryType.setEntPhysicalModelName(chassisType);
						hwVersion = entPhysicalEntryType.getEntPhysicalHardwareRev();
					}

					logger.debug("About to add physical Index " + physicalIndex + " in Chassis");

					equipTypeIndexMap.put(physicalIndex, entPhysicalEntryType);
				}

				if (entPhysicalEntryType.getEntPhysicalClass().equals(EntPhysicalClassType.CONTAINER)) {

					if (entPhysicalEntryType.getEntPhysicalName().toLowerCase().contains("transceiver container"))
						entPhysicalEntryType.setEntPhysicalName(
								entPhysicalEntryType.getEntPhysicalName().toLowerCase().substring(entPhysicalEntryType
										.getEntPhysicalName().toLowerCase().indexOf("transceiver container")));
					logger.debug("About to add physical Index " + physicalIndex + " for Equipment holder");
					equipHolderTypeIndexMap.put(physicalIndex, entPhysicalEntryType);

				}

				if (entPhysicalEntryType.getEntPhysicalClass().equals(EntPhysicalClassType.PORT)) {
					logger.debug("About to add physical Index " + physicalIndex + " for Port");

					portTypeIndexMap.put(physicalIndex, entPhysicalEntryType);
				}

			}
			getLogger().debug("addEntryTypeToMap Ended...");
			getLogger().info(DiscoveryConstants.LOG_EXIT + mid);
		} catch (Exception e) {
			getLogger().error("Exception occured during reading entryTypeArray to Map", e);
			e.printStackTrace();
			throw new ProcessorException(
					"IP Address : " + ipAddress + " " + "Exception occured during reading entryTypeArray to Map", e);
		}

	}

	private String populateSerialNumber(String entPhysicalEntryIndex) {
		String serialNumber = "";
		if (portSerialMap.containsKey(entPhysicalEntryIndex)) {
			// logger.debug("Port found in MAP: "+entPhysicalEntryIndex);
			if (portSerialMap.get(entPhysicalEntryIndex).getEntPhysicalSerialNum() != null
					&& !portSerialMap.get(entPhysicalEntryIndex).getEntPhysicalSerialNum().trim().isEmpty()) {
				// logger.debug("Serial No
				// found"+portSerialMap.get(entPhysicalEntryIndex).getEntPhysicalSerialNum().trim());
				return portSerialMap.get(entPhysicalEntryIndex).getEntPhysicalSerialNum().trim();
			} else {
				// logger.debug("Next loop
				// for"+portSerialMap.get(entPhysicalEntryIndex).getIndex());
				serialNumber = populateSerialNumber(portSerialMap.get(entPhysicalEntryIndex).getIndex());
			}
		}
		// logger.debug("returning no"+serialNumber);
		// logger.debug("populateSerialNumber::"+(portSerialMap.get(entPhysicalEntryIndex).getIndex())+"::
		// "+serialNumber);
		return serialNumber;
	}
	
	private static <T> T checkforNull(T obj) {
		if (obj == null || obj == "") {
			return (T) NA;
		} else {
			return obj;
		}
	}

}
