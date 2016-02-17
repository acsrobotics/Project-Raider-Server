package lib;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

/**
 * For specific use of this library, please refer to this repository
 * https://github.com/GoldenTeeth/azgt_wheels/tree/master/vision/CvBackend
 * CvPipline is essentially an OpenCV wrapper library design to provide 
 * LINQ-like API to complete simple image processing task
 * Please make sure OpenCV_2.4.11 is properly installed on your computer
 * and it is added to the Build Path of this project
 * 
 * @author Zhang
 *
 */
public class CvPipeline {
	
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	LinkedList<MatOfPoint> contours;
	LinkedList<Rect>       rects;
	
	Mat Image;
	
	int iLowH;
	int iLowS;
	int iLowV;
	
	int iHighH;
	int iHighS;
	int iHighV;
	
	public CvPipeline(){
		this.contours = new LinkedList<>();
		this.rects    = new LinkedList<>();
	}
	
	public CvPipeline setLowHSV(int H, int S, int V){
		this.iLowH = H;
		this.iLowS = S;
		this.iLowV = V;
		return this;
	}
	
	public CvPipeline setHighHSV(int H, int S, int V){
		this.iHighH = H;
		this.iHighS = S;
		this.iHighV = V;
		return this;
	}
	
	public CvPipeline combineWith(final Mat second, double alpha){
		
		if(alpha > 1.0){
			return null;
		}
		
		double beta = 1.0 - alpha;
		
		Mat combined = new Mat(this.Image.size(), this.Image.type());
		
		Core.addWeighted(this.Image, alpha, second, beta, 0.0, combined);
		this.Image = combined;
		return this;
	}
	
	public CvPipeline toBGR(){
		Mat imgBGR = new Mat(this.Image.size(), this.Image.type());
		Imgproc.cvtColor(this.Image, imgBGR, Imgproc.COLOR_HSV2BGR);
		this.Image = imgBGR;
		return this;
	}
	
	public CvPipeline toGray(){
		Mat imgGray = new Mat(this.Image.size(), this.Image.type());
		Imgproc.cvtColor(this.Image, imgGray, Imgproc.COLOR_BGR2GRAY);
		this.Image = imgGray;
		return this;
		
	}
	
	public CvPipeline findContours(){
		Imgproc.findContours(this.Image, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		return this;
	}
	
	public CvPipeline computeRectsFromContours(){
		MatOfPoint2f approxCurve = new MatOfPoint2f();
		for(int i=0; i<contours.size(); i++){
			// Convert contours(i) from MatOfPoint to MatOfPoint2f
			MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(i).toArray());
			// processing on mMOP2f1 which is in type MatOfPoint2f
			double approxDistance = Imgproc.arcLength(contour2f, true) * 0.02;
			Imgproc.approxPolyDP(contour2f, approxCurve, approxDistance, true);
			
			// convert back to MatOfPoints
			MatOfPoint points = new MatOfPoint(approxCurve.toArray());
			Rect rect = Imgproc.boundingRect(points);
			if(widthHeightRatio(rect) <= 0.69){
				// filtering rectangles 
				if(rect.width > 15 && rect.height > 30){
					this.rects.add(rect);
				}
			}
		}
		return this;
	}
	
	public CvPipeline reduceRectsToOne(){
		int highestPosition = 0;
		for(int i=0; i<this.rects.size(); i++){
			highestPosition = this.rects.get(i).y > highestPosition ? this.rects.get(i).y : highestPosition;
		}
		for(int i=0; i<this.rects.size(); i++){
			if(this.rects.get(i).y < highestPosition){
				this.rects.remove(i);
			}
		}
		if(this.rects.size() > 1){
			Rect result = this.rects.pop();
			this.rects.clear();
			this.rects.add(result);
		}
		return this;
	}
	
	public List<MatOfPoint> getContours(){
		return this.contours;
	}
	
	public List<Rect> getRects(){
		return this.rects;
	}
	
	public CvPipeline drawRects(Mat drawing){
		if(drawing == null){
			drawing = Mat.zeros(this.Image.size(), CvType.CV_8UC3);
		}
		
		for(int i=0; i<rects.size(); i++){
			Rect rect = this.rects.get(i);
			Core.rectangle(drawing, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0,255,0), 2);
		}
		
