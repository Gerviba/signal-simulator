package hu.gerviba.simulator.web;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RestController;

@Profile({"master", "test"})
@RestController
public class SlaveCommandsController {

    
    
}
