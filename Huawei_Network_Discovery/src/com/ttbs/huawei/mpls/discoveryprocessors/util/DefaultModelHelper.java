package com.ttbs.huawei.mpls.discoveryprocessors.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import oracle.communications.inventory.api.entity.Equipment;
import oracle.communications.platform.nds.snmp.huaweiphysicalcollector_entity_mib.EntPhysicalEntryType;

public class DefaultModelHelper {
	private static Map<String, String> asr920map;
	private static Map<String, String> asr1002map;
	private static Map<String, String> frontRearMap;
	enum Vendor_Name {
		ASR920, ASR1002F;
	}

	static {
		asr920map = new HashMap<>();
		asr920map.put("FIXEDIM", "FIXED_IM_GENERIC");
		asr920map.put("MODULE", "MODULE_GENERIC");
	}
	static {
		asr1002map = new HashMap<>();
		asr1002map.put("TRANSCEIVER", "SFP-GE-L");
	}
	
	static {
		
		
	}

	public static String getPartNumber(String model, String name) {
		System.out.println("model ::" + model + " Name " + name);
		if (model.toUpperCase().replaceAll("-", "").contains(Vendor_Name.ASR920.toString())) {
			for (Entry<String, String> partnumber : asr920map.entrySet()) {
				if (name.replaceAll("\\s+", "").toUpperCase().contains(partnumber.getKey()))
					return partnumber.getValue();
			}
		}

		if (model.toUpperCase().replaceAll("-", "").contains(Vendor_Name.ASR1002F.toString())) {
			for (Entry<String, String> partnumber : asr1002map.entrySet()) {
				if (name.replaceAll("\\s+", "").toUpperCase().contains(partnumber.getKey()))
					return partnumber.getValue();
			}
		}
		return null;
	}
	
	
	/**
	 * @param portdata
	 * @param parentEquipment
	 * @return
	 */
	public static String getAppropriatePortBandwidth(EntPhysicalEntryType portdata,Equipment parentEquipment, boolean checkFromCard) {
		String portSpecName="GIGABITETHERNET";
		String specGenerator = "GigE";
		if (checkFromCard) {
			if (parentEquipment != null && parentEquipment.getDescription() != null) {
				specGenerator = parentEquipment.getDescription();
				//System.out.println("specGenerator name===================="+specGenerator);
			}
		}else {
			if(!portdata.getEntPhysicalName().isEmpty() && !portdata.getEntPhysicalDescr().isEmpty() &&
				portdata.getEntPhysicalName().toUpperCase().contains("GIGABITETHERNET") && portdata.getEntPhysicalDescr().toUpperCase().contains("10G")){
				specGenerator= "10GE";
			}else if(!portdata.getEntPhysicalName().isEmpty() && !portdata.getEntPhysicalDescr().isEmpty() &&
					portdata.getEntPhysicalName().toUpperCase().contains("GIGABITETHERNET") && portdata.getEntPhysicalDescr().toUpperCase().contains("100G")){
					specGenerator= "100GE";
			}else if(!portdata.getEntPhysicalName().isEmpty() && !portdata.getEntPhysicalDescr().isEmpty() &&
					portdata.getEntPhysicalName().toUpperCase().contains("GIGABITETHERNET") && portdata.getEntPhysicalDescr().toUpperCase().contains("10300")){
					specGenerator= "10GE";
			}else if(!portdata.getEntPhysicalName().isEmpty()) {
					specGenerator= portdata.getEntPhysicalName();
			}else {
					specGenerator = portdata.getEntPhysicalDescr();
			}
		}
		//System.out.println("specGenerator====================------------------------------------"+specGenerator);
//		com.oracle.integrity.modelcollections.huaweinetworkdiscovery.deviceinterface.EthernetPort
//		com.oracle.integrity.modelcollections.huaweinetworkdiscovery.port.GigE;
		
		if(specGenerator.toUpperCase().trim().contains("1GB")) {
			portSpecName="1GB";
		}else if(specGenerator.toUpperCase().trim().contains("100GE")) {
			portSpecName="100GE";
		}else if(specGenerator.toUpperCase().trim().contains("1300MB")) {
			portSpecName="1300MB";
		}else if(specGenerator.toUpperCase().trim().contains("1200MB")) {
			portSpecName="1200MB";
		}else if(specGenerator.toUpperCase().trim().contains("10300MB")) {
			portSpecName="10300MB";
		}else if(specGenerator.toUpperCase().trim().contains("GIGABITETHERNET")) {
			portSpecName="GigE";
		}
		else if(specGenerator.toUpperCase().trim().contains("MEth0/0/1")) {
			portSpecName="GigE";
		}
		else if(specGenerator.toUpperCase().trim().contains("100/1000BASE")) {
			portSpecName="100/1000BASE-SFP";
//			com.oracle.integrity.modelcollections.huaweinetworkdiscovery.port.H
		}else if(specGenerator.toUpperCase().trim().contains("100/1000BASE-X-SFP")) {
			portSpecName="100/1000BASE-X-SFP";
//			com.oracle.integrity.modelcollections.huaweinetworkdiscovery.port.H
		}else if(specGenerator.toUpperCase().trim().contains("10GBASE")) {
			portSpecName="10GBASE";
//			com.oracle.integrity.modelcollections.huaweinetworkdiscovery.port.EthernetPort;
		}else if(specGenerator.toUpperCase().trim().contains("2G")) {
			portSpecName="2G";
//			com.oracle.integrity.modelcollections.huaweinetworkdiscovery.port.EthernetPort;
		}else if(specGenerator.toUpperCase().trim().contains("100GBASE")) {
			portSpecName="100GBASE";
//			com.oracle.integrity.modelcollections.huaweinetworkdiscovery.port.EthernetPort;
		}else if(specGenerator.toUpperCase().equals("CARD ATN 910C-B")) {
			portSpecName="10GIGE";	
//			com.oracle.integrity.modelcollections.huaweinetworkdiscovery.port.EthernetPort;
		}else if(specGenerator.toUpperCase().trim().contains("GE/FE") && specGenerator.toUpperCase().trim().contains("AND2EM8F0")) {
			portSpecName="GigE";
//			com.oracle.integrity.modelcollections.huaweinetworkdiscovery.port.EthernetPort;
		}else if(specGenerator.toUpperCase().trim().contains("GE/FE") && specGenerator.toUpperCase().trim().contains("ANK1EX2S0")) {
			portSpecName="10GIGE";
//			com.oracle.integrity.modelcollections.huaweinetworkdiscovery.port.EthernetPort;
		}else if(specGenerator.toUpperCase().trim().contains("AND2CXPB")) {
			portSpecName="AND2CXPB";
//			com.oracle.integrity.modelcollections.huaweinetworkdiscovery.port.EthernetPort;
		}else if(specGenerator.toUpperCase().trim().contains("ANJD00CXPA01")) {
			portSpecName="ANJD00CXPA01";
//			com.oracle.integrity.modelcollections.huaweinetworkdiscovery.port.EthernetPort;
		}else if(specGenerator.toUpperCase().trim().contains("CR57SRU200A5")) {
			portSpecName="GigE";
//			com.oracle.integrity.modelcollections.huaweinetworkdiscovery.port.EthernetPort;
		}else if(specGenerator.toUpperCase().trim().contains("CR57SRU200A5")) {
			portSpecName="GigE";
//			com.oracle.integrity.modelcollections.huaweinetworkdiscovery.port.EthernetPort;
		}else if(specGenerator.toUpperCase().trim().contains("MAINPROCESSINGUNIT")) {
			portSpecName="GigE";
//			com.oracle.integrity.modelcollections.huaweinetworkdiscovery.port.EthernetPort;
		}else if(specGenerator.toUpperCase().trim().contains("GE/FE")) {
			portSpecName="GEFE";
//			com.oracle.integrity.modelcollections.huaweinetworkdiscovery.port.EthernetPort;
		}else if(specGenerator.toUpperCase().trim().contains("10GE")) {
			portSpecName="10GIGE";
//			com.oracle.integrity.modelcollections.huaweinetworkdiscovery.port.EthernetPort;
		}
		
		return portSpecName;
		
	}
	public static String getCardSpecMapping(String specGenerator, String inputString) {
		//System.out.println("in card method================"+inputString);
		
		if (inputString.toUpperCase().contains("POWER")
                || inputString.toUpperCase().contains("PWR")) {
//            specGenerator="DC_POWER_CARD";
            //specGenerator="genericEquipment";
            specGenerator="POWER_CARD";
            System.out.println("Found the POWER Card specGenerator___________"+specGenerator);
        }else if(inputString.toUpperCase().contains("CLK")) {
            specGenerator="CLK_CARD";
        }else if(inputString.toUpperCase().contains("FAN")) {
//            specGenerator="FAN_CARD";
            //specGenerator="genericEquipment"; Huawei Generic Module Card
            specGenerator="FAN_CARD";
            System.out.println("Found the FAN Card specGenerator___________"+specGenerator);
        }else if(inputString.toUpperCase().contains("CX22E2XY")) {
            specGenerator="CX22E2XY_CARD";
            System.out.println("Found the FAN Card specGenerator___________"+specGenerator);
        }else if(inputString.toUpperCase().contains("CR52LPUK")) {
            specGenerator="CR52LPUK_CARD";
            System.out.println("Found the FAN Card specGenerator___________"+specGenerator);
        }else if(inputString.toUpperCase().contains("CR52EBGF")) {
            specGenerator="CR52EBGF_CARD";
            System.out.println("Found the CR52EBGF Card specGenerator___________"+specGenerator);
        }else if(inputString.toUpperCase().contains("CR52L1XX")) {
            specGenerator="CR52L1XX_CARD";
            System.out.println("Found the CR52L1XX Card specGenerator___________"+specGenerator);
        }else if(inputString.toUpperCase().contains("CR52EBGE")) {
            specGenerator="CR52EBGE_CARD";
            System.out.println("Found the CR52EBGE Card specGenerator___________"+specGenerator);
        }else if(inputString.toUpperCase().replaceAll("[0-9]","").trim().equals("CR5D0SRUA470")) {
            specGenerator="CR5D0SRUA470_CARD";
            System.out.println("Found the CR52EBGE Card specGenerator___________"+specGenerator);
        }else if(inputString.toUpperCase().contains("CR56RPUA")) {
            specGenerator="CR56RPUA_CARD";
            System.out.println("Found the CR56RPUA Card specGenerator___________"+specGenerator);
        }else if(inputString.toUpperCase().replaceAll("[0-9]","").trim().equals("SRU")) {
            specGenerator="SRU_CARD";
            System.out.println("Found the SRU Card specGenerator___________"+specGenerator);
        }else if(inputString.toUpperCase().replaceAll("[0-9]","").trim().equals("LPU")) {
            specGenerator="LPU_CARD";
            System.out.println("Found the LPU Card specGenerator___________"+specGenerator);
        }else if(inputString.toUpperCase().contains("CFCARD")) {
            specGenerator="CFCARD";
            System.out.println("Found the CFCARD Card specGenerator___________"+specGenerator);
        }else if(inputString.toUpperCase().contains("CR5DSFUI407C")) {
            specGenerator="CR5DSFUI407C_CARD";
            System.out.println("Found the FAN Card specGenerator___________"+specGenerator);
        }else if(inputString.toUpperCase().contains("CR57SFU40B")) {
            specGenerator="CR57SFU40B_CARD";
            System.out.println("Found the CR57SFU40B Card specGenerator___________"+specGenerator);
        }else if(inputString.toUpperCase().replaceAll("[0-9]","").trim().equals("PEM")) {
            specGenerator="PEM_CARD";
            System.out.println("Found the PEM Card specGenerator___________"+specGenerator);
        }else if(inputString.toUpperCase().replaceAll("[0-9]","").trim().equals("MPU")) {
            specGenerator="MPU_CARD";
            System.out.println("Found the MPU Card specGenerator___________"+specGenerator);
        }else if(inputString.toUpperCase().substring(0, 3).equals("MPU")) {
            specGenerator="MPU_CARD";
            System.out.println("Found the MPU Card 2 specGenerator___________"+specGenerator);
        }
        else if(inputString.toUpperCase().contains("SFU")) {
            specGenerator="SFU_CARD";
            System.out.println("Found the SFU Card specGenerator___________"+specGenerator);
        }
		else if(inputString.toUpperCase().contains("CR52MPUB")) {
            specGenerator="CR52MPUB_CARD";
            System.out.println("Found the CR52MPUB Card specGenerator___________"+specGenerator);
        }
		else if(inputString.toUpperCase().contains("CR52CLKA")) {
            specGenerator="CR52CLKA_CARD";
            System.out.println("Found the CR52CLKA Card specGenerator___________"+specGenerator);
        }
		else if(inputString.toUpperCase().contains("CR52MIFA")) {
            specGenerator="CR52MIFA_CARD";
            System.out.println("Found the CR52MIFA Card specGenerator___________"+specGenerator);
        }else if(inputString.toUpperCase().contains("CR52LCDA")) {
            specGenerator="CR52LCDA_CARD";
            System.out.println("Found the CR52LCDA Card specGenerator___________"+specGenerator);
        }
        /*else {
            specGenerator="FINAL_CARD";
        }*/
        return specGenerator;
		
	}
	
