/* **************************************************************************************************************************************
#  * WebServer.java                                                                                                                     *
#  **************************************************************************************************************************************
#  *                                                                                                                                    *
#  * @License Starts                                                                                                                    *
#  *                                                                                                                                    *
#  * Copyright © 2023. MongoExpUser.  All Rights Reserved.                                                                              *
#  *                                                                                                                                    *
#  * License: MIT - https://github.com/MongoExpUser/Non-blocking-Java-HTTPS-Server-With-No-External-Framework/blob/main/LICENSE         *
#  *                                                                                                                                    *
#  * @License Ends                                                                                                                      *
#  **************************************************************************************************************************************
# *                                                                                                                                     *
# *  Project: Vanilla Non-blocking Java HTTPS Server                                                                                    *
# *                                                                                                                                     *
# *  This module deploys:                                                                                                               *
# *                                                                                                                                     *                                                                                                              
# *     1)  A simple vanilla non-blocking Java HTTPS server (No external framework).                                                    *
# *                                                                                                                                     *
# *                                                                                                                                     *
# *  For Production Deployment:                                                                                                         *
# *                                                                                                                                     *
# *     1) Add relevant middlewares, as deem necessary.                                                                                 *
# *                                                                                                                                     *
# *     2) Add relevant Java native routes, as deem necessary.                                                                          *                                                                                                                       
# *                                                                                                                                     *                                                                                                                                     
# *     3) Add relevant web server codes (.html, .css and .js codes).                                                                   *
# *                                                                                                                                     *                                                                                                                             
# *  Note:                                                                                                                              *
# *                                                                                                                                     *
# *     1) This module is a demo. Hence, only 2 middlewares and 3 routes are included.                                                  *
# *                                                                                                                                     *
# *     2) You can add your own implemented middlewares and routes as indicated with comments on relevant section of the codes.         *
# *                                                                                                                                     *
# *     3) Compile and run in verbose mode as: javac WebServer.java -g -verbose  && java WebServer                                      *
# *                                                                                                                                     *
# **************************************************************************************************************************************/


// Import io and nio object(s)
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
//Import util/collection-related object(s)
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.stream.Collectors;
import java.util.Collections;
// Import HTTP(S) Server-relates and network-related object(s)
import java.net.URI;
import java.security.KeyStore;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLContext;
import java.net.InetSocketAddress;
import javax.net.ssl.TrustManager;
import java.security.SecureRandom;
import javax.net.ssl.SSLParameters;
import com.sun.net.httpserver.Filter;
import java.util.concurrent.Executors;
import com.sun.net.httpserver.Headers;
import javax.net.ssl.KeyManagerFactory;
import com.sun.net.httpserver.HttpServer;
import javax.net.ssl.TrustManagerFactory;
import java.security.cert.X509Certificate;
import com.sun.net.httpserver.HttpsServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.Filter.Chain;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpPrincipal;
import com.sun.net.httpserver.HttpsParameters;
import com.sun.net.httpserver.HttpsConfigurator;
// import time-related object(s)
import java.time.Instant;




/**
* <hr>
* <h2>Desciption</h2>
* The program implements a non-blocking web server (https) with native Java packages and modules, without any external framework.
* It is lightweight and easily extendable by adding routes and middlewareas as deem necessary.
* <br>
* This is based on Java version 21 native Https Server with  com.sun.net.httpserver.HttpsServer and other related packages.
* <hr>
* @author MongoExpuser
* @version 1.0.1
* @since 2023-09-23 
*/



/** Creates a web server (https) */
public class WebServer 
{
    /** The constructor for the web server class */
    public WebServer() {}

