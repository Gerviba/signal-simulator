package hu.gerviba.simulator.model;

import java.util.Comparator;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class Signal {

    public static final Comparator<? super Signal> SORT_COMPARATOR 
            = (a, b) -> Integer.compare(a.getPosition(), b.getPosition());

    @Getter
    @Setter
    private String name;
    
    @Getter
    @Setter
    private int position;
    
    @Getter
    @Setter
    private int length;

    public Signal() {}

    public Signal(String name, int position, int length) {
        this.name = name;
        this.position = position;
        this.length = length;
    }
    
}
