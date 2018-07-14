package hu.gerviba.simulator.input;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hu.gerviba.simulator.assets.DeterministicRandom;
import hu.gerviba.simulator.config.VehicleStorage;
import hu.gerviba.simulator.input.RandomInputGenerator.RandomSignalGenerator;

@DisplayName("Random Input Generator")
public class RandomInputGeneratorTest {

    @Test
    @DisplayName("Vehicle generator")
    void testVehicleGenerator() throws Exception {
        RandomInputGenerator rig = new RandomInputGenerator();
        rig.storage = new VehicleStorage();
        rig.storage.loadFromFile("test/configs/config-multicars.json");
        rig.storage.getVehicles().get(0).setCount(10);
        rig.storage.getVehicles().get(1).setCount(20);
        rig.storage.setRanges();
        rig.setScope(0, Long.MAX_VALUE); /** StandaloneConfiguration#init() */ 
        rig.generateInstances();
        assertEquals(30, rig.getVehicles().size());
        assertEquals(10, rig.getVehicles().stream()
                .map(x -> x.getVehicleType().getVehicleType())
                .filter(x -> x.equals("MyFavouriteVehicleType"))
                .count());
        assertEquals(20, rig.getVehicles().stream()
                .map(x -> x.getVehicleType().getVehicleType())
                .filter(x -> x.equals("MyHatedVehicleType"))
                .count());
    }

    @Test
    @DisplayName("Vehicle generator with ranges")
    void testVehicleGeneratorRanges() throws Exception {
        RandomInputGenerator rig = new RandomInputGenerator();
        rig.storage = new VehicleStorage();
        rig.storage.loadFromFile("test/configs/config-multicars.json");
        rig.storage.getVehicles().get(0).setCount(10);
        rig.storage.getVehicles().get(1).setCount(20);
        rig.storage.setRanges();
        rig.setScope(5, 14);
        rig.generateInstances();
        assertEquals(10, rig.getVehicles().size());
        assertEquals(5, rig.getVehicles().stream()
                .map(x -> x.getVehicleType().getVehicleType())
                .filter(x -> x.equals("MyFavouriteVehicleType"))
                .count());
        assertEquals(5, rig.getVehicles().stream()
                .map(x -> x.getVehicleType().getVehicleType())
                .filter(x -> x.equals("MyHatedVehicleType"))
                .count());
    }
    
    @Test
    @DisplayName("Vehicle generator - Zero count")
    void testVehicleGeneratorZeroCount() throws Exception {
        RandomInputGenerator rig = new RandomInputGenerator();
        rig.storage = new VehicleStorage();
        rig.storage.loadFromFile("test/configs/config-multicars.json");
        rig.storage.getVehicles().get(0).setCount(10);
        rig.storage.getVehicles().get(1).setCount(20);
        rig.storage.setRanges();
        rig.generateInstances();
        assertEquals(0, rig.getVehicles().size());
    }
    
    @Test
    @DisplayName("Data generation - 16 bit")
    void testDataGeneration16bit() throws Exception {
        RandomInputGenerator rig = new RandomInputGenerator();
        rig.storage = new VehicleStorage();
        rig.storage.loadFromFile("test/configs/config-example.json");
        rig.storage.getVehicles().get(0).setCount(1);
        rig.storage.setRanges();
        rig.setScope(0, 0);
        rig.generateInstances();
        
        assertEquals(16, rig.storage.getVehicles().get(0)
                .getFrames().get(0)
                .getSignals().get(0).getLength());
        
        RandomSignalGenerator rsg = new RandomInputGenerator.RandomSignalGenerator(
                rig.storage.getVehicles().get(0)
                    .getFrames().get(0)
                    .getSignals().get(0), 
                new DeterministicRandom(
                    Arrays.asList(0L, 0xFFL, 0xFFFFL, 0xFFFFFFL, 
                            0b1000000000000001L, Long.MAX_VALUE)));
        
        assertEquals(0L, rsg.next());
        assertEquals(0xFFL, rsg.next());
        assertEquals(0xFFFFL, rsg.next());
        assertEquals(0xFFFFL, rsg.next());
        assertEquals(0b10000000_00000001L, rsg.next());
        assertEquals(0xFFFFL, rsg.next());
    }    
    
    @Test
    @DisplayName("Data generation - 1 bit")
    void testDataGeneration1bit() throws Exception {
        RandomInputGenerator rig = new RandomInputGenerator();
        rig.storage = new VehicleStorage();
        rig.storage.loadFromFile("test/configs/config-example.json");
        rig.storage.getVehicles().get(0).setCount(1);
        rig.storage.setRanges();
        rig.setScope(0, 0);
        rig.generateInstances();
        
        assertEquals(1, rig.storage.getVehicles().get(0)
                .getFrames().get(0)
                .getSignals().get(3).getLength());
        
        RandomSignalGenerator rsg = new RandomInputGenerator.RandomSignalGenerator(
                rig.storage.getVehicles().get(0)
                    .getFrames().get(0)
                    .getSignals().get(3), 
                new DeterministicRandom(
                    Arrays.asList(0L, 1L, 2L, 3L, 0xFFFFFFL, 
                            0b1000000000000001L, Long.MAX_VALUE)));
        
        assertEquals(0L, rsg.next());
        assertEquals(1L, rsg.next());
        assertEquals(0L, rsg.next());
        assertEquals(1L, rsg.next());
        assertEquals(1L, rsg.next());
        assertEquals(1L, rsg.next());
        assertEquals(1L, rsg.next());
    }
    
}
