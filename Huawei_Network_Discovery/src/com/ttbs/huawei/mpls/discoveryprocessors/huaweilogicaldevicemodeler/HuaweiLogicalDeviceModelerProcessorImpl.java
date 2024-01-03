package com.ttbs.huawei.mpls.discoveryprocessors.huaweilogicaldevicemodeler;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ttbs.huawei.mpls.discoveryprocessors.huaweilogicaldevicemodeler.HuaweiLogicalDeviceModelerProcessorInterface;
import com.ttbs.huawei.mpls.discoveryprocessors.huaweilogicaldevicemodeler.HuaweiLogicalDeviceModelerProcessorRequest;
import com.ttbs.huawei.mpls.discoveryprocessors.huaweilogicaldevicemodeler.HuaweiLogicalDeviceModelerProcessorResponse;
import com.ttbs.huawei.mpls.discoveryprocessors.util.DiscoveryConstants;
import com.ttbs.huawei.mpls.discoveryprocessors.util.DiscoveryRuntimeException;
import com.ttbs.huawei.mpls.discoveryprocessors.util.DiscoveryUtility;
import com.ttbs.huawei.mpls.discoveryprocessors.util.NIDiscoveryContext;
import com.ttbs.huawei.mpls.discoveryprocessors.util.ResponseContext;

import oracle.communications.integrity.scanCartridges.sdk.ProcessorException;
import oracle.communications.integrity.scanCartridges.sdk.context.DiscoveryProcessorContext;
import oracle.communications.inventory.api.entity.DeviceInterface;
import oracle.communications.inventory.api.entity.LogicalDevice;
import oracle.communications.inventory.api.entity.PhysicalPort;
import oracle.communications.platform.logging.Log;
import oracle.communications.platform.logging.LogFactory;
import oracle.communications.platform.nds.snmp.huaweiphysicalcollector.HuaweiPhysicalCollectorResponseType;
import oracle.communications.platform.nds.snmp.huaweiphysicalcollector_entity_mib.EntPhysicalEntryType;
import oracle.communications.platform.nds.snmp.huaweiphysicalcollector_entity_mib.EntityMibMib;
import oracle.communications.platform.nds.snmp.huaweiphysicalcollector_if_mib.IfEntryType;
import oracle.communications.platform.nds.snmp.huaweiphysicalcollector_if_mib.IfMibMib;
import oracle.communications.platform.nds.snmp.huaweiphysicalcollector_if_mib.IfStackEntryType;
import oracle.communications.platform.nds.snmp.huaweiphysicalcollector_if_mib.IfTypeType;
import oracle.communications.platform.nds.snmp.huaweiphysicalcollector_if_mib.IfXEntryType;
import oracle.communications.platform.nds.snmp.huaweiphysicalcollector_ip_mib.IpAddrEntryType;
import oracle.communications.platform.nds.snmp.huaweiphysicalcollector_ip_mib.IpAddressEntryType;
import oracle.communications.platform.nds.snmp.huaweiphysicalcollector_ip_mib.IpMibMib;
import oracle.communications.platform.nds.snmp.huaweiphysicalcollector_rfc1213_mib.Rfc1213MibMib;
import oracle.communications.platform.persistence.PersistenceHelper;
import oracle.communications.platform.util.Utils;

public class HuaweiLogicalDeviceModelerProcessorImpl implements HuaweiLogicalDeviceModelerProcessorInterface {

	private Logger logger;
	private HuaweiLogicalDeviceModelerProcessorResponse response;
	private DiscoveryUtility util;
	private Rfc1213MibMib rfc1213Mib;
	private ResponseContext resContext;
	private NIDiscoveryContext niDiscoveryContext;
	private DiscoveryConstants discoveryConstants;
	private static final String MID = "HuaweiLogicalDeviceModelerProcessorImpl : ";
	private HuaweiPhysicalCollectorResponseType snmpDataResponseType;
	private boolean rtImportFlag;
	private boolean rtExportFlag;
	private boolean rtBothFlag;
	private HashMap<String, IfXEntryType> ifXEntryTypeMap;
	private Map<Integer, DeviceInterface> deviceInterfaceMapper;
	private Map<String, DeviceInterface> deviceInterfaceMapperForAeIntEntity;
	private HashMap<String, String> servicePolicyMap;
	private HashMap<String, ArrayList> qosConfigItemsMap;
	// private HashMap<String, String> qosParentChildMap;
	private HashMap<String, String> qosObjectIndexMapper;
	private HashMap<String, Object> qosGenericMapCfgMap;
	private HashMap ipAddrEntryTypeMap;
	private HashMap ipAddressEntryTypeMap;
	private Map<String, String> indexAndBaseMap;
	private Map<String, String> ciscoVpnRouteTargetMap;
	private Map<String, String> localPortsMap;
	private Map<String, String> remotePortsMap;
	private Map<String, String> remoteIpMap;
	private Map<String, String> remotePortIndexMap;
	private List<String> aeIntAndSubIntList;
	private HashMap<String, String> vrfNameMap;
	private HashMap<String, String> intefaceVrfRel;
	private Map<String, String> targetMap;
	private StringBuilder rtImportValue;
	private StringBuilder rtExportValue;
	private StringBuilder rtBothValue;
	private EntityMibMib entityMibMib;
	public static final String NO_VALUE = null;
	public static final String GENERIC_MEDIA = "Generic Media";
	public static final String IP_ADDRESSES = "IP Addresses";
	public static final String IP_ADDRESS = "IP Address";
	public static final String IPV4 = "IPV4";
	public static final String IPV6 = "IPV6";
	private String ipAddress;
	private LogicalDevice logicalDevice;
	private EntPhysicalEntryType entPhysicalEntryType[];

	private HashMap<String, Long> ifXEntryHighSpeedMap;

