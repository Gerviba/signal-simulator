package hu.gerviba.simulator.transport;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hu.gerviba.simulator.transport.MockTransporter.MockTransaction;

@DisplayName("Mock Transporter")
public class MockTransporterTest {

    MockTransporter transporter;
    
    @BeforeEach
    void before() {
        transporter = new MockTransporter();
    }
    
    @Test
    @DisplayName("Simple input")
    void testSimple() throws Exception {
        transporter.sendToCloud(new byte[] {10, 20});
        transporter.sendToCloud(new byte[] {20, 30, 40});
        
        assertArrayEquals(new byte[] {10, 20}, transporter.readNext().getData());
        assertArrayEquals(new byte[] {20, 30, 40}, transporter.readNext().getData());
    }
    
    @Test
    @DisplayName("Reading empty content")
    void testNullRead() throws Exception {
        assertEquals(null, transporter.readNext());
    }
    
    @Test
    @DisplayName("Timings")
    void testTimings() throws Exception {
        transporter.sendToCloud(new byte[] {10});
        transporter.waitVirtualTime(1000);
        transporter.sendToCloud(new byte[] {20});
        transporter.waitVirtualTime(10);
        transporter.sendToCloud(new byte[] {30});
        
        MockTransaction mt1 = transporter.readNext();
        MockTransaction mt2 = transporter.readNext();
        MockTransaction mt3 = transporter.readNext();
        
        assertTrue(mt2.getTimestamp() - mt1.getTimestamp() >= 1000);
        assertTrue(mt3.getTimestamp() - mt2.getTimestamp() >= 10);
        assertTrue(mt3.getTimestamp() - mt2.getTimestamp() < 1000);
    }
    
}
