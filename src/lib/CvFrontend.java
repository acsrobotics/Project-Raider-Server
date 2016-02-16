package lib;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.highgui.*;

public class CvFrontend {
	
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
}
class Mat2Image{
	Mat mat = new Mat();
	BufferedImage img;
	byte[] data;
	public Mat2Image(){}
	
	public Mat2Image(Mat mat){
		getSpace(mat);
	}
	
	public void getSpace(Mat mat){
		this.mat = mat;
		int w = mat.cols()
				, h = mat.rows();
		if(data == null || data.length != w * h  * 3){
			data = new byte[w * h * 3];
		}
		if(img == null || img.getWidth() != w || img.getHeight() != h
				|| img.getType() != BufferedImage.TYPE_3BYTE_BGR){
			img = new BufferedImage(w,  h, BufferedImage.TYPE_3BYTE_BGR);
		}
	}
	
	public BufferedImage toBufferedImage(Mat mat) throws IOException{
		int type = 0;
		if(mat.channels() == 1){
			type = BufferedImage.TYPE_BYTE_GRAY;
		}else if(mat.channels() == 3){
			type = BufferedImage.TYPE_3BYTE_BGR;
		}
		BufferedImage img = new BufferedImage(640, 480, type);
		WritableRaster raster = img.getRaster();
		DataBufferByte dataBuffer = (DataBufferByte) raster.getDataBuffer();
		byte[] data = dataBuffer.getData();
		mat.get(0, 0,data);
		return img;
	}
	
}

class VideoCap{
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	VideoCapture cap;
	Mat2Image mat2Img = new Mat2Image();
	Mat img;
//	ImgProc processsor;
	
	public VideoCap() {
		cap = new VideoCapture();
		//cap.open("http://10.47.16.65/mjpg/video.mjpg");
		cap.open("C:\\Users\\Zhang\\Documents\\Share\\SampleVideo_720x480_1mb.mp4");
//		processsor = new ImgProc();
//		processsor.setLowHSV(179, 255, 255);
//		processsor.setHighHSV(179, 255, 255);
	}
	
	public BufferedImage getOneFrame() throws IOException{
		if(!cap.read(mat2Img.mat)){
			return null;
		}
		//mat2Img.mat = processsor.process(mat2Img.mat);
		return mat2Img.toBufferedImage(mat2Img.mat);
	}
	
}


@SuppressWarnings("serial")
class TestFrame extends JFrame {
	private JPanel contentPane;
	TestThread th;
	
	public TestFrame (){
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100,100,650, 490);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		th = new TestThread();
		th.start();
	}
	
	public void Observing(){
		if(TestThread.interrupted()){
			this.setVisible(false);
		}
	}
	
	VideoCap videoCap = new VideoCap();
	
	public void paint(Graphics g){
		g = contentPane.getGraphics();
		try {
			BufferedImage img = videoCap.getOneFrame();
			if(img == null){
				Thread.currentThread().interrupt();
			}
			g.drawImage(videoCap.getOneFrame(), 0, 0, this);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	class TestThread extends Thread{
		public void run(){
			while(true){
				repaint();
				try{
					Thread.sleep(30);
				}catch (InterruptedException e){
					break;
				}
			}
		}
	}
	
}




