package hu.gerviba.simulator.service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile({"master", "test"})
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
    private AtomicBoolean running = new AtomicBoolean(false);
    
    @Async
    @Scheduled(initialDelay = 5000, 
            fixedRateString = "${simulator.slave-update-period:1000}")
    public void updateSlaves() {
        AtomicBoolean changed = new AtomicBoolean(false);
        AtomicLong success = new AtomicLong(0); // They must be final
        AtomicLong failed = new AtomicLong(0);
        slaves.forEach(slave -> {
            try {
                SlaveStatusResponse response = rest.getForObject(
                        slave.getHost() + "/api/slave/status?apiKey={apiKey}",
                        SlaveStatusResponse.class, slave.getKey());
                
                if (slave.changeAvailable(true))
                    changed.set(true);
                slave.setRunning(response.isRunning());
                slave.setSuccess(response.getSuccess());
                slave.setFailed(response.getFailed());
                success.addAndGet(response.getSuccess());
                failed.addAndGet(response.getFailed());
            } catch (RestClientException e) {
                if (slave.changeAvailable(false))
                    changed.set(true);
                slave.setRunning(false);
            }
        });
        
        ((AtomicLong) app.getAttribute("success")).set(success.get());
        ((AtomicLong) app.getAttribute("failed")).set(failed.get());
        
        if (changed.get() && running.get())
            scheduler.runAsync(queryStart);
    }
    
    Runnable queryStart = () -> {
        AtomicBoolean changed = new AtomicBoolean(false);
        do {
            log.info("Sending START packets...");
            changed.set(false);
            distributeSlaves();
            slaves.stream()
                .filter(slave -> slave.getAvailable().get())
                .forEach(slave -> {
                    try {
                        StatusResponse response = rest.postForObject(
                                slave.getHost() + "/api/slave/start?apiKey={apiKey}",
                                new MasterSlaveCommand(
                                        slave.getRangeStart().get(),
                                        slave.getRangeEnd().get(),
                                        storage.getVehicles()),
                                StatusResponse.class, slave.getKey());
                        slave.changeAvailable(true);
                        slave.setRunning(response.getStatus().equals("OK"));
                    } catch (RestClientException e) {
                        if (slave.changeAvailable(false))
                            changed.set(true);
                        slave.setRunning(false);
                    }
                });
        } while (changed.get());
    };
    
    Runnable queryStop = () -> slaves.forEach(slave -> {
        log.info("Sending STOP packets...");
        try {
            StatusResponse response = rest.postForObject(
                    slave.getHost() + "/api/slave/stop?apiKey={apiKey}",
                    new HttpEntity<String>(""),
                    StatusResponse.class, slave.getKey());
            slave.changeAvailable(true);
            slave.setRunning(!response.getStatus().equals("OK"));
        } catch (RestClientException e) {
            slave.changeAvailable(false);
            slave.setRunning(false);
        }
    });
    
    Runnable queryForceStart = () -> {
        
    };
    
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

    @PreDestroy
    public void stop() {
        running.set(false);
        scheduler.runAsync(queryStop);
    }
    
    public int getOnlineSlavesCount() {
        return (int) slaves.stream().filter(s -> s.getAvailable().get()).count();
    }
    
    void distributeSlaves() {
        slaves.forEach(slave -> { 
            slave.setCount(0); 
            slave.setRangeStart(-1); 
            slave.setRangeEnd(-1); 
        }); 
        List<Slave> runnigSlaves = slaves.stream()
                .filter(x -> x.getAvailable().get())
                .collect(Collectors.toList());
        long idCount = storage.getVehicles().stream().mapToLong(x -> x.getCount().get()).sum();
        int slaveCount = runnigSlaves.size();
        long perSlave = (long) Math.ceil((double) idCount / (double) slaveCount);
        runnigSlaves.forEach(slave -> slave.setCount(perSlave));
        
        for (int i = 0; i < (slaveCount * perSlave) - idCount; ++i)
            runnigSlaves.get(i).getCount().decrementAndGet();
        
        setRanges(runnigSlaves);
    }
    
    private void setRanges(List<Slave> slaves) {
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
    
}
