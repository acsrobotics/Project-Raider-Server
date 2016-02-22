package module;

import org.opencv.core.Mat;

import lib.CvPipeline;

public class ImageModule{
	
	int[] relative_position;
	
	Mat imgOriginal;
	Mat imgThresholded;
	Mat imgProcessed;
	
	
	public ImageModule(int[] sharedBuffer){
		this.imgOriginal 		= new Mat();
		this.imgThresholded 	= new Mat();
		this.imgProcessed 		= new Mat();
		this.relative_position 	= sharedBuffer;
	}
	
	public void processCurrentFrame(){
		CvPipeline processor = new CvPipeline();
		Mat imgInput = new Mat();
		
		imgInput = processor
					.setImage(this.getImgOriginal())
					.getImage();
		
		this.setImgOriginal(processor.resizeTo(640, 480).getImage());
		
		
//		this.setImgThresholded(processor
//								.setImage(imgInput)
//								.convertToThreeChannel()
//								.toHSV()
//								.setLowHSV(0, 0, 0)
//								.setHighHSV(150, 90, 70)
//								.threshold()
//								.convertToThreeChannel()
//								.getImage());
//		
//		this.setImgProcessed(processor
//							.toBGR()
//							.toGray()
//							.findContours()
//							.computeRectsFromContours()
//							.drawRects(imgInput)
//							.getImage());
		
		
		// thresh red
		
		Mat imgThRed = processor
						.setImage(imgInput)
						.toHSV()
						.setLowHSV(150, 120, 40)
						.setHighHSV(179, 255, 255)
						.threshold()
						.getImage();
		
		// thresh black
		
		Mat imgThBlack = processor
						.setLowHSV(1, 1, 1)
						.setHighHSV(179, 255, 75)
						.setImage(imgInput)
						.toHSV()
						.threshold()
						.getImage();
		
		// thresh green
		
		Mat imgThGreen = processor
						.setLowHSV(90, 110, 85)
						.setHighHSV(140, 255, 255)
						.setImage(imgInput)
						.toHSV()
						.threshold()
						.getImage();
		
		// combined 
		
		Mat imgCombined = processor
							.getBlackEmptyMat(imgInput)
							.convertToThreeChannel()
							.getImage();
		
		Mat tempThreasholed = processor
								.setLowHSV(1, 1, 1)
								.setHighHSV(179, 255, 255)
								.setImage(imgCombined)
								.combineWith(new CvPipeline()
												.setImage(imgThBlack)
												.convertToThreeChannel()
												.getImage(), 0.0)
								.combineWith(new CvPipeline()
													.setImage(imgThGreen)
													.convertToThreeChannel()
													.getImage(), 0.5)
								.threshold()
								.convertToThreeChannel()
								.combineWith(new CvPipeline()
													.setImage(imgThRed)
													.convertToThreeChannel()
													.getImage(), 0.5)
								.setLowHSV(0, 0, 0)
								.setHighHSV(179, 255, 10)
								.threshold()
								.invert()
								.getImage();
		
		this.setImgThresholded(processor
								.resizeTo(640, 480)
								.getImage());
		
		
		this.setImgProcessed(processor
							.setImage(tempThreasholed)
							.convertToThreeChannel()
							.toBGR()
							.toGray()
							.findContours()
							.computeRectsFromContours()
							.reduceRectsToOne()
							.drawRects(imgInput)
							.drawCircleOnCenter()
							.resizeTo(640, 480)
							.getImage());
		
		this.updateSharedBuffer(processor.computeRectRelativeDifference());
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





