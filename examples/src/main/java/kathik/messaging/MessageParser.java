package kathik.messaging;

import java.util.Optional;

/**
 *
 * @author ben
 */
public interface MessageParser {
    Optional<Message> parse(String msgTxt);
}
