package hu.gerviba.simulator.config;

import java.util.LinkedList;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;

import hu.gerviba.simulator.model.Slaves;

@Profile("master")
@Configuration
public class MasterConfiguration {
    
    @Autowired
    private ServletContext servletContext;
    
    @Autowired
    Environment env;
    
    @PostConstruct
    public void configure() {
        LinkedList<Slaves> slaves = new LinkedList<>();
        for (String slave : env.getProperty("simulator.slaves").split(";"))
            slaves.add(new Slaves(slave.split("\\|")[0], slave.split("\\|")[1]));
        servletContext.setAttribute("slaves", slaves); //TODO: Talán SycList kellene
    }
    
}
