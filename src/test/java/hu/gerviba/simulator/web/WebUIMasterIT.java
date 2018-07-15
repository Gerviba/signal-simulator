package hu.gerviba.simulator.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletContext;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import hu.gerviba.simulator.Simulator;
import hu.gerviba.simulator.config.VehicleStorage;
import hu.gerviba.simulator.dao.ServerStatusResponse;
import hu.gerviba.simulator.input.InputSource;
import hu.gerviba.simulator.service.ClusterService;
import hu.gerviba.simulator.transport.Transporter;

@DisplayName("[IT] WebUI - Master")
@ContextConfiguration(classes = { Simulator.class })
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"master"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebUIMasterIT {

    @Autowired
    InputSource input;

    @Autowired
    VehicleStorage storage;

    @Autowired
    Transporter transporter;
    
    @Autowired
    ClusterService cluster;

    @Autowired
    ServletContext app;
    
    @Test
    @DisplayName("Context loads")
    public void contextLoads() {
        assertThat(input).isNotNull();
        assertThat(storage).isNotNull();
        assertThat(transporter).isNotNull();
        assertThat(cluster).isNotNull();
    }
    
    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    @Test
    @DisplayName("Cluster")
    void testCluster() throws Exception {
        assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/webui/cluster",
                String.class)).contains("<h1>Cluster</h1>");
    }
    
    @Test
    @DisplayName("Redirection: Cluster - Start")
    void testClusterStart() throws Exception {
        cluster.stop();
        assertFalse(cluster.isRunning());
        ResponseEntity<String> response = this.restTemplate
                .postForEntity("http://localhost:" + port + "/webui/cluster/start",
                null, String.class);
        assertEquals(response.getHeaders().getLocation().toString(), 
                "http://localhost:" + port + "/webui/cluster");
        assertTrue(cluster.isRunning());
        cluster.stop();
        assertFalse(cluster.isRunning());
    }
    
    
    @Test
    @DisplayName("Redirection: Cluster - Stop")
    void testClusterStop() throws Exception {
        cluster.start();
        assertTrue(cluster.isRunning());
        ResponseEntity<String> response = this.restTemplate
                .postForEntity("http://localhost:" + port + "/webui/cluster/stop",
                null, String.class);
        assertEquals(response.getHeaders().getLocation().toString(), 
                "http://localhost:" + port + "/webui/cluster");
        assertFalse(cluster.isRunning());
    }
    
    @Test
    @DisplayName("Redirection: Controls")
    void testControls() throws Exception {
        assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/webui/controls",
                String.class)).contains("<h1>Dashboard</h1>");
    }
    
    @Test
    @DisplayName("Redirection: Controls - Start")
    void testControlsStart() throws Exception {
        ResponseEntity<String> response = this.restTemplate
                .postForEntity("http://localhost:" + port + "/webui/controls/start",
                null, String.class);
        assertEquals(response.getHeaders().getLocation().toString(), 
                "http://localhost:" + port + "/webui/");
    }
    
    @Test
    @DisplayName("Redirection: Controls - Stop")
    void testControlsStop() throws Exception {
        ResponseEntity<String> response = this.restTemplate
                .postForEntity("http://localhost:" + port + "/webui/controls/stop",
                null, String.class);
        assertEquals(response.getHeaders().getLocation().toString(), 
                "http://localhost:" + port + "/webui/");
    }
    
    @Test
    @DisplayName("Status for ajax")
    void testStatusRestApi() throws Exception {
        app.setAttribute("success", new AtomicLong(20));
        app.setAttribute("failed", new AtomicLong(13));
        ServerStatusResponse ssr = this.restTemplate
                .getForObject("http://localhost:" + port + "/api/status/server",
                ServerStatusResponse.class);
        assertEquals(0, ssr.getSlaves());
        assertEquals(20, ssr.getSuccess());
        assertEquals(13, ssr.getFailed());
        assertEquals(0, ssr.getVehicles());
        assertEquals(false, ssr.isCluster());
        assertEquals(false, ssr.isRunning());
    }
    
}