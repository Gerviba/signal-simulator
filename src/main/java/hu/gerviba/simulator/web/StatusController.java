package hu.gerviba.simulator.web;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import hu.gerviba.simulator.config.VehicleStorage;
import hu.gerviba.simulator.dao.ServerStatusResponse;
import hu.gerviba.simulator.input.InputSource;
import hu.gerviba.simulator.service.ClusterService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile({"master", "standalone", "test"})
@Controller
@RequestMapping("/api/status")
public class StatusController {
    
    @Autowired
    ServletContext app;
    
    @Autowired
    InputSource input;
    
    @Autowired
    VehicleStorage storage;
    
    @Autowired(required = false)
    ClusterService cluster;
    
    @Autowired
    Environment env;
    
    @PostConstruct
    void init() {
        List<String> profile = Arrays.asList(env.getActiveProfiles());
        app.setAttribute("mode", 
                profile.contains("standalone") ? "STANDALONE" : 
                profile.contains("master") ? "MASTER" : 
                profile.contains("slave") ? "SLAVE" : "N/A");
        log.info("Server mode: " + app.getAttribute("mode"));
    }
    
    @GetMapping("/server")
    @ResponseBody
    ServerStatusResponse serverStatus() {
        ServerStatusResponse stats = new ServerStatusResponse();
        stats.setFailed(((AtomicLong) app.getAttribute("failed")).get());
        stats.setSuccess(((AtomicLong) app.getAttribute("success")).get());
        stats.setRunning(input.isRunning());
        stats.setCluster(app.getAttribute("mode").equals("STANDALONE") ? false : cluster.isRunning());
        stats.setSlaves(app.getAttribute("mode").equals("STANDALONE") ? 0 : cluster.getOnlineSlavesCount());
        stats.setVehicles(storage.getVehicles().stream().mapToLong(v -> v.getCount().get()).sum());
        stats.setMaxMemory(Runtime.getRuntime().totalMemory());
        stats.setFreeMemory(Runtime.getRuntime().freeMemory());
        return stats;
    }
    
}
