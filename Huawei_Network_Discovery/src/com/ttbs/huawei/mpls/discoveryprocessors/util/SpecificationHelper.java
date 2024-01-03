package com.ttbs.huawei.mpls.discoveryprocessors.util;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import oracle.communications.integrity.metadata.CimType;
import oracle.communications.integrity.scanCartridges.sdk.helper.BaseSpecificationHelper;
import oracle.communications.inventory.api.characteristic.impl.CharacteristicHelper;
import oracle.communications.inventory.api.entity.CharacteristicSpecification;
import oracle.communications.inventory.api.entity.DeviceInterface;
import oracle.communications.inventory.api.entity.DeviceInterfaceConfigurationItem;
import oracle.communications.inventory.api.entity.DeviceInterfaceSpecification;
import oracle.communications.inventory.api.entity.Equipment;
import oracle.communications.inventory.api.entity.EquipmentHolder;
import oracle.communications.inventory.api.entity.EquipmentHolderSpecification;
import oracle.communications.inventory.api.entity.EquipmentSpecification;
import oracle.communications.inventory.api.entity.LogicalDevice;
import oracle.communications.inventory.api.entity.LogicalDeviceSpecification;
import oracle.communications.inventory.api.entity.MediaInterface;
import oracle.communications.inventory.api.entity.PhysicalDevice;
import oracle.communications.inventory.api.entity.PhysicalDeviceSpecification;
import oracle.communications.inventory.api.entity.PhysicalPort;
import oracle.communications.inventory.api.entity.PhysicalPortSpecification;
import oracle.communications.inventory.api.entity.Specification;
import oracle.communications.inventory.api.entity.common.CharValue;
import oracle.communications.inventory.api.entity.common.CharacteristicExtensible;
import oracle.communications.platform.persistence.Finder;
import oracle.communications.platform.persistence.PersistenceHelper;

/**
 * This class provide utility methods for operating on specifications.
 * 
 * It caches results, so the typical usage is to instantiate a
 * SpecificationHelper and use it for multiple operations.
 * 
 */

public class SpecificationHelper {
	private Logger logger;
	private static final String GET_SPECIFICATION_METHOD = "getSpecification";
	private static final String SET_SPECIFICATION_METHOD = "setSpecification";

	private final Map<CimType, SpecAndCharacteristicMap> cachedSpecs = new HashMap<CimType, SpecAndCharacteristicMap>();
	private CharacteristicExtensible<? extends CharValue> cachedEntity;
	private GenericSpecificationHelper cachedHelper;

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

	/**
	 * Set the specification for an entity. If the specification does not exist, the
	 * specification is not set.
	 * 
	 * The results of this operation are cached, so the assumption is that the
	 * specification for this entity is not later changed by other means
	 * 
	 * @param entity            - the entity to set the specification for
	 * @param specificationName - the name of the specification
	 * @return True if the specification of the given name exists and was applied,
	 *         false otherwise
	 */
	public boolean setSpecificationByName(CharacteristicExtensible<? extends CharValue> entity,
			final String specificationName) {
		boolean result = false;
		SpecAndCharacteristicMap specInfo = getSpecificationInfo(entity, specificationName);
		if (specInfo != null) {
			GenericSpecificationHelper helper = new GenericSpecificationHelper(entity, specInfo.spec,
					specInfo.characteristicSpecMap);
			helper.setSpecification();
			cachedEntity = entity;
			cachedHelper = helper;
			result = true;
		}
		return result;
	}

	/**
	 * Check if the entity with current specification supports a characteristic of
	 * the given name
	 * 
	 * @param entity
	 * @param characteristicName
	 * @return true if the characteristic is supported, false otherwise
	 */
	public boolean supportsCharacteristic(CharacteristicExtensible<? extends CharValue> entity,
			String characteristicName) {
		GenericSpecificationHelper helper = getHelper(entity);
		return helper == null ? false : helper.supportsCharacteristic(characteristicName);
	}

