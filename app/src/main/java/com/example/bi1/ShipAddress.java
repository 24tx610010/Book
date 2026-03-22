package com.example.bi1;

import java.io.Serializable;

public class ShipAddress implements Serializable {
    private String id;
    private String receiverName;
    private String receiverPhone;
    private String detailAddress;
    private boolean isDefault;

    public ShipAddress() {}

    public ShipAddress(String id, String receiverName, String receiverPhone, String detailAddress, boolean isDefault) {
        this.id = id;
        this.receiverName = receiverName;
        this.receiverPhone = receiverPhone;
        this.detailAddress = detailAddress;
        this.isDefault = isDefault;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getReceiverName() { return receiverName; }
    public void setReceiverName(String receiverName) { this.receiverName = receiverName; }
    public String getReceiverPhone() { return receiverPhone; }
    public void setReceiverPhone(String receiverPhone) { this.receiverPhone = receiverPhone; }
    public String getDetailAddress() { return detailAddress; }
    public void setDetailAddress(String detailAddress) { this.detailAddress = detailAddress; }
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean aDefault) { isDefault = aDefault; }

    @Override
    public String toString() {
        return receiverName + " | " + receiverPhone + "\n" + detailAddress;
    }
}
