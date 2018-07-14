package hu.gerviba.simulator.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Slave pojo")
public class SlaveTest {
    
    @Test
    @DisplayName("Availability change")
    void testSetAvailable() throws Exception {
        Slave slave = new Slave();
        slave.changeAvailable(true);
        assertEquals(true, slave.getAvailable().get());
        assertEquals(false, slave.changeAvailable(true)); // Don't change
        assertEquals(true, slave.getAvailable().get());
        assertEquals(true, slave.changeAvailable(false)); // Change
        assertEquals(false, slave.getAvailable().get());
        assertEquals(false, slave.changeAvailable(false)); // Don't change
        assertEquals(false, slave.getAvailable().get());
        assertEquals(true, slave.changeAvailable(true)); // Change
        assertEquals(true, slave.getAvailable().get());
    }
    
}
