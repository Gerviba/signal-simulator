package hu.gerviba.simulator.input;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hu.gerviba.simulator.assets.DeterministicRandom;
import hu.gerviba.simulator.config.VehicleStorage;
import hu.gerviba.simulator.input.RandomFlowInputGenerator.RandomDeltaGenerator;
import hu.gerviba.simulator.model.Signal;
import hu.gerviba.simulator.model.VehicleInstance;

@DisplayName("Random flow input generator")
public class RandomFlowInputGeneratorTest {

    @Test
    @DisplayName("Delta function")
    void testDeltaFunction() throws Exception {
        RandomDeltaGenerator rdg = new RandomFlowInputGenerator.RandomDeltaGenerator(
                new Signal("", 0, 16), 
                new DeterministicRandom(Arrays.asList(101, 101, 100, 50, 30, 0, -40, -41, -70, -100, -128)));
        assertEquals(2, rdg.deltaFunction());
        assertEquals(2, rdg.deltaFunction());
        assertEquals(1, rdg.deltaFunction());
        assertEquals(1, rdg.deltaFunction());
        assertEquals(0, rdg.deltaFunction());
        assertEquals(0, rdg.deltaFunction());
        assertEquals(0, rdg.deltaFunction());
        assertEquals(-1, rdg.deltaFunction());
        assertEquals(-1, rdg.deltaFunction());
        assertEquals(-1, rdg.deltaFunction());
        assertEquals(-2, rdg.deltaFunction());
    }
    
    @Test
    @DisplayName("Data flow generation")
    void testDataGeneration16bit() throws Exception {
        RandomFlowInputGenerator rfig = new RandomFlowInputGenerator();
        rfig.storage = new VehicleStorage();
        rfig.storage.loadFromFile("test/configs/config-example.json");
        rfig.storage.getVehicles().get(0).setCount(1);
        rfig.storage.setRanges();
        rfig.setScope(0, 0);
        rfig.generateInstances();
        
        assertEquals(16, rfig.storage.getVehicles().get(0)
                .getFrames().get(0)
                .getSignals().get(0).getLength());
        
        RandomDeltaGenerator rdg = new RandomFlowInputGenerator.RandomDeltaGenerator(
                rfig.storage.getVehicles().get(0)
                    .getFrames().get(0)
                    .getSignals().get(0), 
                new DeterministicRandom(
                    Arrays.asList(101, 101, 101, 101, 101, 101, 101, 101, 101, -101,
                            -101)));
        
        assertEquals(rdg.maxDelta, 16);
        assertEquals(rdg.minDelta, -16);
        
        VehicleInstance vehicle = rfig.getVehicles().get(0);
        vehicle.addVariable(rdg.getSignal().getName(), new AtomicLong());
        String deltaName = rdg.getSignal().getName() + RandomFlowInputGenerator.DELTA_SUFFIX;
        vehicle.addVariable(deltaName, new AtomicLong());
        assertEquals(2L, rdg.next(vehicle));
        assertEquals(2L, vehicle.getVariable(deltaName).get());
        assertEquals(6L, rdg.next(vehicle));
        assertEquals(4L, vehicle.getVariable(deltaName).get());
        assertEquals(12L, rdg.next(vehicle));
        assertEquals(6L, vehicle.getVariable(deltaName).get());
        assertEquals(20L, rdg.next(vehicle));
        assertEquals(8L, vehicle.getVariable(deltaName).get());
        assertEquals(30L, rdg.next(vehicle));
        assertEquals(10L, vehicle.getVariable(deltaName).get());
        assertEquals(42L, rdg.next(vehicle));
        assertEquals(12L, vehicle.getVariable(deltaName).get());
        assertEquals(56L, rdg.next(vehicle));
        assertEquals(14L, vehicle.getVariable(deltaName).get());
        assertEquals(72L, rdg.next(vehicle));
        assertEquals(16L, vehicle.getVariable(deltaName).get());
        assertEquals(88L, rdg.next(vehicle));
        assertEquals(16L, vehicle.getVariable(deltaName).get());
        assertEquals(102L, rdg.next(vehicle));
        assertEquals(14L, vehicle.getVariable(deltaName).get());
        assertEquals(114L, rdg.next(vehicle));
    }    
    
}
