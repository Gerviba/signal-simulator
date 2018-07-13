package hu.gerviba.simulator.transport;

import java.util.LinkedList;
import java.util.Queue;

import lombok.Getter;

public class MockTransporter implements Transporter {

    public class MockTransaction {
        @Getter
        private final long timestamp;
        @Getter
        private final byte[] data;
        
        public MockTransaction(long timestamp, byte[] data) {
            this.timestamp = timestamp;
            this.data = data;
        }
    }
    
    private long timeOffset = 0;
    private Queue<MockTransaction> transactions = new LinkedList<>();
    
    public boolean sendToCloud(byte[] data) {
        transactions.add(new MockTransaction(System.currentTimeMillis() + timeOffset, data));
        return true;
    }

    public MockTransaction readNext() {
        return transactions.poll();
    }
    
    public void waitVirtualTime(long time) {
        timeOffset += time;
    }
    
}
