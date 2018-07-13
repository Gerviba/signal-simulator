package hu.gerviba.simulator.config;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

import hu.gerviba.simulator.input.InputSource;
import hu.gerviba.simulator.transport.Transporter;

@Configuration
@EnableScheduling
public class AppConfiguration {

    @Value("${simulator.input-source-class:hu.gerviba.simulator.input.RandomInputGenerator}")
    String inputSourceClassName;

    @Bean
    InputSource inputSource() throws InstantiationException, 
            IllegalAccessException, ClassNotFoundException {
        return (InputSource) Class
                .forName(inputSourceClassName)
                .newInstance();
    }
    
    @Value("${simulator.transporter-class:hu.gerviba.simulator.transport.DebugTransporter}")
    String transporterClassName;

    @Bean
    Transporter transporter() throws InstantiationException, 
            IllegalAccessException, ClassNotFoundException {
        return (Transporter) Class
                .forName(transporterClassName)
                .newInstance();
    }
    
    @Autowired
    ServletContext servletContext;
    
    @Autowired
    Environment environment;
    
    @Bean
    TaskScheduler taskScheduler() {
        return new ConcurrentTaskScheduler();
    }
    
}
