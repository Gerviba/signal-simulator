package hu.gerviba.simulator.transport;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousChannelGroup;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;

import lombok.extern.slf4j.Slf4j;

/**
 * Based on: https://examples.javacodegeeks.com/core-java/nio/java-nio-async-http-client-example/
 * @author gerviba
 */
@Slf4j
public class HTTPTransporter implements Transporter {

    public static final String HEADERS_TEMPLATE = 
            "%s /%s HTTP/1.1\r\n" +
            "Connection: keep-alive\r\n" +
            "Upgrade-Insecure-Requests: 1\r\n" +
            "User-Agent: Simulator\r\n" +
            "Accept: application/json;q=0.9\r\n" +
            "Content-Length: %s\r\n" +
            "Host: %s\r\n\r\n";
    
    public static final String HEADERS_TEMPLATE_BASICAUTH = 
            "%s /%s HTTP/1.1\r\n" +
            "Connection: keep-alive\r\n" +
            "Upgrade-Insecure-Requests: 1\r\n" +
            "User-Agent: Simulator\r\n" +
            "Accept: application/json;q=0.9\r\n" +
            "Content-Length: %s\r\n" +
            "Authorization: Basic %s\r\n" +
            "Host: %s\r\n\r\n";

    @Value("${simulator.http.host:127.0.0.1}")
    String host;
    
    @Value("${simulator.http.port:80}")
    int port;
    
    @Value("${simulator.http.method:POST}")
    String method;
    
    @Value("${simulator.http.action:cloud/}")
    String action;
    
    @Value("${simulator.http.basic-enabled:false}")
    boolean basicAuthEnabled;
    
    @Value("${simulator.http.basic-username:}")
    String username;

    @Value("${simulator.http.basic-password:}")
    String password;
    
    private String headers;
    private AsynchronousChannelGroup asyncChannelGroup;
    
    @PostConstruct
    void init() throws IOException {
        log.info("Transporter: HTTPTransporter");
        if (basicAuthEnabled) {
            headers = String.format(HTTPTransporter.HEADERS_TEMPLATE_BASICAUTH, 
                    method, action, "%s", 
                    Base64.getEncoder().encodeToString((username + ":" + password).getBytes()), 
                    host);
        } else {
            headers = String.format(HTTPTransporter.HEADERS_TEMPLATE, method, action, "%s", host);
        }
        
        asyncChannelGroup = AsynchronousChannelGroup.withFixedThreadPool(2, Executors.defaultThreadFactory());
        
        if (action.startsWith("/"))
            action = action.substring(1);
    }
    
    @Override
    public boolean sendToCloud(byte[] rawData) {
        try {
            Optional<ByteBuffer> data = Optional.of(ByteBuffer.wrap(rawData));
            
            AtomicBoolean pass = new AtomicBoolean(true);
            CountDownLatch latch = new CountDownLatch(1);
            
            Consumer<? super ByteBuffer> success = (buffer) -> {
                try {
                    buffer.flip();
    
                    while (buffer.hasRemaining()) {
                        buffer.get();
                    }
                } catch (Exception e) {
                    pass.set(false);
                } finally {
                    latch.countDown();
                }
            }; 
            
            Consumer<? super Exception> failure = (exc) -> {
                exc.printStackTrace();
                pass.set(false);
                latch.countDown();
            };
            
            httpPost(rawData, data, success, failure);
            
            latch.await();
            return pass.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void httpPost(byte[] rawData, Optional<ByteBuffer> data, 
            Consumer<? super ByteBuffer> success,
            Consumer<? super Exception> failure) 
                    throws URISyntaxException, IOException {
        
        SocketAddress serverAddress = new InetSocketAddress(host, port);
        RequestHandler handler = new RequestHandler(
                AsynchronousSocketChannel.open(asyncChannelGroup), 
                success, failure);
        
        connect(handler, serverAddress, 
                ByteBuffer.wrap(String.format(headers, rawData.length).getBytes()), data);
    }
    
    private void connect(RequestHandler handler, SocketAddress address,
            ByteBuffer headers, Optional<ByteBuffer> body) {

        handler.getChannel().connect(address, null, new CompletionHandler<Void, Void>() {

            @Override
            public void completed(final Void result, final Void attachment) {
                handler.headers(headers, body);
            }

            @Override
            public void failed(final Throwable exc, final Void attachment) {
                handler.getFailure().accept(new Exception(exc));
            }
        });
    }
    
}

/**
 * @author https://examples.javacodegeeks.com/core-java/nio/java-nio-async-http-client-example/
 */
final class RequestHandler {

    private final AsynchronousSocketChannel channel;
    private final Consumer<? super ByteBuffer> success;
    private final Consumer<? super Exception> failure;

    RequestHandler(AsynchronousSocketChannel channel, 
            Consumer<? super ByteBuffer> success, 
            Consumer<? super Exception> failure) {
        
        this.channel = channel;
        this.success = success;
        this.failure = failure;
    }

    AsynchronousSocketChannel getChannel() {
        return this.channel;
    }

    Consumer<? super ByteBuffer> getSuccess() {
        return this.success;
    }

    Consumer<? super Exception> getFailure() {
        return this.failure;
    }

    void closeChannel() {
        try {
            this.channel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void headers(ByteBuffer headers, Optional<ByteBuffer> body) {
        this.channel.write(headers, this, new CompletionHandler<Integer, RequestHandler>() {
            
            @Override
            public void completed(final Integer result, final RequestHandler handler) {
                if (headers.hasRemaining())
                    RequestHandler.this.channel.write(headers, handler, this);
                else if (body.isPresent())
                    RequestHandler.this.body(body.get(), handler);
                else
                    RequestHandler.this.response();
            }

            @Override
            public void failed(final Throwable exc, final RequestHandler handler) {
                handler.getFailure().accept(new Exception(exc));
                RequestHandler.this.closeChannel();
            }
        });
    }

    void body(ByteBuffer body, RequestHandler handler) {
        this.channel.write(body, handler, new CompletionHandler<Integer, RequestHandler>() {

            @Override
            public void completed(final Integer result, final RequestHandler handler) {
                if (body.hasRemaining()) {
                    RequestHandler.this.channel.write(body, handler, this);
                } else {
                    RequestHandler.this.response();
                }
            }

            @Override
            public void failed(final Throwable exc, final RequestHandler handler) {
                handler.getFailure().accept(new Exception(exc));
                RequestHandler.this.closeChannel();
            }
        });
    }

    void response() {
        ByteBuffer buffer = ByteBuffer.allocate(2048);
        this.channel.read(buffer, this, new CompletionHandler<Integer, RequestHandler>() {

            @Override
            public void completed(Integer result, RequestHandler handler) {
                if (result > 0) {
                    handler.getSuccess().accept(buffer);
                    buffer.clear();

                    RequestHandler.this.channel.read(buffer, handler, this);
                } else if (result < 0) {
                    RequestHandler.this.closeChannel();
                } else {
                    RequestHandler.this.channel.read(buffer, handler, this);
                }
            }

            @Override
            public void failed(Throwable exc, RequestHandler handler) {
                handler.getFailure().accept(new Exception(exc));
                RequestHandler.this.closeChannel();
            }
        });
    }
}
