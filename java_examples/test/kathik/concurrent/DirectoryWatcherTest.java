package kathik.concurrent;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author ben
 */
public class DirectoryWatcherTest {

    @Test
    public void queueTest() throws IOException, InterruptedException {
        // Make a tmp/rand-UUID directory
        final Path tmpdir = Files.createTempDirectory(Paths.get("/tmp"), "directory-watcher-test-");
        final String base = tmpdir.toString();
        
        System.out.println("Temp dir for this run is: "+ base);
        DirectoryWatcher dw = DirectoryWatcher.of(base);
        BlockingQueue<String> q = dw.queue();
        Thread t = new Thread(dw);
        t.start();
        
        byte[] a = {1,2,3};
        byte[] b = {4,5,6};
        byte[] c = {7,8,9};
        byte[] a2 = {2,2,3};

        Path testa = Paths.get(base +"/a.class");
        Files.write(testa, a);
        String found = q.take();
        assertEquals(base +"/a.class", found);
        Path testb = Paths.get(base +"/b.class");
        Files.write(testb, b);
        found = q.take();
        assertEquals(base +"/b.class", found);
        // Rewrite the same bytes for a
        Files.write(testa, a);
        // Wait to allow the watcher to notice that a has changed
        Thread.sleep(2000);
        // Now write c
        Path testc = Paths.get(base +"/c.class");
        Files.write(testc, c);
        found = q.take();
        assertEquals(base +"/c.class", found);
        // Now change a
        Files.write(testa, a2);
        found = q.take();
        assertEquals(base +"/a.class", found);
        
        // Cleanup
        Files.delete(testa);
        Files.delete(testb);
        Files.delete(testc);
        dw.shutdown();
        Files.delete(tmpdir);
        t.join();
    }
    
}
