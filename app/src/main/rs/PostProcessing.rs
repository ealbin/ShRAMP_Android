#pragma version(1)
#pragma rs java_package_name(sci.crayfis.shramp)
#pragma rs_fp_full
//#pragma rs_fp_relaxed
//#include "rs_debug.rsh"

// Total number of image frames
long gNframes;

// Total pixel exposure time in seconds
long gExposureSum;

rs_allocation gExposureValueSum;
rs_allocation gExposureValue2Sum;

// Average pixel value / seconds exposure
rs_allocation gMeanRate;

// Standard deviation on the mean / seconds exposure
rs_allocation gStdDevRate;

rs_allocation gStdErrRate;


float RS_KERNEL getMeanRate(uint32_t x, uint32_t y) {

    // Mean Pixel Rate
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    float exp_val_sum    = rsGetElementAt_float(gExposureValueSum, x, y);
    float mean_pixel_val = exp_val_sum / (float) gExposureSum;

    // mean exposure time
    float mean_exposure = gExposureSum / (float) gNframes;

    // mean pixel value / exposure time (nanoseconds)
    float mean_val_per_nanos = mean_pixel_val / (float) mean_exposure;

    rsSetElementAt_float(gMeanRate, mean_val_per_nanos, x, y);

    // Standard Deviation Rate
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    float exp_val2_sum = rsGetElementAt_float(gExposureValue2Sum, x, y);

    float var = ( exp_val2_sum / (float) gExposureSum ) - ( mean_pixel_val * mean_pixel_val );

    // standard deviation / exposure time (nanoseconds)
    float stddev_per_nanos;
    // TODO: figure out what's going on with var
    if (var < 0.) {
        stddev_per_nanos = sqrt((float) mean_pixel_val) / mean_exposure;
    }
    else {
        stddev_per_nanos = sqrt((float) var) / mean_exposure;
    }

    rsSetElementAt_float(gStdDevRate, stddev_per_nanos, x, y);

    // Standard Error Rate
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    float stderr_per_nanos = stddev_per_nanos / (float) sqrt((float) gNframes);

    rsSetElementAt_float(gStdErrRate, stderr_per_nanos, x, y);

    //----------------------------------------------------------------------------------------------

    return mean_val_per_nanos;
}

float RS_KERNEL getStdDevRate(uint32_t x, uint32_t y) {
    return rsGetElementAt_float(gStdDevRate, x, y);
}

float RS_KERNEL getStdErrRate(uint32_t x, uint32_t y) {
    return rsGetElementAt_float(gStdErrRate, x, y);
}