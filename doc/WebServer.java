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
# *     1)  A simple vanilla non-blocking Java HTTPS server (No external framework)                                                     *
# *                                                                                                                                     *
# *                                                                                                                                     *
# *  For Production Deployment:                                                                                                         *
# *                                                                                                                                     *
# *     1) Add relevant middlewares, as deem necessary                                                                                  *
# *                                                                                                                                     *
# *     2) Add relevant Java native routes, as deem necessary                                                                           *                                                                                                                       
# *                                                                                                                                     *                                                                                                                                     
# *     3) Add relevant web server codes (.html, .css and .js codes).                                                                   *
# *                                                                                                                                     *                                                                                                                             
# *  Note:                                                                                                                              *
# *                                                                                                                                     *
# *     1) This module is a demo. Hence, only 2 middlewares and 3 routes are included.                                                  *
# *                                                                                                                                     *
# *     2) You can add your own implemented middlewares and routes as indicated with comments on relevant section of the codes.         *
# *                                                                                                                                     *
# *     3) Compile and run in verbose mode aas: javac WebServer.java -g -verbose  && java WebServer                                     *
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
// import time-realted object(s)
import java.time.Instant;



/**
* <hr>
* <h2>Desciption</h2>
* The program implements a non-blocking web server (https) 
* with native Java packages and modules, without any external framework.
* It is lightweight and easily extendable by adding routes and middlewareas as deem necessary.
* <br>
* This is based on Java version 17 native httpserver  Web Development with  com.sun.net.httpserver.HttpsServer and othe related packages.
* <hr>
* @author MongoExpuser
* @version 1.0
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

    //* Defines routes that handles FileUploadRoute via CURL
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
        String password = "oluolu";                                                 // for ssl/tls
        String cwd = Paths.get("").toAbsolutePath().toString();                      // "..java";
        String serverDir = "../java-server-codes";                                                // "..java";
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
        String clientDirectory = "../java";                         
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


        // generate keystore.jks file from 
        //  ->openssl pkcs12 -export -out keystore.p12 -inkey key.pem -in cert.pem
        //  ->keytool -importkeystore -destkeystore keystore.jks -srcstoretype PKCS12 -srckeystore keystore.p12


        /*  
        if(remote.equals("73.32.91.67") ) 
        {
            // modify the default set for client x
          System.out.println("Request from: " + remote);

        }
        */




         // option 2 fro savin file with nio

                /*
                Path path = ...

               // truncate and overwrite an existing file, or create the file ifit doesn't initially exist
               OutputStream out = Files.newOutputStream(path);

               // append to an existing file, fail if the file does not exist
               out = Files.newOutputStream(path, APPEND);

               // append to an existing file, create file if it doesn't initially exist
               out = Files.newOutputStream(path, CREATE, APPEND);

               // always create new file, even if it already exists
               out = Files.newOutputStream(path, CREATE_NEW);
               */

            //     System.out.println(String.format("uid: %s ", uuidv4));
            //     System.out.println(String.format("host: %s ", host));
            //     System.out.println(String.format("uri: %s ", uri));
            //     System.out.println(String.format("method: %s ", method));
            //     System.out.println(String.format("requestBody: %s ", request));
            //     System.out.println(String.format("requestedPathName: %s ", requestedPathName));
            //     System.out.println(String.format("remote ip address: %s ", remoteIpAddress));
            //     System.out.println(String.format("utc time: %s ", utcTimeNow));
            // 




        /*
        node.js exmaple
            else
            {
                // summary of server: 
                //  1. https://nodejs.org/en/docs/guides/anatomy-of-an-http-transaction: 
                //  2. https://alexmercedcoder.medium.com/the-basics-of-creating-a-crud-api-with-plain-vanilla-node-no-frameworks-f55507e067b5
                //      check links  for refresher
                const secureServer = spdy.createServer(objs.commonOptionsSSL(), function handleRequest(request, response)
                {
                    // common variables
                    let method = (request.method).toUpperCase(); 
                    let url = request.url;
                    let createdMiddlewares = objs.commonModules();
                    let createdRoutes = objs.commonModules();
                    let staticFileOptions =  objs.commonVariables().staticFileOptions;
                    let body = [];
                    let commonIsFormData = objs.commonIsFormData(request);

                    if(commonIsFormData === false)
                    {
                        // handle (parse body) only if content-types is not multipart/form-data
                        // this ensure formidable middlware works properly

                        // body parser middleware
                        request.on("error", function (err)
                        {
                          console.error(err);
                        });
                                
                        request.on("data", function (data)
                        {
                            body.push(data);
                        });
                                
                        request.on("end", function ()
                        {
                            if(body)
                            {
                                body = Buffer.concat(body).toString();
                                request.body = body;
                            }
                        
                            // more middlewares
                            createdMiddlewares.CompressionVanilla(request, response);                               // compression middleware: mutates the response object
                            createdMiddlewares.SecurityVanilla(request, response);                                  // security middleware, like helmet
                            createdMiddlewares.LoggerVanilla(request, response);                                    // logger middlware, like morgan
                            //createdMiddlewares.BasicAuthenticationVanilla(request, response);                     // basic authentication middleware: should come after "logger" middleware - optional
                            
                            // routes
                            createdRoutes.NodePyPhpTemplateVanilla(get, request, response);                         // invoke NodeJS Python & PHP codes's route as middleware
                            createdRoutes.ProxyServerVanilla(get, request, response);                               // proxy Server routes as middelware: should come after "Basic Authentication" (if Basic-Authen is included as middleware)
                            createdRoutes.ServeStaticFilesVanilla(get, request, response, staticFileOptions);       // static file server as routes: should come last

                        });
                    }
                    else if(commonIsFormData === true)
                    {
                        // middlewares
                        createdMiddlewares.CompressionVanilla(request, response);                                   // compression middleware: mutates the response object
                        createdMiddlewares.SecurityVanilla(request, response);                                      // security middleware, like helmet
                        createdMiddlewares.LoggerVanilla(request, response);                                        // logger middlware, like morgan
                        //createdMiddlewares.BasicAuthenticationVanilla(request, response);                         // basic authentication middleware: should come after "logger" middleware - optional
                                    
                        // route
                        createdRoutes.FormDataVanilla(post, request, response);                                     // form data as routes: should come after the middlwares above
                    }
                });

                secureServer.listen(sslPort, function listenOnServer() { console.log(`Server listening on https://${host}:${sslPort}/ ...`) }).setMaxListeners(0);
            }
            */
      

