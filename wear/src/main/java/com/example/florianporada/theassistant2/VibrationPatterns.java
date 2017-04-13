package com.example.florianporada.theassistant2;

import android.os.Vibrator;

/**
 * Created by Iantje on 13/04/2017.
 */

public class VibrationPatterns {
    public void ConvertMessageToVibrations(Vibrator v, String message, long vibrationDelay) {
        long vibratePattern[] = new long[message.length() * 2 + 1];//Make an array of message length to store the vibration pattern
        vibratePattern[0] = 0;//Start with a 0 delay

        for(int i = 0; i < message.length(); i++) {
            int vibrationTime = 0;//Vibration time for this character

            switch(message.charAt(i)) {
                case 's' | 'S'://Short
                    vibrationTime = 200;
                    break;
                case 'm' | 'M'://Medium
                    vibrationTime = 500;
                    break;
                case 'l' | 'L'://Long
                    vibrationTime = 1000;
                    break;
                default:
                    continue;
            }

            vibratePattern[i * 2 + 1] = vibrationTime;//Add the length of vibration
            vibratePattern[i * 2 + 2] = vibrationDelay;//Delay the vibrations
        }

        v.vibrate(vibratePattern, -1);//Vibrate according to generated pattern
    }

    //Just an overload for easier use
    public void ConvertMessageToVibrations(Vibrator v, String message) {
        ConvertMessageToVibrations(v, message, 200);
    }
}
