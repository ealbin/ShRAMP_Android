package edu.crayfis.shramp.camera2.settings;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.support.annotation.NonNull;

abstract class Level5_AWB extends Level4_Intent {


    //**********************************************************************************************
    // Class Variables
    //----------------


    //**********************************************************************************************
    // Class Methods
    //--------------

    protected Level5_AWB(@NonNull CameraCharacteristics characteristics,
                            @NonNull CameraDevice cameraDevice) {
        super(characteristics, cameraDevice);
    }


    /**
     *
     * @return
     */
    @NonNull
    public String toString() {
        String string = super.toString() + "\n";

        return string;
    }

}