	/**
	 * Set a characteristic value. If the entity specification does not support this
	 * characteristic, do nothing
	 * 
	 * @param entity
	 * @param name
	 * @param value
	 * @return true if the entity supports this characteristic, false otherwise
	 */
	public boolean setCharacteristic(CharacteristicExtensible<? extends CharValue> entity, String name, String value) {
		boolean result = false;
		GenericSpecificationHelper helper = getHelper(entity);
		if (helper != null && helper.supportsCharacteristic(name)) {
			helper.setCharacteristic(name, value);
			result = true;
		}
		return result;
	}

	private GenericSpecificationHelper getHelper(CharacteristicExtensible<? extends CharValue> entity) {
		if (cachedEntity == entity) {
			return cachedHelper;
		}
		GenericSpecificationHelper result = null;
		Specification spec = getSpecificationFromEntity(entity);
		if (spec != null) {
			SpecAndCharacteristicMap specInfo = getSpecificationInfo(entity, spec.getName());

			if (specInfo != null) {
				result = new GenericSpecificationHelper(entity, specInfo.spec, specInfo.characteristicSpecMap);
				cachedEntity = entity;
				cachedHelper = result;
			}
		}
		return result;
	}

	private class SpecAndCharacteristicMap {
		Specification spec;
		Map<String, CharacteristicSpecification> characteristicSpecMap;

		public SpecAndCharacteristicMap(Specification spec,
				Map<String, CharacteristicSpecification> characteristicSpecMap) {
			this.spec = spec;
			this.characteristicSpecMap = characteristicSpecMap;
		}
	};

	private SpecAndCharacteristicMap getSpecificationInfo(CharacteristicExtensible<? extends CharValue> entity,
			final String specificationName) {
		SpecAndCharacteristicMap result = null;
		if (entity != null && specificationName != null) {
			CimType cimType = new CimType(entity.getClass(), specificationName);

			if (cachedSpecs.containsKey(cimType)) {
				result = cachedSpecs.get(cimType);
			} else {
				try {
					final Map<String, CharacteristicSpecification> characteristicSpecMap = new HashMap<String, CharacteristicSpecification>();

					// final Specification spec =
					// BaseSpecificationHelper.loadSpecification(specificationName,characteristicSpecMap);
					Specification spec = null;
					Finder finder = PersistenceHelper.makeFinder();
					if (entity instanceof PhysicalDevice) {
						Collection<PhysicalDeviceSpecification> specs = finder
								.findByName(PhysicalDeviceSpecification.class, specificationName); // <== Search for
																									// spec based on
																									// entity and spec
																									// name.
						spec = specs.iterator().next();
					}
					if (entity instanceof LogicalDevice) {
						Collection<LogicalDeviceSpecification> specs = finder
								.findByName(LogicalDeviceSpecification.class, specificationName); // <== Search for spec
																									// based on entity
																									// and spec name.
						spec = specs.iterator().next();
					}
					if (entity instanceof Equipment) {
						Collection<EquipmentSpecification> specs = finder.findByName(EquipmentSpecification.class,
								specificationName); // <== Search for spec based on entity and spec name.
						spec = specs.iterator().next();
					}
					if (entity instanceof EquipmentHolder) {
						Collection<EquipmentHolderSpecification> specs = finder
								.findByName(EquipmentHolderSpecification.class, specificationName); // <== Search for
																									// spec based on
																									// entity and spec
																									// name.
						spec = specs.iterator().next();
					}
					if (entity instanceof PhysicalPort) {
						Collection<PhysicalPortSpecification> specs = finder.findByName(PhysicalPortSpecification.class,
								specificationName); // <== Search for spec based on entity and spec name.
						spec = specs.iterator().next();
					}
					if (entity instanceof DeviceInterface || entity instanceof MediaInterface) {
						Collection<DeviceInterfaceSpecification> specs = finder
								.findByName(DeviceInterfaceSpecification.class, specificationName); // <== Search for
																									// spec based on
																									// entity and spec
																									// name.
						spec = specs.iterator().next();
					}
					if (entity instanceof DeviceInterfaceConfigurationItem) {
						spec = BaseSpecificationHelper.loadSpecification(specificationName, characteristicSpecMap);
					}

					if (characteristicSpecMap.isEmpty()) {
						getCharacteristicsMapWithoutTransaction(spec, characteristicSpecMap);
					}

					if (spec == null) {
						getLogger().warning("Specification " + specificationName + " not found");
					} else if (!isCompatibleSpec(entity, spec)) {
						getLogger()
								.warning("Specification " + specificationName + " found, but incompatible with entity");
					} else {
						result = new SpecAndCharacteristicMap(spec, characteristicSpecMap);
					}
				} catch (Exception e) {
					getLogger().warning("Specification " + specificationName + " not found");
				}
				cachedSpecs.put(cimType, result);
			}
		}

		return result;
	}

