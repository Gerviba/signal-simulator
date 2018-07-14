package hu.gerviba.simulator.web;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import hu.gerviba.simulator.dao.StatusResponse;
import hu.gerviba.simulator.input.InputSource;
import hu.gerviba.simulator.service.ClusterService;

@RequestMapping("/api")
@RestController
public class ConfigurationController {

    @Autowired
    Environment env;
    
    @Autowired
    ServletContext app;
    
    @Autowired
    InputSource input;
    
    @Autowired(required = false)
    ClusterService cluster;
    
    @PostMapping("/inputgenerator/start")
    StatusResponse startInputGenerator() {
        if (app.getAttribute("mode").equals("MASTER"))
            return new StatusResponse("UNSUPPORTED-OPERATION");
        input.start();
        return new StatusResponse("OK");
    }

    @PostMapping("/inputgenerator/stop")
    StatusResponse stopInputGenerator() {
        if (app.getAttribute("mode").equals("MASTER"))
            return new StatusResponse("UNSUPPORTED-OPERATION");
        input.stop();
        return new StatusResponse("OK");
    }

    @PostMapping("/cluster/start")
    StatusResponse startCluster() {
        if (app.getAttribute("mode").equals("STANDALONE"))
            return new StatusResponse("UNSUPPORTED-OPERATION");
        cluster.start();
        return new StatusResponse("OK");
    }

    @PostMapping("/cluster/stop")
    StatusResponse stopCluster() {
        if (app.getAttribute("mode").equals("STANDALONE"))
            return new StatusResponse("UNSUPPORTED-OPERATION");
        cluster.stop();
        return new StatusResponse("OK");
    }
    
}