	/*public static String getCardSpecMapping(String specGenerator, String inputString) {
		
		
		if (inputString.toUpperCase().contains("POWER")
				|| inputString.toUpperCase().contains("PWR")) {
//			specGenerator="DC_POWER_CARD";
			//specGenerator="genericEquipment";
			specGenerator="POWER_CARD";
			System.out.println("Found the POWER Card specGenerator___________");
		}else if(inputString.toUpperCase().contains("CLK")) {
			specGenerator="CLK_CARD";
		}else if(inputString.toUpperCase().contains("FAN")) {
//			specGenerator="FAN_CARD";
			//specGenerator="genericEquipment"; Huawei Generic Module Card
			specGenerator="FAN_CARD";
			System.out.println("Found the FAN Card specGenerator___________");
		}else if(inputString.toUpperCase().contains("CR5D0SRUA470")) {
			specGenerator="CR5D0SRUA470_CARD";
				}else if(inputString.toUpperCase().contains("CR57EFGFB")) {
			specGenerator="CR57EFGFB_CARD";
		}else if(inputString.toUpperCase().contains("CR57LBXF2")) {
			specGenerator="HUAWEICR57LBXF2_CARD";
		}else if(inputString.toUpperCase().contains("CR52LPUN")) {
			specGenerator="CR52LPUN_CARD";
		}else if(inputString.toUpperCase().replaceAll("[0-9]","").trim().equals("CRMPUD")) {
			specGenerator="CR52MPUD_MODULE";
		}else if(inputString.toUpperCase().replaceAll("[0-9]","").trim().equals("CXP")) {
			specGenerator="CXP_CARD";
		}else if(inputString.toUpperCase().replaceAll("[0-9]","").trim().equals("ANDCXPB")) {
			specGenerator="AND2CXPB_CARD";
		}else if(inputString.toUpperCase().replaceAll("[0-9]","").trim().equals("SRU")) {
			specGenerator="SRU_CARD";
		}else if(inputString.toUpperCase().replaceAll("[0-9]","").trim().equals("LPU")) {
			specGenerator="LPU_CARD";
		}else if(inputString.toUpperCase().contains("CR53LPUF")) {
			specGenerator="CR53LPUF_CARD";
		}else if(inputString.toUpperCase().contains("CR53E8GF")) {
			specGenerator="CR53E8GF_CARD";
		}else if(inputString.toUpperCase().contains("CR53TCMB")) {
			specGenerator="CR53TCMB_CARD";
		}else if(inputString.toUpperCase().replaceAll("[0-9]","").trim().equals("CREEGFN")) {
			specGenerator="CR52EEGFN_CARD";
		}else if(inputString.toUpperCase().replaceAll("[0-9]","").trim().equals("MPU")) {
			specGenerator="MPU_CARD";
		}else if(inputString.equals("CARD ATN 910C-B")) {
			specGenerator="ATN910C-B_CARD";
		}else if(inputString.toUpperCase().contains("CR57LPUF41A")) {
			specGenerator="CR57LPUF41A_CARD";
		}else if(inputString.toUpperCase().contains("CR57LPUF120A")) {
			specGenerator="CR57LPUF120A_CARD";
		}else if(inputString.toUpperCase().contains("CR52LPUK 1")) {
			specGenerator="CR52LPUK1_CARD"; // CR57LPUF50C 2
			System.out.println("CR52LPUK1_CARD  found ________");
		}else if(inputString.toUpperCase().contains("CR57LPUF50C 2")) {
		specGenerator="CR57LPUF50C2_CARD"; // CR57LPUF50C 2
		System.out.println("CR57LPUF50C2_CARD  found ________");
		}else if(inputString.toUpperCase().contains("CR57LPUF50C")) {
			specGenerator="CR57LPUF50C_CARD"; // CR57LPUF50C 2
			//System.out.println("CR57LPUF50C2_CARD  found ________");
		}else if(inputString.toUpperCase().contains("CR57MPUH")) {
			specGenerator="CR57MPUH_CARD"; // CR57LPUF50C 2
			//System.out.println("CR57LPUF50C2_CARD  found ________");
		}else if(inputString.toUpperCase().contains("MPU Board")) {
			specGenerator="MPU Board_CARD"; // CR57LPUF50C 2
			//System.out.println("CR57LPUF50C2_CARD  found ________");
		}
		else if(inputString.toUpperCase().contains("CR57LPUF50C 2")) {
				specGenerator="CR57LPUF50C2_CARD"; // CR57LPUF50C 2
				System.out.println("CR57LPUF50C2_CARD  found ________");
		}
		else if(inputString.toUpperCase().contains("NPU-480")) {
			specGenerator="NPU480_CARD";
			//before this are from name
		}else if(inputString.toUpperCase().contains("SFP")) {
			specGenerator="SFP_CARD";
		}else if(inputString.toUpperCase().replaceAll("[0-9]","").replace("/","").trim().equals("CFCARD")) {
			specGenerator="CF_CARD";
		}else if(inputString.toUpperCase().contains("CR57EMGFB23")) {
			specGenerator="CR57EMGFB23_CARD";
		}else if(inputString.toUpperCase().contains("CR57SRU200A5")) {
			specGenerator="CR57SRU200A5_CARD";
		}else if(inputString.toUpperCase().contains("CR57LBXF20")) {
			specGenerator="CR57LBXF20_CARD";
		}else if(inputString.toUpperCase().contains("CR57SFU200C")) {
			specGenerator="CR57SFU200C_CARD";
		}else if(inputString.toUpperCase().trim().equals("CARDATN910C-B")) {
			specGenerator="ATN910C-B_CARD";
		}else if(inputString.toUpperCase().contains("CR57LPUF120A")) {
			specGenerator="CR57LPUF120A_CARD";
		}else if(inputString.toUpperCase().contains("AND2CXPB")) {
			specGenerator="AND2CXPB_CARD";
		}else if(inputString.toUpperCase().trim().contains("SWITCHANDROUTEPROCESSINGUNITA5")) {
			specGenerator="SRU_CARD";
		}else if(inputString.toUpperCase().contains("CR58EANBB")) {
			specGenerator="CR58EANBB_CARD";
		}else if(inputString.toUpperCase().contains("CR56RPUA")) {
			specGenerator="CR56RPUA_CARD";
		}else if(inputString.toUpperCase().equals("ANG1CXPH")) {
			specGenerator="ANG1CXPH_CARD";
		}else if(inputString.toUpperCase().contains("CR52LPUKD0")) {
			specGenerator="CR52LPUKD0_CARD";
		}else if(inputString.toUpperCase().contains("10GBASE LAN/WAN-XFP")) {
			specGenerator="LANWANXFP_CARD";
		}else if(inputString.toUpperCase().contains("CR57L2XEFGB20")) {
			specGenerator="CR57L2XEFGB20_CARD";
		}else if(inputString.toUpperCase().contains("CR52MPUD2")) {
			specGenerator="CR52MPUD2_CARD";
		}
		else {
			specGenerator="FINAL_CARD";
		}
		return specGenerator;
	}
*/}
