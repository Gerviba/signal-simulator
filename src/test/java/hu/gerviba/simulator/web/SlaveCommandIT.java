package hu.gerviba.simulator.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletContext;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import hu.gerviba.simulator.Simulator;
import hu.gerviba.simulator.dao.MasterSlaveCommand;
import hu.gerviba.simulator.dao.SlaveStatusResponse;
import hu.gerviba.simulator.dao.StatusResponse;
import hu.gerviba.simulator.input.InputSource;

@DisplayName("[IT] Slave commands")
@ContextConfiguration(classes = { Simulator.class })
@ExtendWith(SpringExtension.class)
@ActiveProfiles({"slave"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SlaveCommandIT {

    @Autowired
    ServletContext app;
    
    @Autowired
    InputSource input;
    
    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;
    
    @Value("${simulator.api-key:HfnB8694aWKzD9xNbb58zgHb}")
    String apiKey;
    
    @Test
    @DisplayName("Status")
    void testStatus() throws Exception {
        app.setAttribute("success", new AtomicLong(31));
        app.setAttribute("failed", new AtomicLong(53));
        SlaveStatusResponse response = restTemplate.getForObject(
                "http://localhost:" + port + "/api/slave/status?apiKey={apiKey}",
                SlaveStatusResponse.class, apiKey);
        assertEquals(31, response.getSuccess());
        assertEquals(53, response.getFailed());
    }
    
    @Test
    @DisplayName("Status - invalid key")
    void testStatusInvalidKey() throws Exception {
        SlaveStatusResponse response = restTemplate.getForObject(
                "http://localhost:" + port + "/api/slave/status?apiKey={apiKey}",
                SlaveStatusResponse.class, apiKey + "asd");
        assertEquals("INVALID-API-KEY", response.getMessage());
    }
    
    @Test
    @DisplayName("Start")
    void testStart() throws Exception {
        input.stop();
        assertFalse(input.isRunning());
        StatusResponse response = restTemplate.postForObject(
                "http://localhost:" + port + "/api/slave/start?apiKey={apiKey}",
                new MasterSlaveCommand(0L, 10L, Arrays.asList()), 
                StatusResponse.class, apiKey);
        assertEquals("OK", response.getStatus());
        assertTrue(input.isRunning());
        input.stop();
    }
    
    @Test
    @DisplayName("Start - invalid key")
    void testStartInvalidKey() throws Exception {
        StatusResponse response = restTemplate.postForObject(
                "http://localhost:" + port + "/api/slave/start?apiKey={apiKey}",
                new MasterSlaveCommand(0L, 10L, Arrays.asList()), 
                StatusResponse.class, apiKey + "asd");
        assertEquals("INVALID-API-KEY", response.getStatus());
    }
    
    @Test
    @DisplayName("Stop")
    void testStop() throws Exception {
        input.start();
        assertTrue(input.isRunning());
        StatusResponse response = restTemplate.postForObject(
                "http://localhost:" + port + "/api/slave/stop?apiKey={apiKey}",
                null, StatusResponse.class, apiKey);
        assertEquals("OK", response.getStatus());
        assertFalse(input.isRunning());
    }
    
    @Test
    @DisplayName("Stop - invalid key")
    void testStopInvalidKey() throws Exception {
        StatusResponse response = restTemplate.postForObject(
                "http://localhost:" + port + "/api/slave/stop?apiKey={apiKey}",
                null, StatusResponse.class, apiKey + "asd");
        assertEquals("INVALID-API-KEY", response.getStatus());
    }
    
}