	/**
	 * Default constructor
	 * 
	 * @throws ProcessorException
	 */
	public HuaweiLogicalDeviceModelerProcessorImpl() throws ProcessorException {
		response = new HuaweiLogicalDeviceModelerProcessorResponse();
		resContext = new ResponseContext();
		util = new DiscoveryUtility();
		deviceInterfaceMapper = new HashMap<Integer, DeviceInterface>();
		deviceInterfaceMapperForAeIntEntity = new HashMap<String, DeviceInterface>();
		ciscoVpnRouteTargetMap = new TreeMap<String, String>();
		ifXEntryTypeMap = new HashMap<String, IfXEntryType>();
		ipAddrEntryTypeMap = new HashMap();
		ipAddressEntryTypeMap = new HashMap();
		aeIntAndSubIntList = new ArrayList<String>();
		indexAndBaseMap = new HashMap<String, String>();
		targetMap = new TreeMap<String, String>();
		// added for qos Modelling
		servicePolicyMap = new HashMap<String, String>();
		qosConfigItemsMap = new HashMap<String, ArrayList>();
		qosObjectIndexMapper = new HashMap<String, String>();
		// qosParentChildMap = new HashMap<String, String>();
		qosGenericMapCfgMap = new HashMap<String, Object>();
		intefaceVrfRel = new HashMap<String, String>();
		vrfNameMap = new HashMap<String, String>();
		ifXEntryHighSpeedMap = new HashMap<String, Long>();
		localPortsMap = new HashMap<String, String>();
		remotePortsMap = new HashMap<String, String>();
		remoteIpMap = new HashMap<String, String>();
		remotePortIndexMap = new HashMap<String, String>();
	}
	
