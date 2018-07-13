package hu.gerviba.simulator.input;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import hu.gerviba.simulator.model.Signal;

@DisplayName("Simple Input Assembler")
public class SimpleInputAssemblerTest {

    @Test
    @DisplayName("Constructor Long vehicle ID")
    void testConstructorLongId() throws Exception {
        SimpleInputAssembler ia = new SimpleInputAssembler(new ArrayList<>(), 
                0b10000001_10000010_10000011_10000100L, (short) -1);
        assertArrayEquals(new byte[] {-124, -125, -126, -127, -1}, 
                ia.getPacket());
    }

    @Test
    @DisplayName("Constructor Short vehicle ID")
    void testConstructorShortId() throws Exception {
        SimpleInputAssembler ia = new SimpleInputAssembler(new ArrayList<>(), 1, (byte) 1);
        assertArrayEquals(new byte[] {
            (byte) 0b00000001, 
            (byte) 0b00000000, 
            (byte) 0b00000000, 
            (byte) 0b00000000, 
            (byte) 0b00000001
        }, ia.getPacket());
    }
    
    @Test
    @DisplayName("Append signal - Example 1")
    void testSignalAppendEx1() throws Exception {
        List<Signal> signals = Arrays.asList(
                new Signal("EngineRPM", 0, 16),
                new Signal("EngineTemperature", 16, 8),
                new Signal("VehicleSpeed", 24, 12),
                new Signal("IgnitionStatus", 36, 1),
                new Signal("Gear", 37, 3));
        signals.sort(Signal.SORT_COMPARATOR);
        
        SimpleInputAssembler ia = new SimpleInputAssembler(signals, 1, (short) 1);
        ia.appendSignal(signals.get(0), 0b10000000_00000001);
        ia.appendSignal(signals.get(1), 0b10000001);
        ia.appendSignal(signals.get(2), 0b1000_00000001);
        ia.appendSignal(signals.get(3), 0b1);
        ia.appendSignal(signals.get(4), 0b101);
        
        assertArrayEquals(new byte[] {
            (byte) 0b00000001,
            (byte) 0b00000000,
            (byte) 0b00000000,
            (byte) 0b00000000,
            (byte) 0b00000001,
            (byte) 0b00000001,
            (byte) 0b10000000,
            (byte) 0b10000001,
            (byte) 0b00000001,
            (byte) 0b10111000
        }, ia.commit());
    }
    
    @Test
    @DisplayName("Byte array vehicleId - Example 1")
    void testByteArrayvehicleId() throws Exception {
        List<Signal> signals = Arrays.asList(
                new Signal("EngineRPM", 0, 16),
                new Signal("EngineTemperature", 16, 8),
                new Signal("VehicleSpeed", 24, 12),
                new Signal("IgnitionStatus", 36, 1),
                new Signal("Gear", 37, 3));
        signals.sort(Signal.SORT_COMPARATOR);
        
        SimpleInputAssembler ia = new SimpleInputAssembler(signals, 1, (short) 1);
        ia.appendSignal(signals.get(0), 0b10000000_00000001);
        ia.appendSignal(signals.get(1), 0b10000001);
        ia.appendSignal(signals.get(2), 0b1000_00000001);
        ia.appendSignal(signals.get(3), 0b1);
        ia.appendSignal(signals.get(4), 0b101);
        
        assertArrayEquals(new byte[] {
            (byte) 0b00000001,
            (byte) 0b00000000,
            (byte) 0b00000000,
            (byte) 0b00000000,
            (byte) 0b00000001,
            (byte) 0b00000001,
            (byte) 0b10000000,
            (byte) 0b10000001,
            (byte) 0b00000001,
            (byte) 0b10111000
        }, ia.commit());
    }
    
    @Test
    @DisplayName("Append signal - Example 2")
    void testSignalAppendEx2() throws Exception {
        List<Signal> signals = Arrays.asList(
                new Signal("BrakeForceRR", 0, 12),
                new Signal("BrakeForceRL", 12, 12),
                new Signal("BrakeForceFR", 24, 12),
                new Signal("BrakeForceFL", 36, 12));
        signals.sort(Signal.SORT_COMPARATOR);
        
        SimpleInputAssembler ia = new SimpleInputAssembler(signals, 1, (short) 1);
        ia.appendSignal(signals.get(0), 0b1000_00000001);
        ia.appendSignal(signals.get(1), 0b1000_00000001);
        ia.appendSignal(signals.get(2), 0b1000_00000001);
        ia.appendSignal(signals.get(3), 0b1000_00000001);
        
        assertArrayEquals(new byte[] {
            (byte) 0b00000001,
            (byte) 0b00000000,
            (byte) 0b00000000,
            (byte) 0b00000000,
            (byte) 0b00000001,
            (byte) 0b00000001,
            (byte) 0b00011000,
            (byte) 0b10000000,
            (byte) 0b00000001,
            (byte) 0b00011000,
            (byte) 0b10000000
        }, ia.commit());
        
        assertArrayEquals(new byte[] {
            (byte) 0b00000001,
            (byte) 0b00000000,
            (byte) 0b00000000,
            (byte) 0b00000000,
            (byte) 0b00000001,
            (byte) 0b00000000,
            (byte) 0b00000000,
            (byte) 0b00000000,
            (byte) 0b00000000,
            (byte) 0b00000000,
            (byte) 0b00000000
        }, ia.getPacket(), "Commit is not working as expected");
    }
    
