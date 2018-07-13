package hu.gerviba.simulator.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hu.gerviba.simulator.config.VehicleStorage;

@DisplayName("Loading configuration")
public class VehicleStorageTest {

    static String originalConfigFile;
    
    @BeforeAll
    static void init() {
        originalConfigFile = System.getProperty("configFile");
    }
    
    @Test
    @DisplayName("Example config")
    void loadExampleConfig() throws Exception {
        VehicleStorage conf = new VehicleStorage();
        conf.loadFromFile("test/configs/config-example.json");
        assertEquals(1, conf.getVehicles().size());
        assertEquals(2, conf.getVehicles().get(0).getFrames().size());
        assertEquals(5, conf.getVehicles().get(0).getFrames().get(0).getSignals().size());
        assertEquals(4, conf.getVehicles().get(0).getFrames().get(1).getSignals().size());
        assertEquals("BrakeForceFR", conf.getVehicles().get(0).getFrames().get(1).getSignals().get(2).getName());
        assertEquals(12, conf.getVehicles().get(0).getFrames().get(1).getSignals().get(1).getPosition());
        assertEquals(3, conf.getVehicles().get(0).getFrames().get(0).getSignals().get(4).getLength());
    }
    
    @Test
    @DisplayName("Example config system property")
    void loadExampleConfigSysProp() throws Exception {
        VehicleStorage conf = new VehicleStorage();
        System.setProperty("configFile", "test/configs/config-example.json");
        conf.loadFromFile();
        assertEquals(1, conf.getVehicles().size());
        assertEquals(2, conf.getVehicles().get(0).getFrames().size());
        assertEquals(5, conf.getVehicles().get(0).getFrames().get(0).getSignals().size());
        assertEquals(4, conf.getVehicles().get(0).getFrames().get(1).getSignals().size());
        assertEquals("BrakeForceFR", conf.getVehicles().get(0).getFrames().get(1).getSignals().get(2).getName());
        assertEquals(12, conf.getVehicles().get(0).getFrames().get(1).getSignals().get(1).getPosition());
        assertEquals(3, conf.getVehicles().get(0).getFrames().get(0).getSignals().get(4).getLength());
    }
    
    @Test
    @DisplayName("Multi-car config")
    void loadMulticarsConfig() throws Exception {
        VehicleStorage conf = new VehicleStorage();
        conf.loadFromFile("test/configs/config-multicars.json");
        assertEquals(2, conf.getVehicles().size());
        assertEquals(2, conf.getVehicles().get(0).getFrames().size());
        assertEquals(2, conf.getVehicles().get(1).getFrames().size());
        assertEquals("MyFavouriteVehicleType", conf.getVehicles().get(0).getVehicleType());
        assertEquals("MyHatedVehicleType", conf.getVehicles().get(1).getVehicleType());
        assertEquals(5, conf.getVehicles().get(0).getFrames().get(0).getSignals().size());
        assertEquals(4, conf.getVehicles().get(0).getFrames().get(1).getSignals().size());
        assertEquals("BrakeForceFR", conf.getVehicles().get(0).getFrames().get(1).getSignals().get(2).getName());
        assertEquals(12, conf.getVehicles().get(0).getFrames().get(1).getSignals().get(1).getPosition());
        assertEquals(3, conf.getVehicles().get(0).getFrames().get(0).getSignals().get(4).getLength());
        assertEquals("Coolness", conf.getVehicles().get(1).getFrames().get(0).getSignals().get(5).getName());
    }
    
    @AfterAll
    static void destroy() {
        if (originalConfigFile == null)
            System.clearProperty("configFile");
        else
            System.setProperty("configFile", originalConfigFile);
    }
    
    // public void setRanges() 
    // public VehicleType getVehicleByName(String name) {
    
}
