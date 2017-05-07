package com.tacttiles.api;

import android.content.Context;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A Device is a hardware abstraction that handles the low level communication protocol used in devices compatibles with the Tact-Tiles platform.
 */

public class Device {

    public static final char M_SYSTEM = 'S';
    public static final char M_ERROR = 'E';
    public static final char M_DEBUG = 'D';
    public static final char M_GESTURE = 'G';

    public static final String S_PONG_MESSAGE = "PM";
    public static final String S_POWER_OFF = "PO";
    public static final String S_CHARGER_CONNECTED = "CC";
    public static final String S_DRAW_FINISHED = "DF";
    public static final String S_EEPROM_DUMP = "ED";
    public static final String S_ANALOG_READ = "AR";
    public static final String S_DIGITAL_READ = "DR";
    public static final String S_FREE_RAM = "FR";
    public static final String S_SYSTEM_VOLTAGE = "SV";

    public static final String G_ID = "ID";
    public static final String G_DOUBLE_TOUCH = "DT";
    public static final String G_SINGLE_TOUCH = "ST";
    public static final String G_BUTTON_PRESS = "BP";

    public static final String D_DRAW_GESTURE = "DG";
    public static final String D_MESSAGE_RECEIVED = "MR";
    public static final String D_OUTPUT_STATE = "OS";
    public static final String D_START_DRAW = "ST";
    public static final String D_STATE_ADDRESS = "SA";

    /**
     * A device listener receives notifications of new data available in the {@link Device} object. Notifications indicate system status and user interaction related events.
     */
    public abstract static class DeviceListener {
        private boolean enabled = true;

        /**
         * Returns if this listener is enabled.
         *
         * @return True if this listener is enabled.
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Enables or disables this listener.
         *
         * @param enabled If true this listener is enabled.
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * Called when the device receive messages that fall under the {@link #M_SYSTEM} category.
         *
         * @param type Type of the message.
         * @param args Message arguments.
         */
        public void onSystemMessage(String type, String args) {
        }

        /**
         * Called when the device receive messages that fall under the {@link #M_ERROR} category.
         *
         * @param msg The error message.
         */
        public void onErrorMessage(String msg) {
        }

        /**
         * Called when the device receive messages that fall under the {@link #M_DEBUG} category.
         *
         * @param type Type of the message.
         * @param args Message arguments.
         */
        public void onDebugMessage(String type, String args) {
        }

        /**
         * Called when the device receive messages that fall under the {@link #M_GESTURE} category.
         *
         * @param type Type of the message.
         * @param args Message arguments.
         */
        public void onGestureReceived(String type, String args) {
        }

        /**
         * Called when the bluetooth device is connected.
         */
        public void onDeviceFound() {
        }

        /**
         * Called if the bluetooth device connection is lost or when the device is not connected.
         */
        public void onDeviceLost() {
        }
    }

    public static byte DEVICE_INFO_NAME = 0;
    public static byte DEVICE_INFO_TYPE = 1;

    private RuntimeConnection serviceConnection;
    private List<DeviceListener> deviceListeners;

    /**
     *
     */
    public Device() {
        serviceConnection = new RuntimeConnection() {
            @Override
            public void onServiceConnected() {
                serviceConnection.requestFocus(Device.this);
            }
        };
        deviceListeners = new ArrayList<>();
    }

    /**
     * Returns the service connection of this device.
     *
     * @return The service connection of this device.
     */
    public RuntimeConnection getServiceConnection() {
        return serviceConnection;
    }

    /**
     * Adds a listener to the current set of event listeners of this adapter. Events can be system status or user interaction related events.
     *
     * @param deviceListener The listener to be added to the current set of listeners on this adapter.
     */
    public void addListener(DeviceListener deviceListener) {
        deviceListeners.add(deviceListener);
    }

    /**
     * Removes a listener from the set listening to this adapter.
     *
     * @param deviceListener The listener to be removed to the current set of listeners on this adapter.
     */
    public void removeListener(DeviceListener deviceListener) {
        deviceListeners.remove(deviceListener);
    }

    /**
     * Connects this adapter to the Tact-Tiles runtime service,
     * which handles the bluetooth communication and device access.
     *
     * @param appContext The current context of this application.
     */
    public void connect(Context appContext) {
        serviceConnection.connect(appContext);
    }

