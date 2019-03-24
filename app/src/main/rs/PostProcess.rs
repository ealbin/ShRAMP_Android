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


double RS_KERNEL getMeanRate(uint32_t x, uint32_t y) {

    // mean pixel value
    double exp_val_sum    = rsGetElementAt_double(gExposureValueSum, x, y);
    double mean_pixel_val = exp_val_sum / (double) gExposureSum;

    // mean exposure time
    double mean_exposure = gExposureSum / (double) gNframes;

    // mean pixel value / exposure time (nanoseconds)
    double mean_val_per_nanos = mean_pixel_val / (double) mean_exposure;

    rsSetElementAt_double(gMeanRate, mean_val_per_nanos, x, y);

    //----------

    double exp_val2_sum = rsGetElementAt_double(gExposureValue2Sum, x, y);

    double var = ( exp_val2_sum / (double) gExposureSum ) - ( mean_pixel_val * mean_pixel_val );

    // standard deviation / exposure time (nanoseconds)
    double stddev_per_nanos = sqrt((float) var) / mean_exposure;

    rsSetElementAt_double(gStdDevRate, stddev_per_nanos, x, y);

    //-----------

    double stderr_per_nanos = stddev_per_nanos / (double) sqrt((float) gNframes);

    rsSetElementAt_double(gStdErrRate, stderr_per_nanos, x, y);

    //-----------

    return mean_val_per_nanos;
}

double RS_KERNEL getStdDevRate(uint32_t x, uint32_t y) {
    return rsGetElementAt_double(gStdDevRate, x, y);
}

double RS_KERNEL getStdErrRate(uint32_t x, uint32_t y) {
    return rsGetElementAt_double(gStdErrRate, x, y);
}