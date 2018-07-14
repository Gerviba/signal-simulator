package hu.gerviba.simulator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import hu.gerviba.simulator.config.VehicleStorage;

@DisplayName("Cluster Service")
public class ClusterServiceTest {

    @Test
    @DisplayName("PostConstruct load slaves from config")
    void testInit() throws Exception {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("simulator.slaves",  
                "http://127.0.0.1:8090|apikey8090;" + 
                "http://127.0.0.1:8091|apikey8091;" + 
                "http://127.0.0.1:8092|apikey8092;" + 
                "http://127.0.0.1:8093|apikey8093;" + 
                "http://127.0.0.1:8094|apikey8094;" + 
                "http://127.0.0.1:8095|apikey8095;" + 
                "http://127.0.0.1:8096|apikey8096;");
        
        ClusterService cluster = new ClusterService();
        cluster.env = env;
        cluster.init();
        assertEquals(7, cluster.getSlaves().size());
        assertEquals("http://127.0.0.1:8091", cluster.getSlaves().get(1).getHost());
        assertEquals("apikey8096", cluster.getSlaves().get(6).getKey());
    }
    
    @Test
    @DisplayName("Distribute ids")
    void testDistribute() throws Exception {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("simulator.slaves",  
                "http://127.0.0.1:8090|apikey8090;" + 
                "http://127.0.0.1:8091|apikey8091;" + 
                "http://127.0.0.1:8092|apikey8092;");
        
        ClusterService cluster = new ClusterService();
        cluster.env = env;

        cluster.storage = new VehicleStorage();
        cluster.storage.loadFromFile("test/configs/config-multicars.json");
        cluster.storage.getVehicles().get(0).setCount(10);
        cluster.storage.getVehicles().get(1).setCount(20);
        cluster.storage.setRanges();
        
        cluster.init();
        cluster.getSlaves().forEach(slave -> slave.changeAvailable(true));
        cluster.distributeSlaves();
        
        assertEquals(0, cluster.getSlaves().get(0).getRangeStart().get());
        assertEquals(9, cluster.getSlaves().get(0).getRangeEnd().get());
        assertEquals(10, cluster.getSlaves().get(1).getRangeStart().get());
        assertEquals(19, cluster.getSlaves().get(1).getRangeEnd().get());
        assertEquals(20, cluster.getSlaves().get(2).getRangeStart().get());
        assertEquals(29, cluster.getSlaves().get(2).getRangeEnd().get());
    }
    
    @Test
    @DisplayName("Distribute ids - 2 ot of 3 active")
    void testDistributeWithInactive() throws Exception {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("simulator.slaves",  
                "http://127.0.0.1:8090|apikey8090;" + 
                "http://127.0.0.1:8091|apikey8091;" + 
                "http://127.0.0.1:8092|apikey8092");
        
        ClusterService cluster = new ClusterService();
        cluster.env = env;

        cluster.storage = new VehicleStorage();
        cluster.storage.loadFromFile("test/configs/config-multicars.json");
        cluster.storage.getVehicles().get(0).setCount(10);
        cluster.storage.getVehicles().get(1).setCount(20);
        cluster.storage.setRanges();
        
        cluster.init();
        cluster.getSlaves().get(0).changeAvailable(true);
        cluster.getSlaves().get(1).changeAvailable(false);
        cluster.getSlaves().get(2).changeAvailable(true);
        cluster.distributeSlaves();
        
        assertEquals(0, cluster.getSlaves().get(0).getRangeStart().get());
        assertEquals(14, cluster.getSlaves().get(0).getRangeEnd().get());
        assertEquals(-1, cluster.getSlaves().get(1).getRangeStart().get());
        assertEquals(-1, cluster.getSlaves().get(1).getRangeEnd().get());
        assertEquals(15, cluster.getSlaves().get(2).getRangeStart().get());
        assertEquals(29, cluster.getSlaves().get(2).getRangeEnd().get());
    }
    
    @Test
    @DisplayName("Distribute ids - 2 ot of 3 active")
    void testDistributeWithInactiveWith() throws Exception {
        MockEnvironment env = new MockEnvironment();
        env.setProperty("simulator.slaves",  
                "http://127.0.0.1:8090|apikey8090;" + 
                "http://127.0.0.1:8091|apikey8091;" + 
                "http://127.0.0.1:8092|apikey8092");
        
        ClusterService cluster = new ClusterService();
        cluster.env = env;

        cluster.storage = new VehicleStorage();
        cluster.storage.loadFromFile("test/configs/config-multicars.json");
        cluster.storage.getVehicles().get(0).setCount(10);
        cluster.storage.getVehicles().get(1).setCount(21);
        cluster.storage.setRanges();
        
        cluster.init();
        cluster.getSlaves().get(0).changeAvailable(false);
        cluster.getSlaves().get(1).changeAvailable(true);
        cluster.getSlaves().get(2).changeAvailable(true);
        cluster.distributeSlaves();
        
        assertEquals(-1, cluster.getSlaves().get(0).getRangeStart().get());
        assertEquals(-1, cluster.getSlaves().get(0).getRangeEnd().get());
        assertEquals(0, cluster.getSlaves().get(1).getRangeStart().get());
        assertEquals(14, cluster.getSlaves().get(1).getRangeEnd().get());
        assertEquals(15, cluster.getSlaves().get(2).getRangeStart().get());
        assertEquals(30, cluster.getSlaves().get(2).getRangeEnd().get());
    }
    
}
