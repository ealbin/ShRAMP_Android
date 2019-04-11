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

const float gMax8bitValue  = 255.;
const float gMax16bitValue = 1023.;

int gIs8bit;

// gNframes.........................................................................................
// Total number of image frames
long gNframes;

// gExposureSum.....................................................................................
// Total pixel exposure time in seconds
//long gExposureSum;

// Running Sums.....................................................................................
// TODO: description
rs_allocation gValueSum;
rs_allocation gValue2Sum;

// Statistics.......................................................................................
// gMean:   average pixel value
// gStdDev: standard deviation on the mean
// gStdErr: TODO
rs_allocation gMean;
rs_allocation gStdDev;
rs_allocation gStdErr;

rs_allocation gAnomalousStdDev;

// RenderScript Kernels
//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

// getMean......................................................................................
// TODO: description, comments and logging
// @param x bla
// @param y bla
// @return bla
float RS_KERNEL getMean(uint32_t x, uint32_t y) {

    float maxValue = gMax8bitValue;
    if (gIs8bit == 0) {
        maxValue = gMax16bitValue;
    }

    // Mean Pixel 
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    uint val_sum = rsGetElementAt_uint(gValueSum, x, y);
    double mean_pixel_val = val_sum / (double) gNframes;

    rsSetElementAt_float(gMean, (float) mean_pixel_val / maxValue, x, y);

    // Standard Deviation 
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    uint val2_sum = rsGetElementAt_uint(gValue2Sum, x, y);

    double var = ( val2_sum / (double) gNframes) - ( mean_pixel_val * mean_pixel_val );

    // standard deviation
    float stddev_per_nanos;
    if (var < 0.) {
        long count = rsGetElementAt_long(gAnomalousStdDev, 0, 0);
        rsSetElementAt_long(gAnomalousStdDev, count + 1, 0, 0);
        stddev_per_nanos = 0.;
    }
    else {
        stddev_per_nanos = sqrt((float) var) / maxValue;
    }

    rsSetElementAt_float(gStdDev, stddev_per_nanos, x, y);

    // Standard Error 
    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

    float stderr_per_nanos = stddev_per_nanos / sqrt((float) gNframes);

    rsSetElementAt_float(gStdErr, stderr_per_nanos, x, y);

    //----------------------------------------------------------------------------------------------

    return mean_pixel_val / maxValue;
}

// getStdDev........................................................................................
// TODO: description, comments and logging
// @param x bla
// @param y bla
// @return bla
float RS_KERNEL getStdDev(uint32_t x, uint32_t y) {
    return rsGetElementAt_float(gStdDev, x, y);
}

// getStdErr........................................................................................
// TODO: description, comments and logging
// @param x bla
// @param y bla
// @return bla
float RS_KERNEL getStdErr(uint32_t x, uint32_t y) {
    return rsGetElementAt_float(gStdErr, x, y);
}

long RS_KERNEL getAnomalousStdDev(uint32_t x, uint32_t y) {
    return rsGetElementAt_long(gAnomalousStdDev, 0, 0);
}