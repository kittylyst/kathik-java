package kathik.concurrent;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author ben
 */
public class DirectoryWatcher implements Runnable {

    private final String dirToWatch;
    private volatile boolean shutdown;

    public static class WorkItem {

        public final String filename;
        public final long timestamp;
        public final int checksum;

        public WorkItem(final String name, final int hash) {
            filename = null;
            checksum = hash;
            timestamp = System.currentTimeMillis();
        }
    }

    private final Map<String, WorkItem> trackedFiles = new HashMap<>();

    private final BlockingQueue<String> forProcessing = new LinkedBlockingQueue<>();

    private DirectoryWatcher(final String toWatch) {
        dirToWatch = toWatch;
    }

    private void init() {
    }

    public static DirectoryWatcher of(final String dirToWatch) {
        final DirectoryWatcher dw = new DirectoryWatcher(dirToWatch);
        dw.init();
        return dw;
    }

    public void run() {
        System.out.println("Staring up a directory watcher for " + dirToWatch + " ...");
        final Path dir = Paths.get(dirToWatch);
        while (!shutdown) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.class")) {
                ENTRIES:
                for (Path entry : stream) {
                    // System.out.println(entry.getFileName());
                    final String fileName = entry.toString();
                    final byte[] contents = Files.readAllBytes(entry);
                    final int hash = Arrays.hashCode(contents);
                    WorkItem current = trackedFiles.get(fileName);
                    if ((current != null)
                            && (current.checksum == hash))
                        continue ENTRIES;
                    trackedFiles.put(fileName, new WorkItem(fileName, hash));
                    System.out.println("Adding " + fileName + " to processing queue");
                    forProcessing.add(fileName);
                }
            } catch (IOException e) {
                e.printStackTrace();
                shutdown();
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                shutdown();
            }
        }
    }

    public void shutdown() {
        shutdown = true;
    }

    public BlockingQueue<String> queue() {
        return forProcessing;
    }
}
