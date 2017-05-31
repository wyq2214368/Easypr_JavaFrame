package org.easypr;

import static org.bytedeco.javacpp.opencv_highgui.imread;

import java.util.Vector;

import javax.swing.JFrame;

import org.easypr.test.*;
import org.bytedeco.javacpp.opencv_core.IplImage;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.CanvasFrame;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.easypr.core.CharsIdentify;
import org.easypr.core.CharsRecognise;
import org.easypr.core.PlateDetect;
import org.easypr.core.PlateRecognize;
public class cheshi2 {		
	public static void main(String[] args) throws Exception, InterruptedException  
	{  
		String imgPath = "tmp/debug_char_auxRoi_5.jpg";

        Mat src = imread(imgPath);
        CharsIdentify charsIdentify = new CharsIdentify();
        String result = charsIdentify.charsIdentify(src, false, true);
        System.out.println(result);
	}}