    /** Defines the logger middleware */
    static class LoggerMiddleware
    {
        /** The constructor for the LoggerMiddleware class */
        public LoggerMiddleware(HttpExchange httpExchange) throws IOException //, ScriptException 
        {
            UUID uuidv4 = UUID.randomUUID();
            URI uri = httpExchange.getRequestURI();
            List<String> hostList  = httpExchange.getRequestHeaders().get("host");
            String host = hostList.get(0);  
            String method = (httpExchange.getRequestMethod()).toLowerCase();
            InetSocketAddress remoteIpAddress = httpExchange.getRemoteAddress();
            InputStream bodyInputStream = httpExchange.getRequestBody();
            String requestedPathName = new File(uri.getPath()).getName();
            String request = new String(bodyInputStream.readAllBytes());
            Instant utcTimeNow = Instant.now();
            Map<String, Object> logsMap = new HashMap<String, Object>();

            logsMap.put("uid", uuidv4);
            logsMap.put("host", host);
            logsMap.put("method", method);
            logsMap.put("uri", uri);
            logsMap.put("requestedPathName", requestedPathName);
            logsMap.put("remoteIpAddress", remoteIpAddress);
            logsMap.put("utcTimeNow", utcTimeNow);
            String logMapsToJsonStringify = "{"+logsMap.entrySet().stream().map(val -> "\""+ val.getKey() + "\":\"" + String.valueOf(val.getValue()) + "\"").collect(Collectors.joining(", "))+"}";
            Boolean logToFile  = true;

            if(logToFile == true)
            {
                String cwd = Paths.get("").toAbsolutePath().toString();  
                String serverLogFilename = "httpsServerLog.json";
                String fileContent = String.format("%s%s", logMapsToJsonStringify, "\n");
                String fileLocation = String.format("%s/%s", cwd, serverLogFilename);  
                Path path = Paths.get(fileLocation);
                Files.write(path, fileContent.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            }
            else
            {
              // print/view
              System.out.println(String.format("logsJson: %s ", logMapsToJsonStringify));
            }
        } 
    }

    /** Defines the compression middleware */
    static class CompressionMiddleware
    {
        /** The constructor for the CompressionMiddleware class */
        public CompressionMiddleware(HttpExchange httpExchange) throws IOException 
        {
            Headers requestHeaders = httpExchange.getRequestHeaders();
            Headers responseHeaders = httpExchange.getResponseHeaders();
            responseHeaders.set("Content-Encoding", "gzip");
            List<String> responseEncoding = httpExchange.getResponseHeaders().get("Content-Encoding");
        }
    } 

    //...
    //.. Add more middlewares as deem necessary
    //..



    /** Defines route that handles GetUserRoute */
    static class GetUserRoute implements HttpHandler 
    {
        /** Represents the user's name */
        String username;

  
        /** The constructor for the GetUserRoute class 
         * @param args A key-value map of the required user's information
        */
        public GetUserRoute(Map<String, String> args) 
        {
            this.username = args.get("username");
        }
      
        /** Handles getting user. 
        * @param httpExchange The httpserver.HttpExchange object
        * @exception IOException On input error.
        * @see IOException
        */
        @Override
        public void handle(HttpExchange httpExchange) throws IOException 
        {
            // the route
            WebServer ws = new WebServer();
            URI uri = httpExchange.getRequestURI();
            String method = (httpExchange.getRequestMethod()); //.toLowerCase();
            String requestedPathName = new File(uri.getPath()).getName();

            OutputStream outputStream = httpExchange.getResponseBody();

            if(requestedPathName.equals("user") && method.equalsIgnoreCase("GET") )
            {
                //this is just a demo route: should typically get info like this (based on specified username) from a database and then pass to the user interface
                String email = String.format("%s%s", this.username, "@domain.com");
                String info = String.format("Username is: %s <br><br> Email is: %s", this.username, email);
                String response = String.format("<html> <center> <h1> <br> Java HTTPS Server Demo </h1> <h2> <img src='images/java-logo.png' alt='Java-HTTPS-Server-Demo' width='80' height='50'> <br><br> %s </h2> </center> </html>", info);
                httpExchange.sendResponseHeaders(200, response.length());

                // put relevant middlewares here before response is sent
                ws.allMiddlewares(httpExchange);

                outputStream.write(response.getBytes());
                outputStream.close();

            }
            else 
            {
                String response = String.format("<html><center><h2><br>Error 404: Not Found.</h2></center></html>");
                httpExchange.sendResponseHeaders(404, response.length());
                
                // put middlewares here before response is sent
                ws.allMiddlewares(httpExchange);

                outputStream.write(response.getBytes());
                outputStream.close();
            }
      }
    }

    //* Defines routes that handles FileUploadRoute via CURL or Form
    static class FileUploadRoute implements HttpHandler 
    {
        String uploaddir;

        /** The constructor for the FileUploadRoute class 
         * @param args A key-value map of the required file upload's  information
         */
        public FileUploadRoute(Map<String, String> args ) 
        {
          this.uploaddir = args.get("uploaddir");;
        }