//*/

/*

  // Initial handles before static handler was developed
  static class MainHandler implements HttpHandler {
    public void handle(HttpExchange httpExchange) throws IOException {
      String response = "This is the home page response";
      httpExchange.sendResponseHeaders(200, response.getBytes().length);
      //httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");  //part of security
      OutputStream os = httpExchange.getResponseBody();
      os.write(response.getBytes());
      os.close();
    }
  }
  
  static class ImageHandler implements HttpHandler {
    public void handle(HttpExchange httpExchange) throws IOException {
      String response = "This is the image route response";
      //httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");  //part of security
      httpExchange.sendResponseHeaders(200, response.getBytes().length);
      OutputStream os = httpExchange.getResponseBody();
      os.write(response.getBytes());
      os.close();
    }
  }
  
  //server.setExecutor(Executors.newCachedThreadPool());                        // creates a multi-threaed executor


  // process java
  Process iostat = new ProcessBuilder().command("iostat", "-C").inheritIO().start();
  int exitCode = iostat.waitFor();
  System.out.println("exitCode = " + exitCode);
  
  Process process = Runtime.getRuntime().exec("javac -cp src src\\main\\java\\com\\baeldung\\java9\\process\\OutputStreamExample.java");
  
  
  int number = 20;
  Thread newThread = new Thread(() -> {
      System.out.println("Factorial of " + number + " is: " + factorial(number));
  });
  newThread.start();

*/



