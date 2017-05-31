package org.easypr;		
		
		
import java.io.*;
import java.util.Vector;

import javax.swing.JFrame;

import static org.bytedeco.javacpp.opencv_core.*;

import org.bytedeco.javacpp.opencv_core.Mat;
import org.bytedeco.javacv.CanvasFrame;
//import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.easypr.core.CharsRecognise;
import org.easypr.core.PlateDetect;
import org.easypr.core.PlateLocate;		
import org.easypr.test.*;		
import org.easypr.util.Convert;		

import static org.bytedeco.javacpp.opencv_highgui.*;
public class Main {		
		
	
//	import static org.bytedeco.javacpp.opencv_highgui.imread;

//	import java.util.Vector;

	//import javax.swing.JFrame;

	//import org.bytedeco.javacv.OpenCVFrameGrabber;  
	//import org.bytedeco.javacpp.opencv_core.Mat;
	//import org.bytedeco.javacv.CanvasFrame;
	//import org.bytedeco.javacv.Frame;
	//import org.bytedeco.javacv.OpenCVFrameConverter;  
	//import org.bytedeco.javacv.FrameGrabber.Exception;
	//import org.easypr.core.CharsRecognise;
	//import org.easypr.core.PlateDetect;

	  
	/** 
	 * 调用本地摄像头窗口视频 
	 * @author wyq   
	 * @version 2016年6月13日   
	 * @see javavcCameraTest   ``
	 */  
	  

	public static void main(String[] args) throws Exception, InterruptedException  
	{  
	    OpenCVFrameGrabber grabber = new OpenCVFrameGrabber(0);    
	    grabber.start();   //开始获取摄像头数据  
	    CanvasFrame canvas = new CanvasFrame("摄像头");//新建一个窗口  
	    canvas.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
	    canvas.setAlwaysOnTop(true);  
	    
	    CanvasFrame lunkuo = new CanvasFrame("轮廓");//新建一个窗口  
	    lunkuo.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
	    lunkuo.setAlwaysOnTop(true);  
	    
	    CanvasFrame chepai = new CanvasFrame("车牌");//新建一个窗口  
	    chepai.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  
	    chepai.setAlwaysOnTop(true);  
	    
	    IplImage grabbedImage = grabber.grab();
	    Mat matImage = new Mat(grabbedImage);
	    
	  
	
	
	 
	 
	  
	    while(true)  
	    {  canvas.showImage(grabber.grab());
	        if(!canvas.isDisplayable())  
	        {//窗口是否关闭  
	            grabber.stop();//停止抓取  
	            System.exit(2);//退出  
	        }  
	        PlateDetect plateDetect = new PlateDetect();
		    plateDetect.setPDLifemode(true);
		    Vector<Mat> matVector = new Vector<Mat>();
		    Vector<String> SVector = new Vector<String>();
		    if (0 == plateDetect.plateDetect(matImage, matVector)) {
                lunkuo.showImage(new Mat(imread("tmp/debug_result.jpg")));	 
              
		    	CharsRecognise cr = new CharsRecognise();
		   
		        
		        for (int i = 0; i < matVector.size(); ++i) {
		       
		        	chepai.showImage(matVector.get(i));
		        	
		            String result = cr.charsRecognise(matVector.get(i));
		            String plateType = cr.getPlateType(matVector.get(i));
		            System.out.println("车牌识别结果: " +plateType+"--"+result);
		            
		        }
		    }		  
	       
	       
	        canvas.showImage(grabber.grab());//获取摄像头图像并放到窗口上显示， 这里的Frame frame=grabber.grab(); frame是一帧视频图像  
	  
	    
	        Thread.sleep(30);//50毫秒刷新一次图像  
	    }  
	}
}
	 