package hu.gerviba.simulator.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.gerviba.simulator.model.VehicleType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class MasterSlaveCommand {

    @Getter
    @Setter
    private long idFrom;
    
    @Getter
    @Setter
    private long idTo;
    
    @Getter
    @Setter
    private Map<String, Long> vehicleCounts = new HashMap<>();
    
    public MasterSlaveCommand() {};
    
    public MasterSlaveCommand(long idFrom, long idTo, List<VehicleType> vehicles) {
        this.idFrom = idFrom;
        this.idTo = idTo;
        vehicles.forEach(veh -> vehicleCounts.put(veh.getVehicleType(), veh.getCount().get()));
    }
    
}
