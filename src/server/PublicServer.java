package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
/**
 * Public Server is designed to expose server's IP address to client 
 * since FRC changed the network configuration from static address
 * to DHCP.
 * This is basically an echo server that accepts UDP packet and 
 * send it back to where it came from.
 * @author Zhang
 *
 */
public class PublicServer implements Runnable {
	
	public final int ADDR_PORT = 3003;
	private DatagramSocket addrSocket;
	private DatagramPacket packet;
	
	
	public void run(){
		try {
			addrSocket = new DatagramSocket(ADDR_PORT);
			
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		packet = new DatagramPacket(new byte[100], 10);
		System.out.println("udp server is running...");
		while(true){
			try{
				addrSocket.receive(packet);
				System.out.println("Packet recieved on port " + packet.getPort());
				packet.setLength(0);
				packet.setData(new byte[0]);
				addrSocket.send(packet);
			}catch(IOException ex){
				ex.printStackTrace();
			}
		}
	}
	
}
