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
    
    private Map<String, AtomicLong> values;
    
    public VehicleInstance(byte[] vehicleId, VehicleType vehicleType) {
        this.vehicleId = vehicleId;
        this.vehicleType = vehicleType;
        vehicleType.getFrames().forEach(frame ->
            this.assemblers.put(frame.getFrameID(), new SimpleInputAssembler(
                    frame.getSignals(), vehicleId, frame.getFrameID())));
    }

    public void initVariables() {
        values = new HashMap<>();
    }
    
    public InputAssembler getAssembler(short frameId) {
        return assemblers.get(frameId);
    }
    
    public AtomicLong getVariable(String key) {
        return values.get(key);
    }

    public AtomicLong addVariable(String key, AtomicLong value) {
        return values.put(key, value);
    }
    
}
