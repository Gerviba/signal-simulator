package hu.gerviba.simulator.transport;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("HTTP Transporter")
public class HTTPTransporterTest {
    
    // https://examples.javacodegeeks.com/core-java/nio/java-nio-async-http-client-example/
    @Test
    @DisplayName("Post request")
    void testPost() throws Exception {
        HTTPTransporter transporter = new HTTPTransporter();
        transporter.host = "httpbin.org";
        transporter.port = 80;
        transporter.method = "POST";
        transporter.action = "post";
        transporter.init();
        
        byte[] message = new byte[] {20, 15, -70, 32, 58, 23, 52, 54, 54, 32, 0, -1, -40, 25};
        assertTrue(transporter.sendToCloud(message));
    }
    
    @Test
    @DisplayName("Basic authentication")
    void testBasicAuth() throws Exception {
        HTTPTransporter transporter = new HTTPTransporter();
        transporter.host = "httpbin.org";
        transporter.port = 80;
        transporter.method = "GET";
        transporter.action = "basic-auth/testu/testp";
        transporter.basicAuthEnabled = true;
        transporter.username = "testu";
        transporter.password = "testp";
        transporter.init();
        
        byte[] message = new byte[] {20, 15, -70, 32, 58, 23, 52, 54, 54, 32, 0, -1, -40, 25};
        assertTrue(transporter.sendToCloud(message));
    }
    
    
}