	@Override
	public HuaweiLogicalDeviceModelerProcessorResponse invoke(DiscoveryProcessorContext context,
			HuaweiLogicalDeviceModelerProcessorRequest request) throws ProcessorException {
		try {
			final String mid = MID + "invoke";
			getLogger().info(discoveryConstants.LOG_ENTER + mid);

			//
			//Properties locationMapProps = NIDiscoveryContext.getNetworkLocationMappings();
			//
			resContext = request.getResponseContext();
			niDiscoveryContext = resContext.getNiDiscoveryContext();
			snmpDataResponseType = request.getHuaweiPhysicalCollectorResponseDocument();
			Rfc1213MibMib rfc1213MibResults = snmpDataResponseType.getDiscoveryResult().getRfc1213MibResults();
			entityMibMib = snmpDataResponseType.getDiscoveryResult().getEntityMibResults();
			entPhysicalEntryType = entityMibMib.getEntPhysicalTable().getEntPhysicalEntryArray();
			Properties networkLocationMappings = niDiscoveryContext.getNetworkLocationMappings();
			/*LldpMibMibMib lldpMibMibResults = snmpDataResponseType.getDiscoveryResult().getLldpMibMibResults();
			//
			LldpRemManAddrEntryType[] lldpRemManAddrEntryArray = lldpMibMibResults.getLldpRemManAddrTable()
					.getLldpRemManAddrEntryArray();
			for (LldpRemManAddrEntryType lldpRemManAddrEntryType : lldpRemManAddrEntryArray) {

				System.out.println("Zend IP 1 ===========**************************************************"
						+ lldpRemManAddrEntryType.getIndex());
				String reomteIPindex = lldpRemManAddrEntryType.getIndex();
				String iPIndexNo = null;
				if (reomteIPindex != null) {
					String[] split = reomteIPindex.replace(".", "/").split("/");
					iPIndexNo = split[1];
					System.out.println("index value is ===========" + iPIndexNo);
				}
				remoteIpMap.put(iPIndexNo, lldpRemManAddrEntryType.getIndex());
			}

			// Get the Local port details
			LldpLocPortEntryType[] lldpLocPortEntryArray = lldpMibMibResults.getLldpLocPortTable()
					.getLldpLocPortEntryArray();

			for (LldpLocPortEntryType lldpLocPortEntryType : lldpLocPortEntryArray) {
				
				 * System.out.println("Local Port Entry _______" +
				 * lldpLocPortEntryType.getIndex() + " : " +
				 * lldpLocPortEntryType.getLldpLocPortId());
				 
				// localPortsMap = new HashMap<String, String>();
				String index = lldpLocPortEntryType.getIndex();
				localPortsMap.put(index, lldpLocPortEntryType.getLldpLocPortId());
			}
			// get the Remote port details
			LldpRemEntryType[] lldpRemEntryArray = lldpMibMibResults.getLldpRemTable().getLldpRemEntryArray();
			for (LldpRemEntryType lldpRemEntryType : lldpRemEntryArray) {
				String remIndex = lldpRemEntryType.getIndex();
				System.out.println("remIndex====================="+remIndex);
				String RemoteIndexNum = null;
				if (remIndex != null) {
					String[] indexSp = remIndex.replace(".", "-").split("-");
					RemoteIndexNum = indexSp[1];
				}
				System.out.println("Remote Index Number is _____" + RemoteIndexNum);
				remotePortsMap.put(RemoteIndexNum, lldpRemEntryType.getLldpRemSysName());

				remotePortIndexMap.put(RemoteIndexNum, lldpRemEntryType.getLldpRemPortId());
			}
*/
			// End

			ipAddress = request.getScopeAddress();
			logicalDevice = DiscoveryUtility.createLogicalDevice();
			String deviceName = snmpDataResponseType.getDiscoveryResult().getRfc1213MibResults().getSysName();
			String networkLocationCode = "";
			if (snmpDataResponseType.getDiscoveryResult().getRfc1213MibResults().getSysLocation().length() < 25) {
				logicalDevice.setNetworkLocationEntityCode(
						snmpDataResponseType.getDiscoveryResult().getRfc1213MibResults().getSysLocation());
			}
			logicalDevice.setName(checkforNull(deviceName));
			
			String physicalLoc = snmpDataResponseType.getDiscoveryResult().getRfc1213MibResults().getSysLocation();
			if (physicalLoc != null) {
				physicalLoc = physicalLoc.replace(" ", "").replace("\"", "");
				//physicalLoc = locationMapProps.getProperty(logicalDevice.getName(), physicalLoc);
			}
			

			// logicalDevice.setName(deviceName);
			String logicalDescr = snmpDataResponseType.getDiscoveryResult().getRfc1213MibResults().getSysDescr();
			if (logicalDescr.length() > 50) {
				String logicalDescription = logicalDescr.substring(0, 49);
				logicalDevice.setDescription(checkforNull(logicalDescription));
			} else {
				logicalDevice
				.setDescription(checkforNull(snmpDataResponseType.getDiscoveryResult().getRfc1213MibResults().getSysDescr()));
			}
			
			
//			logicalDevice.setIsRootElement(true);
			logicalDevice.setNativeEMSName(checkforNull(rfc1213MibResults.getSysName()));
			logicalDevice.setPhysicalLocation(checkforNull(rfc1213MibResults.getSysLocation()));

			logicalDevice.setNetworkLocationEntityCode(checkforNull(physicalLoc));
			logicalDevice.setAlias(checkforNull(request.getScopeAddress()));
			logicalDevice.setDeviceIdentifier(logicalDevice.getName());
			String location = networkLocationMappings.getProperty(logicalDevice.getName());
			logicalDevice.setNetworkLocationEntityCode(location==null||location.isEmpty()?"N/A":location);
			boolean hasLDSpecification = false;

			if (!Utils.checkNull(request.getPhysicalDevice())
					&& !Utils.checkNull(request.getPhysicalDevice().getSpecification())
					&& !Utils.checkNull(request.getPhysicalDevice().getSpecification().getName())) {
				// System.out.println("Chassis
				// Map::"+request.getPhysicalDevice().getEquipment().get(0).getEquipment().getCharacteristicMap());
				hasLDSpecification = niDiscoveryContext.applySpecification(logicalDevice,
						logicalDevice.getName().trim().replaceAll("\\s+", "") + "_LD");
				String name = request.getPhysicalDevice().getSpecification().getName();
				//System.out.println("LD name is ==============="+name);
			} else {
				hasLDSpecification = niDiscoveryContext.applySpecification(logicalDevice, null);
			}

			if (hasLDSpecification) {
				System.out.println("in ld spec");
				// apply custom physical device characteristics
				Map<String, String> logicalDeviceCharMap = new HashMap<String, String>();
				logicalDeviceCharMap.put("ipAddress", checkforNull(snmpDataResponseType.getManagementIP()));
				logicalDeviceCharMap.put("Vendor", "HUAWEI");
				if (entPhysicalEntryType[0].getEntPhysicalClass().toString().contains("chassis")) {
					if (entPhysicalEntryType[0].getEntPhysicalSoftwareRev().length() > 40) {
						String softwareRev = entPhysicalEntryType[0].getEntPhysicalSoftwareRev();
						softwareRev=softwareRev.substring(0, 39);
						logicalDeviceCharMap.put("softwareVersion", checkforNull(softwareRev));
					}else {
						logicalDeviceCharMap.put("softwareVersion", checkforNull(entPhysicalEntryType[0].getEntPhysicalSoftwareRev()));
					}
					logicalDeviceCharMap.put("hardwareVersion", checkforNull(entPhysicalEntryType[0].getEntPhysicalHardwareRev()));
					//logicalDeviceCharMap.put("softwareVersion", entPhysicalEntryType[0].getEntPhysicalSoftwareRev());
				}
				else {
					logicalDeviceCharMap.put("softwareVersion","N/A");
					logicalDeviceCharMap.put("hardwareVersion","N/A");
				}
				/*if (rfc1213Mib.getSysDescr() != null && rfc1213Mib.getSysDescr().toLowerCase().contains("version")) {
					String ver = rfc1213Mib.getSysDescr().toLowerCase();

					logicalDeviceCharMap
					.put("softwareVersion",
							rfc1213Mib.getSysDescr().substring(ver.indexOf("version") + 7,
									nthOccurrence(ver,"(",2)-1));
				}*/
				String sysName = snmpDataResponseType.getDiscoveryResult().getRfc1213MibResults().getSysName();
				if (sysName != null && sysName.contains("NE40")) {
					logicalDeviceCharMap.put("DEVICE_TYPE", "PE Router");
				} else if (sysName != null && (sysName.contains("ME60") || sysName.contains("NetEngine 8000 M8"))) {
					logicalDeviceCharMap.put("DEVICE_TYPE", "Router");
				} else {
					logicalDeviceCharMap.put("DEVICE_TYPE", "SWITCH");
				}
				logicalDeviceCharMap.put("neId",checkforNull(sysName));
				logicalDeviceCharMap.put("domain","IP-MPLS");
				logicalDeviceCharMap.put("discoveryStatus","Active");
				logicalDeviceCharMap.put("emsName","NCE");
				logicalDeviceCharMap.put("nmsName","NCE");
				logicalDeviceCharMap.put("alarmStatus","ok");
				niDiscoveryContext.applyCharacteristics(logicalDevice, logicalDeviceCharMap);
			} else {
				System.out.println("spec not found");
				getLogger().warning(
						"Specification not found for Logical device against device:" + logicalDevice.getName());
			}

			logicalDevice.getMappedPhysicalDevices().add(request.getPhysicalDevice());

			IfMibMib ifMib = request.getHuaweiPhysicalCollectorResponseDocument().getDiscoveryResult()
					.getIfMibResults();
			IpMibMib ipMib = request.getHuaweiPhysicalCollectorResponseDocument().getDiscoveryResult()
					.getIpMibResults();
			
			if (ifMib.getIfXTable() != null) {

				IfXEntryType ifXEntryTypeArray[] = ifMib.getIfXTable().getIfXEntryArray();
				createMapOfIfXEntryType(ifXEntryTypeArray);
			} else {
				logger.log(Level.WARNING, "No value found for IfXTable");

			}

			if (ipMib.getIpAddrTable() != null) {

				IpAddrEntryType ipAddrEntryArray[] = ipMib.getIpAddrTable().getIpAddrEntryArray();
				createMapOfIpAddrEntryType(ipAddrEntryArray);
			} else {
				logger.log(Level.WARNING, "No value found for IpAddrTable");

			}

			if (ipMib.getIpAddressTable() != null) {

				IpAddressEntryType ipAddressEntryTypeArray[] = ipMib.getIpAddressTable().getIpAddressEntryArray();
				createMappingOfIpAddressEntryType(ipAddressEntryTypeArray);
			} else {
				logger.log(Level.WARNING, "No value found for IpAddressTable");

			}

			if (ifMib.getIfTable() != null) {

				logger.log(Level.FINE, "getIfTable Started...");
				IfEntryType ifEntryTypeArray[] = ifMib.getIfTable().getIfEntryArray();
				IfEntryType aifentrytype[];

				int j = (aifentrytype = ifEntryTypeArray).length;

				for (int i = 0; i < j; i++) {

					IfEntryType ifEntry = aifentrytype[i];
				if (!ifEntry.getIfDescr().toLowerCase().startsWith("cascade")) {
					if (ifEntry.getIfType().equals(IfTypeType.OTHER) || ifEntry.getIfType().equals(IfTypeType.PPP)
							|| ifEntry.getIfType().equals(IfTypeType.SOFTWARE_LOOPBACK)
							|| ifEntry.getIfType().equals(IfTypeType.SOFTWARE_LOOPBACK)
							|| ifEntry.getIfType().equals(IfTypeType.OPTICAL_CHANNEL)
							|| ifEntry.getIfType().equals(IfTypeType.PROP_VIRTUAL)
							|| ifEntry.getIfType().toString().contains((IfTypeType.MPLS).toString())
							|| ifEntry.getIfType().toString().equals((IfTypeType.TUNNEL).toString())
							|| ifEntry.getIfDescr().toLowerCase().contains("mgmt")
							|| ifEntry.getIfDescr().toLowerCase().contains("cpu")
							|| ifEntry.getIfDescr().toLowerCase().contains("rsp")
							|| ifEntry.getIfDescr().toLowerCase().startsWith("bdi")
							|| ifEntry.getIfDescr().toLowerCase().startsWith("virtual-template")
							|| ifEntry.getIfDescr().toLowerCase().startsWith("eth-trunk"))
						continue;
					DeviceInterface deviceInterface = (DeviceInterface) PersistenceHelper
							.makeEntity(oracle.communications.inventory.api.entity.DeviceInterface.class);
					deviceInterface.setName(checkforNull(ifEntry.getIfDescr().trim()));

					IfXEntryType ifXEntry = (IfXEntryType) ifXEntryTypeMap.get(ifEntry.getIndex());
					if (ifEntry.getIfDescr().length()>50) {
						deviceInterface.setDescription(checkforNull(ifEntry.getIfDescr().substring(0, 49)));
					} else {
						deviceInterface.setDescription(checkforNull(ifEntry.getIfDescr()));
					}
					long ifHighSpeed = ifXEntry.getIfHighSpeed();
					
					deviceInterface.setMaxSpeed(checkforNull(BigDecimal.valueOf((ifXEntry.getIfHighSpeed()))));
					deviceInterface.setInterfaceNumber(checkforNull(String.valueOf(ifEntry.getIfIndex())));
					deviceInterface.setPhysicalLocation(checkforNull(rfc1213MibResults.getSysLocation()));
					deviceInterface.setCustomerInterfaceNumber("N/A");;
					deviceInterface.setDescription(checkforNull(ifEntry.getIfDescr()))	;
					deviceInterface.setMinSpeed(checkforNull(BigDecimal.valueOf(0)));
					PhysicalPort mappedPhysicalPort = null;

					if (null != request.getPortNameAndEntity()
							&& request.getPortNameAndEntity().containsKey(deviceInterface.getName().toLowerCase())) {
						
						 deviceInterface.setMappedPhysicalPort(
						 request.getPortNameAndEntity().get(deviceInterface.getName().toLowerCase()));
						 
						mappedPhysicalPort = request.getPortNameAndEntity()
								.get(deviceInterface.getName().toLowerCase());
					}

					boolean hasDISpecification = false;
					if (mappedPhysicalPort != null && null != mappedPhysicalPort.getSpecification()
							&& null != mappedPhysicalPort.getSpecification().getName()) {

						String physicalPortSpecName = mappedPhysicalPort.getSpecification().getName();
						//System.out.println("physicalPortSpecName==========="+physicalPortSpecName);
						String diBandwidth = getAppropriateDIBandwidth(physicalPortSpecName);
						//System.out.println("diBandwidth==========="+diBandwidth);
						hasDISpecification = niDiscoveryContext.applySpecification(deviceInterface,
								diBandwidth + "_DI");
					} else {
						hasDISpecification = niDiscoveryContext.applySpecification(deviceInterface,
								ifEntry.getIfDescr().toUpperCase().trim().replaceAll("\\s+", "").replaceAll("/", "")
										.replace(".", "").replaceAll("[0-9]", "") + "_DI");
					}
					if (hasDISpecification) {

						//
						int ifIndex = ifEntry.getIfIndex().intValue();
						String ipAddress = "";
						String type = "";
						if (!Utils.checkNull(ipAddrEntryTypeMap.get(Integer.valueOf(ifIndex)))) {
							// System.out.println("Checking Index value for hasDISpecification
							// _______________");
							ipAddress = ipAddrEntryTypeMap.get(Integer.valueOf(ifIndex)).toString();
							//System.out.println("IP device ipAddress=" + ipAddress);
						}

						//
						Map<String, String> CharMap = new HashMap<String, String>();
						CharMap.put("ifIndex", checkforNull(String.valueOf(ifEntry.getIfIndex())));
						CharMap.put("interfaceLastChange", checkforNull(String.valueOf(ifEntry.getIfLastChange())));
						CharMap.put("OperationalStatus", checkforNull(ifEntry.getIfOperStatus().toString()));
						// CharMap.put("Speed", String.valueOf(ifEntry.getIfSpeed()));
						if (ifXEntry != null) {
							CharMap.put("Speed", checkforNull(ifXEntry.getIfHighSpeed() + ""));
						}
						CharMap.put("Administrative_Status", checkforNull(String.valueOf(ifEntry.getIfAdminStatus())));
						CharMap.put("MacAddress", checkforNull(ifEntry.getIfPhysAddress()));
						CharMap.put("Ipaddress", checkforNull(ipAddress));

						//
						// String string = localPortsMap.get(deviceInterface.getName());

						/*Set<Entry<String, String>> entrySet = localPortsMap.entrySet();

						for (Entry<String, String> entry : entrySet) {
							// System.out.println("Local LLDP Port Index ____________"
							// +entry.getKey().trim()+" : "+entry.getValue());
							String localPort = entry.getKey().trim(); // description
							String localPortIndex = entry.getValue().trim(); // Index
							if (localPortIndex.equalsIgnoreCase(deviceInterface.getName())) {
								// System.out.println("local port & Interface name are matched_________");
								CharMap.put("ServiceName", localPort);
								// CharMap.put("ServiceType", localPortIndex);
								deviceInterface.setAlias(localPortIndex);
							}

						}

						/// checking the remote port index _______
						Set<Entry<String, String>> charMapEntry = CharMap.entrySet();
						for (Entry<String, String> charEntry : charMapEntry) {
							// System.out.println("charEntry Key : " + charEntry.getKey() + " : charEntry
							// Value : "
							// + charEntry.getValue());
							if (charEntry.getKey().contains("ServiceName")) {
								// System.out.println("Key Found : " + charEntry.getKey() + "Key Vlaue Found : "
								// + charEntry.getValue());
								Set<Entry<String, String>> remotePortsMapEntry = remotePortsMap.entrySet();
								for (Entry<String, String> portEntry : remotePortsMapEntry) {
									String remotekey = portEntry.getKey();
									// System.out.println("Remote Key _________" + remotekey);
									String remotevalue = portEntry.getValue().trim();
									// System.out.println("Remote Value _________" + remotevalue);
									if (!Utils.checkNull(remotekey)) {
										// System.out.println("not null condition___________");
										if (remotekey.equals(charEntry.getValue())) {
											// System.out.println("Sucessfully matched RemoteKey ");

											deviceInterface.setIfType(remotevalue);
											//System.out.println("remotevalue1================" + remotevalue);
											String physicalZendLoc = snmpDataResponseType.getDiscoveryResult()
													.getRfc1213MibResults().getSysLocation();
											if (physicalZendLoc != null) {
												physicalZendLoc = physicalZendLoc.replace(" ", "").replace("\"", "");
												//physicalZendLoc = locationMapProps
												//		.getProperty(deviceInterface.getIfType(), physicalZendLoc);
											}
											String nWZendlocation = "";
											if (deviceInterface.getIfType().toUpperCase().contains("S5320")
													|| deviceInterface.getIfType().toUpperCase().contains("S5335")) {
												deviceInterface.setIfType(remotevalue);
												//System.out.println("remotevalue2================" + remotevalue);
											} else {
												if (physicalZendLoc.contains(".")) {
													nWZendlocation = physicalZendLoc.replace(".", "-");
													String countryCode = nWZendlocation.substring(0, 5);
													//System.out.println(countryCode);

													String device = countryCode.concat(remotevalue);
													//System.out.println(device);
													deviceInterface.setIfType(device);
												} else {
													//System.out.println("else condition");
													String device = physicalZendLoc.concat(remotevalue);
													//System.out.println(device);
													deviceInterface.setIfType(device);

												}
											}
											deviceInterface.setId(remotekey);
										}
									}

								}
							}

						}
						// zend ip
						Set<Entry<String, String>> charMapEntry1 = CharMap.entrySet();
						for (Entry<String, String> charEntry : charMapEntry1) {
							// System.out.println("charEntry Key : " + charEntry.getKey() + " : charEntry
							// Value : "
							// + charEntry.getValue());
							if (charEntry.getKey().contains("ServiceName")) {
								// System.out.println("Key Found : " + charEntry.getKey() + "Key Vlaue Found : "
								// + charEntry.getValue());
								Set<Entry<String, String>> remoteIPMapEntry = remoteIpMap.entrySet();
								for (Entry<String, String> portEntry : remoteIPMapEntry) {
									String remotekey = portEntry.getKey();
									System.out.println("Zend Remote Key=============== _________" + remotekey);
									System.out.println("charEntry.getValue()=============_________" + charEntry.getValue());
									
									String remotevalue = portEntry.getValue().trim();
									System.out.println(
											"Remote Value od xend end before split=========== _________" + remotevalue);
									String replaceIP = remotevalue.replace(".", "/");
									String[] zendIP = replaceIP.split("/");
									String zendIPAddress = zendIP[5] + "." + zendIP[6] + "." + zendIP[7] + "."
											+ zendIP[8];
									System.out.println("zendIPAddress===========================" + zendIPAddress);
									
									if (!Utils.checkNull(remotekey)) {
									 // System.out.println("not null condition___________");
										if (remotekey.equals(charEntry.getValue())) {
											 System.out.println("Sucessfully matched RemoteKey ");
											deviceInterface.setVendorInterfaceNumber(zendIPAddress);
										}
									}
								}
							}
						}
						// after this.
						Iterator<Entry<String, String>> iterator = CharMap.entrySet().iterator();
						while (iterator.hasNext()) {
							Entry<String, String> next = iterator.next();

							if (next.getKey().contains("ServiceName")) {
								Set<Entry<String, String>> remotePortsDesEntry = remotePortIndexMap.entrySet();
								for (Entry<String, String> portDesEntry : remotePortsDesEntry) {
									String remotekey = portDesEntry.getKey();
									// System.out.println("Remote Key _________" + remotekey);
									String remotevalue = portDesEntry.getValue().trim();
									// System.out.println("Remote Value _________" + remotevalue);
									if (!Utils.checkNull(remotekey)) {
										// System.out.println("not null condition___________");
										if (remotekey.equals(next.getValue())) {
											// System.out.println("Sucessfully matched RemoteKey ");
											// CharMap.put("VCID", remotevalue);
											// ethernet0/20/0
											// Frame:0/Slot:20/Port:0
											String remotePort = makeOLTPort(deviceInterface.getIfType(), remotevalue);

											deviceInterface.setCustomerInterfaceNumber(remotePort);
										}
									}
								}
							}

						}
						//

						//
						//CharMap.put("zEndIP", deviceInterface.getVendorInterfaceNumber());
						CharMap.put("aEndDevice", logicalDevice.getName());
						CharMap.put("aEndPort", deviceInterface.getAlias());
						CharMap.put("zEndDevice", deviceInterface.getIfType());
						CharMap.put("zEndPort", deviceInterface.getCustomerInterfaceNumber());
*/
						niDiscoveryContext.applyCharacteristics(deviceInterface, CharMap);
					} else {

						getLogger().warning(
								"Specification not found for DU against DI type:" + ifEntry.getIfDescr().toUpperCase());
					}

					if (ifXEntry != null) {
						deviceInterface.setDescription(ifXEntry.getIfAlias());
					}

					deviceInterfaceMapper.put(Integer.valueOf(ifEntry.getIfIndex().intValue()), deviceInterface);
					deviceInterfaceMapperForAeIntEntity.put(deviceInterface.getName(), deviceInterface);

//					logicalDevice.getAllDeviceInterfaces().addAll(rearrangeDeviceIntefacesWithSubInterfaces(
//							request.getHuaweiPhysicalCollectorResponseDocument(), deviceInterfaceMapper));

					logger.log(Level.FINE, "getIfTable Completed...");
				}
				}

				logicalDevice.getDeviceInterfaces().addAll(rearrangeDeviceIntefacesWithSubInterfaces(
						request.getHuaweiPhysicalCollectorResponseDocument(), deviceInterfaceMapper));

//				logger.log(Level.INFO, "Logical Discovery completed for " + request.getScopeAddress());

//				response.setLogicalDevice(logicalDevice);
//
//				getLogger().info(discoveryConstants.LOG_EXIT+mid);
			}

			logger.log(Level.INFO, "Logical Discovery completed for " + request.getScopeAddress());
			response.setLogicalDevice(logicalDevice);

			getLogger().info(discoveryConstants.LOG_EXIT + mid);
			//context.addToResult(logicalDevice.getName(), "HuaweiDevice", logicalDevice);

		} catch (NullPointerException ex) {
			logger.log(Level.SEVERE, "Null Pointer Exception :");
			ex.printStackTrace();
			throw new ProcessorException("IP Address : " + ipAddress + " " + "Null Pointer Exception :", ex);
		} catch (ArrayIndexOutOfBoundsException ex) {
			logger.log(Level.SEVERE, "Array Index out of Bounds Exception :");
			ex.printStackTrace();
			throw new ProcessorException("IP Address : " + ipAddress + " " + "Array Index out of Bounds Exception :",
					ex);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Exception :");
			e.printStackTrace();
			throw new ProcessorException("IP Address : " + ipAddress + " " + "Exception :", e);
			// e.getMessage();
			// logger.log(Level.SEVERE, e.getMessage());
			//throw new DiscoveryRuntimeException(ipAddress, "Exception in HuaweiLogicalDeviceModelerProcessorImpl:invoke", request.getDeviceStatus());
		}

		return response;
	}

