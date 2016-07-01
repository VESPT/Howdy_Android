package vesp.howdybrain.OpenCV;

import android.util.Log;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.Video;

import java.util.List;

import static org.opencv.imgproc.Imgproc.goodFeaturesToTrack;

/**
 * Created by vesp on 16/06/16.
 * Opticalフローの処理や表示を行うクラス
 */
public class CalcOpticalFlow {
    // メンバ変数
    private String Tag = "CalcOpticalFlow";

    public boolean doOpticalFlow(Mat prevImage, Mat nextImage){
        // グレースケールじゃなければグレースケールへ変換する
        Mat prevGrayImage = new Mat(prevImage.size(), CvType.CV_8UC1);
        if(prevImage.type() !=  CvType.CV_8UC1){
            Log.d(Tag, "前の画像をグレースケール変換実施");
            Imgproc.cvtColor(prevImage, prevGrayImage, Imgproc.COLOR_RGB2GRAY);
        }
        else{
            prevGrayImage = prevImage;
        }
        Mat nextGrayImage = new Mat(nextImage.size(), CvType.CV_8UC1);
        if(nextImage.type() !=  CvType.CV_8UC1){
            Log.d(Tag, "後の画像をグレースケール変換実施");
            Imgproc.cvtColor(nextImage, nextGrayImage, Imgproc.COLOR_RGB2GRAY);
        }
        else{
            nextGrayImage = nextImage;
        }

        MatOfPoint2f nextPts = new MatOfPoint2f(getFeature(nextGrayImage).toArray());   // ポイントの場所
        MatOfPoint2f prevPts = new MatOfPoint2f(getFeature(prevGrayImage).toArray());   // ポイントの場所

        MatOfByte status = new MatOfByte();     // 見つかったかどうかの状態
        MatOfFloat err = new MatOfFloat();      // 複数特徴が見つかった場合？

        Log.d(Tag, "prevPts.total()"+prevPts.total());
        Log.d(Tag, "nextPts.total()"+nextPts.total());
        if(prevPts.total() <= 0){
            Log.d(Tag, "前の特徴点が見つからず・・・");
            return false;
        }
        if(nextPts.total() <= 0){
            Log.d(Tag, "後の特徴点が見つからず・・・");
            return false;
        }
        Video.calcOpticalFlowPyrLK(prevGrayImage, nextGrayImage, prevPts, nextPts, status, err);

        // 表示
        long flow_num = status.total();
        if(flow_num > 0){
            drawVector(nextImage, flow_num, status.toList(), prevPts.toList(), nextPts.toList());
        }
        else{
            Log.d(Tag, "オプティカルフローがうまくいかず・・・");
            return false;
        }

        return true;
    }

    private MatOfPoint getFeature(Mat inputImage){
        // 特徴点抽出
        Size orgSize = inputImage.size();
        //Mat eig_img = new Mat(orgSize, CvType.CV_32FC1); // 固有値
        //Mat tmp_img = new Mat(orgSize, CvType.CV_32FC1); // 処理に使うもの
        Mat mask = new Mat(orgSize, CvType.CV_8UC1); // ブール値で書かれたコーナーの位置がわかるマスク
        int corner_count = 1000; // 最大コーナー数
        //MatOfPoint corners[] = new MatOfPoint[500];
        MatOfPoint corners = new MatOfPoint();
        double qualityLevel = 0.01;
        double minDistance = 3;

        int doCounter = 0;      // 1回しか実行しないと0になる場合があるため、何回か行わせる
        while(doCounter < 2){
            try{
                /*
                goodFeaturesToTrack(
                        inputImage,      // 元の画像
                        corners,        // コーナー情報
                        corner_count,   // コーナーの数
                        qualityLevel,   // 0.01か0.1が定番
                        minDistance,    // 特徴を一つに絞る範囲
                        mask,           // Mask
                        3,              // blocksize
                        true,           // use_harris
                        0.04);          // 重み
                        */

                Imgproc.goodFeaturesToTrack(
                        inputImage,
                        corners,
                        corner_count,   // コーナーの数
                        0.1,           // QualityLevel
                        5);             // mindistance

            }catch(UnsatisfiedLinkError e){ // JNIリンカエラーチェック
                Log.d(Tag, "LinkerError");
            }
            Log.d(Tag, "corners.total="+corners.total());

            if(corners.total() > 0)
                break;
            else
                doCounter++;
        }
        return corners;
    }

    //オプティカルフローのベクトルを描写
    private void drawVector(
            Mat drawTargetImage,                // 描写対象画像
            long flow_num ,                     // 2つの画像で対応出来た特徴点の数
            List<Byte> list_status,             // すべての特徴点のtrue or false の状態
            List<Point> list_features_prev,     // 前の画像の特徴点の座標
            List<Point> list_features_next)     // 後の画像の特徴点の座標
    {
        for(int i=0; i<flow_num; i++){
            if(list_status.get(i)==1){ //オプティカルフローが成功している奴
                Point p1 = new Point();
                p1.x = list_features_prev.get(i).x;
                p1.y = list_features_prev.get(i).y;

                Point p2 = new Point();
                p2.x = list_features_next.get(i).x;
                p2.y = list_features_next.get(i).y;

                // フロー描画
                int thickness = 1;
                Imgproc.line(drawTargetImage, p1, p2, new Scalar(0, 255, 0), thickness);
            }
        }
    }
}
