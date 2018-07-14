package hu.gerviba.simulator.web;

import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import hu.gerviba.simulator.config.VehicleStorage;
import hu.gerviba.simulator.dao.ServerStatusResponse;
import hu.gerviba.simulator.input.InputSource;
import hu.gerviba.simulator.service.ClusterService;

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
    
    @GetMapping("/server")
    @ResponseBody
    ServerStatusResponse all() {
        ServerStatusResponse stats = new ServerStatusResponse();
        stats.setFailed(((AtomicLong) app.getAttribute("failed")).get());
        stats.setSuccess(((AtomicLong) app.getAttribute("success")).get());
        stats.setRunning(input.isRunning());
        stats.setSlaves(app.getAttribute("mode").equals("STANDALONE") ? 0 : cluster.getSlavesCount());
        stats.setVehicles(storage.getVehicles().stream().mapToLong(v -> v.getCount().get()).sum());
        stats.setMaxMemory(Runtime.getRuntime().totalMemory());
        stats.setFreeMemory(Runtime.getRuntime().freeMemory());
        return stats;
    }
    
}
