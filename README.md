# Non-blocking-Java-HTTPS-Server-With-No-External-Framework
 A simple vanilla non-blocking Java HTTPS server (No external framework) 
 

### 1) Compile and Run WebServer.java:                                                                                             
     * Compile in verbose mode as:
       javac WebServer.java -g -verbose  && java WebServer

     * Run/Start Server  as:
       java WebServer
     
 ### 2) Generate the keystore.jksfile (line 479 of WebServer.js) use the commands below on Linux/Ubuntu and MacOS  (or equivalent commands on Windows):                                                                                            
     * Command 1
     openssl pkcs12 -export -out keystore.p12 -inkey key.pem -in cert.pem
     
     * Command 2
     keytool -importkeystore -destkeystore keystore.jks -srcstoretype PKCS12 -srckeystore keystore.p12
     
     where:
     key.pem is the SSL/TLS key file.
     cert.pem is the SSL/TLS certificate file.


# License

Copyright © 2023. MongoExpUser

Licensed under the MIT license.