	@SuppressWarnings("unchecked")
	private static <T> T checkforNull(T obj) {
	    if (obj == null || obj == "") {
	        return (T) "N/A";
	    } else {
	        return obj;
	    }
	}

	private static int nthOccurrence(String str1, String str2, int n) {
    	 
        String tempStr = str1;
        int tempIndex = -1;
        int finalIndex = 0;
        for(int occurrence = 0; occurrence < n ; ++occurrence){
            tempIndex = tempStr.indexOf(str2);
            if(tempIndex==-1){
                finalIndex = 0;
                break;
            }
            tempStr = tempStr.substring(++tempIndex);
            finalIndex+=tempIndex;
        }
        return --finalIndex;
    }

	

	private List rearrangeDeviceIntefacesWithSubInterfaces(HuaweiPhysicalCollectorResponseType scanResponse,
			Map interfaceMapper) throws Exception {
		try {
			final String mid = MID + "rearrangeDeviceIntefacesWithSubInterfaces";
			getLogger().info(discoveryConstants.LOG_ENTER + mid);
			logger.log(Level.FINE, "Device Interface Modelling Started...");

			List deviceInterfaceList = new ArrayList();
			Set subInterfaceIdTracker = new HashSet();
			IfStackEntryType ifStackEntryType[] = scanResponse.getDiscoveryResult().getIfMibResults().getIfStackTable()
					.getIfStackEntryArray();

			List stackEntryList = filterStackEntryArray(ifStackEntryType);

			for (Iterator iterator = stackEntryList.iterator(); iterator.hasNext();) {
				IfStackEntryType ifStackEntry = (IfStackEntryType) iterator.next();
				String valueAfterDot[] = ifStackEntry.getIndex().replace(".", ":").split(":");

				if (valueAfterDot.length == 2 && !"0".equalsIgnoreCase(valueAfterDot[1])) {
					int parentIndex = Integer.parseInt(valueAfterDot[1]);
					int subInterfaceIndex = Integer.parseInt(valueAfterDot[0]);
					DeviceInterface subInterface = (DeviceInterface) interfaceMapper
							.get(Integer.valueOf(subInterfaceIndex));

					if (subInterface != null) {
						DeviceInterface parentInterface = (DeviceInterface) interfaceMapper
								.get(Integer.valueOf(parentIndex));

						if (parentInterface != null) {
							subInterfaceIdTracker.add(Integer.valueOf(subInterfaceIndex));
							// set all interfaces as parent of sub-interfaces,
							// but
							// "ae" interfaces and sub-interfaces would be set
							// as
							// interfaces only.
							subInterface.setParentInterface(parentInterface);
						} else {
							String msg = (new StringBuilder("Cannot find DeviceInterface for parent interface number "))
									.append(parentIndex).append(" of the ifStackEntry ").append(ifStackEntry.getIndex())
									.append(",  so the sub-interface DeviceInterface is left as a top level interface.")
									.toString();
						}
					} else {
						SortedMap<String, String> sMap = new TreeMap<String, String>();
						String msg = (new StringBuilder("Cannot find DeviceInterface for sub-interface number "))
								.append(subInterfaceIndex).append(" of the ifStackEntry ")
								.append(ifStackEntry.getIndex())
								.append(",  so the parent DeviceInterface is not changed.").toString();
					}
				}
			}

			for (Iterator iterator1 = interfaceMapper.keySet().iterator(); iterator1.hasNext();) {
				Integer interfaceIndex = (Integer) iterator1.next();
				if (!subInterfaceIdTracker.contains(interfaceIndex)
						&& !((DeviceInterface) interfaceMapper.get(interfaceIndex)).getName().startsWith("ae")) {
					// Adding all the interfaces except "ae" interfaces in the
					// parent list.
					DeviceInterface deviceInterface = (DeviceInterface) interfaceMapper.get(interfaceIndex);
					deviceInterfaceList.add(deviceInterface);
				}
				if (((DeviceInterface) interfaceMapper.get(interfaceIndex)).getName().startsWith("ae")
						|| ((DeviceInterface) interfaceMapper.get(interfaceIndex)).getName().startsWith("AE")) {
					// Adding the "ae" interface and sub-interface in list.
					aeIntAndSubIntList.add(((DeviceInterface) interfaceMapper.get(interfaceIndex)).getName());
					// deviceInterfaceList =
					// modelAeInterfaceAndSubInterface(aeIntAndSubIntList,deviceInterfaceList);
				}
			}
			// Calling method to set "ae" interface as parent of "ae"
			// sub-interface
			// and then adding "ae" interface in parent list.
			if (aeIntAndSubIntList.size() > 0) {
				deviceInterfaceList = modelAeInterfaceAndSubInterface(aeIntAndSubIntList, deviceInterfaceList);
			} else
				logger.log(Level.WARNING, "No ae interfaces found...");
			logger.log(Level.FINE, "Device Interface Modelling Completed...");

			getLogger().info(discoveryConstants.LOG_EXIT + mid);
			return deviceInterfaceList;
		} catch (NullPointerException ex) {
			logger.log(Level.SEVERE, "Null Pointer Exception :");
			ex.printStackTrace();
			throw new ProcessorException("IP Address : " + ipAddress + " " + "Null Pointer Exception :", ex);
		} catch (ArrayIndexOutOfBoundsException ex) {
			logger.log(Level.SEVERE, "Array Index out of Bounds Exception :");
			ex.printStackTrace();
			throw new ProcessorException("IP Address : " + ipAddress + " " + "Array Index out of Bounds Exception :",
					ex);
		}

	}

