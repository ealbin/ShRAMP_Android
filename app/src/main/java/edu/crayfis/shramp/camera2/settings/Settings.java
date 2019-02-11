package edu.crayfis.shramp.camera2.settings;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.support.annotation.NonNull;

import edu.crayfis.shramp.logging.ShrampLogger;

public final class Settings extends Level13_Lens {

    // logging
    static ShrampLogger mLogger = new ShrampLogger(ShrampLogger.DEFAULT_STREAM);

    public Settings(@NonNull CameraCharacteristics characteristics,
                                  @NonNull CameraDevice cameraDevice) {
        super(characteristics, cameraDevice);

        mLogger.log(super.toString());
    }

}
