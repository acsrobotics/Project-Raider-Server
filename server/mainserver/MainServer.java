package mainserver;

import subservers.*;

/**
 * The main server runs three subservers
 * 1. Public Server, it is essentially an UDP echo server that 
 *    expose server's IP address to client who broadcast UDP 
 *    request message since the IP address of both server and 
 *    client will be configured using DHCP during the event
 *    
 * 2. Config Server. This server is a socket server. It provides 
 *    necessary configuration data to the client during the runtime.
 *    
 * 3. Vision Server. This is also a socket server. It directly using 
 *    the video feed from the axis camera through http://axis-camera.local
 *    It computes the position of the local and return the camera feed
 *    status and the relative difference between the goal and the 
 *    center of the camera
 *    
 * @author Zhang
 *
 */
public class MainServer {

	public static void main(String[] args) {
		Thread addrServerThread = new Thread(new PublicServer());
		Thread configServerThread = new Thread(new ConfigServer());
		Thread visionServerThread = new Thread(new VisionServer());
		
		addrServerThread.start();
		configServerThread.start();
		visionServerThread.start();
		Thread[] ta = {addrServerThread, configServerThread, visionServerThread};
		OuterWhile: while (true) {
			for (Thread t : ta) {
				if (t.isAlive()) {
					while (!t.interrupted()) {
						try {
							Thread.currentThread().sleep(1);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				} else if (!t.isAlive()) {
					System.out.println("Terminating main thread: '" + Thread.currentThread().getName() + "' because all of the"
							+ " child server threads have died or exited.");
					break OuterWhile;
				}
			}
		}
	}

}
