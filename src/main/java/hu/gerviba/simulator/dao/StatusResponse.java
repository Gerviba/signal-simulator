package hu.gerviba.simulator.dao;

import lombok.Getter;
import lombok.Setter;

public class StatusResponse {

    @Getter
    @Setter
    private String status;

    public StatusResponse() {}
    
    public StatusResponse(String status) {
        this.status = status;
    }
    
}
