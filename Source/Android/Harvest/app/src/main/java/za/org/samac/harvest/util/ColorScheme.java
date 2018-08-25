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

    static float asciiColorHash(String string) {
        int hash = 15487469;
        for (int i = 0; i < string.length(); i++) {
            int c = string.codePointAt(i);
            hash = hash * 1301081 + c;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return ((float)Integer.toUnsignedLong(hash)) / (float)4294967295.0;
        } else {
            return (float)((long)hash) / (float)4294967295.0;
        }
    }
}
