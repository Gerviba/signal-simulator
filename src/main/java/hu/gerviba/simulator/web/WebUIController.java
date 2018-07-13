package hu.gerviba.simulator.web;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import hu.gerviba.simulator.config.VehicleStorage;
import hu.gerviba.simulator.dao.StatusDao;
import hu.gerviba.simulator.input.InputSource;
import hu.gerviba.simulator.model.VehicleType;

@Profile({"master", "standalone", "test"})
@ConditionalOnProperty("simulator.enable-webui")
@Controller
public class WebUIController {
    
    @Autowired
    ServletContext app;
    
    @Autowired
    VehicleStorage storage;
    
    @Autowired
    Environment env;
    
    @Autowired
    InputSource input;

    @PostConstruct
    void init() {
        List<String> profile = Arrays.asList(env.getActiveProfiles());
        app.setAttribute("mode", 
                profile.contains("standalone") ? "STANDALONE" : 
                profile.contains("master") ? "MASTER" : 
                profile.contains("slave") ? "SLAVE" : "N/A");
    }
    
    @GetMapping("/")
    String index() {
        return "redirect:webui/";
    }

    @GetMapping("/webui")
    String dashboard(Map<String, Object> model) {
        model.put("mode", app.getAttribute("mode"));
        model.put("profiles", String.join(", ", env.getActiveProfiles()));
        model.put("inputsource", env.getProperty("simulator.input-source-class"));
        model.put("configfile", env.getProperty("simulator.config-file"));
        model.put("transporter", env.getProperty("simulator.transporter-class"));
        model.put("allSuccess", readAppContextLong("success"));
        model.put("allFailed", readAppContextLong("failed"));
        return "index";
    }
    
    @GetMapping("/webui/settings")
    String cluster(Map<String, Object> model) {
        model.put("mode", app.getAttribute("mode"));
        model.put("vehicles", storage.getVehicles());
        return "settings";
    }

    @GetMapping("/webui/controls")
    String controls(Map<String, Object> model) {
        model.put("mode", app.getAttribute("mode"));
        model.put("running", input.isRunning());
        model.put("inputSource", input.getClass().getSimpleName());
        return "controls";
    }
    
    @GetMapping("/webui/cluster")
    String cluster() {
        return "index";
    }
    
    @PostMapping("/webui/ranges")
    String setRange(@RequestParam String name, @RequestParam Long count) {
        VehicleType vehicle = storage.getVehicleByName(name);
        if (vehicle == null)
            return "redirect:/webui/settings";
        vehicle.setCount(count);
        storage.setRanges();
        
        if (input.isRunning()) {
            input.stop();
            input.start();
        }
        
        return "redirect:/webui/settings";
    }
    
    @PostMapping("/webui/controls/start")
    String startInputGenerator() {
        input.start();
        return "redirect:/webui/controls";
    }
    
    @PostMapping("/webui/controls/stop")
    String stopInputGenerator() {
        input.stop();
        return "redirect:/webui/controls";
    }
    
    @GetMapping("/webui/controls/status")
    StatusDao statusInputGenerator() {
        return new StatusDao(input.isRunning() ? "ON" : "OFF");
    }
    
    long readAppContextLong(String attributeName) {
        return ((AtomicLong) app.getAttribute(attributeName)).get();
    }
    
}
