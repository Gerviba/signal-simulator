package hu.gerviba.simulator.transport;

import java.io.PrintStream;

public class DebugTransporter implements Transporter {

    private PrintStream ps;
    
    public DebugTransporter() {
        this.ps = System.out;
    }
    
    public DebugTransporter(PrintStream ps) {
        this.ps = ps;
    }

    public boolean sendToCloud(byte[] data) {
        printByteArray(ps, data);
        return true;
    }

    public static void printByteArray(PrintStream ps, byte[] data) {
        ps.println("   | 76543210 | DEC\n"
                + "---|----------|-----");
        for (int i = 0; i < data.length; ++i)
            ps.printf("%02d | %s | %4d\n", i,
                    String.format("%8s", Integer.toBinaryString(data[i] & 0xFF))
                            .replace(' ', '0'),
                    data[i]);
        ps.println("END");
    }

}
