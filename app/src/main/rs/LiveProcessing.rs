//
// @project: (Sh)ower (R)econstructing (A)pplication for (M)obile (P)hones
// @version: ShRAMP v0.0
//
// @objective: To detect extensive air shower radiation using smartphones
//             for the scientific study of ultra-high energy cosmic rays
//
// @institution: University of California, Irvine
// @department:  Physics and Astronomy
//
// @author: Eric Albin
// @email:  Eric.K.Albin@gmail.com
//
// @updated: 15 April 2019
//

#pragma version(1)
#pragma rs java_package_name(sci.crayfis.shramp)
#pragma rs_fp_full
//#pragma rs_fp_relaxed
//#include "rs_debug.rsh"

// Global Variables
//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

const double gMax8bitValue  = 255.;
const double gMax16bitValue = 1023.;

// gEnableSignificance..............................................................................
// TODO: description
int gEnableSignificance;

float gSignificanceThreshold;
rs_allocation gCountAboveThreshold;

// gExposureTime....................................................................................
// TODO: description
//long gExposureTime;

// Running Sums.....................................................................................
// TODO: description
rs_allocation gValueSum;
rs_allocation gValue2Sum;

// Statistics.......................................................................................
// TODO: description
rs_allocation gMean;
rs_allocation gStdDev;
rs_allocation gSignificance;

// RenderScript Kernels
//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

// process8bitData..................................................................................
// TODO: description, comments and logging
// @param val bla
// @param x bla
// @param y bla
void RS_KERNEL process8bitData(uchar val, uint32_t x, uint32_t y) {
    uint old_val_sum = rsGetElementAt_uint(gValueSum, x, y);
    uint this_val    = (uint) val;
    uint new_val_sum = old_val_sum + this_val;
    rsSetElementAt_uint(gValueSum, new_val_sum, x, y);

    uint old_val2_sum = rsGetElementAt_uint(gValue2Sum, x, y);
    uint this_val2    = this_val * this_val;
    uint new_val2_sum = old_val2_sum + this_val2;
    rsSetElementAt_uint(gValue2Sum, new_val2_sum, x, y);

    //float mean_rate    = rsGetElementAt_float(gMeanRate, x, y);
    //float stddev_rate  = rsGetElementAt_float(gStdDevRate, x, y);

    //float significance;
    //if (gEnableSignificance == 0) {
    //    significance = 0.f;
    //}
    //else if (stddev_rate == 0.f) {
    //    significance = 1./0.;
    //}
    //else {
    //    significance = (float) ( ( val_fraction / (double) gExposureTime ) - (double) mean_rate ) / stddev_rate;
    //    if (significance >= gSignificanceThreshold) {
    //        long count = rsGetElementAt_long(gCountAboveThreshold, 0, 0);
    //        rsSetElementAt_long(gCountAboveThreshold, count + 1, 0, 0);
    //    }
    //}
    //rsSetElementAt_float(gSignificance, significance, x, y);
}

// process16bitData.................................................................................
// TODO: description, comments and logging
// @param val bla
// @param x bla
// @param y bla
void RS_KERNEL process16bitData(ushort val, uint32_t x, uint32_t y) {
    uint old_val_sum = rsGetElementAt_uint(gValueSum, x, y);
    uint this_val    = (uint) val;
    uint new_val_sum = old_val_sum + this_val;
    rsSetElementAt_uint(gValueSum, new_val_sum, x, y);

    uint old_val2_sum = rsGetElementAt_uint(gValue2Sum, x, y);
    uint this_val2    = this_val * this_val;
    uint new_val2_sum = old_val2_sum + this_val2;
    rsSetElementAt_uint(gValue2Sum, new_val2_sum, x, y);
}

// zeroDoubleAllocation.............................................................................
// TODO: description, comments and logging
// @param x bla
// @param y bla
// @return bla
double RS_KERNEL zeroDoubleAllocation(uint32_t x, uint32_t y) {
    return 0.;
}

uint RS_KERNEL zeroUIntAllocation(uint32_t x, uint32_t y) {
    return 0;
}

// getValueSum......................................................................................
// TODO: description, comments and logging
// @param x bla
// @param y bla
// @return bla
uint RS_KERNEL getValueSum(uint32_t x, uint32_t y) {
    return rsGetElementAt_uint(gValueSum, x, y);
}

// getValue2Sum.....................................................................................
// TODO: description, comments and logging
// @param x bla
// @param y bla
// @return bla
uint RS_KERNEL getValue2Sum(uint32_t x, uint32_t y) {
    return rsGetElementAt_uint(gValue2Sum, x, y);
}

// getSignificance..................................................................................
// TODO: description, comments and logging
// @param x bla
// @param y bla
// @return bla
float RS_KERNEL getSignificance(uint32_t x, uint32_t y) {
    return rsGetElementAt_float(gSignificance, x, y);
}

// getCountAboveThreshold...........................................................................
// TODO: description, comments and logging
// @param x bla
// @param y bla
long RS_KERNEL getCountAboveThreshold(uint32_t x, uint32_t y) {
    return rsGetElementAt_long(gCountAboveThreshold, 0, 0);
}