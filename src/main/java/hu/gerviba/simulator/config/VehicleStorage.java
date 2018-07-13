package hu.gerviba.simulator.config;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonStructure;
import javax.json.JsonValue.ValueType;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import hu.gerviba.simulator.model.Frame;
import hu.gerviba.simulator.model.Signal;
import hu.gerviba.simulator.model.VehicleType;
import lombok.Getter;
import lombok.ToString;

@Configuration
@ToString
public class VehicleStorage {

    @Getter
    private List<VehicleType> vehicles = new ArrayList<>(1);
    
    @Value("${simulator.config-file:config-example.json}")
    String defaultConfigFile;
    
    public VehicleStorage() {}
    
    @PostConstruct
    public void loadFromFile() throws FileNotFoundException {
        loadFromFile(System.getProperty("configFile", defaultConfigFile));
    }
    
    public void loadFromFile(String path) throws FileNotFoundException {
        JsonReader reader = Json.createReader(new FileReader(path));
        JsonStructure json = reader.read();
        
        if (json.getValueType() == ValueType.ARRAY)
            json.asJsonArray().forEach(x -> loadVehicle(x.asJsonObject()));
        else if (json.getValueType() == ValueType.OBJECT)
            loadVehicle(json.asJsonObject());
        else
            throw new RuntimeException("Invalid JSON file.");
    }
    
    public void setRanges() {
        long currentIndex = 0;
        for (VehicleType vt : vehicles) {
            if (vt.getCount().get() == 0) {
                vt.setIdFrom(-1);
                vt.setIdTo(-1);
                continue;
            }
            vt.setIdFrom(currentIndex);
            currentIndex += vt.getCount().get();
            vt.setIdTo(currentIndex - 1);
        }
    }
    
    void loadVehicle(JsonObject object) {
        VehicleType vehicle = new VehicleType();
        vehicle.setVehicleType(object.getString("vehicleType"));
        List<Frame> frames = new ArrayList<>();
        object.getJsonArray("frames").forEach(x -> frames.add(loadFrame(x.asJsonObject())));
        frames.forEach(frame -> frame.setVehicleType(vehicle));
        vehicle.setFrames(frames);
        vehicles.add(vehicle);
    }

    Frame loadFrame(JsonObject object) {
        Frame frame = new Frame();
        frame.setName(object.getString("name"));
        frame.setFrameID((short) object.getJsonNumber("frameID").intValue());
        frame.setPeriod(object.getJsonNumber("period").intValue());
        List<Signal> signals = new ArrayList<>();
        object.getJsonArray("signals").forEach(x -> signals.add(loadSignal(x.asJsonObject())));
        frame.setSignals(signals);
        return frame;
    }

    Signal loadSignal(JsonObject object) {
        Signal signal = new Signal();
        signal.setName(object.getString("name"));
        signal.setPosition(object.getJsonNumber("position").intValue());
        signal.setLength(object.getJsonNumber("length").intValue());
        return signal;
    }

    public VehicleType getVehicleByName(String name) {
        return vehicles.stream().filter(x -> x.getVehicleType().equals(name)).findFirst().orElse(null);
    }
    
}
