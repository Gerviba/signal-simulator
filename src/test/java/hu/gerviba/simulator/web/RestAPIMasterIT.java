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
import hu.gerviba.simulator.service.ClusterService;

@DisplayName("[IT] RestAPI - Master")
@ContextConfiguration(classes = { Simulator.class })
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"master"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RestAPIMasterIT {
    
    @Autowired
    ServletContext app;
    
    @Autowired
    ClusterService cluster;
    
    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @PostConstruct
    void init() {
        app.setAttribute("mode", "MASTER");
    }
    
    @Test
    @DisplayName("Cluster - Start")
    void testRestClusterStart() throws Exception {
        cluster.stop();
        assertFalse(cluster.isRunning());
        StatusResponse res = restTemplate.postForObject(
                "http://localhost:" + port + "/api/cluster/start", null, StatusResponse.class);
        assertEquals("OK", res.getStatus());
        assertTrue(cluster.isRunning());
        cluster.stop();
        assertFalse(cluster.isRunning());
    }
    
    @Test
    @DisplayName("Cluster - Stop")
    void testRestClusterStop() throws Exception {
        cluster.start();
        assertTrue(cluster.isRunning());
        StatusResponse res = restTemplate.postForObject(
                "http://localhost:" + port + "/api/cluster/stop", null, StatusResponse.class);
        assertEquals("OK", res.getStatus());
        assertFalse(cluster.isRunning());
    }
    

    @Test
    @DisplayName("Controls - Start")
    void testRestControlsStart() throws Exception {
        StatusResponse res = restTemplate.postForObject(
                "http://localhost:" + port + "/api/inputgenerator/start", null, StatusResponse.class);
        assertEquals("UNSUPPORTED-OPERATION", res.getStatus());
    }
    
    @Test
    @DisplayName("Controls - Stop")
    void testRestControlsStop() throws Exception {
        StatusResponse res = restTemplate.postForObject(
                "http://localhost:" + port + "/api/inputgenerator/stop", null, StatusResponse.class);
        assertEquals("UNSUPPORTED-OPERATION", res.getStatus());
    }
    
}