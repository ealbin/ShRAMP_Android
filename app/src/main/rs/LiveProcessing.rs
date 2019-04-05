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

const double gMax8bitValue  = 255.;
const double gMax16bitValue = 1023.;

// gEnableSignificance..............................................................................
// TODO: description
int gEnableSignificance;

float gSignificanceThreshold;
rs_allocation gCountAboveThreshold;

// gExposureTime....................................................................................
// TODO: description
long gExposureTime;

// Running Sums.....................................................................................
// TODO: description
rs_allocation gExposureValueSum;
rs_allocation gExposureValue2Sum;

// Statistics.......................................................................................
// TODO: description
rs_allocation gMeanRate;
rs_allocation gStdDevRate;
rs_allocation gSignificance;

// RenderScript Kernels
//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

// process8bitData..................................................................................
// TODO: description, comments and logging
// @param val bla
// @param x bla
// @param y bla
void RS_KERNEL process8bitData(uchar val, uint32_t x, uint32_t y) {
    double val_fraction = val / gMax8bitValue;
    double old_exp_val_sum = rsGetElementAt_double(gExposureValueSum, x, y);
    double this_exp_val    = (double) gExposureTime * val_fraction;
    double new_exp_val_sum = (double) (old_exp_val_sum + this_exp_val);
    rsSetElementAt_double(gExposureValueSum, new_exp_val_sum, x, y);

    double old_exp_val2_sum = rsGetElementAt_double(gExposureValue2Sum, x, y);
    double this_exp_val2    = (double) this_exp_val * val_fraction;
    double new_exp_val2_sum = (double) (old_exp_val2_sum + this_exp_val2);
    rsSetElementAt_double(gExposureValue2Sum, new_exp_val2_sum, x, y);

    float mean_rate    = rsGetElementAt_float(gMeanRate, x, y);
    float stddev_rate  = rsGetElementAt_float(gStdDevRate, x, y);

    float significance;
    if (gEnableSignificance == 0) {
        significance = 0.f;
    }
    else if (stddev_rate == 0.f) {
        significance = 1./0.;
    }
    else {
        significance = (float) ( ( val_fraction / (double) gExposureTime ) - (double) mean_rate ) / stddev_rate;
        if (significance >= gSignificanceThreshold) {
            long count = rsGetElementAt_long(gCountAboveThreshold, 0, 0);
            rsSetElementAt_long(gCountAboveThreshold, count + 1, 0, 0);
        }
    }
    rsSetElementAt_float(gSignificance, significance, x, y);
}

// process16bitData.................................................................................
// TODO: description, comments and logging
// @param val bla
// @param x bla
// @param y bla
void RS_KERNEL process16bitData(ushort val, uint32_t x, uint32_t y) {
    double val_fraction = val / gMax16bitValue;
    double old_exp_val_sum = rsGetElementAt_double(gExposureValueSum, x, y);
    double this_exp_val    = (double) gExposureTime * val_fraction;
    double new_exp_val_sum = (double) (old_exp_val_sum + this_exp_val);
    rsSetElementAt_double(gExposureValueSum, new_exp_val_sum, x, y);

    double old_exp_val2_sum = rsGetElementAt_double(gExposureValue2Sum, x, y);
    double this_exp_val2    = (double) this_exp_val * val_fraction;
    double new_exp_val2_sum = (double) (old_exp_val2_sum + this_exp_val2);
    rsSetElementAt_double(gExposureValue2Sum, new_exp_val2_sum, x, y);

    float mean_rate    = rsGetElementAt_float(gMeanRate, x, y);
    float stddev_rate  = rsGetElementAt_float(gStdDevRate, x, y);

    float significance;
    if (gEnableSignificance == 0) {
        significance = 0.f;
    }
    else if (stddev_rate == 0.f) {
        significance = 1./0.;
    }
    else {
        significance = (float) ( ( val_fraction / (double) gExposureTime ) - (double) mean_rate ) / stddev_rate;
        if (significance >= gSignificanceThreshold) {
            long count = rsGetElementAt_long(gCountAboveThreshold, 0, 0);
            rsSetElementAt_long(gCountAboveThreshold, count + 1, 0, 0);
        }
    }
    rsSetElementAt_float(gSignificance, significance, x, y);
}

// zeroDoubleAllocation.............................................................................
// TODO: description, comments and logging
// @param x bla
// @param y bla
// @return bla
double RS_KERNEL zeroDoubleAllocation(uint32_t x, uint32_t y) {
    return 0.;
}

// getExposureValueSum..............................................................................
// TODO: description, comments and logging
// @param x bla
// @param y bla
// @return bla
double RS_KERNEL getExposureValueSum(uint32_t x, uint32_t y) {
    return rsGetElementAt_double(gExposureValueSum, x, y);
}

// getExposureValue2Sum.............................................................................
// TODO: description, comments and logging
// @param x bla
// @param y bla
// @return bla
double RS_KERNEL getExposureValue2Sum(uint32_t x, uint32_t y) {
    return rsGetElementAt_double(gExposureValue2Sum, x, y);
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