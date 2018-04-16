package kathik.messaging;

import java.io.*;
import java.net.Socket;
import static org.mockito.Mockito.*;

public class MockSocketFactoryImpl implements SocketFactory {

    private final OutputStream mockOutputStream;
    private final InputStream mockInputStream;

    public MockSocketFactoryImpl(InputStream is, OutputStream os) {
        mockInputStream = is;
        mockOutputStream = os;
    }
    
    public boolean init() {
        return true;
    }

    public Socket getSock() {
        final Socket socket = mock(Socket.class);

        try {
            when(socket.getOutputStream()).thenReturn(mockOutputStream);
            when(socket.getInputStream()).thenReturn(mockInputStream);
        } catch (IOException iox) {
            return null; // Won't happen, but fail-fast JIC
        }

        return socket;
    }

}
