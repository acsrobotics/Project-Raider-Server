package helper.module;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.ICodec;

public class LoggerModule implements Runnable{
	
	public boolean RUNNING_FLAG = true;
	
	private ConcurrentLinkedQueue<BufferedImage> jobQueue;
	private String fileName;
	private IMediaWriter writter;
	
	private BufferedImage lastWrite;
	
	private long startTime;
	private final int FRAME_WRATE = 30;
	
	public LoggerModule(String fileName) {
		this.jobQueue = new ConcurrentLinkedQueue<>();
		this.fileName = new String(fileName);
		this.writter  = ToolFactory.makeWriter(this.fileName);
		this.writter.addVideoStream(0, 0, ICodec.ID.CODEC_ID_MPEG4, 640 / 2, 480 / 2);
		this.startTime = System.nanoTime();
		this.lastWrite = new BufferedImage(640, 480, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = this.lastWrite.createGraphics();
		g.setColor(new Color(0, 0, 0));
		g.fillRect(0, 0, 640, 480);
		g.dispose();
	}
	
	@Override
	public void run(){
		while (RUNNING_FLAG) {

			BufferedImage currentJob;

			if (jobQueue.size() == 0) {
				// if there is no job to do
				currentJob = lastWrite;
			} else {
				currentJob = convertToType(jobQueue.poll(), BufferedImage.TYPE_3BYTE_BGR);
				this.lastWrite = currentJob;
			}

			// write to file
			writter.encodeVideo(0, currentJob, System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
			try {
				Thread.sleep((long) 1000 / FRAME_WRATE);
			} catch (InterruptedException ex) {
				// ignore
			}
		}
		System.out.println("Ending recording...");
		writter.close();
	}
	
	
	public void pendingOutputImage(BufferedImage img){
		jobQueue.add(img);
	}
	
	private BufferedImage convertToType(BufferedImage sourceImage, int targetType){
		BufferedImage image;
		if(sourceImage.getType() == targetType){
			image = sourceImage;
		}else{
			image = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), targetType);
			image.getGraphics().drawImage(sourceImage, 0, 0, null);
		}
		return image;
	}
		
}