	private List modelAeInterfaceAndSubInterface(List<String> aeIntAndSubIntList, List deviceInterfaceList)
			throws Exception {
		try {
			final String mid = MID + "modelAeInterfaceAndSubInterface";
			getLogger().info(discoveryConstants.LOG_ENTER + mid);
			logger.log(Level.FINEST, "modelAeInterfaceAndSubInterface Started...");

			Collections.sort(aeIntAndSubIntList);
			Map<String, Set> aeIntWithSubIntMap = new HashMap<String, Set>();
			Set<String> aeSubInterfaceSet = new TreeSet<String>();
			String aeInterfaceName = null;

			for (String aeEntity : aeIntAndSubIntList) {

				if (aeEntity.indexOf(".") == -1) {
					aeSubInterfaceSet = new TreeSet<String>();
					aeInterfaceName = aeEntity;
					aeSubInterfaceSet.add("dummy");
				}

				if (aeEntity.indexOf(".") != -1) {
					aeSubInterfaceSet.add(aeEntity);
				}

				aeIntWithSubIntMap.put(aeInterfaceName, aeSubInterfaceSet);
			}

			Map<String, TreeSet> sortedMap = new TreeMap<String, TreeSet>();
			sortedMap = new TreeMap(aeIntWithSubIntMap);

			for (String aeInterface : sortedMap.keySet()) {
				DeviceInterface aeInterfaceDeviceInterface = (DeviceInterface) deviceInterfaceMapperForAeIntEntity
						.get(aeInterface);
				aeInterfaceDeviceInterface.setName(aeInterface);
				Iterator iterator = sortedMap.get(aeInterface).iterator();

				while (iterator.hasNext()) {
					String aeSubInterface = (String) iterator.next();

					if (!aeSubInterface.equals("dummy")) {
						DeviceInterface aeSubInterfaceDeviceInterface = (DeviceInterface) deviceInterfaceMapperForAeIntEntity
								.get(aeSubInterface);
						aeSubInterfaceDeviceInterface.setName(aeSubInterface);
						aeSubInterfaceDeviceInterface.setParentInterface(aeInterfaceDeviceInterface);
					}
				}

				deviceInterfaceList.add(aeInterfaceDeviceInterface);
			}

			logger.log(Level.FINEST, "modelAeInterfaceAndSubInterface Completed...");

			getLogger().info(discoveryConstants.LOG_EXIT + mid);
			return deviceInterfaceList;

		} catch (NullPointerException ex) {
			logger.log(Level.SEVERE, "Null Pointer Exception :");
			ex.printStackTrace();
			throw new ProcessorException("IP Address : " + ipAddress + " " + "Null Pointer Exception :", ex);
		} catch (ArrayIndexOutOfBoundsException ex) {
			logger.log(Level.SEVERE, "Array Index out of Bounds Exception :");
			ex.printStackTrace();
			throw new ProcessorException("IP Address : " + ipAddress + " " + "Array Index out of Bounds Exception :",
					ex);
		}

	}

