package server.subservers;

import helper.module.DisplayFrame;
import helper.module.ImageModule;
import helper.module.VideoCap.Status;
import org.opencv.core.Core;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class VisionServer implements Runnable {
	
	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}
	
	public static final int X = 0;
	public static final int Y = 1;

	final int VISION_PORT = 3005;
	ServerSocket visionSocket;
	
	DisplayFrame displayFrame;
	ImageModule imageModule;
	int[] sharedBuffer;
	
	public VisionServer() {
		this.sharedBuffer = new int[2];
		this.imageModule  = new ImageModule(sharedBuffer);
		this.displayFrame = new DisplayFrame(imageModule);
	}
	
	@Override
	public void run(){
		try {
			visionSocket = new ServerSocket(VISION_PORT);
			System.out.println("Vision server initialized ... ");
			while(true){
				Socket client = visionSocket.accept();
				System.out.println("Recieved client request...");
				
				Status statusCode = this.displayFrame.getStatus();
				
				System.out.println("Camera status: " + statusCode.getDeclaringClass().getName());
				
				if(statusCode.equals(Status.INVALID_CAMERA)){
					// TODO Handle error
				}
				
				if(statusCode.equals(Status.READ_ERROR)){
					// TODO Handle error
				}
				
				String content = "{"
								+ "\"status\":" + statusCode.getCode() + ","
								+ "\"x_diff\":" + this.getDifference(X) + ","
								+ "\"y_diff\":" + this.getDifference(Y) 
								+ "}";
				
				PrintWriter out = new PrintWriter(client.getOutputStream());
				out.write(content);
				System.out.println("Vision Server >> Message out");
				out.close();
				client.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private synchronized int getDifference(int index){
		return this.sharedBuffer[index];
	}
	
}

