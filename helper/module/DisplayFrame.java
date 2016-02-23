package module;


import java.awt.Graphics;
import java.awt.GridLayout;

import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import java.awt.event.*;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import module.VideoCap.Status;

@SuppressWarnings("serial")
public class DisplayFrame extends JFrame {
	
	private JPanel parentPane;
	
	private JPanel thresholdedPane;
	private JPanel processedPane;
	
	UpdateThread backgroundThread;
	VideoCap videoCap;
	
	public DisplayFrame (ImageModule imgModule){
		
		addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent e){
				System.err.println("Intercepting window closing signal");
				videoCap.endRecording();
				System.exit(0);
			}
		});
		
		parentPane = new JPanel(new GridLayout(0, 2));
		
		thresholdedPane = new JPanel();
		processedPane = new JPanel();
		
		parentPane.add(thresholdedPane);
		parentPane.add(processedPane);
		
		videoCap = new VideoCap(imgModule);
		
		setBounds(100,100,1280, 500);
		parentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(parentPane);
		setVisible(true);
		
		backgroundThread = new UpdateThread();
		backgroundThread.start();
		
		System.out.println("DisplayFrame initialization completed.");
	}
	
	public Status getStatus(){
		return this.videoCap.getStatus();
	}
	
	
	public void paint(Graphics g){
		try {
			
			BufferedImage[] imgs = videoCap.getOneFrame();
			
			g = thresholdedPane.getGraphics();
			g.drawImage(imgs[VideoCap.THRESHED], 0, 0, this);
			
			g = processedPane.getGraphics();
			g.drawImage(imgs[VideoCap.PROCESSED], 0, 0, this);
			
		} catch (NullPointerException e){
			System.err.println("VideoCap Error: " + "Unable to update frame");
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
