package hu.gerviba.simulator.service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import hu.gerviba.simulator.config.VehicleStorage;
import hu.gerviba.simulator.dao.MasterSlaveCommand;
import hu.gerviba.simulator.dao.SlaveStatusResponse;
import hu.gerviba.simulator.dao.StatusResponse;
import hu.gerviba.simulator.model.Slave;
import hu.gerviba.simulator.model.VehicleType;
import lombok.Getter;

@Profile("master")
@Service
public class ClusterService {
    
    @Autowired
    SchedulerService scheduler;
    
    @Autowired
    Environment env;

    @Autowired
    ServletContext app;
    
    @Autowired
    RestTemplate rest;

    @Autowired
    VehicleStorage storage;
    
    @Getter
    private List<Slave> slaves = Collections.synchronizedList(new LinkedList<>());
    private AtomicBoolean running = new AtomicBoolean();
    
    @Async
    @Scheduled(initialDelay = 5000, 
            fixedRateString = "${simulator.slave-update-period:1000}")
    public void updateSlaves() {
        AtomicBoolean changed = new AtomicBoolean(false);
        slaves.forEach(slave -> {
            try {
                SlaveStatusResponse response = rest.getForObject(
                        slave.getHost() + "/api/slave/status?apiKey={apiKey}",
                        SlaveStatusResponse.class, slave.getKey());
                slave.setAvailable(true);
                slave.setRunning(response.isRunning());
                slave.setSuccess(response.getSuccess());
                slave.setFailed(response.getFailed());
                ((AtomicLong) app.getAttribute("success")).addAndGet(response.getSuccess());
                ((AtomicLong) app.getAttribute("failed")).addAndGet(response.getSuccess());
            } catch (RestClientException e) {
                slave.setAvailable(false);
                slave.setRunning(false);
            }
        });
        if (changed.get()) {
            setRanges();
            setCounts();
            scheduler.runAsync(queryStart);
        }
    }
    
    Runnable queryStart = () -> slaves.forEach(slave -> {
        try {
            StatusResponse response = rest.postForObject(
                    slave.getHost() + "/api/slave/start?apiKey={apiKey}",
                    new MasterSlaveCommand(
                            slave.getRangeStart().get(),
                            slave.getRangeEnd().get(),
                            storage.getVehicles()),
                    StatusResponse.class, slave.getKey());
            slave.setAvailable(true);
            slave.setRunning(response.getStatus().equals("OK"));
        } catch (RestClientException e) {
            slave.setAvailable(false);
            slave.setRunning(false);
        }
    });
    
    Runnable queryStop = () -> slaves.forEach(slave -> {
        try {
            StatusResponse response = rest.postForObject(
                    slave.getHost() + "/api/slave/stop?apiKey={apiKey}",
                    new HttpEntity<String>(""),
                    StatusResponse.class, slave.getKey());
            slave.setAvailable(true);
            slave.setRunning(!response.getStatus().equals("OK"));
        } catch (RestClientException e) {
            slave.setAvailable(false);
            slave.setRunning(false);
        }
    });
    
    @PostConstruct
    public void init() {
        for (String slave : env.getProperty("simulator.slaves").split(";"))
            slaves.add(new Slave(slave.split("\\|")[0], slave.split("\\|")[1]));
    }
    
    public boolean isRunning() {
        return running.get();
    }

    public void start() {
        ((AtomicLong) app.getAttribute("success")).set(0);
        ((AtomicLong) app.getAttribute("failed")).set(0);
        
        scheduler.runAsync(queryStart);
        running.set(true);
    }

    public void stop() {
        scheduler.runAsync(queryStop);
        running.set(false);
    }
    
    public void setRanges() {
        long currentIndex = 0;
        for (Slave slave : slaves) {
            if (slave.getCount().get() == 0) {
                slave.setRangeStart(-1);
                slave.setRangeEnd(-1);
                continue;
            }
            slave.setRangeStart(currentIndex);
            currentIndex += slave.getCount().get();
            slave.setRangeEnd(currentIndex - 1);
        }
    }
    
    public void setCounts() {
        long allCount = storage.getVehicles().stream().mapToLong(x -> x.getCount().get()).sum();
    }
    
    public int getSlavesCount() {
        return (int) slaves.stream().filter(s -> s.getAvailable().get()).count();
    }
    
}
