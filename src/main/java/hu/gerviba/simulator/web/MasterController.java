package hu.gerviba.simulator.web;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import hu.gerviba.simulator.dao.StatusDao;

@Profile({"master", "test"})
@RestController
public class MasterController {

    @Autowired
    private ServletContext servletContext;
    
    @PostMapping("/api/slave")
    StatusDao register() {
        return new StatusDao("REGISTERED");
    }
    
    @DeleteMapping("/api/slave")
    StatusDao unregister() {
        return new StatusDao("UNREGISTERED");
    }
    
}