/*
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;


//a simple static http server

public class SimpleHttpServer {

  public static void main(String[] args) throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
    server.createContext("/test", new MyHandler());
    server.setExecutor(null); // creates a default executor
    server.start();
  }

  static class MyHandler implements HttpHandler {
    public void handle(HttpExchange t) throws IOException {
      byte [] response = "Welcome Real's HowTo test page".getBytes();
      t.sendResponseHeaders(200, response.length);
      OutputStream os = t.getResponseBody();
      os.write(response);
      os.close();
    }
  }
}


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.Headers;

public class SimpleHttpServer {

  public static void main(String[] args) throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
    server.createContext("/info", new InfoHandler());
    server.createContext("/get", new GetHandler());
    server.setExecutor(null); // creates a default executor
    server.start();
  }

  static class InfoHandler implements HttpHandler {
    public void handle(HttpExchange t) throws IOException {
      String response = "Use /get to download a PDF";
      t.sendResponseHeaders(200, response.length());
      OutputStream os = t.getResponseBody();
      os.write(response.getBytes());
      os.close();
    }
  }

  static class GetHandler implements HttpHandler {
    public void handle(HttpExchange t) throws IOException {

      // add the required response header for a PDF file
      Headers h = t.getResponseHeaders();
      h.add("Content-Type", "application/pdf");

      // a PDF (you provide your own!)
      File file = new File ("c:/temp/doc.pdf");
      byte [] bytearray  = new byte [(int)file.length()];
      FileInputStream fis = new FileInputStream(file);
      BufferedInputStream bis = new BufferedInputStream(fis);
      bis.read(bytearray, 0, bytearray.length);

      // ok, we are ready to send the response.
      t.sendResponseHeaders(200, file.length());
      OutputStream os = t.getResponseBody();
      os.write(bytearray,0,bytearray.length);
      os.close();
    }
  }
}

*/








