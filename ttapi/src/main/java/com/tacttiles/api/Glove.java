package com.tacttiles.api;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * A Glove is a hardware abstraction that handles the low level communication protocol used in devices compatibles with the HELENA Glove platform.
 * <p>
 * In this class gestures are simplified to letters, which may be composed of a single/double tap (input) or vibration (output).
 * <p>
 * High level user-interaction events can be handled by a custom implementation and register of the inner static class {@link GloveListener}.
 */

public class Glove {

    /**
     * A glove listener receives notifications of new data available in the {@link Glove} object. Notifications indicate user interaction related events, such as receiving a button press or a input gesture.
     * This class can also handle low level system messages, such as battery status.
     */
    public abstract static class GloveListener extends Device.DeviceListener {
        /**
         * Called after each letter of a text was drawn by calling {@link #draw(String)}  draw}.
         *
         * @param index Index of the letter.
         * @param str   Text being drawn.
         */
        public void onLetterDraw(int index, char[] str) {
        }

        /**
         * Called when the glove button is pressed.
         *
         * @param duration Duration of the button press.
         */
        public void onButtonPressed(int duration) {
        }

        /**
         * Called when a received gesture is compatible with singe and double tap codded symbols.
         * Otherwise it can be handled by {@link com.tacttiles.api.Device.DeviceListener#onGestureReceived(String, String) DeviceListener.onGestureReceived()}.
         *
         * @param c The letter received.
         */
        public void onLetterReceived(char c) {

        }
    }

    private static int[] map = {9, 12, 7, 4, 1, 10, 13, 8, 5, 2, 11, 14, 15, 6, 3, -9, -12, -7, -4, -1, -11, -14, -16, -15, -6, -3};
    private static char spaceChar = 'w';
    private static int delayL = 250;
    private static int delayW = 250;
    private static int singleH = 150;
    private static int doubleH = 150;
    private static int doubleL = 100;


    private List<GloveListener> gloveListeners;
    private Device device;
    private char[] drawing = null;
    private int drawingIndex = 0;
    private boolean nextLetter;

    /**
     * Constructs a glove adapter.
     */
    public Glove() {
        device = new Device();
        gloveListeners = new ArrayList<>();
    }

    /**
     * Returns the core device of this class. It can be used to access lower level control over
     * the physical device that this adapter class is connected to.
     *
     * @return The core device
     */
    public Device getDevice() {
        return device;
    }

    /**
     * Adds a listener to the current set of event listeners of this adapter. Events can be button presses or I/O letter gestures.
     *
     * @param gloveListener The listener to be added to the current set of listeners on this adapter.
     */
    public void addListener(GloveListener gloveListener) {
        device.addListener(gloveListener);
        gloveListeners.add(gloveListener);
    }

    /**
     * Removes a listener from the set listening to this adapter.
     *
     * @param gloveListener The listener to be removed to the current set of listeners on this adapter.
     */
    public void removeListener(GloveListener gloveListener) {
        device.removeListener(gloveListener);
        gloveListeners.remove(gloveListener);
    }

    /**
     * Set the default values used while drawing the letter gestures.
     *
     * @param delayL  Delay in ms between letters, default = 250 ms.
     * @param delayW  Delay in ms between words, default = 250 ms.
     * @param singleH Single vibration duration in ms, default = 150 ms.
     * @param doubleH Vibration duration in ms for double tap letters, default = 150 ms.
     * @param doubleL Delay between vibrations in double tap letters, default = 100 ms.
     */
    public void setup(int delayL, int delayW, int singleH, int doubleH, int doubleL) {
        this.delayL = delayL;
        this.delayW = delayW;
        this.singleH = singleH;
        this.doubleH = doubleH;
        this.doubleL = doubleL;
    }

