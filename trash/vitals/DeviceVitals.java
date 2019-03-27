package recycle_bin.vitals;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.HardwarePropertiesManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.List;

import sci.crayfis.shramp.util.NumToString;

@TargetApi(21)
public abstract class DeviceVitals {

    private static HardwarePropertiesManager mHardwarePropertiesManager;
    private static final List<Integer> mTypes   = new ArrayList<>();
    private static final List<Integer> mSources = new ArrayList<>();

    public static void initialize(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
            mHardwarePropertiesManager = (HardwarePropertiesManager) activity.getSystemService(Context.HARDWARE_PROPERTIES_SERVICE);

            mTypes.add(HardwarePropertiesManager.DEVICE_TEMPERATURE_SKIN);
            mTypes.add(HardwarePropertiesManager.DEVICE_TEMPERATURE_BATTERY);
            mTypes.add(HardwarePropertiesManager.DEVICE_TEMPERATURE_CPU);
            mTypes.add(HardwarePropertiesManager.DEVICE_TEMPERATURE_GPU);

            mSources.add(HardwarePropertiesManager.TEMPERATURE_CURRENT);
            mSources.add(HardwarePropertiesManager.TEMPERATURE_THROTTLING);
            mSources.add(HardwarePropertiesManager.TEMPERATURE_SHUTDOWN);
            mSources.add(HardwarePropertiesManager.TEMPERATURE_THROTTLING_BELOW_VR_MIN);
        }
    }

    @TargetApi(24)
    @Contract(pure = true)
    @Nullable
    public static String getTemperatures() {
        if (mHardwarePropertiesManager == null) {
            return null;
        }

        int maxlen = "Unknown type throttling VR min temperature threshold:".length();

        String out = " \n";
        for (Integer type : mTypes) {
            String typeString = getTypeString(type);

            for (Integer source : mSources) {
                String sourceString = getSourceString(source);

                String text = typeString + " " + sourceString + ":";
                for (int i = text.length(); i < maxlen; i++ ) {
                    text += " ";
                }

                float[] temps = mHardwarePropertiesManager.getDeviceTemperatures(type, source);
                String tempString = "  ";
                for (float temp : temps) {
                    tempString += NumToString.decimal(temp) + "  ";
                }

                out += "\t" + text + tempString + "[Celsius]\n";
            }
        }

        return out;
    }

    @TargetApi(24)
    @Contract(pure = true)
    @NonNull
    private static String getTypeString(int type) {
        switch (type) {
            case (HardwarePropertiesManager.DEVICE_TEMPERATURE_SKIN): {
                return "Skin";
            }
            case (HardwarePropertiesManager.DEVICE_TEMPERATURE_BATTERY): {
                return "Battery";
            }
            case (HardwarePropertiesManager.DEVICE_TEMPERATURE_CPU): {
                return "CPU";
            }
            case (HardwarePropertiesManager.DEVICE_TEMPERATURE_GPU): {
                return "GPU";
            }
            default:
                return "Unknown type";
        }
    }

    @TargetApi(24)
    @Contract(pure = true)
    @NonNull
    private static String getSourceString(int source) {
        switch (source) {
            case (HardwarePropertiesManager.TEMPERATURE_CURRENT): {
                return "current temperature";
            }
            case (HardwarePropertiesManager.TEMPERATURE_THROTTLING): {
                return "throttling temperature threshold";
            }
            case (HardwarePropertiesManager.TEMPERATURE_SHUTDOWN): {
                return "shutdown temperature threshold";
            }
            case (HardwarePropertiesManager.TEMPERATURE_THROTTLING_BELOW_VR_MIN): {
                return "throttling VR min temperature threshold";
            }
            default:
                return "unknown temperature source";
        }
    }

}