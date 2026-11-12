package com.annadhata;

import de.bwaldvogel.mongo.MongoServer;
import de.bwaldvogel.mongo.backend.memory.MemoryBackend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

@SpringBootApplication
@EnableScheduling
public class AnnaDhataApplication {

    private static final Logger log = LoggerFactory.getLogger(AnnaDhataApplication.class);
    private static MongoServer embeddedServer;

    public static void main(String[] args) {
        startEmbeddedMongoIfNeeded();
        SpringApplication.run(AnnaDhataApplication.class, args);
    }

    private static void startEmbeddedMongoIfNeeded() {
        if (!isMongoPortListening("localhost", 27017)) {
            log.info("No external MongoDB detected on localhost:27017. Starting embedded in-JVM MongoServer...");
            try {
                embeddedServer = new MongoServer(new MemoryBackend());
                embeddedServer.bind("localhost", 27017);
                log.info("Embedded in-JVM MongoServer successfully started and bound to localhost:27017");
                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    if (embeddedServer != null) {
                        embeddedServer.shutdown();
                    }
                }));
            } catch (Exception e) {
                log.warn("Could not bind embedded MongoServer: {}", e.getMessage());
            }
        } else {
            log.info("External MongoDB instance detected on localhost:27017.");
        }
    }

    private static boolean isMongoPortListening(String host, int port) {
        try (Socket s = new Socket()) {
            s.connect(new InetSocketAddress(host, port), 600);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
