package hu.gerviba.simulator.transport;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;

/**
 * MQTT Transporter
 * @docs http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/csd02/mqtt-v3.1.1-csd02.html
 * @author gerviba
 */
public class MQTTTransporter implements Transporter {

    private static final byte PACKET_TYPE_CONNECT = 0x10;
    private static final byte PACKET_TYPE_PUBLISH = 0x30;
    private static final byte PACKET_TYPE_DISCONNECT = (byte) 0xE0;

    private static final int FLAG_PASSWORD = 0x40;
    private static final int FLAG_USERNAME = 0x80;
    private static final int FLAG_CLEAN_SESSION = 0x02;

    private static final byte[] DISCONNECT_PACKET = {PACKET_TYPE_DISCONNECT, 0x00};

    @Value("${simulator.mqtt.host:127.0.0.1}")
    String host;

    @Value("${simulator.mqtt.port:1883}")
    int port = 1883;

    @Value("${simulator.mqtt.id:unset-ud}")
    String clientId;

    @Value("${simulator.mqtt.username:}")
    String username;

    @Value("${simulator.mqtt.password:}")
    String password;

    @Value("${simulator.mqtt.topic:default}")
    String topic;

    @Value("${simulator.mqtt.keep-alive:60}")
    int keepAlive;
    
    private byte[] keepAliveBytes = new byte[2];
    
    @PostConstruct
    void init() {
        keepAliveBytes[0] = getShortMSB(keepAlive);
        keepAliveBytes[1] = getShortLSB(keepAlive);
    }
    
    byte[] assambleConnectPacket() {
        int length = 12;
        byte flags = FLAG_CLEAN_SESSION;
        
        length += 2 + clientId.length();
        if (username != null && username.length() > 0) {
            length += 2 + username.length();
            flags |= FLAG_USERNAME;
            if (password != null && password.length() > 0) {
                length += 2 + password.length();
                flags |= FLAG_PASSWORD;
            }
        }

        byte[] lengthBytes = getPacketLength(length);
        byte[] result = new byte[1 + lengthBytes.length + length];
        int index = 0;
        result[index++] = PACKET_TYPE_CONNECT;
        index = append(result, lengthBytes, index);
        index = append(result, new byte[] {
                    0x00, 0x06,   // Protocol name length: 6
                    0x4d, 0x51, 0x49, 0x73, 0x64, 0x70, // MQIsdp
                    0x03,         // Protocol version: 3
                    flags,        // 0x02: Clean session | 0x80: Password | 0x40: Username 
                    keepAliveBytes[0], 
                    keepAliveBytes[1],
                }, index);
        
        index = appendVariableLength(result, clientId.getBytes(), index);
        if ((flags & FLAG_USERNAME) != 0)
            index = appendVariableLength(result, username.getBytes(), index);
        if ((flags & FLAG_PASSWORD) != 0)
            index = appendVariableLength(result, password.getBytes(), index);
        
        return result; 
    }
    
    byte[] assamblePublishPacket(byte[] data) {
        int length = 0;
        length += 2 + topic.length();
        length += data.length;

        byte[] lengthBytes = getPacketLength(length);
        byte[] result = new byte[1 + lengthBytes.length + length];
        int index = 0;
        result[index++] = PACKET_TYPE_PUBLISH;
        index = append(result, lengthBytes, index);
        
        index = appendVariableLength(result, topic.getBytes(), index);
        append(result, data, index);
        return result;
    }
    
    @Override
    public boolean sendToCloud(byte[] rawData) {
        InetSocketAddress addr = new InetSocketAddress(host, port);
        
        try (SocketChannel client = SocketChannel.open(addr)) {
            client.write(ByteBuffer.wrap(assambleConnectPacket()));

            ByteBuffer buffer = ByteBuffer.allocate(4);
            int length = client.read(buffer);
            if (length == -1)
                return false;

//            System.out.println(Arrays.toString(buffer.array()));
            if (buffer.array()[0] == 0x20 && buffer.array()[2] == 0x00) {
                if (!validate(buffer.array()[3]))
                    return false;
                
                client.write(ByteBuffer.wrap(assamblePublishPacket(rawData)));
                client.write(ByteBuffer.wrap(DISCONNECT_PACKET));
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean validate(byte connack) {
        switch (connack) {
            case 0x00: return true;
            case 0x01: System.err.println("Unsupported protocol version"); return false;
            case 0x02: System.err.println("ID rejected"); return false;
            case 0x03: System.err.println("MQTT not available"); return false;
            case 0x04: System.err.println("Invalid username or password"); return false;
            case 0x05: System.err.println("Unauhorized"); return false;
            default: System.err.println("Unknown error"); return false;
        }
    }
    
    static byte getShortMSB(int number) {
        return (byte) ((number >> 8) & 0xFF);
    }
    
    static byte getShortLSB(int number) {
        return (byte) (number & 0xFF);
    }
    
    static int appendVariableLength(byte[] result, byte[] data, int index) {
        result[index++] = getShortMSB(data.length);
        result[index++] = getShortLSB(data.length);
        System.arraycopy(data, 0, result, index, data.length);
        return index + data.length;
    }
    
    static int append(byte[] result, byte[] data, int index) {
        System.arraycopy(data, 0, result, index, data.length);
        return index + data.length;
    }
    
    /**
     * Based on:
     * http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/csd02/mqtt-v3.1.1-csd02.html#_Toc385349213
     * @param length > 0
     */
    static byte[] getPacketLength(int length) {
        byte[] result = new byte[
                length < 128 ? 1 :
                length < 16384 ? 2 : 
                length < 2097152 ? 3 : 4];
        int i = 0;
        
        do {
            int encodedByte = length % 128;
            length = length / 128;
            if (length > 0)
                encodedByte = encodedByte | 128;

            result[i++] = (byte) encodedByte;
        } while (length > 0);
        return result;
    }

}

