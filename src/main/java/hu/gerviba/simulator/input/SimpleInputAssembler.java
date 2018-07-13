package hu.gerviba.simulator.input;

import java.util.Arrays;
import java.util.List;

import hu.gerviba.simulator.model.Signal;
import lombok.Getter;

public class SimpleInputAssembler implements InputAssembler {

    /** VehicleID length in byte */
    private static final int VEHICLE_ID_LENGTH = 4;
    /** bitmask for VehicleID */
    private static final int VEHICLE_ID_MASK = 0xFFFFFFFF;
    
    /** FrameID length in byte */
    private static final int FRAME_ID_LENGTH = 1;
    /** bitmask for FrameID */
    private static final int FRAME_ID_MASK = 0xFF;
    
    @Getter
    private byte[] packet;
    
    private final byte[] packetBase;
    
    int currentIndex = 0;
    int currentBit = 0;
    
    public SimpleInputAssembler(List<Signal> signals, long vehicleId, short frameId) {
        packetBase = new byte[getSignalLength(signals) + VEHICLE_ID_LENGTH + FRAME_ID_LENGTH];
        append(packetBase, vehicleId & VEHICLE_ID_MASK, VEHICLE_ID_LENGTH * 8);
        append(packetBase, frameId & FRAME_ID_MASK, FRAME_ID_LENGTH * 8);
//        packetBase[VEHICLE_ID_LENGTH] = (byte) frameId;
        commit();
    }
    
    public SimpleInputAssembler(List<Signal> signals, byte[] vehicleId, short frameId) {
        packetBase = new byte[getSignalLength(signals) + VEHICLE_ID_LENGTH + FRAME_ID_LENGTH];
        for (int i = 0; i < VEHICLE_ID_LENGTH; ++i)
            packetBase[i] = vehicleId[i];
        packetBase[VEHICLE_ID_LENGTH] = (byte) frameId;
        commit();
    }
    
    @Override
    public synchronized void appendSignal(Signal signal, long data) {
        append(packet, data & getLongMask(signal.getLength()), signal.getLength());
    }

    @Override
    public synchronized byte[] commit() {
        currentIndex = VEHICLE_ID_LENGTH + FRAME_ID_LENGTH;
        currentBit = 0;
        byte[] result = packet;
        packet = Arrays.copyOf(packetBase, packetBase.length);
        return result;
    }

    int getSignalLength(List<Signal> signals) {
        return (int) Math.ceil(signals.stream()
                .mapToInt(sig -> sig.getLength())
                .sum() / 8.0);
    }
    
    void append(byte[] container, long data, int length) {
        final int bitSize = Math.min(8 - currentBit, length);
        container[currentIndex] |= (byte) ((data & getMask(bitSize)) << currentBit);
        data >>= bitSize;
        length -= bitSize;
        
        currentBit += bitSize;
        if (currentBit == 8) {
            currentBit = 0;
            ++currentIndex;
        }
        
        if (length != 0)
            append(container, data, length);
    }
    
    long getMask(int size) {
        return 0xFFL >> (8 - size);
    }

    long getLongMask(int size) {
        return 0xFFFFFFFFFFFFFFFFL >> (64 - size);
    }
    
}
