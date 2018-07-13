package hu.gerviba.simulator.service;

import java.util.concurrent.ScheduledFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

@Service
public class SchedulerService {

    @Autowired
    private TaskScheduler executor;
    
    public ScheduledFuture<?> schedule(final Runnable task, long time) {
        return executor.scheduleAtFixedRate(task, time);
    }
    
}