    /**
     * Called when a bluetooth message is received by the runtime service and this device is properly connected.
     *
     * @param msg Message contents.
     */
    protected void onMessage(String msg) {
        String type = "" + msg.charAt(2) + msg.charAt(3);
        String contents = msg.substring(4);
        switch (msg.charAt(0)) {
            case 'S':
                switch (type) {
                    case Device.S_PONG_MESSAGE:
                        break;
                    case Device.S_POWER_OFF:
                        break;
                    case Device.S_CHARGER_CONNECTED:
                        break;
                    case Device.S_DRAW_FINISHED:
                        break;
                    case Device.S_EEPROM_DUMP:
                        break;
                    case Device.S_ANALOG_READ:
                        break;
                    case Device.S_DIGITAL_READ:
                        break;
                    case Device.S_FREE_RAM:
                        break;
                    case Device.S_SYSTEM_VOLTAGE:
                        break;
                    default:
                        throw new RuntimeException("Invalid Message Type");
                }
                for (DeviceListener deviceListener : deviceListeners) {
                    if (deviceListener.isEnabled()) {
                        deviceListener.onSystemMessage(type, contents);
                    }
                }
                break;
            case 'E':
                for (DeviceListener deviceListener : deviceListeners) {
                    if (deviceListener.isEnabled()) {
                        deviceListener.onErrorMessage(msg.substring(2));
                    }
                }
                break;
            case 'D':
                switch (type) {
                    case Device.D_DRAW_GESTURE:
                        break;
                    case Device.D_MESSAGE_RECEIVED:
                        break;
                    case Device.D_OUTPUT_STATE:
                        break;
                    case Device.D_START_DRAW:
                        break;
                    case Device.D_STATE_ADDRESS:
                        break;
                    default:
                        throw new RuntimeException("Invalid Message Type");
                }
                for (DeviceListener deviceListener : deviceListeners) {
                    if (deviceListener.isEnabled()) {
                        deviceListener.onDebugMessage(type, contents);
                    }
                }
                break;
            case 'G':
                switch (type) {
                    case Device.G_ID:
                        break;
                    case Device.G_DOUBLE_TOUCH:
                        break;
                    case Device.G_SINGLE_TOUCH:
                        break;
                    case Device.G_BUTTON_PRESS:
                        break;
                    default:
                        throw new RuntimeException("Invalid Message Type");
                }
                for (DeviceListener deviceListener : deviceListeners) {
                    if (deviceListener.isEnabled()) {
                        deviceListener.onGestureReceived(type, contents);
                    }
                }
                break;
            default:
                throw new RuntimeException("Invalid Message Category");
        }
    }

    /**
     * Called when the bluetooth device is connected.
     */
    protected void onDeviceFound() {
        for (DeviceListener deviceListener : deviceListeners) {
            if (deviceListener.isEnabled()) {
                deviceListener.onDeviceFound();
            }
        }
    }

    /**
     * Called when the bluetooth device connection is lost or the device is not connected.
     */
    protected void onDeviceLost() {
        for (DeviceListener deviceListener : deviceListeners) {
            if (deviceListener.isEnabled()) {
                deviceListener.onDeviceLost();
            }
        }
    }

    /**
     * Power the connected device off.
     */
    public void powerOff() {
        send("[!POWER_OFF]");
    }

    /**
     * Request the battery status.
     */
    public void requestBatteryStatus() {
        send("[!GET_VCC]");
    }

    /**
     * Vibrates the physical device.
     *
     * @param times    Number of pulses.
     * @param duration Duration in milliseconds of the on and off state.
     */
    public void vibrateDevice(int times, int duration) {
        send("[!BLINK][0][" + times + "][2|" + duration + "]");
    }

    /**
     * Sets the touch sensibility of the device's tiles. The value passed is proportional to the
     * sensibility.
     *
     * @param sensibility Integer in the range [0, 16].
     */
    public void setTouchSensibility(int sensibility) {
        send("[!SET_THRESHOLD][" + sensibility + "]");
    }

    /**
     * Send a low level message to the physical device.
     *
     * @param msg Message to be sent.
     */
    public void send(byte[] msg) {
        serviceConnection.sendBTMessage(msg);
    }

