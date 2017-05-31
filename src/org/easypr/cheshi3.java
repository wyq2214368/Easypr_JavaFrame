package org.easypr;

import static org.bytedeco.javacpp.opencv_highgui.imread;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.easypr.core.CharsRecognise;
import org.easypr.train.ANNTrain;
import org.easypr.train.SVMTrain;
public class cheshi3 {		
	public static void main(String[] args) throws Exception, InterruptedException  
	{ 
	//	ANNTrain a = new ANNTrain();
		//a.annMain();
		SVMTrain test2 = new SVMTrain();
		test2.svmTrain(false,false);
		}
}
