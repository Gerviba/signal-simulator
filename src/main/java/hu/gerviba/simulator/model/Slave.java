package hu.gerviba.simulator.model;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class Slave {

    @Getter
    @Setter
    private String host;
    
    @Getter
    @Setter
    @JsonIgnore
    private String key;
    
    @Getter
    private AtomicBoolean available = new AtomicBoolean(false);

    @Getter
    private AtomicBoolean running = new AtomicBoolean(false);
    
    @Getter
    private AtomicLong success = new AtomicLong(0);
    
    @Getter
    private AtomicLong failed = new AtomicLong(0);
    
    @Getter
    private AtomicLong rangeStart = new AtomicLong(-1);
    
    @Getter
    private AtomicLong rangeEnd = new AtomicLong(-1);
    
    @Getter
    private AtomicLong lastUpdated = new AtomicLong(System.currentTimeMillis());
    
    @Getter
    private AtomicLong count = new AtomicLong(0);
    
    public Slave() {}
    
    public Slave(String host, String key) {
        this.host = host;
        this.key = key;
    }

    public boolean changeAvailable(boolean available) {
        if (available) {
            this.lastUpdated.set(System.currentTimeMillis());
        } else {
            this.success.set(0);
            this.failed.set(0);
        }
        boolean old = this.available.get();
        this.available.set(available);
        return this.available.get() != old;
    }

    public void setRunning(boolean running) {
        this.running.set(running);
    }

    public void setSuccess(long success) {
        this.success.set(success);
    }

    public void setFailed(long failed) {
        this.failed.set(failed);
    }

    public void setRangeStart(long rangeStart) {
        this.rangeStart.set(rangeStart);
    }

    public void setRangeEnd(long rangeEnd) {
        this.rangeEnd.set(rangeEnd);
    }

    public void setCount(long count) {
        this.count.set(count);
    }
    
    
}
