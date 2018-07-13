package hu.gerviba.simulator.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class Slaves {

    @Getter
    @Setter
    private String host;
    
    @Getter
    @Setter
    private String key;
    
    @Getter
    @Setter
    private boolean available = false;

    @Getter
    @Setter
    private boolean running = false;
    
    @Getter
    @Setter
    private long success = 0;
    
    @Getter
    @Setter
    private long failed = 0;
    
    @Getter
    @Setter
    private long rangeStart = 0;
    
    @Getter
    @Setter
    private long rangeEnd = 0;
    
    public Slaves() {}
    
    public Slaves(String host, String key) {
        this.host = host;
        this.key = key;
    }

    public Slaves(String host, String key, boolean available, boolean running, long success, long failed, long rangeStart, long rangeEnd) {
        this.host = host;
        this.key = key;
        this.available = available;
        this.running = running;
        this.success = success;
        this.failed = failed;
        this.rangeStart = rangeStart;
        this.rangeEnd = rangeEnd;
    }
    
}
