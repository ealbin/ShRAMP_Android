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
// @updated: 24 April 2019
//

#pragma version(1)
#pragma rs java_package_name(sci.crayfis.shramp)

// TODO: check if there is substantial performance increase with relaxed
#pragma rs_fp_full
//#pragma rs_fp_relaxed

// Enable debugging
//#include "rs_debug.rsh"

// Global Variables
//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

// Running Sums.....................................................................................
// Sum of pixel value (for mean computation)
// Sum of pixel value**2 (for standard deviation computation)
rs_allocation gValueSum;
rs_allocation gValue2Sum;

// Statistics.......................................................................................
// Used for determining pixel significance = (value - mean) / stddev
rs_allocation gMean;
rs_allocation gStdDev;
rs_allocation gMask;
rs_allocation gSignificance;

// gMax8bitValue / gMax16bitValue...................................................................
// Statistics (mean, stddev) are saved as normalized values, i.e. mean = gMean * gMax_bitValue
const float gMax8bitValue  = 255.;
const float gMax16bitValue = 1023.;

// gEnableSignificance..............................................................................
// "1" for pixel statistical significance testing, "0" for no testing
int gEnableSignificance;

// gSignificanceThreshold...........................................................................
// Pixels with significance above this theshold are considered "actually significant"
float gSignificanceThreshold;

// gCountAboveThreshold.............................................................................
// Number of pixels with significance above threshold ("actually significant")
rs_allocation gCountAboveThreshold;

// RenderScript Kernels
//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

// TODO: figure out a way to write one processData kernel?

// process8bitData..................................................................................
// Updates running sums and computes significance if enabled (exact same as process16bitData)
// @param val 8-bit depth pixel value
// @param x row pixel coordinate
// @param y column pixel coordinate
void RS_KERNEL process8bitData(uchar val, uint32_t x, uint32_t y) {
    // Value Sum
    uint old_val_sum = rsGetElementAt_uint(gValueSum, x, y);
    uint this_val    = (uint) val;
    uint new_val_sum = old_val_sum + this_val;
    rsSetElementAt_uint(gValueSum, new_val_sum, x, y);

    // Value**2 Sum
    uint old_val2_sum = rsGetElementAt_uint(gValue2Sum, x, y);
    uint this_val2    = this_val * this_val;
    uint new_val2_sum = old_val2_sum + this_val2;
    rsSetElementAt_uint(gValue2Sum, new_val2_sum, x, y);

    // Statistical Significance
    float significance;
    if (gEnableSignificance == 0) {
        // Disabled
        significance = 0.f;
    }
    else { // Enabled
        //                                                   this is the only difference
        float mean   = rsGetElementAt_float(gMean,   x, y) * gMax8bitValue;
        float stddev = rsGetElementAt_float(gStdDev, x, y) * gMax8bitValue;

        if (stddev == 0.f) {
            // positive infinity, avoid 0./0.
            significance = 1./0.;
        }
        else {
            significance = ( ((float) val) - mean ) / stddev;

            uchar mask = rsGetElementAt_uchar(gMask, x, y);
            if (mask == 1 && significance >= gSignificanceThreshold) {
                long count = rsGetElementAt_long(gCountAboveThreshold, 0, 0);
                rsSetElementAt_long(gCountAboveThreshold, count + 1, 0, 0);
            }
        }
    }
    rsSetElementAt_float(gSignificance, significance, x, y);
}

