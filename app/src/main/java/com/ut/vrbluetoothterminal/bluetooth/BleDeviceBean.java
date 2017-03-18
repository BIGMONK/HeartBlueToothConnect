package com.ut.vrbluetoothterminal.bluetooth;

import java.util.UUID;

/**
 * Created by djf on 2017/3/17.
 */

public class BleDeviceBean {
    private String name;
    private String mac;
    private int type;//1  手环   2  主控板   3 计步器   4  三角心率计
    private UUID serviceUUID;
    private UUID notifyUUID;
    private UUID sendUUID;
    private UUID configUUID;
    private byte[] values;
    private int state;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getValues() {
        StringBuffer sb = new StringBuffer();
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                sb.append(values[i] + "    ");
            }
        }

        return sb.toString();
    }

    public void setValues(byte[] values) {
        this.values = values;
    }

    public BleDeviceBean(String name, String mac, int type, UUID serviceUUID, UUID notifyUUID, UUID sendUUID, UUID configUUID) {
        this.name = name;
        this.mac = mac;
        this.type = type;
        this.serviceUUID = serviceUUID;
        this.notifyUUID = notifyUUID;
        this.sendUUID = sendUUID;
        this.configUUID = configUUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public UUID getServiceUUID() {
        return serviceUUID;
    }

    public void setServiceUUID(UUID serviceUUID) {
        this.serviceUUID = serviceUUID;
    }

    public UUID getNotifyUUID() {
        return notifyUUID;
    }

    public void setNotifyUUID(UUID notifyUUID) {
        this.notifyUUID = notifyUUID;
    }

    public UUID getSendUUID() {
        return sendUUID;
    }

    public void setSendUUID(UUID sendUUID) {
        this.sendUUID = sendUUID;
    }

    public UUID getConfigUUID() {
        return configUUID;
    }

    public void setConfigUUID(UUID configUUID) {
        this.configUUID = configUUID;
    }


    @Override
    public String toString() {

        StringBuffer sb = new StringBuffer();
        if (values != null) {
            for (int i = 0; i < values.length; i++) {
                sb.append(values[i] + "    ");
            }
        }
        return "BleDeviceBean{" +
                "name='" + name + '\'' +
                ", mac='" + mac + '\'' +
                ", type=" + type +
                ", values=" + sb.toString() +
                ", state=" + state +
                ", serviceUUID=" + serviceUUID +
                ", notifyUUID=" + notifyUUID +
                ", sendUUID=" + sendUUID +
                ", configUUID=" + configUUID +
                '}';
    }
}