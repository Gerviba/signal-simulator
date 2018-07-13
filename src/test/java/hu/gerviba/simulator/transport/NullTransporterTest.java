package hu.gerviba.simulator.transport;

import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Null Transporter")
public class NullTransporterTest {

    @Test
    @DisplayName("Simple test")
    void testSimple() throws Exception {
        Transporter transporter = new NullTransporter();
        assertTrue(transporter.sendToCloud(new byte[] {10, 20, 30, 40}));
    }
    
}
