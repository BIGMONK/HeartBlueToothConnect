package com.ut.vrbluetoothterminal.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.ut.vrbluetoothterminal.client.LTNClient;
import com.ut.vrbluetoothterminal.client.LTNConstants;
import com.ut.vrbluetoothterminal.client.interfaces.ILTNClient;
import com.ut.vrbluetoothterminal.model.AppConfigModel;
import com.ut.vrbluetoothterminal.serialndk.SerialManager;
import com.ut.vrbluetoothterminal.utils.SharedPreferencesUtil;
import com.ut.vrbluetoothterminal.utils.UIUtils;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Description:
 * Author：Giousa
 * Date：2016/8/3
 * Email：giousa@chinayoutu.com
 */
public class TService extends Service implements LTNConstants,
        SerialManager.SerialSpeedChangeListener,
        SerialManager.SerialAngleChangeListener{

    private static final String TAG = TService.class.getSimpleName();
    private static final String DEFAULT_DEVICE_NAME = "BTxxoo";
    private static final String DEFAULT_SERVER_IP = "127.0.0.1";
    private static final int DEFAULT_SERVER_PORT = 8090;
    private final String mTraineeConfig = "/mnt/sdcard/TraineeConfig.cfg";
    private AppConfigModel mAppConfigModel;
    private LTNClient mLTNClient = null;
    private boolean mLTNConnected = false;
    private String mBleDeviceName = "UT03";
    private int mConfigServerPort = 9090;
    //    private String mConfigServerIP = "192.168.0.222";
    private SerialManager mSerialManager;
    private Timer mTimer = null;
    private TimerTask mTimerTask = null;
    private boolean isPause = false;
    private static int delay = 5000;  //20s
    private static int period = 5000;  //20s
    private static final int HOST_IP = 110;
    private static final int COACH_CONNECT = 111;
    private final static String SP_NAME = "CONFIG_IP";
    private String mConfigIP;
    private boolean achievedIP = false;


    private Handler mHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case HOST_IP:
                    Log.d(TAG,"HOST_IP"+HOST_IP);
                    if(!achievedIP){
                        achieveHostIP();
                    }
                    break;
                case COACH_CONNECT:
                    Log.d(TAG,"COACH_CONNECT"+COACH_CONNECT);
                    if(mConfigIP != null){
                        if(!mLTNConnected){
                            connectToServer(mConfigIP);
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "============> TService.onBind");
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "============> TService.onCreate");
        achievedIP = false;
        mConfigIP = SharedPreferencesUtil.getString(UIUtils.getContext(), SP_NAME, "");
        connectToDevice();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "============> TService.onStartCommand 重启服务");
