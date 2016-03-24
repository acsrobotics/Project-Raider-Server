package module;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.net.NoRouteToHostException;
import java.util.ArrayList;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

/**
 * All status code update should be handled at this layer
 * @author Zhang
 *
 */
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
	
	LoggerModule proccedImgLogger;
	Thread proccedImgLoggerThread;
	
	LoggerModule thresholdedImgLogger;
	Thread thresholdedImgLoggerThread;
	
	Status status;
	
	String hostName = "C:\\Users\\Student\\temp\\XX.mp4";
	// Camera: "http://axis-camera.local/mjpg/video.mjpg"
	// Field footage: "C:\\Users\\Zhang\\Documents\\Share\\dior.mp4"
	// Just for fun: "C:\\Users\\Zhang\\Downloads\\temp\\XX.mp4"
	
	public VideoCap(ImageModule imgModule) {
		
		cap = new VideoCapture();

		cap.open(this.hostName);
		
		this.imgModule = imgModule;
		
		this.proccedImgLogger = new LoggerModule("Processed.mp4");
		this.thresholdedImgLogger = new LoggerModule("Thresholded.mp4");
		
		this.proccedImgLoggerThread = new Thread(this.proccedImgLogger);
		this.thresholdedImgLoggerThread = new Thread(this.thresholdedImgLogger);
		
		this.thresholdedImgLoggerThread.start();
		this.proccedImgLoggerThread.start();
	}
	
	public BufferedImage[] getOneFrame() throws IOException{
		Mat currentFrame = new Mat();
		ArrayList<BufferedImage> results = new ArrayList<>(3);
		
		

		
		try{		
			if (!cap.read(currentFrame)) {
				this.setStatus(Status.READ_ERROR);
				throw new NoRouteToHostException("Fetching new Frame failed");
			}
			imgModule.setImgOriginal(currentFrame);
			this.setStatus(Status.AOK);
		}catch(Exception e){
			throw e;
		}
		
		imgModule.processCurrentFrame();
		
		results.add(toBufferedImage(imgModule.getImgOriginal()));
		results.add(toBufferedImage(imgModule.getImgThresholded()));
		results.add(toBufferedImage(imgModule.getImgProcessed()));
		
		// load image to background thread to write to a file
		this.proccedImgLogger.pendingOutputImage(results.get(PROCESSED));
		this.thresholdedImgLogger.pendingOutputImage(results.get(THRESHED));
		
		return results.toArray(new BufferedImage[3]);
	}
	
	public void tryConnectCamera() throws NoRouteToHostException{
		
		// if it is already opened 
		if(cap.isOpened()){
			// close it
			cap.release();
		}
		
		// try open
		cap.open(this.hostName);
		
		// if it is not opened, keep trying
		if(!cap.isOpened()){
			this.setStatus(Status.INVALID_CAMERA);
			System.err.println("Retry");
			throw new NoRouteToHostException();
		}
		
		this.setStatus(Status.AOK);
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
	
	public void endRecording(){
		this.proccedImgLogger.RUNNING_FLAG = false;
		this.thresholdedImgLogger.RUNNING_FLAG = false;
	}

	public synchronized void setStatus(Status status) {
		this.status = status;
	}

	public synchronized Status getStatus(){
		return status;
	}

}
