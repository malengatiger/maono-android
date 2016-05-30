package com.boha.monitor.firebase.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by aubreymalabie on 5/21/16.
 */

public class UserDTO implements Serializable {
    public UserDTO() {}


    private String firstName = "",
            lastName = "", fullName,
            email = "", userID = "", companyID = "", password, uid;
    private String uri;
    private HashMap<String, PhotoUploadDTO> photos;
    private List<PhotoUploadDTO> photoList;

    public List<PhotoUploadDTO> getPhotoList() {
        photoList = new ArrayList<>();
        if (photos != null) {
            for (PhotoUploadDTO p : photos.values()) {
                photoList.add(p);
            }
        }
        return photoList;
    }

    public void setPhotos(HashMap<String, PhotoUploadDTO> photos) {
        this.photos = photos;
        getPhotoList();
    }

    public HashMap<String, PhotoUploadDTO> getPhotos() {
        return photos;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getFullName() {
        if (fullName == null) {
            return firstName + " " + lastName;
        } else {
            return fullName;
        }
    }


    public String getCompanyID() {
        return companyID;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCompanyID(String companyID) {
        this.companyID = companyID;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }


}
