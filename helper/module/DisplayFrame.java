package module;

import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import module.VideoCap.Status;

@SuppressWarnings("serial")
public class DisplayFrame extends JFrame {
	
	private JPanel parentPane;
	
	private JPanel originalPane;
	private JPanel thresholdedPane;
	private JPanel processedPane;
	
	
	UpdateThread backgroundThread;
	VideoCap videoCap;
	
	public DisplayFrame (ImageModule imgModule){
		
		parentPane = new JPanel(new GridLayout(2, 2));
		
		originalPane = new JPanel();
		thresholdedPane = new JPanel();
		processedPane = new JPanel();
		
		parentPane.add(originalPane);
		parentPane.add(thresholdedPane);
		parentPane.add(processedPane);
		
		videoCap = new VideoCap(imgModule);
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100,100,1000, 750);
		parentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(parentPane);
		
		backgroundThread = new UpdateThread();
		backgroundThread.start();
	}
	
	public Status getStatus(){
		return this.videoCap.getStatus();
	}
	
	public void paint(Graphics g){
		try {
			
			BufferedImage[] imgs = videoCap.getOneFrame();
			
			g = originalPane.getGraphics();
			g.drawImage(imgs[VideoCap.RAW], 0, 0, this);
			
			g = thresholdedPane.getGraphics();
			g.drawImage(imgs[VideoCap.THRESHED], 0, 0, this);
			
			g = processedPane.getGraphics();
			g.drawImage(imgs[VideoCap.PROCESSED], 0, 0, this);
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	class UpdateThread extends Thread{
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
