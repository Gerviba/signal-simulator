package hu.gerviba.simulator.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

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
import hu.gerviba.simulator.input.InputSource;
import hu.gerviba.simulator.transport.Transporter;

@DisplayName("[IT] WebUI - Standalone")
@ContextConfiguration(classes = { Simulator.class })
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"standalone"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WebUIStandaloneIT {

    @Autowired
    InputSource input;

    @Autowired
    VehicleStorage storage;

    @Autowired
    Transporter transporter;

    @Test
    @DisplayName("Context loads")
    public void contextLoads() {
        assertThat(input).isNotNull();
        assertThat(storage).isNotNull();
        assertThat(transporter).isNotNull();
    }
    
    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;
    
    @Test
    @DisplayName("Dashboard")
    void testDashboard() throws Exception {
        assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/webui/",
                String.class)).contains("<h1>Dashboard</h1>");
    }

    @Test
    @DisplayName("Settings")
    void testSettings() throws Exception {
        assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/webui/settings",
                String.class)).contains("<h1>Settings</h1>");
    }
    
    @Test
    @DisplayName("Settings - Ranges")
    void testSettingsRanges() throws Exception {
        Map<String, Object> values = new HashMap<>();
        values.put("name", "MyFavouriteVehicleType");
        values.put("count", 10);
        ResponseEntity<String> response = this.restTemplate
                .postForEntity("http://localhost:" + port + "/webui/ranges?name={name}&count={count}",
                null, String.class, values);
        assertEquals(response.getHeaders().getLocation().toString(), 
                "http://localhost:" + port + "/webui/settings");
        assertEquals(10, storage.getVehicles().get(0).getCount().get());
    }

    @Test
    @DisplayName("Controls")
    void testControls() throws Exception {
        assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/webui/controls",
                String.class)).contains("<h1>Controls</h1>");
    }
    
    @Test
    @DisplayName("Redirection: Controls - Start")
    void testControlsStart() throws Exception {
        input.stop();
        assertFalse(input.isRunning());
        ResponseEntity<String> response = this.restTemplate
                .postForEntity("http://localhost:" + port + "/webui/controls/start",
                null, String.class);
        assertEquals(response.getHeaders().getLocation().toString(), 
                "http://localhost:" + port + "/webui/controls");
        assertTrue(input.isRunning());
        input.stop();
        assertFalse(input.isRunning());
    }
    
    
    @Test
    @DisplayName("Redirection: Controls - Stop")
    void testControlsStop() throws Exception {
        input.start();
        assertTrue(input.isRunning());
        ResponseEntity<String> response = this.restTemplate
                .postForEntity("http://localhost:" + port + "/webui/controls/stop",
                null, String.class);
        assertEquals(response.getHeaders().getLocation().toString(), 
                "http://localhost:" + port + "/webui/controls");
        assertFalse(input.isRunning());
    }
    
    @Test
    @DisplayName("Redirection: Cluster")
    void testCluster() throws Exception {
        assertThat(this.restTemplate.getForObject("http://localhost:" + port + "/webui/cluster",
                String.class)).contains("<h1>Dashboard</h1>");
    }
    
    @Test
    @DisplayName("Redirection: Cluster - Start")
    void testClusterStart() throws Exception {
        ResponseEntity<String> response = this.restTemplate
                .postForEntity("http://localhost:" + port + "/webui/cluster/start",
                null, String.class);
        assertEquals(response.getHeaders().getLocation().toString(), 
                "http://localhost:" + port + "/webui/");
    }
    
    @Test
    @DisplayName("Redirection: Cluster - Stop")
    void testClusterStop() throws Exception {
        ResponseEntity<String> response = this.restTemplate
                .postForEntity("http://localhost:" + port + "/webui/cluster/stop",
                null, String.class);
        assertEquals(response.getHeaders().getLocation().toString(), 
                "http://localhost:" + port + "/webui/");
    }
    
}
