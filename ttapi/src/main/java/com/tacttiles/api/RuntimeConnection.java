package com.tacttiles.api;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.Vibrator;

/**
 * Created by andy on 17/04/17.
 */

public class RuntimeConnection {

    public static class RS_CMD {
        public static final int SEND = 0;
        public static final int CONNECT_BT = 1;
        public static final int REQUEST_FOCUS = 2;
        public static final int REGISTER = 3;
        public static final int UNREGISTER = 4;

    };

    public static class RC_CMD {
        public static final int RECEIVED = 0;
        public static final int FOCUS_LOST = 1;
        public static final int DEVICE_LOST = 2;
        public static final int DEVICE_CONNECTED = 4;
        public static final int SERVICE_STOPPED_BY_USER = 5;

    }

    public static void sendAnonymousIPCMessage (Context appContext, final int type, final Bundle data, final Handler handler) {
        Intent i = new Intent();
        i.setPackage("com.tacttiles.runtime");
        i.setAction("com.tacttiles.runtime.RuntimeService");
        appContext.startService(i);
        appContext.bindService(i, new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                // We are conntected to the service
                Messenger messenger = new Messenger(service);

                try {
                    Message msg = Message.obtain(null, type);
                    msg.replyTo = new Messenger(handler);
                    if (data != null) {
                        msg.setData(data);
                    }
                    messenger.send(msg);
                } catch (RemoteException e) {
                }
            }
        }, Context.BIND_AUTO_CREATE);
    }

    private Context appContext;
    private Messenger messenger;
    private Messenger mThis;
    private Device device;

    public RuntimeConnection(){
        mThis = new Messenger(new Handler() {
            @Override
            public void handleMessage(Message msg) {
                onIPCMessage(msg.what, msg.getData());
            }
        });
    }

    public void vibratePhone(long[] pattern, int repeat) {
        if (appContext == null){
            throw new RuntimeException("RuntimeConnection not conected");
        }
        Vibrator v = (Vibrator) appContext.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(pattern, repeat);
    }

    protected void onIPCMessage (int type, Bundle data) {
        switch (type){
            case RC_CMD.RECEIVED:
                device.onMessage(data.getString("msg"));
                break;
            case RC_CMD.DEVICE_CONNECTED:
                device.onDeviceFound();
                break;
            case RC_CMD.DEVICE_LOST:
                device.onDeviceLost();
                break;
        }
    }

    protected void sendIPCMessage(int type, Bundle data){
        Message msg = Message.obtain(null, type);
        msg.replyTo = mThis;
        if (data != null) {
            msg.setData(data);
        }
        try {
            messenger.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void sendBTMessage(byte [] msg){
        Bundle bundle = new Bundle();
        bundle.putByteArray("msg", msg);
        sendIPCMessage(RS_CMD.SEND, bundle);
    }

    boolean connect(Context context){
        this.appContext = context;
        Intent i = new Intent();
        i.setPackage("com.tacttiles.runtime");
        i.setAction("com.tacttiles.runtime.RuntimeService");

        appContext.startService(i); //keep service running

        return appContext.bindService(i, new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mThis = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                // We are conntected to the service
                messenger = new Messenger(service);

                try {
                    Message msg = Message.obtain(null, RS_CMD.REGISTER);
                    msg.replyTo = mThis;
                    messenger.send(msg);
                    RuntimeConnection.this.onServiceConnected();
                } catch (RemoteException e) {
                    // In this case the service has crashed before we could even
                    // do anything with it; we can count on soon being
                    // disconnected (and then reconnected if it can be restarted)
                    // so there is no need to do anything here.
                }
            }
        }, Context.BIND_AUTO_CREATE);
    }

    public void disconnect(){
        sendIPCMessage(RS_CMD.UNREGISTER, null);
    }

    public void requestFocus(Device device){
        this.device = device;
        sendIPCMessage(RS_CMD.REQUEST_FOCUS, null); //TODO use device id
    }

    public void requestBTDiscovery(){
        sendIPCMessage(RS_CMD.CONNECT_BT, null);
    }

    public void onServiceConnected(){

    }


}
