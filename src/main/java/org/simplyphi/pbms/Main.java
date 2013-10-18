package org.simplyphi.pbms;

import org.apache.log4j.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

/**
 * Main class.
 *
 */
public class Main {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://0.0.0.0:9090/bms/";
    private static final Logger logger = Logger.getLogger(Main.class);

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        // create a resource config that scans for JAX-RS resources and providers
        // in org.simplyphi.pbms package
        final ResourceConfig rc = new ResourceConfig().packages("org.simplyphi.pbms").register(JacksonFeature.class);
        StaticHttpHandler shh = new StaticHttpHandler();
        shh.addDocRoot("src/main/webapp");
        shh.start();
        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        HttpServer httpServer = GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
        httpServer.getServerConfiguration().addHttpHandler(shh);
        return httpServer;

    }

    /**
     * Main method.
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException, ParseException {
        final HttpServer server = startServer();
        logger.info(String.format("Jersey app started with WADL available at "
                + "%sapplication.wadl", BASE_URI));
        System.out.println("Hit enter to stop it...");
        System.in.read();
        server.stop();

    }

}

