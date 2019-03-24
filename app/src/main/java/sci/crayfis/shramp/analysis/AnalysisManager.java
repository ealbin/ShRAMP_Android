package sci.crayfis.shramp.analysis;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Type;
import android.util.Size;

import sci.crayfis.shramp.GlobalSettings;
import sci.crayfis.shramp.ScriptC_PostProcess;
import sci.crayfis.shramp.ScriptC_StreamProcess;
import sci.crayfis.shramp.camera2.CameraController;

public final class AnalysisManager {

    private static final AnalysisManager mInstance = new AnalysisManager();

    private AnalysisManager() {}

    private static RenderScript mRS;

    // mDoubleType..................................................................................
    // TODO: description
    private static Type mDoubleType;

    // mShortType...................................................................................
    // TODO: description
    private static Type mShortType;

    // mShortType...................................................................................
    // TODO: description
    private static Type mByteType;


    public static void create(Activity activity) {
        mRS = RenderScript.create(activity, RenderScript.ContextType.NORMAL,
                                            GlobalSettings.RENDER_SCRIPT_FLAGS);
        mRS.setPriority(GlobalSettings.RENDER_SCRIPT_PRIORITY);

        ImageProcessor.setRenderScript(new ScriptC_StreamProcess(mRS));
        ImageProcessor.setRenderScriptPost(new ScriptC_PostProcess(mRS));

        Element byteElement   = Element.U8(mRS);
        Element shortElement  = Element.U16(mRS);
        Element doubleElement = Element.F64(mRS);

        Size outputSize = CameraController.getOutputSize();
        assert outputSize != null;
        int width  = outputSize.getWidth();
        int height = outputSize.getHeight();
        ImageWrapper.setNpixels(width * height);

        mByteType   = new Type.Builder(mRS, byteElement  ).setX(width).setY(height).create();
        mShortType  = new Type.Builder(mRS, shortElement ).setX(width).setY(height).create();
        mDoubleType = new Type.Builder(mRS, doubleElement).setX(width).setY(height).create();

        Integer outputFormat = CameraController.getOutputFormat();
        assert outputFormat != null;
        switch (outputFormat) {
            case (ImageFormat.YUV_420_888): {
                ImageWrapper.setIs8bitData();
                ImageProcessor.setImageAllocation(newByteAllocation());
                break;
            }
            case (ImageFormat.RAW_SENSOR): {
                ImageWrapper.setIs16bitData();
                ImageProcessor.setImageAllocation(newShortAllocation());
                break;
            }

            default: {
                // TODO: error
            }
        }

        ImageProcessor.setSignificanceAllocation(newDoubleAllocation());
        ImageProcessor.resetTotals();
        ImageProcessor.updateResults(newDoubleAllocation(), newDoubleAllocation(), newDoubleAllocation());
    }

    static Allocation newByteAllocation() {
        return Allocation.createTyped(mRS, mByteType, Allocation.USAGE_SCRIPT);
    }

    static Allocation newShortAllocation() {
        return Allocation.createTyped(mRS, mShortType, Allocation.USAGE_SCRIPT);
    }

    static Allocation newDoubleAllocation() {
        return Allocation.createTyped(mRS, mDoubleType, Allocation.USAGE_SCRIPT);
    }

    static void resetAllocation(Allocation allocation) {
        if (allocation == null) {
            return;
        }
        allocation.destroy();
        allocation = null;
    }

    public static void resetRunningTotals() {
        ImageProcessor.resetTotals();
    }

}