	private List<IfStackEntryType> filterStackEntryArray(IfStackEntryType ifStackEntryType[]) {

		final String mid = MID + "filterStackEntryArray";
		getLogger().info(discoveryConstants.LOG_ENTER + mid);
		logger.log(Level.FINEST, "filterStackEntryArray Started...");

		List<IfStackEntryType> stackEntryList = new ArrayList<IfStackEntryType>();
		IfStackEntryType aifstackentrytype[];

		int j = (aifstackentrytype = ifStackEntryType).length;

		for (int i = 0; i < j; i++) {
			IfStackEntryType ifStackEntry = aifstackentrytype[i];
			if (!ifStackEntry.getIndex().startsWith("0"))
				stackEntryList.add(ifStackEntry);
		}
		logger.log(Level.FINEST, "filterStackEntryArray Completed...");

		getLogger().info(discoveryConstants.LOG_EXIT + mid);

		return stackEntryList;
	}

	private void createMappingOfIpAddressEntryType(IpAddressEntryType ipAddressEntryTypeArray[]) throws Exception {
		try {
			// logger.debug("", "createMappingOfIpAddressEntryType Started...");

			ArrayList ipAddressEntries = null;
			if (!Utils.checkNull(ipAddressEntryTypeArray) && ipAddressEntryTypeArray.length > 0) {

				IpAddressEntryType aipaddressentrytype[];
				int j = (aipaddressentrytype = ipAddressEntryTypeArray).length;
				for (int i = 0; i < j; i++) {
					IpAddressEntryType ipAddressEntry = aipaddressentrytype[i];
					if (ipAddressEntry.getIpAddressIfIndex() != null && ipAddressEntry.getIpAddressType() != null) {
						int ifIndex = ipAddressEntry.getIpAddressIfIndex().intValue();
						// System.out.println(" Print -1 ___________");
						String type = ipAddressEntry.getIpAddressType().toString();
						if (!Utils.checkNull(type)) {
							// System.out.println(" Print -2 ___________");
							// String type = ipAddressEntry.getIpAddressType().toString();
							// System.out.println("Value of type" + type);
							if (ipAddressEntryTypeMap.containsKey(Integer.valueOf(ifIndex))) {
								type = ipAddressEntryTypeMap.get(Integer.valueOf(ifIndex)).toString();
							} else {
								// ipAddressEntries = new ArrayList();
								// ipAddressEntries.add(ipAddressEntry);
								ipAddressEntryTypeMap.put(Integer.valueOf(ifIndex), type);
							}
						}
					}
				}
			}
			// logger.log("", "createMappingOfIpAddressEntryType Completed...");

		} catch (NullPointerException ex) {

			ex.printStackTrace();
			throw new ProcessorException("IP Address : " + ipAddress + " " + "Null Pointer Exception :", ex);
		} catch (ArrayIndexOutOfBoundsException ex) {

			ex.printStackTrace();
			throw new ProcessorException("IP Address : " + ipAddress + " " + "Array Index out of Bounds Exception :",
					ex);
		}

	}

