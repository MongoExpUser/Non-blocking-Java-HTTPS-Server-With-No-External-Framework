# Non-blocking-Java-HTTPS-Server
 A simple vanilla non-blocking Java HTTPS server (No external framework) 

 ### A Vanilla Java HTTPS Server's Rendered Page
![Image description](https://github.com/MongoExpUser/Non-blocking-Java-HTTPS-Server-With-No-External-Framework/blob/main/page.png)


### 1) Compile and Run WebServer.java:                                                                                             
     * Compile in verbose mode as:
       javac WebServer.java -g -verbose 

     * Run/Start Server  as:
       java WebServer
     
 ### 2) Generate the keystore.jks file (On line 480 of WebServer.java):
     * Use the commands below on Linux/Ubuntu and MacOS  (or equivalent commands on Windows): 
 
     ** Command 1
        openssl pkcs12 -export -out keystore.p12 -inkey key.pem -in cert.pem
     
     ** Command 2
        keytool -importkeystore -destkeystore keystore.jks -srcstoretype PKCS12 -srckeystore keystore.p12
     
     where:
       key.pem is the SSL/TLS key file.
       cert.pem is the SSL/TLS certificate file.


# License

Copyright © 2023. MongoExpUser

Licensed under the MIT license.
