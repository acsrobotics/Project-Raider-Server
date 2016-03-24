package subservers;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Config Server is designed to provide essential configuration
 * data to client during the runtime. By changing the value in "config.jsos"
 * file, one can change the behavior of the robot without rebooting it.
 * @author Zhang
 *
 */
public class ConfigServer implements Runnable {
	
	final int CONFIG_PORT = 3004;
	ServerSocket configSocket;
	
	public void run(){
		try {
			configSocket = new ServerSocket(CONFIG_PORT);
			System.out.println("Config server initialized ... ");
			while(true){
				Socket client = configSocket.accept();
				System.out.println("Recieved client request...");
				String content = read("config.json");
				
				Scanner in = new Scanner(client.getInputStream());
				PrintWriter out = new PrintWriter(client.getOutputStream());
				
//				String str = "";
//				try{
//					str = in.nextLine();
//				}catch(NoSuchElementException e){
//					e.printStackTrace();
//				}finally{
//					System.out.println(str);
//				}
//				
				out.write(content);
				System.out.println("Config Server >> Message out");
				out.close();
				client.close();
				in.close();
			}
		} catch (IOException e) {
		
			e.printStackTrace();
		}
	}	
	
	private String read(String fileName){
		String text = "";
		try{
			StringBuilder sb = new StringBuilder();
			BufferedInputStream input = 
					new BufferedInputStream(new FileInputStream(fileName));
			while(input.available() > 0){
				sb.append((char)input.read());
			}
			text = sb.toString();
			input.close();
		} catch (IOException ex){
			System.out.println("Unable to perform IO operations\n"
					+ "Please make sure the input file name is correct");
		}
		return text;
	}
	
}
