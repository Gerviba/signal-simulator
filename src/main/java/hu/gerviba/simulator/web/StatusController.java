package hu.gerviba.simulator.web;

import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import hu.gerviba.simulator.config.VehicleStorage;
import hu.gerviba.simulator.dao.ServerStatusDao;
import hu.gerviba.simulator.input.InputSource;

@Controller
@RequestMapping("/api/status")
public class StatusController {
    
    @Autowired
    ServletContext app;
    
    @Autowired
    InputSource input;
    
    @Autowired
    VehicleStorage storage;
    
    @GetMapping("/all")
    @ResponseBody
    ServerStatusDao all() {
        ServerStatusDao stats = new ServerStatusDao();
        stats.setFailed(((AtomicLong) app.getAttribute("failed")).get());
        stats.setSuccess(((AtomicLong) app.getAttribute("success")).get());
        stats.setRunning(input.isRunning());
        stats.setSlaves(app.getAttribute("mode").equals("STANDALONE") ? 0 : -1); //TODO: Get the count of slaves
        stats.setVehicles(storage.getVehicles().stream().mapToLong(v -> v.getCount().get()).sum());
        stats.setMaxMemory(Runtime.getRuntime().totalMemory());
        stats.setFreeMemory(Runtime.getRuntime().freeMemory());
        return stats;
    }
    
}
