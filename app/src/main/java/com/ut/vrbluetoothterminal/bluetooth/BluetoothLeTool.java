package com.ut.vrbluetoothterminal.bluetooth;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ut.vrbluetoothterminal.utils.UIUtils;

import java.util.List;
import java.util.UUID;


/**
 * Created by black on 2016/4/12.
 */
public class BluetoothLeTool {

    private final static String TAG = BluetoothLeTool.class.getSimpleName();

    public interface BluetoothLeDataListener {
        void onDataAvailable(byte[] value);
    }

    public interface BluetoothLeDiscoveredListener {
        void onDiscovered(List<BluetoothGattService> supportedService);
    }

    public interface BluetoothLeStatusListener {
        //        void onConnected();
//        void onDisconnected();
        void onBlueToothConnectState(int state);
    }

    private Context mContext = null;

    private BluetoothLeDataListener mBluetoothLeDataListener;
    private BluetoothLeDiscoveredListener mBluetoothLeDiscoveredListener;
    private BluetoothLeStatusListener mBluetoothLeStatusListener;

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;

    public int getmConnectionState() {
        return mConnectionState;
    }

    public void setmConnectionState(int mConnectionState) {
        this.mConnectionState = mConnectionState;
    }

    private int mConnectionState = STATE_DISCONNECTED;

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    public static final int STATE_SCANING = -1;

    public final static UUID UUID_HEART_RATE_MEASUREMENT = UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

    /**
     * 监听蓝牙连接状态和数据
     */
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i(TAG, "onConnectionStateChange newState=" + newState);
            mBluetoothLeStatusListener.onBlueToothConnectState(newState);

            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mConnectionState = STATE_CONNECTED;

                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onServicesDiscovered 发现服务" + gatt.getServices().size());
                if (mBluetoothLeDiscoveredListener != null) {
                    mBluetoothLeDiscoveredListener.onDiscovered(gatt.getServices());
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
//                if (mBluetoothLeDataListener != null) {
//                    mBluetoothLeDataListener.onDataAvailable(characteristic.getValue());
//                }
            }
            byte[] bs = characteristic.getValue();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < bs.length; i++) {
                sb.append(bs[i] + "\t");
            }
            Log.d(TAG, "onCharacteristicRead" + sb.toString());

        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
//            byte[] bs = characteristic.getValue();
//            StringBuffer sb = new StringBuffer();
//            for (int i = 0; i < bs.length; i++) {
//                sb.append(bs[i] + "\t");
//            }
//            Log.d(TAG, "onCharacteristicChanged" + sb.toString() +"  "+ characteristic.getUuid());

            //得到心率信息的service
//            BluetoothGattService service = gatt.getService(SampleGattAttributes.HAND_BAND_SERVICE_UUID);
//            if (service == null) {
//                Log.d(TAG, "onCharacteristicChanged  没有得到心率服务");
//
//            } else {
//                Log.d(TAG, "onCharacteristicChanged  得到心率服务");
//                BluetoothGattCharacteristic bluetoothGattCharacteristic
//                        = service.getCharacteristic(UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb"));
//            }

            if (characteristic != null && characteristic.getUuid().equals(SampleGattAttributes.HAND_BAND_RECEIVE_UUID)) {
                final byte[] data = characteristic.getValue();
                if (mBluetoothLeDataListener != null) {
                    mBluetoothLeDataListener.onDataAvailable(data);
                }
            }
//            broadcastUpdate(characteristic);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            byte[] bs = characteristic.getValue();
//            StringBuffer sb = new StringBuffer();
//            for (int i = 0; i < bs.length; i++) {
//                sb.append(bs[i] + "\t");
//            }
            if (status == BluetoothGatt.GATT_SUCCESS) {

                Log.d(TAG, "onCharacteristicWrite SUCCESS  ");
            } else {
                Log.d(TAG, "onCharacteristicWrite ERR    ");
            }
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.d(TAG, "onDescriptorRead :" + descriptor.getUuid());
        }
    };

    public void setBluetoothLeDataListener(BluetoothLeDataListener listener) {
        mBluetoothLeDataListener = listener;
    }

    public void setBluetoothLeDiscoveredListener(BluetoothLeDiscoveredListener listener) {
        mBluetoothLeDiscoveredListener = listener;
    }

    public void setBluetoothLeStatusListener(BluetoothLeStatusListener listener) {
        mBluetoothLeStatusListener = listener;
    }

    private void broadcastUpdate(String action) {
        Intent intent = new Intent(action);
        UIUtils.getContext().sendBroadcast(intent);
    }

    private void broadcastUpdate(BluetoothGattCharacteristic characteristic) {
//        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
//                int flag = characteristic.getProperties();
//                int format = -1;
//            if ((flag & 0x01) != 0) {
//                format = BluetoothGattCharacteristic.FORMAT_UINT16;
//                Log.d(TAG, "Heart rate format UINT16.");
//            } else {
//                format = BluetoothGattCharacteristic.FORMAT_UINT8;
//                Log.d(TAG, "Heart rate format UINT8.");
//            }
//            final int heartRate = characteristic.getIntValue(format, 1);
//            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
//            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
//            Log.d(TAG, "data is 1 : " + heartRate);
//        } else {
        // For all other profiles, writes the data formatted in HEX.
        final byte[] data = characteristic.getValue();
        if (mBluetoothLeDataListener != null) {
            mBluetoothLeDataListener.onDataAvailable(data);
        }
//        }

    }


    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) UIUtils.getContext().getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    public boolean connect(final String address) {
        Log.i(TAG, "connect");
        if (mBluetoothAdapter == null || address == null) {
            Log.i(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.i(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                mBluetoothLeStatusListener.onBlueToothConnectState(STATE_CONNECTING);
                return true;
            } else {
                return false;
            }
        }

        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }

        mBluetoothGatt = device.connectGatt(UIUtils.getContext(), false, mGattCallback);

        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        mBluetoothLeStatusListener.onBlueToothConnectState(STATE_CONNECTING);
        return true;
    }

    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    public boolean setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null || characteristic == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        if (!mBluetoothGatt.setCharacteristicNotification(characteristic, enabled)) {
            Log.d(TAG, "setCharacteristicNotification  false");
            return false;
        }

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));

        if (descriptor == null) {
            Log.d(TAG, String.format("getDescriptor for notify null!"));
            return false;
        }

        byte[] value = (enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);

        if (!descriptor.setValue(value)) {
            Log.d(TAG, String.format("setValue for notify descriptor failed!"));
            return false;
        }

        if (!mBluetoothGatt.writeDescriptor(descriptor)) {
            Log.d(TAG, String.format("writeDescriptor for notify failed"));
            return false;
        }
        return true;
//        return mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

//        // This is specific to Heart Rate Measurement.
//        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
//            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
//                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
////            Write the value of a given descriptor to the associated remote device.
//            mBluetoothGatt.writeDescriptor(descriptor);
//        }
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) {
            return null;
        }
        return mBluetoothGatt.getServices();
    }


}
