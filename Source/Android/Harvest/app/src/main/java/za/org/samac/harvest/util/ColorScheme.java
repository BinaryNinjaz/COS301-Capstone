package za.org.samac.harvest.util;

import android.graphics.Color;
import android.os.Build;

public class ColorScheme {
    static public int hashColor(String parent, String child, int alpha) {
        float hueRatio = asciiColorHash(parent) * 360;
        float satRatio = asciiColorHash(child.substring(0, child.length() / 2)) * (float)0.5 + (float)0.5;
        float briRatio = asciiColorHash(child.substring(child.length() / 2, child.length() - 1)) * (float)0.33 + (float)0.66;

        return Color.HSVToColor(alpha, new float[]{ hueRatio, satRatio, briRatio });
    }

    static public int hashColorOnce(String key) {
        return hashColor(key, key, 255);
    }

    static float asciiColorHash(String string) {
        int hash = 15487469;
        for (int i = 0; i < string.length(); i++) {
            int c = string.codePointAt(i);
            hash = hash * 1301081 + c;
        }
        return Math.abs(hash) / (float)2147483647.0;
    }
}
