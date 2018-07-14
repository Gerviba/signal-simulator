package hu.gerviba.simulator.dao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class SlaveStatusResponse {

    @Getter
    @Setter
    private boolean running;
    
    @Getter
    @Setter
    private String message;
    
    @Getter
    @Setter
    private long success;
    
    @Getter
    @Setter
    private long failed;
    
    public SlaveStatusResponse() {}
    
    public SlaveStatusResponse(boolean running, String message, long success, long failed) {
        this.running = running;
        this.message = message;
        this.success = success;
        this.failed = failed;
    }
    
}
