package com.ttbs.huawei.mpls.discoveryprocessors.util;

import java.util.ArrayList;

import java.util.List;

import oracle.communications.inventory.api.entity.PhysicalDevice;
 
public class HuaweiReportContext {
 
    // Private static instance variable

    private static HuaweiReportContext instance;
 
    // List to hold physical devices

    private List<PhysicalDevice> physicalDeviceList;
 
    // Private constructor to prevent instantiation from outside the class

    private HuaweiReportContext() {

        physicalDeviceList = new ArrayList<>();

    }
 
    // Public method to get the singleton instance

    public static synchronized HuaweiReportContext getInstance() {

        if (instance == null) {

            instance = new HuaweiReportContext();

        }

        return instance;

    }
 
    // Method to add a physical device to the list

    public void addPhysicalDevice(PhysicalDevice physicalDevice) {

        physicalDeviceList.add(physicalDevice);

    }
 
    // Method to get the list of physical devices

    public List<PhysicalDevice> getPhysicalDeviceList() {

        return physicalDeviceList;

    }
 
    // Other methods and properties of the class (if any)

}

