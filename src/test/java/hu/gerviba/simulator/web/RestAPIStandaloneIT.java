package hu.gerviba.simulator.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import hu.gerviba.simulator.Simulator;
import hu.gerviba.simulator.dao.StatusResponse;
import hu.gerviba.simulator.input.InputSource;

@DisplayName("[IT] RestAPI - Standalone")
@ContextConfiguration(classes = { Simulator.class })
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"standalone"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RestAPIStandaloneIT {
    
    @Autowired
    ServletContext app;
    
    @Autowired
    private InputSource input;
    
    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @PostConstruct
    void init() {
        app.setAttribute("mode", "STANDALONE");
    }
    
    @Test   
    @DisplayName("Controls - Start")
    void testRestControlsStart() throws Exception {
        input.stop();
        assertFalse(input.isRunning());
        StatusResponse res = restTemplate.postForObject(
                "http://localhost:" + port + "/api/inputgenerator/start", null, StatusResponse.class);
        assertEquals("OK", res.getStatus());
        assertTrue(input.isRunning());
        input.stop();
        assertFalse(input.isRunning());
    }
    
    @Test
    @DisplayName("Controls - Stop")
    void testRestControlsStop() throws Exception {
        input.start();
        assertTrue(input.isRunning());
        StatusResponse res = restTemplate.postForObject(
                "http://localhost:" + port + "/api/inputgenerator/stop", null, StatusResponse.class);
        assertEquals("OK", res.getStatus());
        assertFalse(input.isRunning());
    }
    
    @Test
    @DisplayName("Cluster - Start")
    void testRestClusterStart() throws Exception {
        StatusResponse res = restTemplate.postForObject(
                "http://localhost:" + port + "/api/cluster/start", null, StatusResponse.class);
        assertEquals("UNSUPPORTED-OPERATION", res.getStatus());
    }
    
    @Test
    @DisplayName("Cluster - Stop")
    void testRestClusterStop() throws Exception {
        StatusResponse res = restTemplate.postForObject(
                "http://localhost:" + port + "/api/cluster/stop", null, StatusResponse.class);
        assertEquals("UNSUPPORTED-OPERATION", res.getStatus());
    }
    
}