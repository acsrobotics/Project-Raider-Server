# Raider Server
This repository holds the server-side code for FRC2016 Sronghold. 
The server provides three different functionalities.

1. Expose server IP address.

2. Enable the robot to update it's configuration during the runtime.

3. Provide vision processing capability.

The main server runs three subservers
### Public Server.
It is essentially an UDP echo server that expose server's IP address to client who broadcast UDP request message since the IP address of both server and client will be configured using DHCP during the event
    
### Config Server. 
This server is a socket server. It provides necessary configuration data to the client during the runtime.
  
### Vision Server. 
This is also a socket server. It directly using the video feed from the axis camera through http://axis-camera.local. It computes the position of the local and return the camera feed status and the relative difference between the goal and the center of the camera

---------------------------------------------------------------------
### Dependency
The server utilizes [CvBackend](https://github.com/acsrobotics/CvBackend) as the backend processor. It is a wrapper library with LINQ-like syntax that provides essential functionalities for 2016 Stronghold. 
CvBackend uses [OpenCV](http://opencv.org/) library. The version number of OpenCV library being utilized is 2.4.11. It can be downloaded from [here](http://opencv.org/downloads.html). Please make sure the library is downloaded and added to the eclipse build path before any test is conducted on this project. The process of adding library to a eclipse project can be found [here](http://stackoverflow.com/questions/957700/how-to-set-the-java-library-path-from-eclipse)
