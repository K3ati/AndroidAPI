package com.tacttiles.api;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by andy on 17/04/17.
 */

public class Glove {

    public abstract static class GloveListener extends Device.DeviceListener {
        public void onLetterDraw(int index, char[] str) {
        }

        public void onButtonPressed(int time) {
        }

        public void onLetterReceived(char c){

        }
    }

    private static int[] map = {9, 12, 7, 4, 1, 10, 13, 8, 5, 2, 11, 14, 15, 6, 3, -9, -12, -7, -4, -1, -11, -14, -16, -15, -6, -3};
    private static char spaceChar = 'w';
    private static int delayL = 300;
    private static int delayW = 300;
    private static int singleH = 150;
    private static int doubleH = 150;
    private static int doubleL = 100;


    private List<GloveListener> gloveListeners;
    private Device device;
    private char[] drawing = null;
    private int drawingIndex = 0;
    private boolean nextLetter;

    public Glove() {
        device = new Device();
        gloveListeners = new ArrayList<>();
    }

    public Device getDevice(){
        return device;
    }

    public void addListener(GloveListener gloveListener) {
        device.addListener(gloveListener);
        gloveListeners.add(gloveListener);
    }

    public void removeListener(GloveListener gloveListener) {
        device.removeListener(gloveListener);
        gloveListeners.remove(gloveListener);
    }

    public void removeAllListeners() {
        gloveListeners.clear();
    }

    /**
     * @param delayL  delay in ms between letters, default = 600 ms
     * @param delayW  delay in ms between words, default = 600 ms
     * @param singleH single vibration duration in ms, default = 150 ms
     * @param doubleH vibration duration in ms for double tap letters, default = 150 ms
     * @param doubleL delay between vibrations in double tap letters, default = 100 ms
     */
    public void setup(int delayL, int delayW, int singleH, int doubleH, int doubleL) {
        this.delayL = delayL;
        this.delayW = delayW;
        this.singleH = singleH;
        this.doubleH = doubleH;
        this.doubleL = doubleL;
    }

    public void connect(Context appContext) {
        device.addListener(new Device.DeviceListener() {
            @Override
            public void onDeviceFound() {
                //TODO set debug mode 2
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
     * @param spaceChar new space character, default = 'w'
     */
    public void setSpaceChar(char spaceChar) {
        this.spaceChar = spaceChar;
    }

    public void setGloveIOMap(int[] map) {
        this.map = map;
    }

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
     * @param letter a char in the range [97,122] or space ' '
     * @return array of bytes containing the raw commands for drawing the letter
     */
    protected String buildLetterGestureMessage(char letter) {
        if (letter == ' ') {
            return "[!PLAY][%len][1,0," + delayL + "]";
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