        /** Handles file upload
        * @param httpExchange The httpserver.HttpExchange object
        * @exception IOException On input error.
        * @see IOException
        */
        @Override
        public void handle(HttpExchange httpExchange) throws IOException 
        {
            // the route
            WebServer ws = new WebServer();
            String method = httpExchange.getRequestMethod();
            String targetMethod = "POST";
            OutputStream outputStream = httpExchange.getResponseBody(); 

            if(targetMethod.equalsIgnoreCase(method)) 
            {
                List<String> filenameList = httpExchange.getRequestHeaders().get("filename");
                String filename = filenameList.get(0);

                if(filename != null) 
                {
                    String savedPath = String.format("%s/%s", uploaddir, filename);
                    Path filePath = Paths.get(savedPath);
                    OutputStream newOutputStream = Files.newOutputStream(filePath);
                    httpExchange.getRequestBody().transferTo(newOutputStream);
                    String response = String.format("<html><center><h2><br> %s is successfully uploaded. </h2></center></html>", filename);
                    httpExchange.sendResponseHeaders(200, response.getBytes().length);

                    // put middlewares here before response is sent
                    ws.allMiddlewares(httpExchange);

                    outputStream.write(response.getBytes());
                    outputStream.close();
                      
                  } 
                  else 
                  {
                      String response = String.format("<html><center><h2><br>Error 404: Not Found.</h2></center></html>");
                      httpExchange.sendResponseHeaders(404, response.length());
    
                      // put middlewares here before response is sent
                      ws.allMiddlewares(httpExchange);

                      outputStream.write(response.getBytes());
                      outputStream.close();
                  }
            }
            else 
            {
                String response = String.format("<html><center><h2><br>Error 405: Method Not Allowed.</h2></center></html>");
                httpExchange.sendResponseHeaders(405, response.length());

                // put middlewares here before response is sent
                ws.allMiddlewares(httpExchange);

                outputStream.write(response.getBytes());
                outputStream.close();
            }
        }
    }

    /** Defines route that handles StaticFileRoute (static files server) */
    static class StaticFileRoute implements HttpHandler 
    {
        /** Represents the static or base files directory */
        String staticDirectory;

        /** Represents the defaut page e.g index.html or default.html */
        String defaultPage;
  
        /** The constructor for the StaticFileHandler class 
         * @param args A key-value map of the static (or base) files directory name and the default page file name
         */
        public StaticFileRoute(Map<String, String> args) 
        {
            this.staticDirectory = args.get("staticDirectory");
            this.defaultPage = args.get("defaultPage");
        }
      
        /** Handles files. 
        * @param httpExchange The httpserver.HttpExchange object
        * @exception IOException On input error.
        * @see IOException
        */
        @Override
        public void handle(HttpExchange httpExchange) throws IOException 
        {
            // the route
            // Static file server - route: should be the last
            WebServer ws = new WebServer();
            URI uri = httpExchange.getRequestURI();
            String requestedPathName = new File(uri.getPath()).getName();
            String filepath;

            if(requestedPathName == null || requestedPathName.isEmpty() || requestedPathName == defaultPage)
            {
                // default page (index.html) when web site initially loads
                filepath = String.format("%s/%s", staticDirectory, defaultPage);
            }
            else 
            {
                filepath = String.format("%s/%s", staticDirectory, requestedPathName);
            }

            File file = new File(filepath);
            OutputStream outputStream = httpExchange.getResponseBody();


            if(file.exists()) 
            {
                httpExchange.sendResponseHeaders(200, file.length());
                
                // put middlewares here before response is sent
                ws.allMiddlewares(httpExchange);

                outputStream.write(Files.readAllBytes(file.toPath()));
                outputStream.close();
            }
            else 
            {
                String response = String.format("<html><center><h2><br>Error 404: Not Found.</h2></center></html>");
                System.err.println(response);
                httpExchange.sendResponseHeaders(404, response.length());
                
                // put middlewares here before response is sent
                ws.allMiddlewares(httpExchange);

                outputStream.write(response.getBytes());
                outputStream.close();
            }
      }
    }


    //...
    //.. Add more routes as deem necessary
    //..


    /** Class instance method: Read and return a plain text/html file 
    * @param filename The name of the file
    * @exception IOException On input error.
    * @see IOException
    * @return A string representing the content of the file
    */
    public String readFile(String filename) throws IOException 
    {
        String line = "";
        String content = "";

        try
        {
            Path path = Paths.get(filename);
            BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8);

            while( (line = br.readLine()) != null )
            {
                content = content + line;
            }

            br.close();
        }
        catch(IOException error)
        {
            error.printStackTrace();
            System.out.println();
            System.out.println("Error: Reading file error...");
            System.out.println();
        }
        