//        flags = START_STICKY;
//        return super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startId) {
        Log.d(TAG, "============> TService.onStart");
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                mAppConfigModel = parseAppConfig(mTraineeConfig);
//                String configBtDeviceName = getConfigBtDeviceName();
//                String configServerIp = getConfigServerIp();
//                int configServerPort = getConfigServerPort();
//                Log.d(TAG,"name="+configBtDeviceName+" ip="+configServerIp+"  prot="+configServerPort);
//            }
//        }).start();
    }

    private void connectToDevice() {

        mSerialManager = SerialManager.getInstance();
        mSerialManager.openSerial();
        mSerialManager.setSerialSpeedChangeListener(this);
        mSerialManager.setSerialAngleChangeListener(this);
        startTimer();
    }


    private void achieveHostIP() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG,"achieve host ip");
                int port = 9999;
                DatagramSocket ds = null;
                DatagramPacket dp = null;
                byte[] buf = new byte[1024];
                StringBuffer sbuf = new StringBuffer();
                try {
                    ds = new DatagramSocket(port);
                    dp = new DatagramPacket(buf, buf.length);
                    Log.d(TAG,"监听广播端口打开：");
                    ds.receive(dp);
                    ds.close();
                    int i;
                    for(i=0;i<1024;i++){
                        if(buf[i] == 0){
                            break;
                        }
                        sbuf.append((char) buf[i]);
                    }

                    if(sbuf != null){
                        String mConfigServerIP = sbuf.toString();
                        Log.d(TAG,"收到广播: "+mConfigServerIP);
                        if(!mConfigServerIP.equals(mConfigIP) && !mConfigServerIP.isEmpty()){
                            SharedPreferencesUtil.saveString(UIUtils.getContext(),SP_NAME,mConfigServerIP);
                            achievedIP =  true;
                        }
                        mConfigIP = mConfigServerIP;
                    }

                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }



    public boolean onUnbind(Intent intent) {
        Log.d(TAG, "============> TService.onUnbind");
        return false;
    }

    public void onRebind(Intent intent) {
        Log.d(TAG, "============> TService.onRebind");
    }

    public void onDestroy() {
        Log.d(TAG, "============> TService.onDestroy");
        stopTimer();
        if(mSerialManager != null){
            mSerialManager.closeSerial();
        }
        Intent localIntent = new Intent();
        localIntent.setClass(this, TService.class); // 销毁时重新启动Service
        this.startService(localIntent);
    }

    private void connectToServer(String ip) {

        Log.d(TAG,"connectToServerIP:"+ip);

        mLTNClient = new LTNClient(ip, (short) mConfigServerPort);

        mLTNClient.setClientAddedListener(new LTNClient.ILTNClientAddedListener() {
            @Override
            public void connectedTo(ILTNClient client, String host, short port) {
                Log.d(TAG, "LTNClient adb");
                mLTNConnected = true;
                notifyTrainerConnectSuccessed();
                stopTimer();
            }
        });
        mLTNClient.setClientRemovedListener(new LTNClient.ILTNClientRemovedListener() {
            @Override
            public void disconnectedFrom(ILTNClient client, String host, short port) {
                Log.d(TAG, "LTNClient disconnected");
                mLTNConnected = false;
                startTimer();
            }
        });
        mLTNClient.setClientExceptionListener(new LTNClient.ILTNClientExceptionListener() {

            @Override
            public void exceptionWhileConnect(ILTNClient client, String host, short port) {
                Log.d(TAG, "LTNClient exception while connect");
                mLTNConnected = false;
                rebootDevice();

            }

        });

        mLTNClient.connectToHost();
    }

    private void rebootDevice() {
        try {
            Runtime.getRuntime().exec("su");
            Runtime.getRuntime().exec("reboot");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void notifyTrainerConnectSuccessed() {
        if (mLTNConnected) {
            Map<String, String> params = new HashMap<>();
            params.put(LTN_COMMAND_CLINET_ID, mBleDeviceName);
            mLTNClient.sendCommand(LTN_COMMAND_NOTIFY, params);
        } else {
            Log.d(TAG, "未连接服务器");
        }
    }

    @Override
    public void onSerialSpeedChanged(short speed) {

        Log.d(TAG,"onSerialSpeedChanged speed="+speed);

        if (mLTNConnected) {
            Log.d(TAG, "已连接服务器，获取速度：" + speed);
            Map<String, String> params = new HashMap<>();
            params.put(LTN_COMMAND_CLINET_ID, mBleDeviceName);
            params.put(LTN_PARAM_SPEED, speed + "");
            mLTNClient.sendCommand(LTN_COMMAND_SPEED, params);
        } else {
            Log.d(TAG, "未连接服务器，获取速度：" + speed);
        }
    }

    @Override
    public void onSerialAngleChanged(float angle) {

        Log.d(TAG,"onSerialAngleChanged angle="+angle);

        if (mLTNConnected) {
            Log.d(TAG, "已连接服务器，获取角度：" + angle);
            Map<String, String> params = new HashMap<>();
            params.put(LTN_COMMAND_CLINET_ID, mBleDeviceName);
            params.put(LTN_PARAM_ANGEL, angle + "");
            mLTNClient.sendCommand(LTN_COMMAND_ANGEL, params);
        } else {
            Log.d(TAG, "未连接服务器，获取角度：" + angle);
        }
    }


    private void startTimer(){
        if (mTimer == null) {
            mTimer = new Timer();
        }

        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    Log.d(TAG,"timer start");
                    sendMessage(HOST_IP);
                    sendMessage(COACH_CONNECT);
                    do {
                        try {
                            Log.i(TAG, "sleep(5000)...");
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                        }
                    } while (isPause);

                }
            };
        }

        if(mTimer != null && mTimerTask != null )
            mTimer.schedule(mTimerTask, delay, period);

    }

    private void stopTimer(){

        Log.d(TAG,"timer end");

        if(mTimer!=null){
            mTimer.cancel();
            mTimer = null;
        }

        if(mTimerTask != null){
            mTimerTask.cancel();
            mTimerTask = null;
        }

    }

    public void sendMessage(int id){
        if (mHandler != null) {
            Message message = Message.obtain(mHandler, id);
            mHandler.sendMessage(message);
        }
    }

    private AppConfigModel parseAppConfig(String configFilepath) {
        try {

            JsonReader reader = new JsonReader(new FileReader(configFilepath));
            return new Gson().fromJson(reader, AppConfigModel.class);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getConfigServerIp() {
        if (mAppConfigModel == null
                || mAppConfigModel.getBasicProfile() == null
                || mAppConfigModel.getBasicProfile().getServerIp() == null) {
            Log.w(TAG, "Can not get the config server ip");
            return DEFAULT_SERVER_IP;
        }
        return mAppConfigModel.getBasicProfile().getServerIp();
    }

    private int getConfigServerPort() {
        if (mAppConfigModel == null
                || mAppConfigModel.getBasicProfile() == null
                || mAppConfigModel.getBasicProfile().getServerPort() == 0) {
            Log.w(TAG, "Can not get the config server port");
            return DEFAULT_SERVER_PORT;
        }
        return mAppConfigModel.getBasicProfile().getServerPort();
    }


    private String getConfigBtDeviceName() {
        if (mAppConfigModel == null
                || mAppConfigModel.getBasicProfile() == null
                || mAppConfigModel.getBasicProfile().getDeviceName() == null) {
            Log.w(TAG, "Can not get the config device name");
            return DEFAULT_DEVICE_NAME;
        }
        return mAppConfigModel.getBasicProfile().getDeviceName();
    }
}
