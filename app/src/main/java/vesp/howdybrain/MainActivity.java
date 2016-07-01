package vesp.howdybrain;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import vesp.howdybrain.CameraSettting.CustomizableCameraView;
import vesp.howdybrain.OpenCV.CalcOpticalFlow;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener {
    //ImageView imgv;
    private static final String TAG = "MainActivity"; // Debug TAG
    private Mat mOutputFrame;
    private Mat prevImage;
    private Mat nextImage;

    // Initialize OpenCV
    static {
        if(!OpenCVLoader.initDebug()){
            Log.d(TAG, "OpenCV not loaded");
        } else {
            Log.d(TAG, "OpenCV loaded");
        }
    }

    // カメラビューのインスタンス
    // CameraBridgeViewBase は JavaCameraView/NativeCameraView のスーパークラス
    //private CameraBridgeViewBase mCameraView;
    //private JavaCameraView mCameraView;
    private CustomizableCameraView mCameraView;

    // ライブラリ初期化完了後に呼ばれるコールバック (onManagerConnected)
    // public abstract class BaseLoaderCallback implements LoaderCallbackInterface
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                // 読み込みが成功したらカメラプレビューを開始
                case LoaderCallbackInterface.SUCCESS:
                    mCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // OpenCV
        // カメラビューのインスタンスを変数にバインド(ここでJavaCameraViewとバインド)
        //mCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        //org.opencv.android.JavaCameraView
        mCameraView = (CustomizableCameraView) findViewById(R.id.camera_view);
        // リスナーの設定 (後述)
        mCameraView.setCvCameraViewListener(this);
        /*
        Mat mat = new Mat(20,20, CvType.CV_8UC3,new Scalar(0, 0, 255));
        Bitmap bitmap= Bitmap.createBitmap(mat.width(), mat.height(), Bitmap.Config.ARGB_8888);

        Utils.matToBitmap(mat, bitmap);
        ImageView iv = (ImageView) findViewById(R.id.imageView1);
        iv.setBackgroundColor(Color.GRAY);
        iv.setImageBitmap(bitmap);*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //OpenCV
    @Override
    public void onPause() {
        if (mCameraView != null) {
            mCameraView.disableView();
        }
        super.onPause();
    }

    // Activityが表示された時の動き
    @Override
    protected void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCameraView != null) {
            mCameraView.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        // カメラプレビュー開始時に呼ばれる
        // Mat(int rows, int cols, int type)
        // rows(行): height, cols(列): width
        mOutputFrame = new Mat(height, width, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        //mOutputFrame.release();
        mOutputFrame.release();
    }

    // フレームをキャプチャする毎(30fpsなら毎秒30回)に呼ばれる
    // 処理はここを中心に書く！
    @Override
    public Mat onCameraFrame(Mat inputFrame) {
        // 既に前の画像を確保されているなら処理を行う
        if(prevImage != null) {
            nextImage = inputFrame;

            // オプティカルフロー描写
            CalcOpticalFlow calcOpticalFlow = new CalcOpticalFlow();
            if(calcOpticalFlow.doOpticalFlow(prevImage, nextImage)){
                Log.d(TAG,"オプティカルフロー成功");
            }
            else{
                Log.d(TAG,"オプティカルフロー失敗");
            }
        }

        prevImage = inputFrame.clone();
        mOutputFrame = nextImage;
        //mOutputFrame = inputFrame;

        return mOutputFrame;
    }
}
