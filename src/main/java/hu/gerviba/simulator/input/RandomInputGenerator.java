package hu.gerviba.simulator.input;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;

import hu.gerviba.simulator.config.VehicleStorage;
import hu.gerviba.simulator.model.Signal;
import hu.gerviba.simulator.model.VehicleInstance;
import hu.gerviba.simulator.model.VehicleType;
import hu.gerviba.simulator.service.InputProcessorService;
import hu.gerviba.simulator.service.SchedulerService;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RandomInputGenerator implements InputSource {

    static final class RandomSignalGenerator {
        
        @Getter
        private final Signal signal;
        private final Random random;
        private final long mask;
        
        public RandomSignalGenerator(Signal signal, Random random) {
            this.mask = 0x7FFFFFFFFFFFFFFFL >> (63 - signal.getLength());
            this.signal = signal;
            this.random = random;
        }
        
        public long next() {
            return random.nextLong() & mask;
        }

    }
    
    //TODO: Test
    static final class GeneratorTask implements Runnable {

        private InputProcessorService inputProcessor;
        private List<VehicleInstance> vehicles = Collections.synchronizedList(new LinkedList<>());
        private short frameId;
        private Random random = new Random();
        private List<RandomSignalGenerator> generators = new LinkedList<>();
        
        GeneratorTask(VehicleType type, short frameId, InputProcessorService inputProcessor, 
                List<VehicleInstance> vehicles) {
            
            this.frameId = frameId;
            this.inputProcessor = inputProcessor;
            this.vehicles.addAll(vehicles);
            type.getFrame(frameId).getSignals()
                    .forEach(sig -> generators.add(new RandomSignalGenerator(sig, random)));
        }
        
        @Override
        public void run() {
            vehicles.forEach(vehicle -> {
                InputAssembler ia = vehicle.getAssembler(frameId);
                generators.forEach(gen -> ia.appendSignal(gen.getSignal(), gen.next()));
                inputProcessor.applyInput(ia.commit());
            });
        }
        
    }
    
    @Autowired
    SchedulerService scheduler;
    
    @Autowired
    VehicleStorage storage;
    
    @Autowired
    InputProcessorService inputProcessor;
    
    @Getter(AccessLevel.PROTECTED)
    protected List<VehicleInstance> vehicles = Collections.synchronizedList(new LinkedList<>());
    protected List<ScheduledFuture<?>> tasks = null;
    protected AtomicBoolean running = new AtomicBoolean(false);
    private long fromId = -1;
    private long toId = -1;
    
    public RandomInputGenerator() {}

    @PostConstruct
    @Override
    public void init() {
        log.info("Input source: RandomFlowInputGenerator");
    }

    @PreDestroy
    void destroy() {
        stop();
    }
    
    @Override
    public void setScope(long from, long to) {
        this.fromId = from;
        this.toId = to;
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public void start() {
        running.set(true);
        generateInstances();
        startTasks();
    }
    
    void generateInstances() {
        vehicles.clear();
        long currentId = fromId;
        int vehcileTypeId = 0;
        while (currentId <= toId) {
            if (storage.getVehicles().size() == vehcileTypeId)
                return;
            if (storage.getVehicles().get(vehcileTypeId).getIdFrom().get() > currentId) {
                ++vehcileTypeId;
                continue;
            }
            if (storage.getVehicles().get(vehcileTypeId).getIdTo().get() < currentId) {
                ++vehcileTypeId;
                continue;
            }
            vehicles.add(new VehicleInstance(
                    InputAssembler.generateVehicleIdBytes(currentId), 
                    storage.getVehicles().get(vehcileTypeId)));
            ++currentId;
        }
    }

    void startTasks() {
        tasks = storage.getVehicles().stream()
                .flatMap(x -> x.getFrames().stream())
                .distinct()
                .map(frame -> scheduler.schedule(
                        new GeneratorTask(frame.getVehicleType(), frame.getFrameID(), inputProcessor, vehicles.stream()
                                .filter(v -> v.getVehicleType().equals(frame.getVehicleType()))
                                .collect(Collectors.toList())), 
                        frame.getPeriod()))
                .collect(Collectors.toList());
    }

    @Override
    public void stop() {
        running.set(false);
        vehicles.clear();
        if (tasks == null)
            return;
        for (ScheduledFuture<?> task : tasks)
            if (task != null && !task.isDone())
                task.cancel(false);
    }

}
