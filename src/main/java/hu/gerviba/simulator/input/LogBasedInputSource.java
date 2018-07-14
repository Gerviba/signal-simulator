package hu.gerviba.simulator.input;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import hu.gerviba.simulator.service.InputProcessorService;

public class LogBasedInputSource implements InputSource {

    @Autowired
    InputProcessorService inputProcessor;
    
    @PostConstruct
    @Override
    public void init() {
        // TODO Load signal data from file
        throw new RuntimeException("This feature is not implemented!");
    }

    @Override
    public boolean isRunning() {
        // TODO Is there any scheduled tasks?
        return false;
    }

    @Override
    public void start() {
        // TODO Schedule a fixed-rate task to send the loaded 
        // sensor data via inputProcessor.applyInput(byte[])
    }

    @Override
    public void stop() {
        // TODO Stop the scheduled repeating tasks
    }

    @Override
    public void setScope(long fromId, long toId) {
        // TODO Filter the recorded data
    }

}
