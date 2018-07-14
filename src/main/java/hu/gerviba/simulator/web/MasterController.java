package hu.gerviba.simulator.web;

import java.util.List;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import hu.gerviba.simulator.model.Slave;
import hu.gerviba.simulator.service.ClusterService;

@Profile({"master", "test"})
@RestController
public class MasterController {

    @Autowired
    ServletContext app;
    
    @Autowired
    ClusterService cluster;

// TODO Unimplemented due to security reasons
//
//    @PostMapping("/api/slave")
//    StatusResponse register() {
//        return new StatusResponse("REGISTERED");
//    }
//    
//    @DeleteMapping("/api/slave")
//    StatusResponse unregister() {
//        return new StatusResponse("UNREGISTERED");
//    }
//    
    @GetMapping("/api/slaves")
    List<Slave> getSlaves() {
        return cluster.getSlaves();
    }
    
}
