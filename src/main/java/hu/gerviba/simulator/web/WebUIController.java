package hu.gerviba.simulator.web;

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
import hu.gerviba.simulator.input.InputSource;
import hu.gerviba.simulator.model.VehicleType;
import hu.gerviba.simulator.service.ClusterService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@Profile({"master", "standalone", "test"})
@ConditionalOnProperty("simulator.enable-webui")
public class WebUIController {
    
    @Autowired
    ServletContext app;
    
    @Autowired
    VehicleStorage storage;
    
    @Autowired
    Environment env;
    
    @Autowired
    InputSource input;
    
    @Autowired(required = false)
    ClusterService cluster;

    @PostConstruct
    void init() {
        log.info("WebUI: enabled");
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
    String settings(Map<String, Object> model) {
        model.put("mode", app.getAttribute("mode"));
        model.put("vehicles", storage.getVehicles());
        return "settings";
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
    
    @GetMapping("/webui/controls")
    String controls(Map<String, Object> model) {
        if (app.getAttribute("mode").equals("MASTER"))
            return "redirect:/webui/";
        model.put("mode", app.getAttribute("mode"));
        model.put("running", input.isRunning());
        model.put("inputSource", input.getClass().getSimpleName());
        model.put("zeroVehicle", storage.getVehicles().stream()
                .mapToLong(v -> v.getCount().get()).sum() == 0);
        return "controls";
    }
    

    @PostMapping("/webui/controls/start")
    String startInputGenerator() {
        if (app.getAttribute("mode").equals("MASTER"))
            return "redirect:/webui/";
        log.info("Starting input generator");
        input.start();
        return "redirect:/webui/controls";
    }

    @PostMapping("/webui/controls/stop")
    String stopInputGenerator() {
        if (app.getAttribute("mode").equals("MASTER"))
            return "redirect:/webui/";
        log.info("Stopping input generator");
        input.stop();
        return "redirect:/webui/controls";
    }
    
    @GetMapping("/webui/cluster")
    String cluster(Map<String, Object> model) {
        if (app.getAttribute("mode").equals("STANDALONE"))
            return "redirect:/webui/";
        model.put("mode", app.getAttribute("mode"));
        model.put("clusterRunning", cluster.isRunning());
        model.put("zeroVehicle", storage.getVehicles().stream()
                .mapToLong(v -> v.getCount().get()).sum() == 0);
        return "cluster";
    }

    @PostMapping("/webui/cluster/start")
    String startCluster() {
        if (app.getAttribute("mode").equals("STANDALONE"))
            return "redirect:/webui/";
        cluster.start();
        return "redirect:/webui/cluster";
    }

    @PostMapping("/webui/cluster/stop")
    String stopCluster() {
        if (app.getAttribute("mode").equals("STANDALONE"))
            return "redirect:/webui/";
        cluster.stop();
        return "redirect:/webui/cluster";
    }
    
    long readAppContextLong(String attributeName) {
        return ((AtomicLong) app.getAttribute(attributeName)).get();
    }
    
}
