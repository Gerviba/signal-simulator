package hu.gerviba.simulator.assets;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

@SuppressWarnings("serial")
public class DeterministicRandom extends Random {

    public Queue<Number> nextValues = new LinkedList<>();
    
    public DeterministicRandom(Collection<Number> value) {
        super();
        nextValues.addAll(value);
    }
    
    @Override
    public long nextLong() {
        return nextValues.poll().longValue();
    }    
    
    @Override
    public int nextInt() {
        return nextValues.poll().intValue();
    }
    
}
