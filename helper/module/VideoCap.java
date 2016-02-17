package module;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

public class VideoCap {
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	

	public enum Status{
		AOK(0),
		INVALID_CAMERA(1),
		READ_ERROR(2);
		
		private final int code;
		
		Status(int code){
			this.code = code;
		}
		
		public int getCode(){
			return this.code;
		}
		
	}
	
	
	public static final int RAW       = 0;
	public static final int THRESHED  = 1;
	public static final int PROCESSED = 2;
	
	VideoCapture cap;
	ImageModule imgModule;
	
	Status status;
	
	public VideoCap(ImageModule imgModule) {
		cap = new VideoCapture();
		cap.open("http://axis-camera.local/mjpg/video.mjpg");
		if(!cap.isOpened()){
			this.setStatus(Status.INVALID_CAMERA);
		}
		this.imgModule = imgModule;
	}
	
	public BufferedImage[] getOneFrame() throws IOException{
		Mat currentFrame = new Mat();
		if(!cap.read(currentFrame)){
			this.setStatus(Status.READ_ERROR);
			return null;
		}
		
		this.setStatus(Status.AOK);
		
		imgModule.setImgOriginal(currentFrame);
		imgModule.processCurrentFrame();
		
		ArrayList<BufferedImage> results = new ArrayList<>(3);
		results.add(toBufferedImage(imgModule.getImgOriginal()));
		results.add(toBufferedImage(imgModule.getImgThresholded()));
		results.add(toBufferedImage(imgModule.getImgProcessed()));
		
		return results.toArray(new BufferedImage[3]);
	}
	
	public BufferedImage toBufferedImage(Mat mat) throws IOException {
		int type = 0;
		if (mat.channels() == 1) {
			type = BufferedImage.TYPE_BYTE_GRAY;
		} else if (mat.channels() == 3) {
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		BufferedImage img = new BufferedImage(640, 480, type);
		WritableRaster raster = img.getRaster();
		DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
		byte[] data = dataBuffer.getData();
		mat.get(0, 0, data);
		return img;
	}
	


	public synchronized void setStatus(Status status) {
		this.status = status;
	}

	public synchronized Status getStatus(){
		return status;
	}

}