        return content;

    }

     /** Class instance method: Defines the SSL/TLS configuration for the web server
    * @param sslContext The SSLContext object
    * @exception IOException On input error.
    * @see IOException
    * @return An HttpsConfigurator object representing SSL/TLS configuration for the web server
    */
    public HttpsConfigurator sslConfiguration(SSLContext sslContext) throws IOException 
    {
        HttpsConfigurator config = new HttpsConfigurator(sslContext) 
        {
            public void configure (HttpsParameters params) 
            {
                InetSocketAddress remote = params.getClientAddress();
                SSLContext sc = getSSLContext();
                SSLParameters sslparams = sc.getDefaultSSLParameters();
                params.setSSLParameters(sslparams);
            }
        };

        return config;
    }
    

    /** Class instance method: Instantiate  all middleware for the web server
     * @param httpExchange The HttpExchange object 
     * @exception IOException On input error.
     * @see IOException 
     */
    public void allMiddlewares(HttpExchange httpExchange) throws IOException 
    {

        // put relevant middlewares here before response is sent
        // e.g  logger, compresssion, basic authen, vhost, multi-party upload, session mgt, response header obfuscation (i.e. like node.js helmet), etc.
        new LoggerMiddleware(httpExchange); 
        //new CompressionMiddleware(httpExchange);
        //new BasicAuthenticationMiddleware(httpExchange);
        // 1
        // 2
        // .
        // .
        // etc.

    }

    /** Class instance method: Start the web server.
    * @exception IOException On input error.
    * @see IOException
    */
    public void startWebServer() throws Exception 
    {
        int sslPort = 443;
        String password = "pawword";                                                 // for ssl/tls
        String cwd = Paths.get("").toAbsolutePath().toString();                      // "..java";
        String serverDir = "../java";                                                // "..java";
        String keyStoreFilename = String.format("%s/ssl/keystore.jks", serverDir);   // for ssl/tls
        String endpoint = "localhost";                           
        String domain = "domain.com"; 
        WebServer ws = new WebServer();                          
        String message = String.format("%s Web Server with endpoint of %s is running on port %s.", domain, endpoint, sslPort);
        
        // arguments to route objects
        // 1. GetUserRoute - this is just a demo route for testing: for production deployment, the arg should be passed in through a "form" or other non hand-coded option
        Map<String, String> getUserRouteArgs = new HashMap<String, String>();
        getUserRouteArgs.put("username", "userone");

        // 2. FileUploadRoute
        Map<String, String> fileUploadRouteArgs = new HashMap<String, String>();
        fileUploadRouteArgs.put("uploaddir", cwd);

        // 3. StaticFileRoute
        String clientDirectory = "../client";                         
        String defaultPage =  "index.html"; 
        Map<String, String> staticFileArgs = new HashMap<String, String>();
        staticFileArgs.put("staticDirectory", clientDirectory);
        staticFileArgs.put("defaultPage", defaultPage);

        // create server
        HttpsServer server = HttpsServer.create(new InetSocketAddress(sslPort), 0);

        // congigure SSL/TLS
        char [] passphrase = password.toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        ks.load(new FileInputStream(keyStoreFilename), passphrase);
        kmf.init(ks, passphrase);
        tmf.init(ks);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        HttpsConfigurator httpsConfig = ws.sslConfiguration(sslContext);
        server.setHttpsConfigurator (httpsConfig);

        // create context for routes and start server
        // 1. 
        server.createContext("/user", new GetUserRoute(getUserRouteArgs));
        server.createContext("/upload", new FileUploadRoute(fileUploadRouteArgs));  
        // ... 
        // ...
        // ... etc.
        // n. static file server route should be the last
        server.createContext("/", new StaticFileRoute(staticFileArgs)); 
        server.setExecutor(null); // single-threaded, non-blocking and async
        server.start();
        System.out.println("===================================================================================");
        System.out.println(message);
        System.out.println("===================================================================================");


    }

    //...
    //.. Add more methods as deem necessary
    //..



    /** The main method for the class.
    * @param args Unused.
    * @exception IOException On input error.
    * @see IOException
    */
    public static void main(String [] args) throws Exception 
    {
        WebServer ws = new WebServer();
        ws.startWebServer();
    }
}
