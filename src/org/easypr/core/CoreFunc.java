package org.easypr.core;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_highgui.cvShowImage;
import static org.bytedeco.javacpp.opencv_highgui.cvWaitKey;
import static org.bytedeco.javacpp.opencv_imgproc.CV_BGR2HSV;
import static org.bytedeco.javacpp.opencv_imgproc.cvtColor;
import static org.bytedeco.javacpp.opencv_imgproc.equalizeHist;
import static org.bytedeco.javacpp.opencv_imgproc.resize;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacpp.opencv_core.MatVector;
import org.bytedeco.javacpp.opencv_core.Size;
import org.bytedeco.javacpp.indexer.FloatIndexer;

/**
 * @author lin.yao
 * 
 */
public class CoreFunc {
    public enum Color {
        UNKNOWN, BLUE, YELLOW
    };

    public enum Direction {
        UNKNOWN, VERTICAL, HORIZONTAL
    }

    /**
     * 鏍规嵁涓�骞呭浘鍍忎笌棰滆壊妯℃澘鑾峰彇瀵瑰簲鐨勪簩鍊煎浘
     * 
     * @param src
     *            杈撳叆RGB鍥惧儚
     * @param r
     *            棰滆壊妯℃澘锛堣摑鑹层�侀粍鑹诧級
     * @param adaptive_minsv
     *            S鍜孷鐨勬渶灏忓�肩敱adaptive_minsv杩欎釜bool鍊煎垽鏂�
     *            <ul>
     *            <li>濡傛灉涓簍rue锛屽垯鏈�灏忓�煎彇鍐充簬H鍊硷紝鎸夋瘮渚嬭“鍑�
     *            <li>濡傛灉涓篺alse锛屽垯涓嶅啀鑷�傚簲锛屼娇鐢ㄥ浐瀹氱殑鏈�灏忓�糾inabs_sv
     *            </ul>
     * @return 杈撳嚭鐏板害鍥撅紙鍙湁0鍜�255涓や釜鍊硷紝255浠ｈ〃鍖归厤锛�0浠ｈ〃涓嶅尮閰嶏級
     */
    public static Mat colorMatch(final Mat src, final Color r, final boolean adaptive_minsv) {
        final float max_sv = 255;
        final float minref_sv = 64;
        final float minabs_sv = 95;

        // blue鐨凥鑼冨洿
        final int min_blue = 100;
        final int max_blue = 140;

        // yellow鐨凥鑼冨洿
        final int min_yellow = 15;
        final int max_yellow = 40;

        // 杞埌HSV绌洪棿杩涜澶勭悊锛岄鑹叉悳绱富瑕佷娇鐢ㄧ殑鏄疕鍒嗛噺杩涜钃濊壊涓庨粍鑹茬殑鍖归厤宸ヤ綔
        Mat src_hsv = new Mat();
        cvtColor(src, src_hsv, CV_BGR2HSV);
        MatVector hsvSplit = new MatVector();
        split(src_hsv, hsvSplit);
        equalizeHist(hsvSplit.get(2), hsvSplit.get(2));
        merge(hsvSplit, src_hsv);

        // 鍖归厤妯℃澘鍩鸿壊,鍒囨崲浠ユ煡鎵炬兂瑕佺殑鍩鸿壊
        int min_h = 0;
        int max_h = 0;
        switch (r) {
        case BLUE:
            min_h = min_blue;
            max_h = max_blue;
            break;
        case YELLOW:
            min_h = min_yellow;
            max_h = max_yellow;
            break;
        default:
            break;
        }

        float diff_h = (float) ((max_h - min_h) / 2);
        int avg_h = (int) (min_h + diff_h);

        int channels = src_hsv.channels();
        int nRows = src_hsv.rows();
        // 鍥惧儚鏁版嵁鍒楅渶瑕佽�冭檻閫氶亾鏁扮殑褰卞搷锛�
        int nCols = src_hsv.cols() * channels;

        // 杩炵画瀛樺偍鐨勬暟鎹紝鎸変竴琛屽鐞�
        if (src_hsv.isContinuous()) {
            nCols *= nRows;
            nRows = 1;
        }

        for (int i = 0; i < nRows; ++i) {
            BytePointer p = src_hsv.ptr(i);
            for (int j = 0; j < nCols; j += 3) {
                int H = p.get(j) & 0xFF;
                int S = p.get(j + 1) & 0xFF;
                int V = p.get(j + 2) & 0xFF;

                boolean colorMatched = false;

                if (H > min_h && H < max_h) {
                    int Hdiff = 0;
                    if (H > avg_h)
                        Hdiff = H - avg_h;
                    else
                        Hdiff = avg_h - H;

                    float Hdiff_p = Hdiff / diff_h;

                    float min_sv = 0;
                    if (true == adaptive_minsv)
                        min_sv = minref_sv - minref_sv / 2 * (1 - Hdiff_p);
                    else
                        min_sv = minabs_sv;

                    if ((S > min_sv && S <= max_sv) && (V > min_sv && V <= max_sv))
                        colorMatched = true;
                }

                if (colorMatched == true) {
                    p.put(j, (byte) 0);
                    p.put(j + 1, (byte) 0);
                    p.put(j + 2, (byte) 255);
                } else {
                    p.put(j, (byte) 0);
                    p.put(j + 1, (byte) 0);
                    p.put(j + 2, (byte) 0);
                }
            }
        }

        // 鑾峰彇棰滆壊鍖归厤鍚庣殑浜屽�肩伆搴﹀浘
        MatVector hsvSplit_done = new MatVector();
        split(src_hsv, hsvSplit_done);
        Mat src_grey = hsvSplit_done.get(2);

        return src_grey;
    }