/*
   Features to add:
   Ref: http://www.freeutils.net/source/jlhttp/

  0) route redirect from 80 tom 443 - based on HttpServer - SSL - https://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/HttpsServer.html
     or - https://docs.oracle.com/javase/8/docs/jre/api/net/httpserver/spec/com/sun/net/httpserver/package-summary.html
  0) security - like multer andl related issues - see - https://ecotert.com/ecotert_security.html
  0) session management - for server and dbs -
  0) session - appEcotert.use(session({secret: uuid.v4(), cookie: {maxAge: 2000, secure: true}, rolling: true, saveUninitialized: false, resave: false }));
  0) cookie management
  0) appEcotert.use(bodyParser.json({limit: '100000mb'}));                         //bodyParser.json middleware -> default: '100kb' -> now 100GB
  0) appEcotert.use(bodyParser.urlencoded({extended: true, limit: '100000mb'}));   //bodyParser.urlencoded middleware -> default: '100kb' -> now 100GB
  0) methodOverride
  0) flash
  0) enable('trust proxy'); for: reverse proxies (load balancers - LB) enforcing SSL (e.g LB used by Heroku and nodejitsu)
  0) OCSP - ocspCache1
  0) others see - node.js code below
  ||
  1) RFC compliant - correctness is not sacrificed for the sake of size - OK
  2) Virtual hosts - multiple domains and subdomains per server
  3) File serving - built-in handler to serve files and folders from disk - OK
  4) Mime type mappings - configurable via API or a standard mime.types file
  5) Directory index generation - enables browsing folder contents
  6) Welcome files - configurable default filename (e.g. index.html)
  7) All HTTP methods supported - GET/HEAD/OPTIONS/TRACE/POST/PUT/DELETE/custom
  8) Conditional statuses - ETags and If-* header support
  9) Chunked transfer encoding - for serving dynamically-generated data streams
  10) Gzip/deflate compression - reduces bandwidth and download time
  12) HTTPS - secures all server communications
  13) Partial content - download continuation (a.k.a. byte range serving)
  14) File upload - multipart/form-data handling as stream or iterator
  15) Multiple context handlers - a different handler method per URL path
  16) @Context annotations - auto-detection of context handler methods
  17) Parameter parsing - from query string or x-www-form-urlencoded body
  18) A single source file - super-easy to integrate into any application. - OK
  18) Standalone - no dependencies other than the Java runtime
  19) Small footprint - standard jar is ~50K, stripped jar is ~35K
  20) Extensible design - easy to override, add or remove functionality - OK
  21) Reusable utility methods to simplify your custom code
  22) Extensive documentation of API and implementation (>40% of source lines) - OK
  ||
  23) node.js functionality below:
  
  other handlles for numerica or db computation
  //*********************ROUTING: Get, Post, etc. - Request Handler Routes - Mapping Request to Handle Routes *********************************************************************START
  //use developed "ROUTING" modules/files (inside privateServerfolder) as routes
  appEcotert.use(commonModules(appEcotert, true).EcotertAuthentificationRoute);       // EcotertURPPS authentication routes as middleware
  appEcotert.use(commonModules(appEcotert, true).EcotertServerlessRoute(appEcotert)); // Ecotert serverless routes as middleware
  //*********************ROUTING: Get, Post, etc. - Request Handler Routes - Mapping Request to Handle Routes ************************************************************************END


  // also called create index
  / serve-index - formerly directory on ecotert.com: serve URLs like /ftp/thing as public/ftp/thing
  // express.static serves the file contents and serveIndex serves the directory -> see ref: https://github.com/expressjs/serve-index
  appEcotert.use('/publicShared', express.static(path.resolve(__dirname + '/publicShared')), serveIndex(path.resolve(__dirname + '/publicShared') ,
  {
    // 'filter'    : true,
    'hidden'    : false, //display hidden (dot) files
    'icons'     : true,
    //'stylesheet': 'publicClient/css/webPreTwo.css',
    //'template'  : 'path_to_html_template__if_any'
  }));
  
  
*/





