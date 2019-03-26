#pragma version(1)
#pragma rs java_package_name(sci.crayfis.shramp)
#pragma rs_fp_relaxed

#include "rs_debug.rsh"

// the running sum
rs_allocation gRunningSum;

// the elapsed time multplier [seconds]
//double gElapsedTime = 1.;

//long gSum = 100;
//rs_allocation gInput;

void RS_KERNEL addRaw(ushort in, uint32_t x, uint32_t y) {
    double pixelSum = rsGetElementAt_double(gRunningSum, x, y);
    double inD = (double)in;
    rsSetElementAt_double(gRunningSum, pixelSum + inD, x, y);

    //rsSetElementAt_double(gRunningSum, pixelSum + (gElapsedTime * in), x, y);
    //rsSetElementAt_double(gRunningSum, 0., x, y);
    //gSum = gSum + 1; //rsGetElementAt_short(gInput,x,y);
}


double RS_KERNEL getOutput(uint32_t x, uint32_t y) {
    return rsGetElementAt_double(gRunningSum, x, y);
}





//ushort RS_KERNEL add(ushort pixelValue, uint32_t x, uint32_t y) {
//    double pixelSum = rsGetElementAt_double(gRunningSum, x, y);
//    return pixelSum + (gElapsedTime * pixelValue);
//}

//rs_allocation gImage;

//void dump() {
//    for (int i = 0; i < 20; i++ ) {
//        rsDebug("Data: ", rsGetElementAt_double(gRunningSum, i, i));
//    }
//}

//void RS_KERNEL addYuv(uchar in, uint32_t x, uint32_t y) {
//}


//void RS_KERNEL YUVadd(uchar in, uint32_t x, uint32_t y) {
    //rsDebug("Son of a bitch", 0);
   //Convert input uchar4 to float4
    //float4 f4 = rsUnpackColor8888(in);

    //Get YUV channels values
    //float Y = 0.299f * f4.r + 0.587f * f4.g + 0.114f * f4.b;
    //float U = ((0.492f * (f4.b - Y))+1)/2;
    //float V = ((0.877f * (f4.r - Y))+1)/2;

    //Get Y value between 0 and 255 (included)
    //int32_t val = Y * 255;

    //double pixelSum = rsGetElementAt_double(gRunningSum, x, y);
    //rsSetElementAt_double(gRunningSum, pixelSum + (gElapsedTime * val), x, y);
//}
