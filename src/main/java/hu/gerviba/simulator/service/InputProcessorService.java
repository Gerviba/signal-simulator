package hu.gerviba.simulator.service;

import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hu.gerviba.simulator.transport.Transporter;

@Service
public class InputProcessorService {

    @Autowired
    Transporter transporter;
    
    @Autowired
    ServletContext app;
    
    @PostConstruct
    public void init() {
        app.setAttribute("success", new AtomicLong());
        app.setAttribute("failed", new AtomicLong());
    }
    
    public void applyInput(byte[] data) {
        System.out.println("Send");
        if (transporter.sendToCloud(data)) {
            ((AtomicLong) app.getAttribute("success")).incrementAndGet();
        } else {
            ((AtomicLong) app.getAttribute("failed")).incrementAndGet();
        }
    }
    
}
