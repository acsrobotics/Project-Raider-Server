package helper.lib;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.objdetect.CascadeClassifier;

import helper.lib.FilterPipeline.Filter;

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
public class CvEngine {
	
	// TODO make a operation interface 
	
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	LinkedList<MatOfPoint>  contours;
	LinkedList<Rect>        rects;
	LinkedList<RotatedRect> ellipses;
	
	LinkedList<Point>       startPoints;
	LinkedList<Point>       endPoints;
	
	Mat Image;
	
	int iLowH;
	int iLowS;
	int iLowV;
	
	int iHighH;
	int iHighS;
	int iHighV;
	
	FilterPipeline filters;
	
	public CvEngine(){
		this.contours = new LinkedList<>();
		this.rects    = new LinkedList<>();
		this.ellipses = new LinkedList<>();
		this.startPoints = new LinkedList<>();
		this.endPoints   = new LinkedList<>();
		this.filters  = new FilterPipeline();
		this.filters.injectPipeDependency(this);
	}
	
	public CvEngine setLowHSV(int H, int S, int V){
		this.iLowH = H;
		this.iLowS = S;
		this.iLowV = V;
		return this;
	}
	
	public CvEngine setHighHSV(int H, int S, int V){
		this.iHighH = H;
		this.iHighS = S;
		this.iHighV = V;
		return this;
	}
	
	public CvEngine combineWith(final Mat second, double alpha){
		
		if(alpha > 1.0){
			return null;
		}
		
		double beta = 1.0 - alpha;
		
		Mat combined = new Mat(this.Image.size(), this.Image.type());
		
		Core.addWeighted(this.Image, alpha, second, beta, 0.0, combined);
		this.Image = combined;
		return this;
	}
	
	
	public CvEngine detectEdge(double lowThresh, double highThresh){
		
		Mat mask   = new Mat(this.Image.size(), this.Image.type());
		Mat detect = new CvEngine().getBlackEmptyMat(this.Image).getImage();
		
		Imgproc.Canny(this.Image, mask, lowThresh, highThresh);
		
		this.Image.copyTo(detect, mask);
		this.Image = detect;
		
		return this;
	}
	
	public CvEngine gaussianBlur(Size ksize, double sigmaX, double sigmaY){
		
		Mat imgBlurred = new Mat(this.Image.size(), this.Image.type());
		Imgproc.GaussianBlur(this.Image, imgBlurred, ksize, sigmaX, sigmaY);
		this.Image = imgBlurred;
		return this;
	}
	
	public CvEngine gassianBlur(Size ksize){
		return gaussianBlur(ksize, 0, 0);
	}
	
	public CvEngine toBGR(){
		Mat imgBGR = new Mat(this.Image.size(), this.Image.type());
		Imgproc.cvtColor(this.Image, imgBGR, Imgproc.COLOR_HSV2BGR);
		this.Image = imgBGR;
		return this;
	}
	
	public CvEngine toGray(){
		Mat imgGray = new Mat(this.Image.size(), this.Image.type());
		Imgproc.cvtColor(this.Image, imgGray, Imgproc.COLOR_BGR2GRAY);
		this.Image = imgGray;
		return this;
		
	}
	
	public CvEngine findContours(){
		Imgproc.findContours(this.Image, contours, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);
		return this;
	}
	
	
	