/*

//SSL Server Example

//OPTION 0 : http://rememberjava.com/http/2017/04/29/simple_https_server.html
           : https://www.codota.com/code/java/classes/com.sun.net.httpserver.HttpsServer
           : https://www.programcreek.com/java-api-examples/?api=com.sun.net.httpserver.HttpsServer
           : https://www.codeproject.com/Tips/1043003/Create-a-Simple-Web-Server-in-Java-HTTPS-Server
          
// OPTION 1:

  void start() throws Exception {
    HttpsServer httpsServer = HttpsServer.create(new InetSocketAddress(PORT), 0);

    SSLContext sslContext = getSslContext();
    httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext));

    httpsServer.createContext("/secure", new StaticFileHandler(BASEDIR));
    httpsServer.start();
  }

  private SSLContext getSslContext() throws Exception {
    KeyStore ks = KeyStore.getInstance("JKS");
    ks.load(new FileInputStream(KEYSTORE_FILE), KEYSTORE_PASSWORD.toCharArray());

    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
    kmf.init(ks, KEY_PASSWORD.toCharArray());

    TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
    tmf.init(ks);

    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

    return sslContext;
  }

//OPTION 2
try {
    // Set up the socket address
    InetSocketAddress address = new InetSocketAddress(InetAddress.getLocalHost(), config.getHttpsPort());

    // Initialise the HTTPS server
    HttpsServer httpsServer = HttpsServer.create(address, 0);
    SSLContext sslContext = SSLContext.getInstance("TLS");

    // Initialise the keystore
    char[] password = "simulator".toCharArray();
    KeyStore ks = KeyStore.getInstance("JKS");
    FileInputStream fis = new FileInputStream("lig.keystore");
    ks.load(fis, password);

    // Set up the key manager factory
    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
    kmf.init(ks, password);

    // Set up the trust manager factory
    TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
    tmf.init(ks);

    // Set up the HTTPS context and parameters
    sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
    httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
        public void configure(HttpsParameters params) {
            try {
                // Initialise the SSL context
                SSLContext c = SSLContext.getDefault();
                SSLEngine engine = c.createSSLEngine();
                params.setNeedClientAuth(false);
                params.setCipherSuites(engine.getEnabledCipherSuites());
                params.setProtocols(engine.getEnabledProtocols());

                // Get the default parameters
                SSLParameters defaultSSLParameters = c.getDefaultSSLParameters();
                params.setSSLParameters(defaultSSLParameters);
            } catch (Exception ex) {
                ILogger log = new LoggerFactory().getLogger();
                log.exception(ex);
                log.error("Failed to create HTTPS port");
            }
        }
    });
    LigServer server = new LigServer(httpsServer);
    joinableThreadList.add(server.getJoinableThread());
} catch (Exception exception) {
    log.exception(exception);
    log.error("Failed to create HTTPS server on port " + config.getHttpsPort() + " of localhost");
}



// OPTION 3:

import java.io.*;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpsServer;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import com.sun.net.httpserver.*;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;

import javax.net.ssl.SSLContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class MyHttpsServer {

    public static class MyHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "This is the response";
            t.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            t.sendResponseHeaders(200, response.getBytes().length);
            try (OutputStream os = t.getResponseBody()) {
                os.write(response.getBytes());
            }
        }
    }

    public static void main(String[] args) throws Exception {

        try {
            // setup the socket address
            InetSocketAddress address = new InetSocketAddress(443);

            // initialise the HTTPS server
            HttpsServer httpsServer = HttpsServer.create(address, 0);
            SSLContext sslContext = SSLContext.getInstance("TLS");

            // The keystore is generated using the following three files:
            //    - private_key.key
            //    - site.crt
            //    - site.ca-bundle
            // ...and using the following set of commands (and password as "password"):
            //    openssl pkcs12 -export -out keystore.pkcs12 -inkey private_key.key -certfile site.ca-bundle -in site.crt
            //    keytool -v -importkeystore -srckeystore keystore.pkcs12 -srcstoretype PKCS12 -destkeystore keystore.jks -deststoretype pkcs12

            // initialise the keystore
            char[] password = "password".toCharArray();
            KeyStore ks = KeyStore.getInstance("PKCS12");
            FileInputStream fis = new FileInputStream("/ssl/keystore.jks");
            ks.load(fis, password);

            // setup the key manager factory
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(ks, password);

            // setup the trust manager factory
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
            tmf.init(ks);

            // setup the HTTPS context and parameters
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            httpsServer.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                @Override
                public void configure(HttpsParameters params) {
                    try {
                        // initialise the SSL context
                        SSLContext c = getSSLContext();
                        SSLEngine engine = c.createSSLEngine();
                        params.setNeedClientAuth(false);
                        params.setCipherSuites(engine.getEnabledCipherSuites());
                        params.setProtocols(engine.getEnabledProtocols());

                        // Set the SSL parameters
                        SSLParameters sslParameters = c.getSupportedSSLParameters();
                        params.setSSLParameters(sslParameters);

                    } catch (Exception ex) {
                        System.out.println("Failed to create HTTPS port");
                        System.out.println(ex.getMessage());
                    }
                }
            });
            httpsServer.createContext("/", new MyHandler());
            httpsServer.setExecutor(null); // creates a default executor
            httpsServer.start();

        } catch (IOException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException | CertificateException ex) {
            System.out.println("Failed to create HTTPS server on port 443");
            System.out.println(ex.getMessage());
        }
    }
}

//*/