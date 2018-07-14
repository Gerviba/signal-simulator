package hu.gerviba.simulator.dao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class ServerStatusResponse {

    @Getter
    @Setter
    private long success;
    
    @Getter
    @Setter
    private long failed;
    
    @Getter
    @Setter
    private int slaves;
    
    @Getter
    @Setter
    private long vehicles;
    
    @Getter
    @Setter
    private long maxMemory;
    
    @Getter
    @Setter
    private long freeMemory;
    
    @Getter
    @Setter
    private boolean running;
    
    @Getter
    @Setter
    private boolean cluster;
    
    public ServerStatusResponse() {}
    
}
