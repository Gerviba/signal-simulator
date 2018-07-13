package hu.gerviba.simulator.model;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@EqualsAndHashCode(of = {"vehicleType"})
@ToString
public class VehicleType {
    
    @Getter
    @Setter
    private String vehicleType;
    
    @Getter
    @Setter
    private List<Frame> frames;
    
    @Getter
    private AtomicLong idFrom = new AtomicLong(-1);

    @Getter
    private AtomicLong idTo = new AtomicLong(-1);

    @Getter
    private AtomicLong count = new AtomicLong(0);

    public VehicleType() {};
    
    public VehicleType(String vehicleType, List<Frame> frames) {
        this.vehicleType = vehicleType;
        this.frames = frames;
    }

    public void setIdFrom(long idFrom) {
        this.idFrom.set(idFrom);
    }

    public void setIdTo(long idTo) {
        this.idTo.set(idTo);
    }

    public void setCount(long count) {
        this.count.set(count);
    }

    public Frame getFrame(short frameId) {
        for (Frame f : frames)
            if (f.getFrameID() == frameId)
                return f;
        return null;
    }
    
}
