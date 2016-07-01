package vesp.howdybrain.CameraSettting;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;

import org.opencv.android.JavaCameraView;

/**
 * Created by vesp on 16/07/01.
 */
public class CustomizableCameraView extends JavaCameraView {
    public CustomizableCameraView(Context context, int cameraId) {
        super(context, cameraId);
    }
    public CustomizableCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public void setPreviewFPS(double min, double max){
        Camera.Parameters params = mCamera.getParameters();
        params.setPreviewFpsRange((int)(min*1000), (int)(max*1000));
        mCamera.setParameters(params);
    }
}
