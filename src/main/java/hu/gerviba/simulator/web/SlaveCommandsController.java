package hu.gerviba.simulator.web;

import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hu.gerviba.simulator.config.VehicleStorage;
import hu.gerviba.simulator.dao.MasterSlaveCommand;
import hu.gerviba.simulator.dao.SlaveStatusResponse;
import hu.gerviba.simulator.dao.StatusResponse;
import hu.gerviba.simulator.input.InputSource;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Profile({"slave", "test"})
@RestController
@RequestMapping("/api/slave")
public class SlaveCommandsController {

    @Autowired
    ServletContext app;
    
    @Autowired
    InputSource input;
    
    @Autowired
    VehicleStorage storage;
    
    @Value("${simulator.api-key:HfnB8694aWKzD9xNbb58zgHb}")
    String validApiKey;
    
    @GetMapping("/status")
    SlaveStatusResponse status(String apiKey) {
        if (!validApiKey.equals(apiKey)){
            log.info("Invalid API key (status query)");
            return new SlaveStatusResponse(false, "INVALID-API-KEY", 0, 0);
        }
        
        return new SlaveStatusResponse(input.isRunning(), "OK", 
                readAppContextLong("success"),
                readAppContextLong("failed"));
    }
    
    @PostMapping("/start")
    StatusResponse start(String apiKey, @RequestBody MasterSlaveCommand command) {
        log.info("Received START packet");
        if (!validApiKey.equals(apiKey)) {
            log.info("Invalid API key");
            return new StatusResponse("INVALID-API-KEY");
        }
        log.info(command.toString());
            
        if (input.isRunning())
            input.stop();
        ((AtomicLong) app.getAttribute("success")).set(0);
        ((AtomicLong) app.getAttribute("failed")).set(0);
        
        input.setScope(command.getIdFrom(), command.getIdTo());
        command.getVehicleCounts().forEach((k, v) -> 
            storage.getVehicleByName(k).setCount(v));
        storage.setRanges();

        if (command.getIdFrom() >= 0)
            input.start();
        return new StatusResponse("OK");
    }
    
    @PostMapping("/stop")
    StatusResponse stop(String apiKey) {
        log.info("Received STOP packet");
        if (!validApiKey.equals(apiKey)) {
            log.info("Invalid API key");
            return new StatusResponse("INVALID-API-KEY");
        }
        
        input.stop();
        return new StatusResponse("OK");
    }
    
    long readAppContextLong(String attributeName) {
        return ((AtomicLong) app.getAttribute(attributeName)).get();
    }
    
}
