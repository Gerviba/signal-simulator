package hu.gerviba.simulator.transport;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("MQTT Transporter")
public class MQTTTransporterTest {

    // https://www.ibm.com/developerworks/community/blogs/messaging/entry/write_your_own_mqtt_client_without_using_any_api_in_minutes1?lang=en
    @Test
    @DisplayName("Publishing")
    void testPublish() throws Exception {
        MQTTTransporter transporter = new MQTTTransporter();
        transporter.host = "127.0.0.1";
        transporter.port = 1883;
        transporter.clientId = "id";
        transporter.username = "testu";
        transporter.password = "testp";
        transporter.topic = "test/integ";
        transporter.init();
        
        assertTrue(transporter.sendToCloud(new byte[] {
                0x01, 0x31, 0x52, (byte) 0xC8, (byte) 0x8E, 0x12, 0x00, 0x01, (byte) 0x91
        }));
    }
    
    /**
     * Table 2.4 Size of Remaining Length field
     * http://docs.oasis-open.org/mqtt/mqtt/v3.1.1/csd02/mqtt-v3.1.1-csd02.html#_Toc385349213
     */
    @Test
    @DisplayName("Packet length algorithm")
    void testPacketLengthAlgorithm() throws Exception {
        assertArrayEquals(new byte[] {30}, 
                MQTTTransporter.getPacketLength(30));
        assertArrayEquals(new byte[] {127}, 
                MQTTTransporter.getPacketLength(127));
        assertArrayEquals(new byte[] {(byte) 0x80, 0x01}, 
                MQTTTransporter.getPacketLength(128));
        assertArrayEquals(new byte[] {(byte) 0xFF, 0x7F}, 
                MQTTTransporter.getPacketLength(16383));
        assertArrayEquals(new byte[] {(byte) 0x80, (byte) 0x80, 0x01}, 
                MQTTTransporter.getPacketLength(16384));
        assertArrayEquals(new byte[] {(byte) 0xFF, (byte) 0xFF, 0x7F}, 
                MQTTTransporter.getPacketLength(2097151));
        assertArrayEquals(new byte[] {(byte) 0x80, (byte) 0x80, (byte) 0x80, 0x01}, 
                MQTTTransporter.getPacketLength(2097152));
        assertArrayEquals(new byte[] {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, 0x7F}, 
                MQTTTransporter.getPacketLength(268435455));
    }
    
    @Test
    @DisplayName("Connect - no user, no pass")
    void testConnectPacket() throws Exception {
        MQTTTransporter transporter = new MQTTTransporter();
        transporter.clientId = "clientid";
        transporter.keepAlive = 60;
        transporter.init();
        
        assertArrayEquals(new byte[] {
                // Heaer + length:
                0x10, 0x16,
                // MQIsdp
                0x00, 0x06, 0x4d, 0x51, 0x49, 0x73, 0x64, 0x70,
                // Protocol + Flags
                0x03, 0x02,
                // Keep-alive
                0x00, 0x3c,
                // Clientid
                0x00, 0x8, 0x63, 0x6c, 0x69, 0x65, 0x6e, 0x74, 0x69, 0x64 
        }, transporter.assambleConnectPacket());
    }
    
    @Test
    @DisplayName("Connect - user, no pass")
    void testConnectOnlyUsername() throws Exception {
        MQTTTransporter transporter = new MQTTTransporter();
        transporter.clientId = "clientid";
        transporter.username = "myUserName";
        transporter.keepAlive = 60;
        transporter.init();
        
        assertArrayEquals(new byte[] {
                // Heaer + length:
                0x10, 0x22,
                // MQIsdp
                0x00, 0x06, 0x4d, 0x51, 0x49, 0x73, 0x64, 0x70,
                // Protocol + Flags
                0x03, (byte) 0x82,
                // Keep-alive
                0x00, 0x3c,
                // Clientid
                0x00, 0x8, 0x63, 0x6c, 0x69, 0x65, 0x6e, 0x74, 0x69, 0x64,
                // Username
                0x00, 0x0a, 0x6d, 0x79, 0x55, 0x73, 0x65, 0x72, 0x4e, 0x61, 0x6d, 0x65
        }, transporter.assambleConnectPacket());
    }
    
