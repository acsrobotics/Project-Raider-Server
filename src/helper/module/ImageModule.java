package helper.module;

import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.objdetect.CascadeClassifier;

import helper.lib.CvEngine;

public class ImageModule{
	
	int[] relative_position;
	
	Mat imgOriginal;
	Mat imgThresholded;
	Mat imgProcessed;
	
	CascadeClassifier classifier;
	
	public ImageModule(int[] sharedBuffer){
		this.imgOriginal 		= new Mat();
		this.imgThresholded 	= new Mat();
		this.imgProcessed 		= new Mat();
		this.relative_position 	= sharedBuffer;
		
		//----------Uncommented following lines for facial detecton------//
//		classifier = new CascadeClassifier();
//		classifier.load("haarcascade_frontalface_alt.xml");
		
	}
	
	public void processCurrentFrame(){
		CvEngine processor = new CvEngine();
		Mat imgInput = new Mat();
		
		imgInput = processor
					.setImage(this.getImgOriginal())
					.getImage();
		
		this.setImgOriginal(processor.resizeTo(640, 480).getImage());
		
		//-----------Demo: Simple color-------------------//
//		this.setImgThresholded(processor
//								.setImage(imgInput)
//								.convertToThreeChannel()
//								.toHSV()
//								.setLowHSV(0, 0, 0)
//								.setHighHSV(150, 90, 70)
//								.threshold()
//								.convertToThreeChannel()
//								.resizeTo(640, 480)
//								.getImage());
//		
//		this.setImgProcessed(processor
//							.toBGR()
//							.toGray()
//							.findContours()
//							.computeRectsFromContours()
//							.drawRects(imgInput)
//							.resizeTo(640, 480)
//							.getImage());

		
		//------------------Demo: Face detection------------//
		

		
//		Mat img = processor
//				.setImage(imgInput)
//				.convertToThreeChannel()
//				.toGray()
//				.detectFaces(classifier)
//				.drawRects(imgInput)
//				.drawCircleOnCenter()
//				.resizeTo(640, 480)
//				.getImage();
//		
//		this.setImgOriginal(img);
//		this.setImgProcessed(img);
//		this.setImgThresholded(processor.getBlackEmptyMat(imgInput).getImage());
//		
		//----------------- Game Code V1 ----------------//
		
//		Mat imgThBlack = processor
//						.setLowHSV(1, 1, 1)
//						.setHighHSV(179, 255, 75)
//						.setImage(imgInput)
//						.toHSV()
//						.threshold()
//						.getImage();
//		
//		this.setImgThresholded(processor
//								.resizeTo(640, 480)
//								.getImage());
//		
//		
//		this.setImgProcessed(processor
//							.setImage(imgThBlack)
//							.convertToThreeChannel()
//							.toBGR()
//							.toGray()
//							.findContours()
//							.addFilter((p, r) -> {
//								double radio = (double) r.width / (double)r.height;
//								return radio <= 0.70 && radio >= 0.45 ? true : false;
//							})
//							.addFilter((p, r) -> r.width > 20 && r.height > 30 ? true : false)
//							.addFilter((p, r) -> {
//								Mat img = p.getImage();
//								return isAtTheRim(img, r, 50);
//							})
//							.computeRectsFromContours()
//							.reduceRectsToOne()
//							.drawRects(imgInput)
//							.drawCircleOnCenter()
//							.resizeTo(640, 480)
//							.getImage());

		//-------------- Game Code V2 ----------------------//
		
		// TODO Improve performance 

		this.setImgThresholded(processor
								.setImage(imgInput.clone())
								.convertToThreeChannel()
								.toGray()
								.gaussianBlur(new Size(5, 5), 0, 0)
								.detectEdge(30, 170)
								.resizeTo(640, 480)
								.getImage());
		
		this.setImgProcessed(processor
								.detectLinesQuick(150, 30, 50)
								.drawLines(imgInput)
								.resizeTo(640, 480)
								.getImage());	
		
		this.updateSharedBuffer(processor.computeRectRelativeDifference());
	}
	
	private static boolean isAtTheRim(Mat img, Rect rect, int width){
		Size size = img.size();
		// order: x-left, x-right, y-top, y-bottom
		int[] rim = {
			width,
			(int) (size.width - width),
			width,
			(int) (size.height - width)
		};
		if(rect.x > rim[0] && rect.x < rim[1] 
			&& rect.y > rim[2] && rect.y < rim[3]){
			return true;
		}else{
			return false;
		}
	}
	
	private synchronized void updateSharedBuffer(int[] sharedBuffer){
		this.relative_position = sharedBuffer;
	}
	
	public synchronized int[] getRelativePosition(){
		return this.relative_position;
	}
	
	
	
	public synchronized void setImgOriginal(Mat imgOriginal) {
		this.imgOriginal = imgOriginal;
	}

	public synchronized void setImgThresholded(Mat imgThresholded) {
		this.imgThresholded = imgThresholded;
	}

	public synchronized void setImgProcessed(Mat imgProcessed) {
		this.imgProcessed = imgProcessed;
	}



	public synchronized Mat getImgOriginal() {
		return imgOriginal;
	}

	public synchronized Mat getImgThresholded() {
		return imgThresholded;
	}

	public synchronized Mat getImgProcessed() {
		return imgProcessed;
	}
	
	
}





