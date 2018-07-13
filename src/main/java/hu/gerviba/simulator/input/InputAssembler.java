package hu.gerviba.simulator.input;

import hu.gerviba.simulator.model.Signal;

public interface InputAssembler {

    public void appendSignal(Signal signal, long data);
    
    public byte[] commit();
    
    public byte[] getPacket();
    
    public static byte[] generateVehicleIdBytes(final long vehicleId) {
        return new byte[] {
            (byte) ((vehicleId) & 0xFFL),
            (byte) ((vehicleId >> 8) & 0xFFL),
            (byte) ((vehicleId >> 16) & 0xFFL),
            (byte) ((vehicleId >> 24) & 0xFFL)
        };
    }
    
}