// process16bitData.................................................................................
// Updates running sums and computes significance if enabled (exact same as process8bitData)
// @param val 16-bit depth pixel value
// @param x row pixel coordinate
// @param y column pixel coordinate
void RS_KERNEL process16bitData(ushort val, uint32_t x, uint32_t y) {
    // Value Sum
    uint old_val_sum = rsGetElementAt_uint(gValueSum, x, y);
    uint this_val    = (uint) val;
    uint new_val_sum = old_val_sum + this_val;
    rsSetElementAt_uint(gValueSum, new_val_sum, x, y);

    // Value**2 Sum
    uint old_val2_sum = rsGetElementAt_uint(gValue2Sum, x, y);
    uint this_val2    = this_val * this_val;
    uint new_val2_sum = old_val2_sum + this_val2;
    rsSetElementAt_uint(gValue2Sum, new_val2_sum, x, y);

    // Statistical Significance
    float significance;
    if (gEnableSignificance == 0) {
        // Disabled
        significance = 0.f;
    }
    else { // Enabled
        //                                                   this is the only difference
        float mean   = rsGetElementAt_float(gMean,   x, y) * gMax16bitValue;
        float stddev = rsGetElementAt_float(gStdDev, x, y) * gMax16bitValue;

        if (stddev == 0.f) {
            // positive infinity, avoid 0./0.
            significance = 1./0.;
        }
        else {
            significance = ( ((float) val) - mean ) / stddev;

            uchar mask = rsGetElementAt_uchar(gMask, x, y);
            if (mask == 1 && significance >= gSignificanceThreshold) {
                long count = rsGetElementAt_long(gCountAboveThreshold, 0, 0);
                rsSetElementAt_long(gCountAboveThreshold, count + 1, 0, 0);
            }
        }
    }
    rsSetElementAt_float(gSignificance, significance, x, y);
}

////////////////////////////////////////////////////////////////////////////////////////////////////

// getValueSum......................................................................................
// Transfer RenderScript Allocation back into Java
// @param x row pixel coordinate
// @param y column pixel coordinate
// @return pixel value sum
uint RS_KERNEL getValueSum(uint32_t x, uint32_t y) {
    return rsGetElementAt_uint(gValueSum, x, y);
}

// getValue2Sum.....................................................................................
// Transfer RenderScript Allocation back into Java
// @param x row pixel coordinate
// @param y column pixel coordinate
// @return pixel value**2 sum
uint RS_KERNEL getValue2Sum(uint32_t x, uint32_t y) {
    return rsGetElementAt_uint(gValue2Sum, x, y);
}

// getSignificance..................................................................................
// Transfer RenderScript Allocation back into Java
// @param x row pixel coordinate
// @param y column pixel coordinate
// @return pixel significance
float RS_KERNEL getSignificance(uint32_t x, uint32_t y) {
    return rsGetElementAt_float(gSignificance, x, y);
}

// getCountAboveThreshold...........................................................................
// Transfer RenderScript Allocation back into Java
// @param x row pixel coordinate
// @param y column pixel coordinate
// @return number of pixels above threshold
long RS_KERNEL getCountAboveThreshold(uint32_t x, uint32_t y) {
    return rsGetElementAt_long(gCountAboveThreshold, 0, 0);
}

////////////////////////////////////////////////////////////////////////////////////////////////////

// zeroUIntAllocation...............................................................................
// @param x row pixel coordinate
// @param y column pixel coordinate
// @return 0
uint RS_KERNEL zeroUIntAllocation(uint32_t x, uint32_t y) {
    return 0;
}

// zeroFloatAllocation..............................................................................
// @param x row pixel coordinate
// @param y column pixel coordinate
// @return 0.f
float RS_KERNEL zeroFloatAllocation(uint32_t x, uint32_t y) {
    return 0.f;
}

// zeroDoubleAllocation.............................................................................
// @param x row pixel coordinate
// @param y column pixel coordinate
// @return 0.
double RS_KERNEL zeroDoubleAllocation(uint32_t x, uint32_t y) {
    return 0.;
}

// oneFloatAllocation...............................................................................
// @param x row pixel coordinate
// @param y column pixel coordinate
// @return 1.f
float RS_KERNEL oneFloatAllocation(uint32_t x, uint32_t y) {
    return 1.f;
}

// oneCharAllocation................................................................................
// @param x row pixel coordinate
// @param y column pixel coordinate
// @return 1
uchar RS_KERNEL oneCharAllocation(uint32_t x, uint32_t y) {
    return 1;
}