	private boolean isCompatibleSpec(CharacteristicExtensible<? extends CharValue> entity,
			Specification specification) {
		try {
			entity.getClass().getMethod(SET_SPECIFICATION_METHOD, specification.getEntityType());
			return true;
		} catch (NoSuchMethodException e) {
			// specification with this name belongs to a different type
			return false;
		}
	}

	private Specification getSpecificationFromEntity(CharacteristicExtensible<? extends CharValue> entity) {
		try {
			Method getSpecMethod = entity.getClass().getMethod(GET_SPECIFICATION_METHOD);
			Specification spec = (Specification) getSpecMethod.invoke(entity);
			return spec;
		} catch (Exception e) {
			String msg = "Model is not correct, " + entity.getClass() + " should have an associated "
					+ GET_SPECIFICATION_METHOD + " method";
			getLogger().info(msg);
			return null;
		}
	}

	/**
	 * Get the characteristics of a Specification.
	 * 
	 * @param specification
	 * @param inventoryResponseType
	 * @return
	 */
	public static Map<String, CharacteristicSpecification> getCharacteristicsMapWithoutTransaction(
			Specification specification, Map<String, CharacteristicSpecification> characteristicMap) {

		Set charSpecs = null;
		charSpecs = CharacteristicHelper.getCharacteristicSpecifications(specification);
		Iterator charSpecsIterator = charSpecs.iterator();

		while (charSpecsIterator.hasNext()) {
			CharacteristicSpecification charSpec = (CharacteristicSpecification) charSpecsIterator.next();
			String name = charSpec.getName();
			characteristicMap.put(name, charSpec);
			// log.debug("", "Adding Char: " + name + " to map.");
		}

		return characteristicMap;
	}

	// This implementation uses the base class for Specification Helpers
	// to retrieve a specification by name.

	private static class GenericSpecificationHelper extends BaseSpecificationHelper {
		private final Specification spec;
		private final Map<String, CharacteristicSpecification> characteristicSpecMap;

		public GenericSpecificationHelper(CharacteristicExtensible<? extends CharValue> entity, Specification spec,
				Map<String, CharacteristicSpecification> characteristicSpecMap) {
			super(entity);
			this.spec = spec;
			this.characteristicSpecMap = characteristicSpecMap;
		}

		@Override
		public String getSpecificationName() {
			return spec.getName();
		}

		@Override
		protected Specification getSpecification() {
			return spec;
		}

		@Override
		protected Map<String, CharacteristicSpecification> getCharacteristicMap() {
			return characteristicSpecMap;
		}

		public boolean supportsCharacteristic(String name) {
			return getCharacteristicMap().containsKey(name);
		}

		@Override
		public void setSpecification() {
			super.setSpecification();
		}

		@Override
		public void setCharacteristic(String name, String value) {
			super.setCharacteristic(name, value);
		}
	}

}
