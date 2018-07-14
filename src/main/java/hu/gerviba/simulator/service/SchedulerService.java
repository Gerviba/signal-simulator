package hu.gerviba.simulator.service;

import java.util.concurrent.ScheduledFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class SchedulerService {

    @Autowired
    private TaskScheduler executor;
    
    public ScheduledFuture<?> schedule(Runnable task, long time) {
        return executor.scheduleAtFixedRate(task, time);
    }

    @Async
    public void runAsync(Runnable task) {
        task.run();
    }

}
