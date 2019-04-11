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

rs_allocation gAnomalousStdDev;

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

    double exp_val_sum    = rsGetElementAt_double(gExposureValueSum, x, y);
    double mean_pixel_val = exp_val_sum / (double) gExposureSum;

    // mean exposure time
    double mean_exposure = gExposureSum / (double) gNframes;

    // mean pixel value / exposure time (nanoseconds)
    double mean_val_per_nanos = mean_pixel_val / mean_exposure;

    rsSetElementAt_float(gMeanRate, (float) mean_val_per_nanos, x, y);

    // Standard Deviation Rate
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    double exp_val2_sum = rsGetElementAt_double(gExposureValue2Sum, x, y);

    double var = ( exp_val2_sum / (double) gExposureSum ) - ( mean_pixel_val * mean_pixel_val );

    // standard deviation / exposure time (nanoseconds)
    float stddev_per_nanos;
    // TODO: figure out what's going on with var
    if (var < 0.) {
        long count = rsGetElementAt_long(gAnomalousStdDev, 0, 0);
        rsSetElementAt_long(gAnomalousStdDev, count + 1, 0, 0);
        stddev_per_nanos = 0.;
    }
    else {
        stddev_per_nanos = sqrt((float) var) / (float) mean_exposure;
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

long RS_KERNEL getAnomalousStdDev(uint32_t x, uint32_t y) {
    return rsGetElementAt_long(gAnomalousStdDev, 0, 0);
}