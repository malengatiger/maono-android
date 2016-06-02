/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.boha.monitor.library.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author aubreyM
 */
public class LocationTrackerDTO implements Serializable, Comparable<LocationTrackerDTO> {

    private static final long serialVersionUID = 1L;
    private Integer locationTrackerID;
    private Integer staffID, monitorID, companyID;
    private Long dateTracked, dateUploaded;
    private Double latitude;
    private Double longitude;
    private float accuracy;
    private String geocodedAddress, staffName, monitorName, message;
    private Long dateAdded;
    private List<Integer> monitorList, staffList;
    private PhotoUploadDTO photo;

    public LocationTrackerDTO() {
    }

    public PhotoUploadDTO getPhoto() {
        return photo;
    }

    public void setPhoto(PhotoUploadDTO photo) {
        this.photo = photo;
    }

    public Integer getCompanyID() {
        return companyID;
    }

    public void setCompanyID(Integer companyID) {
        this.companyID = companyID;
    }

    public Long getDateUploaded() {
        return dateUploaded;
    }

    public void setDateUploaded(Long dateUploaded) {
        this.dateUploaded = dateUploaded;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getMonitorName() {
        return monitorName;
    }

    public void setMonitorName(String monitorName) {
        this.monitorName = monitorName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Integer> getMonitorList() {
        if (monitorList == null) {
            monitorList = new ArrayList<>();
        }
        return monitorList;
    }

    public void setMonitorList(List<Integer> monitorList) {
        this.monitorList = monitorList;
    }

    public List<Integer> getStaffList() {
        if (staffList == null) {
            staffList = new ArrayList<>();
        }
        return staffList;
    }

    public void setStaffList(List<Integer> staffList) {
        this.staffList = staffList;
    }

    public Integer getLocationTrackerID() {
        return locationTrackerID;
    }

    public void setLocationTrackerID(Integer locationTrackerID) {
        this.locationTrackerID = locationTrackerID;
    }

    public Integer getStaffID() {
        return staffID;
    }

    public void setStaffID(Integer staffID) {
        this.staffID = staffID;
    }

    public Integer getMonitorID() {
        return monitorID;
    }

    public void setMonitorID(Integer monitorID) {
        this.monitorID = monitorID;
    }

    public Long getDateTracked() {
        return dateTracked;
    }

    public void setDateTracked(Long dateTracked) {
        this.dateTracked = dateTracked;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public float getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(float accuracy) {
        this.accuracy = accuracy;
    }

    public String getGeocodedAddress() {
        return geocodedAddress;
    }

    public void setGeocodedAddress(String geocodedAddress) {
        this.geocodedAddress = geocodedAddress;
    }

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public Long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Long dateAdded) {
        this.dateAdded = dateAdded;
    }

    @Override
    public int compareTo(LocationTrackerDTO another) {
        if (this.getDateTracked().intValue() < another.getDateTracked().intValue()) {
            return 1;
        }
        if (this.getDateTracked().intValue() > another.getDateTracked().intValue()) {
            return -1;
        }
        return 0;
    }
}
