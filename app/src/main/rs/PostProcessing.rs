//******************************************************************************
//                                                                             *
// @project: (Sh)ower (R)econstructing (A)pplication for (M)obile (P)hones     *
// @version: ShRAMP v0.0                                                       *
//                                                                             *
// @objective: To detect extensive air shower radiation using smartphones      *
//             for the scientific study of ultra-high energy cosmic rays       *
//                                                                             *
// @institution: University of California, Irvine                              *
// @department:  Physics and Astronomy                                         *
//                                                                             *
// @author: Eric Albin                                                         *
// @email:  Eric.K.Albin@gmail.com                                             *
//                                                                             *
// @updated: 25 March 2019                                                     *
//                                                                             *
//******************************************************************************

#pragma version(1)
#pragma rs java_package_name(sci.crayfis.shramp)
#pragma rs_fp_full
//#pragma rs_fp_relaxed
//#include "rs_debug.rsh"

// Global Variables
//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

// gNframes.........................................................................................
// Total number of image frames
long gNframes;

// gExposureSum.....................................................................................
// Total pixel exposure time in seconds
long gExposureSum;

// Running Sums.....................................................................................
// TODO: description
rs_allocation gExposureValueSum;
rs_allocation gExposureValue2Sum;

// Statistics.......................................................................................
// gMeanRate:   average pixel value / seconds exposure
// gStdDevRate: standard deviation on the mean / seconds exposure
// gStdErrRate: TODO
rs_allocation gMeanRate;
rs_allocation gStdDevRate;
rs_allocation gStdErrRate;

// RenderScript Kernels
//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

// getMeanRate......................................................................................
// TODO: description, comments and logging
// @param x bla
// @param y bla
// @return bla
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

// getStdDevRate....................................................................................
// TODO: description, comments and logging
// @param x bla
// @param y bla
// @return bla
float RS_KERNEL getStdDevRate(uint32_t x, uint32_t y) {
    return rsGetElementAt_float(gStdDevRate, x, y);
}

// getStdErrRate....................................................................................
// TODO: description, comments and logging
// @param x bla
// @param y bla
// @return bla
float RS_KERNEL getStdErrRate(uint32_t x, uint32_t y) {
    return rsGetElementAt_float(gStdErrRate, x, y);
}