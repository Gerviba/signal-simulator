package hu.gerviba.simulator.transport;

public class NullTransporter implements Transporter {

    @Override
    public boolean sendToCloud(byte[] data) {
        return true;
    }

}