    @Test
    @DisplayName("Zero filled empty space")
    void testZeroFill() throws Exception {
        List<Signal> signals = Arrays.asList(
                new Signal("Test", 0, 1));
        signals.sort(Signal.SORT_COMPARATOR);
        
        SimpleInputAssembler ia = new SimpleInputAssembler(signals, 1, (short) 1);
        ia.appendSignal(signals.get(0), 0b1);
        
        assertArrayEquals(new byte[] {
            (byte) 0b00000001,
            (byte) 0b00000000,
            (byte) 0b00000000,
            (byte) 0b00000000,
            (byte) 0b00000001,
            (byte) 0b00000001
        }, ia.getPacket());
    }
    
    @Test
    @DisplayName("Break line")
    void testBreakLine() throws Exception {
        List<Signal> signals = Arrays.asList(
                new Signal("Test1", 0, 1),
                new Signal("Test2", 1, 1),
                new Signal("Test3", 2, 1),
                new Signal("Test4", 3, 1),
                new Signal("Test5", 4, 1),
                new Signal("Test6", 5, 1),
                new Signal("Test7", 6, 1),
                new Signal("Test8", 7, 1),
                new Signal("Test9", 8, 1));
        signals.sort(Signal.SORT_COMPARATOR);
        
        SimpleInputAssembler ia = new SimpleInputAssembler(signals, 1, (short) 1);
        for (int i = 0; i < 9; ++i)
            ia.appendSignal(signals.get(i), 0b1);
        
        assertArrayEquals(new byte[] {
            (byte) 0b00000001,
            (byte) 0b00000000,
            (byte) 0b00000000,
            (byte) 0b00000000,
            (byte) 0b00000001,
            (byte) 0b11111111,
            (byte) 0b00000001,
        }, ia.getPacket());
    }
    @Test
    @DisplayName("Longer data than accepted")
    void testOverWriting() throws Exception {
        List<Signal> signals = Arrays.asList(
                new Signal("Test1", 0, 3));
        signals.sort(Signal.SORT_COMPARATOR);
        
        SimpleInputAssembler ia = new SimpleInputAssembler(signals, 1, (short) 1);
        ia.appendSignal(signals.get(0), 0b11111111);
        
        assertArrayEquals(new byte[] {
            (byte) 0b00000001,
            (byte) 0b00000000,
            (byte) 0b00000000,
            (byte) 0b00000000,
            (byte) 0b00000001,
            (byte) 0b00000111,
        }, ia.getPacket());
    }
    
    @Test
    @DisplayName("Reuse")
    void testReuse() throws Exception {
        List<Signal> signals = Arrays.asList(
                new Signal("BrakeForceRR", 0, 12),
                new Signal("BrakeForceRL", 12, 12),
                new Signal("BrakeForceFR", 24, 12),
                new Signal("BrakeForceFL", 36, 12));
        signals.sort(Signal.SORT_COMPARATOR);
        
        SimpleInputAssembler ia = new SimpleInputAssembler(signals, 1, (short) 1);
        ia.appendSignal(signals.get(0), 0b1000_00000001);
        ia.appendSignal(signals.get(1), 0b1000_00000001);
        ia.appendSignal(signals.get(2), 0b1000_00000001);
        ia.appendSignal(signals.get(3), 0b1000_00000001);
        
        assertArrayEquals(new byte[] {
            (byte) 0b00000001,
            (byte) 0b00000000,
            (byte) 0b00000000,
            (byte) 0b00000000,
            (byte) 0b00000001,
            (byte) 0b00000001,
            (byte) 0b00011000,
            (byte) 0b10000000,
            (byte) 0b00000001,
            (byte) 0b00011000,
            (byte) 0b10000000
        }, ia.commit());
        
        ia.appendSignal(signals.get(0), 0b1000_00000011);
        ia.appendSignal(signals.get(1), 0b1000_00000001);
        ia.appendSignal(signals.get(2), 0b1000_00000001);
        ia.appendSignal(signals.get(3), 0b1100_00000001);
        
        assertArrayEquals(new byte[] {
            (byte) 0b00000001,
            (byte) 0b00000000,
            (byte) 0b00000000,
            (byte) 0b00000000,
            (byte) 0b00000001,
            (byte) 0b00000011,
            (byte) 0b00011000,
            (byte) 0b10000000,
            (byte) 0b00000001,
            (byte) 0b00011000,
            (byte) 0b11000000
        }, ia.commit());
    }
    
}