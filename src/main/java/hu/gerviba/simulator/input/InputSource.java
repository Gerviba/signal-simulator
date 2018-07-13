package hu.gerviba.simulator.input;

public interface InputSource {

    void init();

    boolean isRunning();

    void start();
    
    void stop();

    void setScope(long fromId, long toId);
    
}
