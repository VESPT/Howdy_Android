package vesp.howdybrain.CameraSettting;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;

import org.opencv.android.JavaCameraView;

import java.util.List;

/**
 * Created by vesp on 16/07/01.
 */
public class CustomizableCameraView extends JavaCameraView {
    private String Tag = "CustomizeCameraView";

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

    // 保証範囲内の最小値のFPSに設定する
    public void setPreviewFPSMin(){
        Log.d(Tag,"TEST");

        // mCamera.getParameters()でparameterクラスを取得
        Camera.Parameters params = mCamera.getParameters();
        // FPSの最大値、最小値を調べる
        List<int[]> fps_range = params.getSupportedPreviewFpsRange();
        Log.d(Tag,"最小FPS="+fps_range.get(0));
        // FPS設定
        //params.setPreviewFpsRange((int)(min*1000), (int)(max*1000));
        //mCamera.setParameters(params);

    }
}
