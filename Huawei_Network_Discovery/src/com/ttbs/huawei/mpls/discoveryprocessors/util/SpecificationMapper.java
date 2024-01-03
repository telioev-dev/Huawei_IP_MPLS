package com.ttbs.huawei.mpls.discoveryprocessors.util;




import oracle.communications.inventory.api.entity.common.CharValue;
import oracle.communications.inventory.api.entity.common.CharacteristicExtensible;

public interface SpecificationMapper {
   
    String mapSpecificationName(CharacteristicExtensible<? extends CharValue> entity, String specificationName);
}
