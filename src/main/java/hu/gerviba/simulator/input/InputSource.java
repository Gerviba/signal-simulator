package hu.gerviba.simulator.input;

import javax.annotation.PostConstruct;

public interface InputSource {

    @PostConstruct
    public void init();

    public boolean isRunning();

    public void start();
    
    public void stop();

    public void setScope(long fromId, long toId);
    
}
