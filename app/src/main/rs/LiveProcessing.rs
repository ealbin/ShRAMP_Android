#pragma version(1)
#pragma rs java_package_name(sci.crayfis.shramp)
#pragma rs_fp_full
//#pragma rs_fp_relaxed
//#include "rs_debug.rsh"

int gEnableSignificance;

long gExposureTime;
rs_allocation gMeanRate;
rs_allocation gStdDevRate;

rs_allocation gExposureValueSum;
rs_allocation gExposureValue2Sum;
rs_allocation gSignificance;

void RS_KERNEL process8bitData(uchar val, uint32_t x, uint32_t y) {
    float old_exp_val_sum = rsGetElementAt_float(gExposureValueSum, x, y);
    float this_exp_val    = (float) gExposureTime * (float) val;
    float new_exp_val_sum = old_exp_val_sum + this_exp_val;
    rsSetElementAt_float(gExposureValueSum, new_exp_val_sum, x, y);

    float old_exp_val2_sum = rsGetElementAt_float(gExposureValue2Sum, x, y);
    float this_exp_val2    = (float) gExposureTime * (float) val * (float) val;
    float new_exp_val2_sum = old_exp_val2_sum + this_exp_val2;
    rsSetElementAt_float(gExposureValue2Sum, new_exp_val2_sum, x, y);

    if (gEnableSignificance == 1) {
        float mean_rate    = rsGetElementAt_float(gMeanRate, x, y);
        float stddev_rate  = rsGetElementAt_float(gStdDevRate, x, y);
        if (stddev_rate == 0.f) {
            rsSetElementAt_float(gSignificance, 1./0., x, y);
        }
        else {
            float significance = ( ( val / (float) gExposureTime ) - mean_rate ) / stddev_rate;
            rsSetElementAt_float(gSignificance, significance, x, y);
        }
    }
}

void RS_KERNEL process16bitData(ushort val, uint32_t x, uint32_t y) {
    float old_exp_val_sum = rsGetElementAt_float(gExposureValueSum, x, y);
    float this_exp_val    = (float) gExposureTime * (float) val;
    float new_exp_val_sum = old_exp_val_sum + this_exp_val;
    rsSetElementAt_float(gExposureValueSum, new_exp_val_sum, x, y);

    float old_exp_val2_sum = rsGetElementAt_float(gExposureValue2Sum, x, y);
    float this_exp_val2    = (float) gExposureTime * (float) val * (float) val;
    float new_exp_val2_sum = old_exp_val2_sum + this_exp_val2;
    rsSetElementAt_float(gExposureValue2Sum, new_exp_val2_sum, x, y);

    if (gEnableSignificance == 1) {
        float mean_rate    = rsGetElementAt_float(gMeanRate, x, y);
        float stddev_rate  = rsGetElementAt_float(gStdDevRate, x, y);
        if (stddev_rate == 0.f) {
            rsSetElementAt_float(gSignificance, 1./0., x, y);
        }
        else {
            float significance = ( ( val / (float) gExposureTime ) - mean_rate ) / stddev_rate;
            rsSetElementAt_float(gSignificance, significance, x, y);
        }
    }
}

float RS_KERNEL getExposureValueSum(uint32_t x, uint32_t y) {
    return rsGetElementAt_float(gExposureValueSum, x, y);
}

float RS_KERNEL getExposureValue2Sum(uint32_t x, uint32_t y) {
    return rsGetElementAt_float(gExposureValue2Sum, x, y);
}

float RS_KERNEL getSignificance(uint32_t x, uint32_t y) {
    return rsGetElementAt_float(gSignificance, x, y);
}