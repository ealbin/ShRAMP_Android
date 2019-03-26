#pragma version(1)
#pragma rs java_package_name(sci.crayfis.shramp)
#pragma rs_fp_full
//#pragma rs_fp_relaxed

//#include "rs_debug.rsh"

//**************************************************************************************************
// Global variables
//-----------------

// Accumulators for capture processing
//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

// Pixel-wise sum of exposure times pixel value
// Updated in kernel, and after capture session, read into Java for statistics
//rs_allocation gExposureValueSum;

// Pixel-wise sum of exposure time times pixel value^2
// Updated in kernel, and after capture session, read into Java for statistics
//rs_allocation gExposureValue2Sum;

// Current image exposure time in seconds
// Updated by Java after every image, before calling the update kernel
long gExposureTime;

// Post-capture statistics processing
//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

// Total number of image frames
long gNframes;

// Total pixel exposure time in seconds
long gExposureSum;

// Average pixel value / seconds exposure
rs_allocation gMean;

// Standard deviation on the mean / seconds exposure
rs_allocation gStdDev;

////////////////////////////////////////////////////////////////////////////////////////////////////
//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
////////////////////////////////////////////////////////////////////////////////////////////////////

//**************************************************************************************************
// Renderscript Kernels
//---------------------

// Capture processing
//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

rs_allocation gOldExposureValueSum;
double RS_KERNEL updateExposureValueSum(ushort val, uint32_t x, uint32_t y) {

    double old_exp_val_sum = rsGetElementAt_double(gOldExposureValueSum,  x, y);
    double this_exp_val = (double) gExposureTime * (double) val;
    return old_exp_val_sum + this_exp_val;
}

rs_allocation gOldExposureValue2Sum;
double RS_KERNEL updateExposureValue2Sum(ushort val, uint32_t x, uint32_t y) {

    double old_exp_val2_sum = rsGetElementAt_double(gOldExposureValue2Sum,  x, y);
    double this_exp_val2 = (double) gExposureTime * (double) val * (double) val;
    return old_exp_val2_sum + this_exp_val2;
}



// update...........................................................................................
//* Update running sums of exposure^n * pixel^m value, for {n, m} = {1, 1}, {1, 2}
//*
//* @param val: pixel data from current image being processed
//* @param x: pixel coordinate x
//* @param y: pixel coordinate y
//void RS_KERNEL update_16bit(ushort val, uint32_t x, uint32_t y) {
//    double dVal = (double) val;

//    double old_exp_val_sum  = rsGetElementAt_double(gExposureValueSum,  x, y);
//    double old_exp_val2_sum = rsGetElementAt_double(gExposureValue2Sum, x, y);

//    double this_exp_val  = (gExposureTime) * (dVal);          // {n, m} = {1, 1}
//    double this_exp_val2 = (gExposureTime) * (dVal * dVal);  // {n, m} = {1, 2}

    //rsSetElementAt_double(gExposureValueSum , old_exp_val_sum  + this_exp_val , x, y);
    //rsSetElementAt_double(gExposureValue2Sum, old_exp_val2_sum + this_exp_val2, x, y);
//    rsSetElementAt_double(gExposureValueSum , old_exp_val_sum  + 11., x, y);
//    rsSetElementAt_double(gExposureValue2Sum, old_exp_val2_sum + 13., x, y);
//}

// TODO: figure out how to make this work for both / call a helper function w/typecast
//void RS_KERNEL update_8bit(uchar val, uint32_t x, uint32_t y) {
//    double dVal = (double) val;

//    double old_exp_val_sum  = rsGetElementAt_double(gExposureValueSum,  x, y);
//    double old_exp_val2_sum = rsGetElementAt_double(gExposureValue2Sum, x, y);

//    double this_exp_val  = (gExposureTime) * (dVal);          // {n, m} = {1, 1}
//    double this_exp_val2 = (gExposureTime) * (dVal * dVal);  // {n, m} = {1, 2}

    //rsSetElementAt_double(gExposureValueSum , old_exp_val_sum  + this_exp_val , x, y);
    //rsSetElementAt_double(gExposureValue2Sum, old_exp_val2_sum + this_exp_val2, x, y);
//    rsSetElementAt_double(gExposureValueSum , old_exp_val_sum  + 11., x, y);
//    rsSetElementAt_double(gExposureValue2Sum, old_exp_val2_sum + 13., x, y);
//}




// Session statistics
//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::

rs_allocation gExposureValueSum;
rs_allocation gExposureValue2Sum;

// getMean..........................................................................................
//* Compute the mean pixel value / nanoseconds-exposure for this capture session
//*
//* @param x: pixel coordinate x
//* @param y: pixel coordinate y
//* @return: mean value / exposure of pixel (x,y) for this capture session
double RS_KERNEL getMean(uint32_t x, uint32_t y) {

    // mean pixel value
    double  exp_val_sum   = rsGetElementAt_double(gExposureValueSum, x, y);  // {n, m} = {1, 1}
    double mean_pixel_val = exp_val_sum / (double) gExposureSum;

    // mean exposure time
    double mean_exposure = gExposureSum / (double) gNframes;

    // mean pixel value / exposure time (nanoseconds)
    double mean_val_per_nanos = mean_pixel_val / (double) mean_exposure;

    // set global, and return value
    return mean_val_per_nanos;
}

// getStdDev........................................................................................
//* Compute the standard deviation of pixel value / nanoseconds-exposure for this capture session
//*
//* @param x: pixel coordinate x
//* @param y: pixel coordinate y
//* @return: standard deviation of pixel value / exposure of pixel (x,y) for this capture session
double RS_KERNEL getStdDev(uint32_t x, uint32_t y) {

    // mean exposure time
    double mean_exp = gExposureSum / (double) gNframes;

    // accumlated statistics
    double exp_val_sum  = rsGetElementAt_double(gExposureValueSum,  x, y);
    double exp_val2_sum = rsGetElementAt_double(gExposureValue2Sum, x, y);  // {n, m} = {1, 2}

    // pixel mean / exposure
    double mean = rsGetElementAt_double(gMean, x, y);
    double mean_val = mean * mean_exp;

    // pixel variance
    // var = < ( value - mean_value )^2 >
    //     = Sum{ exposure * ( value - mean_value )^2 } / Sum {exposure}
    //     = < exposure * value^2 > - mean_value^2
    double var = ( exp_val2_sum / (double) gExposureSum ) - ( mean_val * mean_val );


    // standard deviation / exposure time (nanoseconds)
    double stddev_per_nanos = sqrt((float) var) / (double) mean_exp;

    // set global, and return value
    return stddev_per_nanos;
}

// getStdErr........................................................................................
//* Compute the standard error of pixel mean value / nanoseconds-exposure for this capture session
//*
//* @param x: pixel coordinate x
//* @param y: pixel coordinate y
//* @return: standard error of pixel mean value / exposure of pixel (x,y) for this capture session
double RS_KERNEL getStdErr(uint32_t x, uint32_t y) {
    double stddev = rsGetElementAt_double(gStdDev, x, y);
    return stddev / (double) sqrt((float) gNframes);
}
