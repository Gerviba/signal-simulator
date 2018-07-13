package hu.gerviba.simulator.transport;

import java.util.List;

public interface Transporter {

    public boolean sendToCloud(byte[] data);
//    public boolean sendToCloud(List<byte[]> data);
    
}
