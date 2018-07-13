package hu.gerviba.simulator.model;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import hu.gerviba.simulator.input.InputAssembler;
import hu.gerviba.simulator.input.SimpleInputAssembler;
import lombok.Getter;
import lombok.ToString;

@ToString
public class VehicleInstance {
    
    @Getter
    private final byte[] vehicleId;

    @Getter
    private final VehicleType vehicleType;
    
    private Map<Short, InputAssembler> assemblers = new HashMap<>();
    
    private Map<String, AtomicLong> values = new HashMap<>();
    
    public VehicleInstance(byte[] vehicleId, VehicleType vehicleType) {
        this.vehicleId = vehicleId;
        this.vehicleType = vehicleType;
        vehicleType.getFrames().forEach(frame ->
            this.assemblers.put(frame.getFrameID(), new SimpleInputAssembler(
                    frame.getSignals(), vehicleId, frame.getFrameID())));
    }

    public InputAssembler getAssembler(short frameId) {
        return assemblers.get(frameId);
    }
    
    public AtomicLong getVariable(Object key) {
        return values.get(key);
    }

    public AtomicLong setVariable(String key, AtomicLong value) {
        return values.put(key, value);
    }
    
}
