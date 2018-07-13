package hu.gerviba.simulator.dao;

import lombok.Getter;
import lombok.Setter;

public class StatusDao {

    @Getter
    @Setter
    private String status;

    public StatusDao() {}
    
    public StatusDao(String status) {
        this.status = status;
    }
    
}
