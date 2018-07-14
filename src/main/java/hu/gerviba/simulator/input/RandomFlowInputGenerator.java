package hu.gerviba.simulator.input;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import hu.gerviba.simulator.model.Signal;
import hu.gerviba.simulator.model.VehicleInstance;
import hu.gerviba.simulator.model.VehicleType;
import hu.gerviba.simulator.service.InputProcessorService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RandomFlowInputGenerator extends RandomInputGenerator implements InputSource {

    static final String DELTA_SUFFIX = "_delta";

    static final class RandomDeltaGenerator {
        
        @Getter
        private final Signal signal;
        @Getter
        private final long mask;
        private final Random random;
        final long maxDelta;
        final long minDelta;
        
        public RandomDeltaGenerator(Signal signal, Random random) {
            this.mask = 0x7FFFFFFFFFFFFFFFL >> (63 - signal.getLength());
            this.signal = signal;
            this.random = random;
            this.maxDelta = 
                    signal.getLength() >= 24 ? (long) Math.pow(2, signal.getLength() - 16) :
                    signal.getLength() >= 16 ? 16 :
                    signal.getLength() > 8 ? 8 :
                    signal.getLength() >= 6 ? 4 :
                    signal.getLength() >= 4 ? 2 : 1;
            this.minDelta = -this.maxDelta;
        }
        
        public long next(VehicleInstance vehicle) {
            long lastValue = vehicle.getVariable(signal.getName()).get();
            long newDelta = Math.max(minDelta, Math.min(maxDelta, 
                    vehicle.getVariable(signal.getName() + DELTA_SUFFIX).get() + deltaFunction()));
            
            vehicle.getVariable(signal.getName() + DELTA_SUFFIX).set(newDelta);
            lastValue = Math.max(0, Math.min(mask, lastValue + newDelta));
            vehicle.getVariable(signal.getName()).set(lastValue);
            return lastValue;
        }

        public long deltaFunction() {
            byte temp = (byte) (random.nextInt() & 0xFF);
            return temp > 100 ? 2 : 
                temp > 40 ? 1 : 
                temp >= -40 ? 0 : 
                temp >= -100 ? -1 : -2; 
        }
    }
    
    static final class GeneratorTask implements Runnable {

        private InputProcessorService inputProcessor;
        private List<VehicleInstance> vehicles = Collections.synchronizedList(new LinkedList<>());
        private short frameId;
        private Random random = new Random();
        private List<RandomDeltaGenerator> generators = new LinkedList<>();
        
        GeneratorTask(VehicleType type, short frameId, InputProcessorService inputProcessor, 
                List<VehicleInstance> vehicles) {
            
            this.frameId = frameId;
            this.inputProcessor = inputProcessor;
            this.vehicles.addAll(vehicles);
            type.getFrame(frameId).getSignals()
                    .forEach(sig -> generators.add(new RandomDeltaGenerator(sig, random)));
            initVehicles();
        }
        
        void initVehicles() {
            generators.forEach(gen -> vehicles.forEach(veh -> {
                veh.addVariable(gen.getSignal().getName(), new AtomicLong(random.nextLong() & gen.mask));
                veh.addVariable(gen.getSignal().getName() + DELTA_SUFFIX, new AtomicLong(0));
            }));
        }
        
        @Override
        public void run() {
            vehicles.forEach(vehicle -> {
                InputAssembler ia = vehicle.getAssembler(frameId);
                generators.forEach(gen -> ia.appendSignal(gen.getSignal(), gen.next(vehicle)));
                inputProcessor.applyInput(ia.commit());
            });
        }
        
    }
    
    public RandomFlowInputGenerator() {}
    
    @PostConstruct
    @Override
    public void init() {
        log.info("Input source: RandomFlowInputGenerator");
    }
    
    @Override
    void generateInstances() {
        super.generateInstances();
        vehicles.forEach(veh -> veh.initVariables());
    }
    
    @Override
    void startTasks() {
        tasks = storage.getVehicles().stream()
                .flatMap(x -> x.getFrames().stream())
                .distinct()
                .map(frame -> scheduler.schedule(
                        new GeneratorTask(frame.getVehicleType(), frame.getFrameID(), inputProcessor, 
                                vehicles.stream()
                                .filter(v -> v.getVehicleType().equals(frame.getVehicleType()))
                                .collect(Collectors.toList())), 
                        frame.getPeriod()))
                .collect(Collectors.toList());
    }

}