	private void createMapOfIfXEntryType(IfXEntryType ifXEntryTypeArray[]) throws Exception {

		IfXEntryType aifxentrytype[];
		try {
			final String mid = MID + "createMapOfIfXEntryType";
			getLogger().info(discoveryConstants.LOG_ENTER + mid);
			logger.log(Level.FINEST, "createMapofIfXEntryType Started...");

			int j = (aifxentrytype = ifXEntryTypeArray).length;

			for (int i = 0; i < j; i++) {
				IfXEntryType ifXEntry = aifxentrytype[i];
				ifXEntryTypeMap.put(ifXEntry.getIndex(), ifXEntry);
				ifXEntryHighSpeedMap.put(ifXEntry.getIndex(), ifXEntry.getIfHighSpeed());
			}
			logger.log(Level.FINEST, "createMapofIfXEntryType Completed...");

		} catch (NullPointerException ex) {
			logger.log(Level.SEVERE, "Null Pointer Exception :");
			ex.printStackTrace();
			throw new ProcessorException("IP Address : " + ipAddress + " " + "Null Pointer Exception :", ex);
		} catch (ArrayIndexOutOfBoundsException ex) {
			logger.log(Level.SEVERE, "Array Index out of Bounds Exception :");
			ex.printStackTrace();
			throw new ProcessorException("IP Address : " + ipAddress + " " + "Array Index out of Bounds Exception :",
					ex);
		}

	}

