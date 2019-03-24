#pragma version(1)
#pragma rs java_package_name(sci.crayfis.shramp)
#pragma rs_fp_full
//#pragma rs_fp_relaxed
//#include "rs_debug.rsh"

int gDoSignificance;

long gExposureTime;
rs_allocation gMeanRate;
rs_allocation gStdDevRate;

rs_allocation gExposureValueSum;
rs_allocation gExposureValue2Sum;
rs_allocation gSignificance;

void RS_KERNEL process8bitData(uchar val, uint32_t x, uint32_t y) {
    double old_exp_val_sum = rsGetElementAt_double(gExposureValueSum, x, y);
    double this_exp_val    = (double) gExposureTime * (double) val;
    double new_exp_val_sum = old_exp_val_sum + this_exp_val;
    rsSetElementAt_double(gExposureValueSum, new_exp_val_sum, x, y);

    double old_exp_val2_sum = rsGetElementAt_double(gExposureValue2Sum, x, y);
    double this_exp_val2    = (double) gExposureTime * (double) val * (double) val;
    double new_exp_val2_sum = old_exp_val2_sum + this_exp_val2;
    rsSetElementAt_double(gExposureValue2Sum, new_exp_val2_sum, x, y);

    if (gDoSignificance == 1) {
        double mean_rate    = rsGetElementAt_double(gMeanRate, x, y);
        double stddev_rate  = rsGetElementAt_double(gStdDevRate, x, y);
        double significance = ( ( val / (double) gExposureTime ) - mean_rate ) / stddev_rate;
        rsSetElementAt_double(gSignificance, significance, x, y);
    }
}

void RS_KERNEL process16bitData(ushort val, uint32_t x, uint32_t y) {
    double old_exp_val_sum = rsGetElementAt_double(gExposureValueSum, x, y);
    double this_exp_val    = (double) gExposureTime * (double) val;
    double new_exp_val_sum = old_exp_val_sum + this_exp_val;
    rsSetElementAt_double(gExposureValueSum, new_exp_val_sum, x, y);

    double old_exp_val2_sum = rsGetElementAt_double(gExposureValue2Sum, x, y);
    double this_exp_val2    = (double) gExposureTime * (double) val * (double) val;
    double new_exp_val2_sum = old_exp_val2_sum + this_exp_val2;
    rsSetElementAt_double(gExposureValue2Sum, new_exp_val2_sum, x, y);

    if (gDoSignificance == 1) {
        double mean_rate    = rsGetElementAt_double(gMeanRate, x, y);
        double stddev_rate  = rsGetElementAt_double(gStdDevRate, x, y);
        double significance = ( ( val / (double) gExposureTime ) - mean_rate ) / stddev_rate;
        rsSetElementAt_double(gSignificance, significance, x, y);
    }
}

double RS_KERNEL getExposureValueSum(uint32_t x, uint32_t y) {
    return rsGetElementAt_double(gExposureValueSum, x, y);
}

double RS_KERNEL getExposureValue2Sum(uint32_t x, uint32_t y) {
    return rsGetElementAt_double(gExposureValue2Sum, x, y);
}

double RS_KERNEL getSignificance(uint32_t x, uint32_t y) {
    return rsGetElementAt_double(gSignificance, x, y);
}