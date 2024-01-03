
package com.ttbs.huawei.mpls.discoveryprocessors.util;

import java.util.Properties;
import java.util.Map.Entry;

import oracle.communications.inventory.api.entity.DeviceInterface;
import oracle.communications.inventory.api.entity.PhysicalPort;
import oracle.communications.inventory.api.entity.common.CharValue;
import oracle.communications.inventory.api.entity.common.CharacteristicExtensible;
import oracle.communications.platform.util.Utils;

public class DefaultSpecificationMapper implements SpecificationMapper {
	
	Properties prop;
	
	public DefaultSpecificationMapper(Properties prop) {
		this.prop = prop;
	}
	
	

	@Override
	public String mapSpecificationName(CharacteristicExtensible<? extends CharValue> entity, String specificationName) {
		
		if(specificationName == null)
					return null;
		
		if(prop != null ){
					
			 if(!Utils.checkNull(entity) && entity instanceof PhysicalPort) {
				
					for(Entry<Object, Object> keyvalue:	prop.entrySet())
		    		{
		    			if(keyvalue.getKey().toString().endsWith("_PORT")
		    					&& specificationName.startsWith(keyvalue.getKey().toString().replace("_PORT", "")))
		    			{
		    				return keyvalue.getValue().toString();
		    			}
		    			
		    		}
		    			
		    	}
				
			 if(!Utils.checkNull(entity) && entity instanceof DeviceInterface) {
				
					for(Entry<Object, Object> keyvalue:	prop.entrySet())
		    		{
		    			if(keyvalue.getKey().toString().endsWith("_DI") &&
		    					specificationName.startsWith(keyvalue.getKey().toString().replace("_DI", "")))
		    			{
		    				return keyvalue.getValue().toString();
		    			}
		    			
		    		}
		    			
		    	}
			if(prop.get(specificationName) != null)
			 {
				
				 return ((String) prop.get(specificationName)).trim();
			 }
			 }
			 
		
	
		return null;
	}

}