    /**
     * Send a low level message to the physical device in a more human readable form.
     *
     * @param msg Message to be sent.
     */
    public void send(String msg) {
        ByteBuffer bbuf = ByteBuffer.allocate(64);
        msg = msg.replace("[", "");
        Map<Integer, Integer> lengthMap = new HashMap<>();
        Map<Integer, Integer> refMap = new HashMap<>();
        Map<String, Byte> cmdMap = new HashMap<>();

        cmdMap.put("PLAY", (byte) 1);
        cmdMap.put("PRINT_CHAR", (byte) 2);
        cmdMap.put("BLINK", (byte) 3);
        cmdMap.put("SEND_AGAIN", (byte) 4);
        cmdMap.put("ENABLE_AUDIO_UPDATE", (byte) 5);
        cmdMap.put("SET_DEBUG_MODE", (byte) 6);
        cmdMap.put("GET_DFA_STATE", (byte) 7);
        cmdMap.put("GET_FREE_RAM", (byte) 8);
        cmdMap.put("GET_VCC", (byte) 9);
        cmdMap.put("READ_PIN", (byte) 10);
        cmdMap.put("EPROM_DUMP", (byte) 11);
        cmdMap.put("POWER_OFF", (byte) 12);
        cmdMap.put("RESET", (byte) 13);
        cmdMap.put("SET_THRESHOLD", (byte) 14);
        int id = 0;

        for (String t : msg.split("]")) {
            try {
                int index = 0;
                if (t.startsWith("!")) {
                    //command
                    bbuf.put(cmdMap.get(t.substring(1).toUpperCase()));
                } else if (t.startsWith("'")) {
                    //string
                    String str = t.substring(1, t.length() - 1);
                    byte[] array = str.getBytes(Charset.forName("UTF-8"));
                    int pos = bbuf.position();
                    bbuf.put(array);
                    bbuf.put((byte) 0);
                    for (int i = 0; i < array.length + 1; i++) {
                        lengthMap.put(pos + i, array.length + 1);
                    }
                    lengthMap.put(id, array.length + 1);
                } else if (t.endsWith("f")) {
                    //float
                    bbuf.putFloat(Float.parseFloat(t));
                } else if (t.endsWith("d")) {
                    //double
                    bbuf.putDouble(Double.parseDouble(t));
                } else if ((index = t.indexOf("%len")) >= 0) {
                    //special
                    if (t.length() == 4) {
                        refMap.put(bbuf.position(), bbuf.position() + 1);
                    } else {
                        int num = Integer.parseInt(t.substring(index + 4));
                        refMap.put(bbuf.position(), bbuf.position() + num);
                    }
                    bbuf.put((byte) 0);

                } else if (t.contains("|")) {
                    //multi byte integer
                    int size = Integer.parseInt(t.substring(0, t.indexOf("|")));
                    int num = Integer.parseInt(t.substring(t.indexOf("|") + 1));
                    byte[] array = ByteBuffer.allocate(4).putInt(num).array();
                    for (int i = 3; i >= size; i--) {
                        bbuf.put(array[i]);
                    }
                } else if (t.contains(",")) {
                    //byte array
                    String[] array = t.split(",");
                    for (String n : array) {
                        bbuf.put((byte) Integer.parseInt(n));
                        lengthMap.put(bbuf.position() - 1, array.length);
                        System.out.println(">" + (bbuf.position() - 1) + " " + array.length);
                    }
                } else {
                    //single byte
                    bbuf.put((byte) Integer.parseInt(t));
                }
                id++;
            } catch (Exception e) {
                System.err.println("Error on " + id + " [" + t + "] : " + e.getMessage());
            }
        }

        byte[] ret = new byte[bbuf.position()];
        bbuf.rewind();
        bbuf.get(ret, 0, ret.length);

        for (int refId : refMap.keySet()) {
            System.out.println("*" + ret[refId] + "  " + ret[refMap.get(refId)] + "(" + refMap.get(refId) + ")");
            Integer i = lengthMap.get(refMap.get(refId));
            ret[refId] = (i == null) ? (byte) -1 : i.byteValue();
        }

        send(ret);
    }

}