    @Test
    @DisplayName("Connect - user, pass")
    void testConnectUsernamePassword() throws Exception {
        MQTTTransporter transporter = new MQTTTransporter();
        transporter.clientId = "clientid";
        transporter.username = "myUserName";
        transporter.password = "myPassWd1234";
        transporter.keepAlive = 60;
        transporter.init();
        
        assertArrayEquals(new byte[] {
                // Heaer + length:
                0x10, 0x30,
                // MQIsdp
                0x00, 0x06, 0x4d, 0x51, 0x49, 0x73, 0x64, 0x70,
                // Protocol + Flags
                0x03, (byte) 0xc2,
                // Keep-alive
                0x00, 0x3c,
                // Clientid
                0x00, 0x8, 0x63, 0x6c, 0x69, 0x65, 0x6e, 0x74, 0x69, 0x64,
                // Username
                0x00, 0x0a, 0x6d, 0x79, 0x55, 0x73, 0x65, 0x72, 0x4e, 0x61, 0x6d, 0x65,
                // Password
                0x00, 0x0c, 0x6d, 0x79, 0x50, 0x61, 0x73, 0x73, 0x57, 0x64, 0x31, 0x32, 0x33, 0x34
        }, transporter.assambleConnectPacket());
    }
    
    @Test
    @DisplayName("Connect - long username")
    void testConnectLongNames() throws Exception {
        MQTTTransporter transporter = new MQTTTransporter();
        transporter.clientId = "clientid";
        transporter.username = "012345678901234567890123456789"
                + "0123456789012345678901234567890123456789"
                + "0123456789012345678901234567890123456789"
                + "0123456789012345678901234567890123456789";
        transporter.keepAlive = 60;
        transporter.init();
        
        assertArrayEquals(new byte[] {
                // Heaer + length:
                0x10, (byte) 0xae, 0x01,
                // MQIsdp
                0x00, 0x06, 0x4d, 0x51, 0x49, 0x73, 0x64, 0x70,
                // Protocol + Flags
                0x03, (byte) 0x82,
                // Keep-alive
                0x00, 0x3c,
                // Clientid
                0x00, 0x8, 0x63, 0x6c, 0x69, 0x65, 0x6e, 0x74, 0x69, 0x64,
                // Username
                0x00, (byte) 0x96,
                0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 
                0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 
                0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 
                0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 
                0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 
                0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 
                0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 
                0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 
                0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 
                0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 
                0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 
                0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 
                0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 
                0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 
                0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39,
        }, transporter.assambleConnectPacket());
    }
    
    @Test
    @DisplayName("Publish - simple")
    void testPublishShortData() throws Exception {
        MQTTTransporter transporter = new MQTTTransporter();
        transporter.topic = "test/topic/name";
        transporter.init();
        
        assertArrayEquals(new byte[] {
                // Header + Length
                0x30, 0x1a, 
                // Topic name length
                0x0, 0x0f,
                // Topic name
                0x74, 0x65, 0x73, 0x74, 0x2f, 0x74, 0x6f, 0x70, 0x69, 0x63, 0x2f, 0x6e, 0x61, 0x6d, 0x65,
                // Data
                0x01, 0x31, 0x52, (byte) 0xc8, (byte) 0x8e, 0x12, 0x00, 0x01, (byte) 0x91,    
        }, transporter.assamblePublishPacket(new byte[] {
                0x01, 0x31, 0x52, (byte) 0xC8, (byte) 0x8E, 0x12, 0x00, 0x01, (byte) 0x91
        }));
    }
    
    @Test
    @DisplayName("Publish - long topic name")
    void testPublishLongTopic() throws Exception {
        MQTTTransporter transporter = new MQTTTransporter();
        transporter.topic = "000000000/000000000/000000000/000000000/000000000/"
                          + "000000000/000000000/000000000/000000000/000000000/"
                          + "000000000/000000000/000000000/000000000/000000000/"
                          + "000000000/000000000/000000000/000000000/000000000/"
                          + "000000000/000000000/000000000/000000000/000000000/"
                          + "000000000/";
        transporter.init();
        
        assertArrayEquals(new byte[] {
                // Header + Length (2 byte)
                0x30, (byte) 0x8f, 0x02, 
                // Topic name length (more than 255)
                0x01, 0x04,
                // Topic name
                0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x2f, 
                0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x2f, 
                0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x2f, 
                0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x2f, 
                0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x2f, 
                0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x2f, 
                0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x2f, 
                0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x2f, 
                0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x2f, 
                0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x2f,
                0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x2f,
                0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x2f, 
                0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x2f, 
                0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x2f,
                0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x2f,
                0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x2f, 
                0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x2f, 
                0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x2f,
                0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x2f,
                0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x2f,
                0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x2f, 
                0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x2f,
                0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x2f, 
                0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x2f,
                0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x2f, 
                0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x2f,
                // Data
                0x01, 0x31, 0x52, (byte) 0xc8, (byte) 0x8e, 0x12, 0x00, 0x01, (byte) 0x91,    
        }, transporter.assamblePublishPacket(new byte[] {
                0x01, 0x31, 0x52, (byte) 0xC8, (byte) 0x8E, 0x12, 0x00, 0x01, (byte) 0x91
        }));
    }
    
}