	public CvEngine computeRectsFromContours(){
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
			
			if(filters.eval(rect)){
				this.rects.add(rect);
			}
			
		}
		return this;
	}
	

	public CvEngine detectEllipse(){
		for(int i=0; i<this.contours.size(); i++){
			if(this.contours.get(i).toList().size() > 5){
				MatOfPoint2f contour2f = new MatOfPoint2f(contours.get(i).toArray());
				this.ellipses.add(Imgproc.fitEllipse(contour2f));
			}
		}
		
		return this;
	}
	
	public CvEngine drawEllipses(Mat canvas){
		
		Scalar color = new Scalar(0, 255, 0);
		for(RotatedRect r : this.ellipses){
			Core.ellipse(canvas, r, color, 2, 8);
		}
		this.Image = canvas;
		return this;
	}
	
	public CvEngine drawCircleOnCenter(){
		if(this.rects.size() != 0){
			double x_center = this.Image.size().width / 2;
			double y_center = this.Image.size().height / 2;
			
			// set the radius of the circle to 70% of the rectangle's width 
			double radius = this.rects.getFirst().width / 2 * 0.75;
			
			Scalar color = onTarget(x_center, y_center, radius) ? 
							new Scalar(0, 255, 0) : new Scalar(0, 0, 255);
							
			Core.circle(this.Image, new Point(x_center, y_center), (int) radius, color, 2);
		}
		
		return this;
	}
	
	public CvEngine detectLinesQuick(int threshold, int minLineSize, int lineGap){
		
		Mat lines = new Mat();
		
		Imgproc.HoughLinesP(this.Image, lines, 1, Math.PI / 180, threshold, minLineSize, lineGap);
		
		for(int i = 0; i<lines.cols(); i++){
			double[] vec = lines.get(0, i);
			double x1 = vec[0],
					y1 = vec[1],
					x2 = vec[2],
					y2 = vec[3];
			this.startPoints.add(new Point(x1, y1));
			this.endPoints.add(new Point(x2, y2));
		}
		
		return this;
	}
	
	public CvEngine detectLines(int threshold, int minLineSize, int lineGap){
		
		MatOfPoint2f lines = new MatOfPoint2f();
		
		Imgproc.HoughLines(this.Image, lines, 1, Math.PI / 180, threshold, minLineSize, lineGap);
		
		for(int i = 0; i<lines.cols(); i++){
			double[] vec = lines.get(0, i);
			double rho  = vec[0],
					theta = vec[1];
			
			double a = Math.cos(theta), b = Math.sin(theta);
			
			double x0 = a*rho, y0 = b*rho;
			
			this.startPoints.add(new Point(Math.round(x0 + 1000*(-b)), Math.round(y0 + 1000 * (a))));
			this.endPoints.add(new Point(Math.round(x0 - 1000*(-b)), Math.round(y0 - 1000 * (a))));
		}
		
		return this;
	}
	
	public CvEngine drawLines(Mat canvas){
		for(int i=0; i<this.startPoints.size(); i++){
			Core.line(canvas, this.startPoints.get(i), this.endPoints.get(i), new Scalar(0, 255, 0), 3);
		}
		this.startPoints.clear();
		this.endPoints.clear();
		this.Image = canvas;
		return this;
	}
	
	public CvEngine reduceRectsToOne(){
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
	
	public CvEngine drawRects(Mat drawing){
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
	
	public CvEngine resizeTo(int width, int height){
		Mat out = new Mat();
		Imgproc.resize(this.Image, out, new Size(width, height));
		this.Image = out;
		return this;
	}
	
	public CvEngine detectWithClassifier(CascadeClassifier classifier){
		Mat frame = new Mat();
		Imgproc.equalizeHist(this.Image, frame);
		MatOfRect rects = new MatOfRect();
		classifier.detectMultiScale(frame, rects, 1.1, 2, 0, new Size(30,30), new Size(300, 300));
		for(Rect r : rects.toArray()){
			if(filters.eval(r)){
				this.rects.add(r);
			}
		}
		return this;
	}
	
	public CvEngine momentTrack(final Mat imgOriginal, final Mat imgThresholded){
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
	
	public CvEngine addFilter(Filter filter){
		this.filters.addFilter(filter);
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
	
	
	/**
	 * calculate whether the circle is on target(rectangle) or not
	 * @param x_center
	 * @param y_center
	 * @param radius
	 * @return
	 */
	private boolean onTarget(double x_center, double y_center, double radius){
		// calculate the order in x_left, x_right, y_top, y_bottom
		Rect rect = this.rects.getFirst();
		double[] rectangle = {
				rect.x,
				rect.x + (rect.width),
				rect.y,
				rect.y + (rect.height )
		};
		
		double[] circle = {
				x_center - radius,
				x_center + radius,
				y_center - radius,
				y_center + radius
		};
		
		boolean status;
		
		
		// if circle is within the rectangle 
		if(rectangle[0] < circle[0] &&
			rectangle[1] > circle[1] &&
			rectangle[2] < circle[2] &&
			rectangle[3] > circle[3]){
			status = true;
		}else{
			status = false;
		}
		
		return status;
	}
	
	public CvEngine convertToThreeChannel(){
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
	
	public CvEngine toHSV(){
		Mat imgHSV = new Mat(this.Image.size(), this.Image.type());
		Imgproc.cvtColor(this.Image, imgHSV, Imgproc.COLOR_BGR2HSV);
		this.Image = imgHSV;
		return this;
	}
	
	public CvEngine threshold(){
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
	
	public CvEngine invert(){
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
	
	
	public CvEngine writeToFileWithName(String fileName){
		Highgui.imwrite(fileName + ".jpg", this.Image);
		return this;
	}
	
	public CvEngine getBlackEmptyMat(Mat sample){
		this.Image = new Mat(sample.size(), sample.type(), new Scalar(0,0,0));
		return this;
	}
	
	public CvEngine setImage(Mat img){
		this.Image = img;
		return this;
	}
	
	public Mat getImage(){
		return this.Image;
	}
	
}