    /**
     * Connects this adapter to the Tact-Tiles runtime service,
     * which handles the bluetooth communication and device access.
     *
     * @param appContext The current context of this application.
     */
    public void connect(Context appContext) {
        device.addListener(new Device.DeviceListener() {
            @Override
            public void onDeviceFound() {
                device.send("[!SET_DEBUG_MODE][2][!SET_THRESHOLD][6]");
            }

            @Override
            public void onSystemMessage(String type, String args) {
                if (type.equals(Device.S_DRAW_FINISHED)) {
                    for (GloveListener gloveListener : gloveListeners) {
                        if (gloveListener.isEnabled()) {
                            gloveListener.onLetterDraw(drawingIndex, drawing);
                        }
                    }
                    nextLetter = true;
                }
            }

            @Override
            public void onGestureReceived(String type, String args) {
                int symbol;
                switch (type) {
                    case Device.G_ID:
                        //TODO DFA
                        break;
                    case Device.G_SINGLE_TOUCH:
                        symbol = Integer.parseInt(args);
                        for (GloveListener gloveListener : gloveListeners) {
                            if (gloveListener.isEnabled()) {
                                gloveListener.onLetterReceived(getLetter(symbol));
                            }
                        }
                        break;
                    case Device.G_DOUBLE_TOUCH:
                        symbol = -Integer.parseInt(args);
                        for (GloveListener gloveListener : gloveListeners) {
                            if (gloveListener.isEnabled()) {
                                gloveListener.onLetterReceived(getLetter(symbol));
                            }
                        }
                        break;
                    case Device.G_BUTTON_PRESS:
                        for (GloveListener gloveListener : gloveListeners) {
                            if (gloveListener.isEnabled()) {
                                gloveListener.onButtonPressed(0);
                            }
                        }
                        break;
                }
            }
        });

        device.connect(appContext);
    }

    /**
     * The glove I/O map is a array containing the tile indexes for each of the 26 letters of the alphabet.
     * The sign of the numbers in the array represents the type of gesture, single tap gestures are positive
     * and double tap gestures are negative integers.
     *
     * @param map An array containing the tile indexes for each letter of the alphabet.
     */
    public void setGloveIOMap(int[] map) {
        this.map = map;
    }

    /**
     * Set the space character as the inverse gesture of the passed letter. If the input letter
     * gesture is a single tap, the space char input becomes the double tap of the said tile, and vice versa.
     *
     * @param spaceChar The new space character, default is 'w'.
     */
    public void setSpaceChar(char spaceChar) {
        this.spaceChar = spaceChar;
    }

    /**
     * Converts a received symbol into a letter. The symbol follows the map logic, in which a positive
     * integer codes a single tap gesture and a negative, a double tap one.
     *
     * @param symbol Symbol coded as a positive or negative tile index.
     * @return The letter as set by the I/O map.
     */
    public char getLetter(int symbol) {
        if (symbol == -map[spaceChar - 97]) {
            return ' ';
        }
        for (int i = 0; i < map.length; i++) {
            if (map[i] == symbol) {
                return (char) (i + 97);
            }
        }
        return 0;
    }

    /**
     * Returns the index of the tile that is used as input for the passed letter.
     *
     * @param letter A character in the range of [a - z] or space ' '.
     * @return The index of the I/O tile.
     */
    public int getTileId(char letter) {
        if (letter >= 97 && letter <= 122) {
            int tile = map[(int) letter - 97];
            return tile < 0 ? -tile : tile;
        } else if (letter == spaceChar) {
            return getTileId(spaceChar);
        }
        return -1;
    }

    /**
     * Constructs a gesture for the passed letter. The gesture is coded as a string compatible with {@link Device#send(String)}.
     *
     * @param letter a char in the range [97,122] or space ' '.
     * @return array of bytes containing the raw commands for drawing the letter.
     */
    protected String buildLetterGestureMessage(char letter) {
        if (letter == ' ') {
            return "[!PLAY][%len][1,0," + delayW + ",7]";
        } else {
            if (letter >= 97 && letter <= 122) {
                int tile = map[(int) letter - 97];

                if (tile >= 0) {
                    tile--;
                    return "[!PLAY][%len][2,1," + tile + ",1,0," + singleH + ",5,7]";
                } else {
                    tile = -tile;
                    tile--;
                    return "[!PLAY][%len][2,1," + tile + ",1,0," + doubleH + ",5,1,0," + doubleL + ",2,1," + tile + ",1,0," + doubleH + ",5,7]";
                }
            }
        }
        throw new IllegalArgumentException("Argument letter ('" + letter + "') is not in the range [97,122].");
    }

    /**
     * Draws a text as a series of vibration gestures. Each letter can be converted into a single or double tap output gesture,
     * which is sent trough a series of adapters and results in the calling of {@link GloveListener#onLetterDraw(int, char[])}, if everything goes as planed.
     *
     * @param text the text to be drawn.
     * @return a non started Thread object that performs the drawing.
     */
    public Thread draw(String text) {
        drawing = text.toCharArray();
        drawingIndex = 0;
        Thread drawingThread = new Thread("Drawing Thread") {
            @Override
            public void run() {
                for (char c : drawing) {
                    device.send(buildLetterGestureMessage(drawing[drawingIndex]));
                    nextLetter = false;
                    while (!nextLetter) {
                        try {
                            Thread.sleep(100);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    drawingIndex++;
                }
            }
        };
        return drawingThread;
    }
}
