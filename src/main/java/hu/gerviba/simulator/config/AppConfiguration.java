package hu.gerviba.simulator.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.web.client.RestTemplate;

import hu.gerviba.simulator.input.InputSource;
import hu.gerviba.simulator.transport.Transporter;

@Configuration
@EnableScheduling
@EnableAsync
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
    
    @Bean
    TaskScheduler taskScheduler() {
        return new ConcurrentTaskScheduler();
    }
    
    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
    
}
