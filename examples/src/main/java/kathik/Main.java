/*
 * 
 * 
 * 
 */
package kathik;

/**
 *
 * @author ben
 */
public class Main {
    private static final String HELLO = "Hello";

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        Runnable r = () -> System.out.println(HELLO);
        Thread t = new Thread(r);
        t.start();
        t.join();
    }
    
}
