package com.tacttiles.api;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
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

    private Handler handler;

    public static class RS_CMD {
        public static final int SEND = 0;
        public static final int CONNECT_BT = 1;
        public static final int REQUEST_FOCUS = 2;
        public static final int UNBIND = 3;

    };

    public static class RC_CMD {
        public static final int RECEIVED = 0;
        public static final int FOCUS_LOST = 1;
        public static final int DEVICE_LOST = 2;
        public static final int DEVICE_CONNECTED = 4;
        public static final int SERVICE_STOPPED_BY_USER = 5;

    }

    public static void sendAnonymousIPCMessage (Context appContext, int type, Bundle data) {
        Intent i = new Intent();
        i.setPackage("com.tacttiles.runtime");
        i.setAction("com.tacttiles.runtime.RuntimeService");
        i.putExtra("type", type);
        i.putExtras(data);
        appContext.startService(i);
    }

    private Context appContext;
    private Messenger messenger;
    private Device device;

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

        msg.replyTo = new Messenger(handler);

        msg.setData(data);

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

    public static ResultReceiver receiverForSending(ResultReceiver actualReceiver) {
        Parcel parcel = Parcel.obtain();
        actualReceiver.writeToParcel(parcel,0);
        parcel.setDataPosition(0);
        ResultReceiver receiverForSending = ResultReceiver.CREATOR.createFromParcel(parcel);
        parcel.recycle();
        return receiverForSending;
    }

    boolean connect(Context context){
        this.appContext = context;
        Intent i = new Intent();
        i.setPackage("com.tacttiles.runtime");
        i.setAction("com.tacttiles.runtime.RuntimeService");
        i.putExtra("receiver", receiverForSending(new ResultReceiver(null) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                onIPCMessage(resultCode,resultData);
            }
        }));

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                onIPCMessage(msg.what, msg.getData());
            }
        };

        return appContext.bindService(i, new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                messenger = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                // We are conntected to the service
                messenger = new Messenger(service);

                RuntimeConnection.this.onServiceConnected();
                Vibrator v = (Vibrator) appContext.getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                v.vibrate(new long[]{0, 100, 1000, 300}, -1);
            }
        }, Context.BIND_AUTO_CREATE);
    }

    public void disconnect(){
        sendIPCMessage(RS_CMD.UNBIND, null);
    }

    public void requestFocus(Device device){
        this.device = device;
        sendIPCMessage(RS_CMD.REQUEST_FOCUS, null); //TODO use device id
    }

    public void onServiceConnected(){

    }


}
