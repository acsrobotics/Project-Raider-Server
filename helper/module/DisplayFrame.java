package module;


import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;

import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.NoRouteToHostException;
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
			
		} catch (Exception e){
			
			//--------Paint the panel black and display "SIGNAL LOST"----------------//
			BufferedImage img = new BufferedImage(640, 480, BufferedImage.TYPE_3BYTE_BGR);
			Graphics2D g2d = img.createGraphics();
			g2d.setColor(new Color(0, 0, 0, 0));
			g2d.fillRect(0, 0, 640, 480);
			
			g2d.setPaint(Color.red);
			g2d.setFont(new Font("Serif", Font.BOLD, 20));
			String s = "SIGNAL LOST";
			FontMetrics fm = g2d.getFontMetrics();
			int x = (img.getWidth() - fm.stringWidth(s))/2;
			int y = img.getHeight() / 2;
			g2d.drawString(s, x, y);
			
			g2d.dispose();
			//----------------------------------------------------------------------------//
			
			
			// update the image display panel 
			g = thresholdedPane.getGraphics();
			g.drawImage(img, 0, 0, this);
			
			g = processedPane.getGraphics();
			g.drawImage(img, 0, 0, this);
			
			e.printStackTrace();
			try {
				videoCap.tryConnectCamera();
			} catch (NoRouteToHostException e1) {
				// TODO Auto-generated catch block
				try {
					Thread.sleep(3000);
					videoCap.tryConnectCamera();
				} catch (Exception e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
			}
		} 
	}
	
	class UpdateThread extends Thread{
		public void run(){
			while(true){
				repaint();
				try{
					Thread.sleep(35);
				}catch (InterruptedException e){
					break;
				}
			}
		}
	}


}
