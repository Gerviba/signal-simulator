package hu.gerviba.simulator.transport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Debug Transporter")
public class DebugTransporterTest {

    ByteArrayOutputStream baos;
    PrintStream out;
    
    @BeforeEach
    void before() {
        baos = new ByteArrayOutputStream();
        out = new PrintStream(baos);
    }
    
    @AfterEach
    void close() throws IOException {
        out.close();
        baos.close();
    }
    
    @Test
    @DisplayName("Simple input")
    void testSendSimple() throws Exception {
        Transporter transporter = new DebugTransporter(out);
        transporter.sendToCloud(new byte[] { (byte) 255, (byte) 0, (byte) 127, (byte) -128, (byte) 42 });
        
        assertEquals("   | 76543210 | DEC\n" + 
                "---|----------|-----\n" + 
                "00 | 11111111 |   -1\n" + 
                "01 | 00000000 |    0\n" + 
                "02 | 01111111 |  127\n" + 
                "03 | 10000000 | -128\n" + 
                "04 | 00101010 |   42\n" + 
                "END\n", baos.toString().replace(System.lineSeparator(), "\n"));
    }

    @Test
    @DisplayName("Empty input")
    void testSendEmpty() throws Exception {
        Transporter transporter = new DebugTransporter(out);
        transporter.sendToCloud(new byte[] { });
        
        assertEquals("   | 76543210 | DEC\n" + 
                "---|----------|-----\n" + 
                "END\n", baos.toString().replace(System.lineSeparator(), "\n"));
    }

    @Test
    @DisplayName("Null input")
    void testSendNull() throws Exception {
        Transporter transporter = new DebugTransporter(out);
        assertThrows(NullPointerException.class, () -> transporter.sendToCloud(null));
    }
    
    @Test
    @DisplayName("Sysout output")
    void testSysout() throws Exception {
        PrintStream ps = System.out;
        System.setOut(out);
        Transporter transporter = new DebugTransporter();
        transporter.sendToCloud(new byte[] { (byte) 255, (byte) 0, (byte) 3, (byte) 56, (byte) 42 });
        
        assertEquals("   | 76543210 | DEC\n" + 
                "---|----------|-----\n" + 
                "00 | 11111111 |   -1\n" + 
                "01 | 00000000 |    0\n" + 
                "02 | 00000011 |    3\n" + 
                "03 | 00111000 |   56\n" + 
                "04 | 00101010 |   42\n" + 
                "END\n", baos.toString().replace(System.lineSeparator(), "\n"));
        
        System.setOut(ps);
    }
    
}