		this.Image = drawing;
		return this;
	}
	
	public CvPipeline resizeTo(int width, int height){
		Mat out = new Mat();
		Imgproc.resize(this.Image, out, new Size(width, height));
		this.Image = out;
		return this;
	}
	
	public CvPipeline momentTrack(final Mat imgOriginal, final Mat imgThresholded){
		Mat imgProcessed = imgOriginal.clone();
		Size size = new Size(imgOriginal.width(), imgOriginal.height());
		Moments oMoments = Imgproc.moments(imgThresholded);
		
		double dM01 = oMoments.get_m01();
		double dM10 = oMoments.get_m10();
		double dArea = oMoments.get_m00();
		
		if(dArea > 10000){
			int posX = (int) (dM10 / dArea);
			int posY = (int) (dM01 / dArea);
			
			if(posX >=0 && posY >=0){
				Point target = new Point(posX, posY);
				Point center = new Point(size.width/2, size.height/2);
				
				Core.circle(imgProcessed, target, 15, new Scalar(0,255,0), 2);
				if(onTarget(center, target, 60)){
					Core.circle(imgProcessed, center, 60, new Scalar(0,255), 2);
				}

			}
		}
		this.Image = imgProcessed;
		return this;
	}
	
	public int[] computeRectRelativeDifference(){
		if(this.rects.size() != 1){
			int[] ret = {-1,-1};
			return ret;
		}
		int[] center = {(int) (this.Image.size().width / 2), (int) (this.Image.size().height / 2)};
		int[] rect = {this.rects.getFirst().x, this.rects.getFirst().y};
		int[] difference = {rect[0] - center[0], center[1] - rect[1]};
		return difference;
	}
	
	private double widthHeightRatio(Rect rect){
		return (double)rect.width / (double)rect.height;
	}
	
	public CvPipeline convertToThreeChannel(){
		if(this.Image.type() != CvType.CV_8UC3){
			Mat out = new Mat(this.Image.size(), CvType.CV_8UC3);
			List<Mat> in = new ArrayList<>(3);
			for(int i=0; i<3; i++){
				in.add(this.Image);
			}
			Core.merge(in, out);
			this.Image = out;
		}
		return this;
	}
	
	public CvPipeline toHSV(){
		Mat imgHSV = new Mat(this.Image.size(), this.Image.type());
		Imgproc.cvtColor(this.Image, imgHSV, Imgproc.COLOR_BGR2HSV);
		this.Image = imgHSV;
		return this;
	}
	
	public CvPipeline threshold(){
		Mat imgProcessed = new Mat(this.Image.size(), this.Image.type());
		this.Image.copyTo(imgProcessed);
		Size size = imgProcessed.size();
		Core.circle(imgProcessed, new Point(size.width/2, size.height/2), 60, new Scalar(0,0,255), 2);
		
		Mat imgThresholded = new Mat(size, imgProcessed.type());
		Core.inRange(this.Image, new Scalar(
								this.iLowH
								,this.iLowS
								,this.iLowV), 
							new Scalar(
									this.iHighH
									,this.iHighS
									,this.iHighV), imgThresholded);
		
		preProcess(imgThresholded);
		this.Image = imgThresholded;
		return this;
	}
	
	public CvPipeline invert(){
		Mat inverted = new Mat(this.Image.size(), this.Image.type());
		Imgproc.threshold(this.Image, inverted, 1, 255, Imgproc.THRESH_BINARY_INV);
		this.Image = inverted;
		return this;
	}
	
	private boolean onTarget(Point center, Point target, int radius){
		int x_diff = (int) Math.abs(center.x - target.x);
		int y_diff = (int) Math.abs(center.y - target.y);
		int distance = (int) Math.sqrt(x_diff * x_diff + y_diff * y_diff);
		if(distance < radius){
			return true;
		}else{
			return false;
		}
	}
	
	private void preProcess(Mat img){
		Imgproc.erode(img, img, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5,5)));
		Imgproc.dilate(img, img, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5,5)));
		Imgproc.dilate(img, img, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5,5)));
		Imgproc.erode(img, img, Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5,5)));
	}
	
	
	public CvPipeline writeToFileWithName(String fileName){
		Highgui.imwrite(fileName + ".jpg", this.Image);
		return this;
	}
	
	public CvPipeline getBlackEmptyMat(Mat sample){
		this.Image = new Mat(sample.size(), sample.type(), new Scalar(0,0,0));
		return this;
	}
	
	public CvPipeline setImage(Mat img){
		this.Image = img;
		return this;
	}
	
	public Mat getImage(){
		return this.Image;
	}
	
}
