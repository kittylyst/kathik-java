package kathik.messaging;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author ben
 */
public class MessageReceiver implements Runnable {

    private Socket sock;
    private volatile boolean shutdown = false;
    private MessageParser parser;
    private final AtomicLong seqNo = new AtomicLong(System.nanoTime() >> 32);
    
    @Override
    public void run() {
        try (final InputStream from = sock.getInputStream();
                final PrintWriter to = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));) {

            MAINLOOP:
            while (!shutdown) {
                final String xmlStr = readMessage(from);
                if ("".equals(xmlStr)) {
                    // Socket close from the other side, so this
                    // task is dead. Punt back to manager for reopen / retry.
                    return;
                }

                // Parse it & check it's valid XML
                final Optional<Message> obj = parser.parse(xmlStr);
                if (!obj.isPresent()) {
                    // We have failed to parse the packet so send a Nak
                    final String nakStr = makeNakStr();
                    to.print(nakStr);
                    to.flush();
                } else {
                    final Message newMsg = obj.get();
                    String ackStr;
                    if (newMsg instanceof Transaction) {
                        final Transaction txn = (Transaction) newMsg;
                        ackStr = makeAckStr();
                        // Do stuff with txn...
                    } else if (newMsg instanceof Heartbeat) {
                        ackStr = makeAckStr();
                    } else {
                        // Don't ack unknown messages
                        continue MAINLOOP;
                    }
                    // Now ack the packet
                    to.print(ackStr);
                    to.flush();
                }
            }
        } catch (IOException ex) {
        }
    }

    /**
     * Performs a blocking read of all bytes available on @from and returns the
     * result as a String, with no padding or other manipulation.
     * @param from
     * @return
     * @throws IOException 
     */
    String readMessage(final InputStream from) throws IOException {
        final byte[] buf = new byte[4096];
        int len = 0;
        if ((len = from.read(buf)) > 0) {
//            lastPacketSeen = LocalDateTime.now();
            return new String(buf, 0, len, Charset.forName("UTF-8"));
        }
        return "";
    }

    /**
     * Returns a simple Ack (in response to a Heartbeat) - has no content except
     * the increasing SeqNo
     * @return 
     */
    String makeAckStr() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Ack AckSeq=\"" + seqNo.getAndIncrement() + "\"></Ack>";
    }

    /**
     * Returns a simple Nak (negative acknowledgment) - this has no content and is 
     * only ever sent in response to a malformed or unparseable message.
     * @return 
     */
    String makeNakStr() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<Nak/>";
    }

    public Socket getSock() {
        return sock;
    }

    public void setSock(Socket sock) {
        this.sock = sock;
    }

    public MessageParser getParser() {
        return parser;
    }

    public void setParser(MessageParser parser) {
        this.parser = parser;
    }

    public void shutdown() {
        shutdown = true;
    }
}
