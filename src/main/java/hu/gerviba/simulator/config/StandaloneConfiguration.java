package hu.gerviba.simulator.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import hu.gerviba.simulator.input.InputSource;

@Profile("standalone")
@Configuration
public class StandaloneConfiguration {

    @Autowired
    InputSource input;
    
    @PostConstruct
    void init() {
        input.setScope(0, Long.MAX_VALUE);
    }
    
}