    /**
     * 鍒ゆ柇涓�涓溅鐗岀殑棰滆壊
     * 
     * @param src
     *            杞︾墝mat
     * @param r
     *            棰滆壊妯℃澘
     * @param adaptive_minsv
     *            S鍜孷鐨勬渶灏忓�肩敱adaptive_minsv杩欎釜bool鍊煎垽鏂�
     *            <ul>
     *            <li>濡傛灉涓簍rue锛屽垯鏈�灏忓�煎彇鍐充簬H鍊硷紝鎸夋瘮渚嬭“鍑�
     *            <li>濡傛灉涓篺alse锛屽垯涓嶅啀鑷�傚簲锛屼娇鐢ㄥ浐瀹氱殑鏈�灏忓�糾inabs_sv
     *            </ul>
     * @return
     */
    public static boolean plateColorJudge(final Mat src, final Color color, final boolean adaptive_minsv) {
        // 鍒ゆ柇闃堝��
        final float thresh = 0.49f;

        Mat gray = colorMatch(src, color, adaptive_minsv);

        float percent = (float) countNonZero(gray) / (gray.rows() * gray.cols());

        return (percent > thresh) ? true : false;
    }

    /**
     * getPlateType 鍒ゆ柇杞︾墝鐨勭被鍨�
     * 
     * @param src
     * @param adaptive_minsv
     *            S鍜孷鐨勬渶灏忓�肩敱adaptive_minsv杩欎釜bool鍊煎垽鏂�
     *            <ul>
     *            <li>濡傛灉涓簍rue锛屽垯鏈�灏忓�煎彇鍐充簬H鍊硷紝鎸夋瘮渚嬭“鍑�
     *            <li>濡傛灉涓篺alse锛屽垯涓嶅啀鑷�傚簲锛屼娇鐢ㄥ浐瀹氱殑鏈�灏忓�糾inabs_sv
     *            </ul>
     * @return
     */
    public static Color getPlateType(final Mat src, final boolean adaptive_minsv) {
        if (plateColorJudge(src, Color.BLUE, adaptive_minsv) == true) {
            return Color.BLUE;
        } else if (plateColorJudge(src, Color.YELLOW, adaptive_minsv) == true) {
            return Color.YELLOW;
        } else {
            return Color.UNKNOWN;
        }
    }

    /**
     * 鑾峰彇鍨傜洿鎴栨按骞虫柟鍚戠洿鏂瑰浘
     * 
     * @param img
     * @param direction
     * @return
     */
    public static float[] projectedHistogram(final Mat img, Direction direction) {
        int sz = 0;
        switch (direction) {
        case HORIZONTAL:
            sz = img.rows();
            break;

        case VERTICAL:
            sz = img.cols();
            break;

        default:
            break;
        }

        // 缁熻杩欎竴琛屾垨涓�鍒椾腑锛岄潪闆跺厓绱犵殑涓暟锛屽苟淇濆瓨鍒皀onZeroMat涓�
        float[] nonZeroMat = new float[sz];
        extractChannel(img, img, 0);
        for (int j = 0; j < sz; j++) {
            Mat data = (direction == Direction.HORIZONTAL) ? img.row(j) : img.col(j);
            int count = countNonZero(data);
            nonZeroMat[j] = count;
        }

        // Normalize histogram
        float max = 0;
        for (int j = 0; j < nonZeroMat.length; ++j) {
            max = Math.max(max, nonZeroMat[j]);
        }

        if (max > 0) {
            for (int j = 0; j < nonZeroMat.length; ++j) {
                nonZeroMat[j] /= max;
            }
        }

        return nonZeroMat;
    }

    /**
     * Assign values to feature
     * <p>
     * 鏍锋湰鐗瑰緛涓烘按骞炽�佸瀭鐩寸洿鏂瑰浘鍜屼綆鍒嗚鲸鐜囧浘鍍忔墍缁勬垚鐨勭煝閲�
     * 
     * @param in
     * @param sizeData
     *            浣庡垎杈ㄧ巼鍥惧儚size = sizeData*sizeData, 鍙互涓�0
     * @return
     */
    public static Mat features(final Mat in, final int sizeData) {

        float[] vhist = projectedHistogram(in, Direction.VERTICAL);
        float[] hhist = projectedHistogram(in, Direction.HORIZONTAL);

        Mat lowData = new Mat();
        if (sizeData > 0) {
            resize(in, lowData, new Size(sizeData, sizeData));
        }

        int numCols = vhist.length + hhist.length + lowData.cols() * lowData.rows();
        Mat out = Mat.zeros(1, numCols, CV_32F).asMat();
        FloatIndexer idx = out.createIndexer();

        int j = 0;
        for (int i = 0; i < vhist.length; ++i, ++j) {
            idx.put(0, j, vhist[i]);
        }
        for (int i = 0; i < hhist.length; ++i, ++j) {
            idx.put(0, j, hhist[i]);
        }
        for (int x = 0; x < lowData.cols(); x++) {
            for (int y = 0; y < lowData.rows(); y++, ++j) {
                float val = lowData.ptr(x, y).get() & 0xFF;
                idx.put(0, j, val);
            }
        }

        return out;
    }

    /**
     * Show image
     * 
     * @param title
     * @param src
     */
    public static void showImage(final String title, final Mat src) {
        try {
            IplImage image = src.asIplImage();
            if (image != null) {
                cvShowImage(title, image);
                cvWaitKey(0);
            }
        } catch (Exception ex) {
        }
    }
}
