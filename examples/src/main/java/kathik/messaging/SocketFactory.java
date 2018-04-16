package kathik.messaging;

import java.net.Socket;

/**
 *
 * @author ben
 */
public interface SocketFactory {

    boolean init();

    Socket getSock();
}
