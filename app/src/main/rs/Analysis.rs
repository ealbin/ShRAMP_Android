#pragma version(1)
#pragma rs java_package_name(sci.crayfis.shramp)
#pragma rs_fp_relaxed

//**************************************************************************************************
// Global variables
//-----------------

// Pixel-wise sum of exposure times pixel value
rs_allocation gExposureValue;

// Pixel-wise sum of exposure time^2 times pixel value
rs_allocation gExposure2Value;

// Pixel-wise sum of exposure time times pixel value^2
rs_allocation gExposureValue2;

// Current image exposure time in seconds
double gExposureTime;

//..................................................................................................

// Total number of image frames
double gNframes;

// Total pixel exposure time in seconds
double gExposureSum;

// Total pixel exposure time^3 in seconds
double gExposureSum3;

// Average pixel value / seconds exposure
rs_allocation gMean;

// Standard deviation on the mean / seconds exposure
rs_allocation gStdDev;

//**************************************************************************************************
// Renderscript Kernels
//---------------------

// Stream processing
//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

void RS_KERNEL update(ushort val, uint32_t x, uint32_t y) {
    double old_exp_val  = rsGetElementAt_double(gExposureValue, x, y);
    double old_exp2_val = rsGetElementAt_double(gExposure2Value, x, y);
    double old_exp_val2 = rsGetElementAt_double(gExposureValue2, x, y);

    double this_exp_val  = gExposureTime * (double) val;
    double this_exp2_val = gExposureTime * gExposureTime * (double) val;
    double this_exp_val2 = gExposureTime * (double) val  * (double) val;

    rsSetElementAt_double(gExposureValue , old_exp_val  + this_exp_val , x, y);
    rsSetElementAt_double(gExposure2Value, old_exp2_val + this_exp2_val, x, y);
    rsSetElementAt_double(gExposureValue2, old_exp_val2 + this_exp_val2, x, y);
}

// Session statistics
//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

double RS_KERNEL getMean(uint32_t x, uint32_t y) {
    double exp_val  = rsGetElementAt_double(gExposureValue, x, y);
    double mean_val = exp_val / gExposureSum;
    double mean_exp = gExposureSum / gNframes;
    return mean_val / mean_exp;
}

double RS_KERNEL getStdDev(uint32_t x, uint32_t y) {
    double mean_exp = gExposureSum / gNframes;
    double exp_val2 = rsGetElementAt_double(gExposureValue2, x, y);
    double exp2_val = rsGetElementAt_double(gExposure2Value, x, y);
    double mean     = rsGetElementAt_double(gMean, x, y);
    double var = ( exp_val2 + (mean * mean * gExposureSum3) - (2. * mean * exp2_val) ) / gExposureSum;

    return sqrt((float) var) / mean_exp;
}

double RS_KERNEL getStdErr(uint32_t x, uint32_t y) {
    double stddev = rsGetElementAt_double(gStdDev, x, y);
    return stddev / sqrt((float) gNframes);
}