	private void createMapOfIpAddrEntryType(IpAddrEntryType ipAddrEntryArray[]) throws Exception {
		try {

			ArrayList ipAddrEntries = null;
			IpAddrEntryType aipaddrentrytype[];
			int j = (aipaddrentrytype = ipAddrEntryArray).length;
			for (int i = 0; i < j; i++) {
				IpAddrEntryType ipAddrEntry = aipaddrentrytype[i];
				if (ipAddrEntry.getIpAdEntIfIndex() != null) {
					int ifIndex = ipAddrEntry.getIpAdEntIfIndex().intValue();
					String ipAdEntAddr = ipAddrEntry.getIpAdEntAddr().toString();
					// System.out.println("Value of ipAdEntAddr" + ipAdEntAddr);
					if (ipAddrEntryTypeMap.containsKey(Integer.valueOf(ifIndex))) {

						ipAdEntAddr = ipAddrEntryTypeMap.get(Integer.valueOf(ifIndex)).toString();
					} else {
						ipAddrEntryTypeMap.put(Integer.valueOf(ifIndex), ipAdEntAddr);
					}
				}
			}
		} catch (NullPointerException ex) {
			// logger.debug("", "Null Pointer Exception :");
			ex.printStackTrace();
			throw new ProcessorException("IP Address : " + ipAddress + " " + "Null Pointer Exception :", ex);
		} catch (ArrayIndexOutOfBoundsException ex) {
			// logger.debug("", "Array Index out of Bounds Exception :");
			ex.printStackTrace();
			throw new ProcessorException("IP Address : " + ipAddress + " " + "Array Index out of Bounds Exception :",
					ex);
		}
	}

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

	private String getAppropriateDIBandwidth(String specGenerator) {

		String portSpecName = "genericInterface";

		specGenerator = specGenerator.toUpperCase().trim().replaceAll("\\s+", "");

		if (specGenerator.contains("GIGABITETHERNET")) {
			portSpecName = "GIGABITETHERNET";
		} else if (specGenerator.contains("ETHERNET")) {
			portSpecName = "genericInterface";
//			com.oracle.integrity.modelcollections.huaweinetworkdiscovery.port.EthernetPort;
		} else if (specGenerator.contains("100-1000BASE-X-SFP")) {
			portSpecName = "100-1000BASE-X-SFP";
//			com.oracle.integrity.modelcollections.huaweinetworkdiscovery.port.H
		} else if (specGenerator.contains("100-1000BASE-SFP")) {
			portSpecName = "100-1000BASE-SFP";
//			com.oracle.integrity.modelcollections.huaweinetworkdiscovery.port.H
		} else if (specGenerator.contains("10GBASE")) {
			portSpecName = "10GBASE";
//			com.oracle.integrity.modelcollections.huaweinetworkdiscovery.port.EthernetPort;
		} else if (specGenerator.contains("100GIGE")) {
			portSpecName = "100GigE";
//			com.oracle.integrity.modelcollections.huaweinetworkdiscovery.port.EthernetPort;
		} else if (specGenerator.contains("100GBASE") || specGenerator.equals("100BASE-TX")) {
			portSpecName = "100GBASE";
//			com.oracle.integrity.modelcollections.huaweinetworkdiscovery.port.EthernetPort;
		} else if (specGenerator.contains("FASTE")) {
			portSpecName = "FASTE";
//			com.oracle.integrity.modelcollections.huaweinetworkdiscovery.port.EthernetPort;
		} else if (specGenerator.contains("1GIGE")) {
			portSpecName = "1GIGE";
//			com.oracle.integrity.modelcollections.huaweinetworkdiscovery.port.EthernetPort;
		} else if (specGenerator.contains("40GIGE")) {
			portSpecName = "40GIGE";
//			com.oracle.integrity.modelcollections.huaweinetworkdiscovery.port.EthernetPort;
		} else if (specGenerator.contains("10GIGE")) {
			portSpecName = "10GIGE";
//			com.oracle.integrity.modelcollections.huaweinetworkdiscovery.port.EthernetPort;
		}else if (specGenerator.contains("MEth0/0/1")) {
			portSpecName = "1GIGE";
//			com.oracle.integrity.modelcollections.huaweinetworkdiscovery.port.EthernetPort;
		}

		return portSpecName;

	}

}
