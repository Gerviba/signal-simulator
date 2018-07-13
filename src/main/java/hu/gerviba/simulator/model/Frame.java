package hu.gerviba.simulator.model;

import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Frame {

    @Getter 
    @Setter
    private String name;
    
    @Getter 
    @Setter
    private short frameID;
    
    @Getter 
    @Setter
    private int period;
    
    @Getter 
    private List<Signal> signals;
    
    @Getter
    @Setter
    private VehicleType vehicleType;
    
    public Frame() {}

    public Frame(String name, VehicleType vehicleType, short frameID, int period, List<Signal> signals) {
        this.name = name;
        this.vehicleType = vehicleType;
        this.frameID = frameID;
        this.period = period;
        this.signals = signals;
    }
    
    public void setSignals(List<Signal> signals) {
        this.signals = signals;
        this.signals.sort(Signal.SORT_COMPARATOR);
    }

    
}
