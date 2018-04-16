package kathik.messaging;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 *
 * @author ben
 */
public class TestSocketComms {

    @Test
    public void given_mocked_socket_that_sends_heartbeat_then_ack_is_seen_on_outputstream() throws IOException {
        final String hb = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Heartbeat/>";
        final String response = testInputAndReturnOutput(hb, 3);

        final Pattern p = Pattern.compile("<\\?xml version=\"1.0\" encoding=\"UTF-8\"\\?>"
                + "<Ack AckSeq=\"(\\d+)\"></Ack>");
        final Matcher m = p.matcher(response);

        assertTrue("Response " + response + " doesn't match Ack pattern", m.matches());
    }

    @Test
    public void given_mocked_socket_that_sends_malformed_data_then_nak_is_seen() throws IOException {
        final String hb = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>Heartbeat/>";
        final String response = testInputAndReturnOutput(hb, 3);
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Nak/>", response);
    }

    /*
     * Helper method for simple message in / message out response on the RCT side
     */
    private String testInputAndReturnOutput(final String inputMsg, final int secsToWait) throws IOException {
        // Set up a message into the InputStream (so MessageReceiver can read it)
        final byte[] buf = inputMsg.getBytes();

        // Get the output stream to manipulate
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();

        final MessageStreamManager translationManager = new MessageStreamManager();

        final MessageReceiver l = new MessageReceiver();
        translationManager.setListener(l);
        final SocketFactory mockSocketFactory = new MockSocketFactoryImpl(new ByteArrayInputStream(buf), baos);
        translationManager.setSockFactory(mockSocketFactory);

        // Now bring up the Test Thread
        final Thread testThread = new Thread(() -> translationManager.init());
        testThread.start();

        // Here we're still on the main harness thread
        try {
            // Wait for secsToWait, to allow spinup time
            Thread.sleep(secsToWait * 1000);

            // Now close the socket
            final Socket sock = mockSocketFactory.getSock();
            sock.close();

            testThread.join();
        } catch (InterruptedException ex) {
            throw new IOException(inputMsg + " test interrupted");
        }
        // Read the response from the OutputStream
        return baos.toString();
    }

}
