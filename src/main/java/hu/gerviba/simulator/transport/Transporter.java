package hu.gerviba.simulator.transport;

public interface Transporter {

    public boolean sendToCloud(byte[] data);
//    public boolean sendMoreToCloud(List<byte[]> data);
    
}
