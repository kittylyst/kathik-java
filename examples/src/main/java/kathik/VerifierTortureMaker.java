package kathik;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

/**
 *
 * @author ben
 */
public final class VerifierTortureMaker {

    public static void main(String[] args) throws IOException {
        final VerifierTortureMaker bmm = new VerifierTortureMaker();
        bmm.run();
    }

    private void run() throws IOException {
        final Path out = Paths.get("Exceptionalism.class");
        Files.write(out, dump("Exceptionalism"));
    }

    public byte[] dump(final String outputClassName) {
        final ClassWriter cw = new ClassWriter(0);

        // Setup the basic metadata for the bootstrap class
        // Use a version 5 classfile to avoid fiddling with StackMapTable
        cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, outputClassName, null, "java/lang/Object", null);

        // Create a standard void constructor
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        // Create exception heavy methods
        final int NUM_METHS = 1000;
        for (int j = 0; j < NUM_METHS; j++) {
            addExceptionalMethod(cw, "addEntry" + j);
        }

        // Create a main method
        mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        mv.visitCode();
        addStartupPrintout(mv);
        for (int j = 0; j < NUM_METHS; j++) {
            mv.visitMethodInsn(INVOKESTATIC, "Exceptionalism", "addEntry" + j, "()V", false);
        }
        mv.visitInsn(RETURN);
        mv.visitMaxs(3, 3);
        mv.visitEnd();

        cw.visitEnd();

        return cw.toByteArray();
    }

    private void addExceptionalMethod(ClassWriter cw, String methodName) {
        final MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, methodName, "()V", null, null);
        for (int i = 0; i < 6000; i++) {
            final Label startTry = new Label();
            final Label endTry = new Label();
            final Label startCatch = new Label();
            mv.visitTryCatchBlock(startTry, endTry, startCatch, "java/lang/Object");
            mv.visitLabel(startTry);
            mv.visitTypeInsn(NEW, "java/lang/Exception");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Exception", "<init>", "()V", false);
            mv.visitInsn(ATHROW);
            mv.visitLabel(endTry);
            mv.visitLabel(startCatch);
            mv.visitVarInsn(ASTORE, 1);
        }

        mv.visitInsn(RETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }

//       0: invokestatic  #2                  // Method java/time/Instant.now:()Ljava/time/Instant;
//       3: astore_1
//       4: getstatic     #3                  // Field java/lang/System.out:Ljava/io/PrintStream;
//       7: aload_1
//       8: invokevirtual #4                  // Method java/time/Instant.toString:()Ljava/lang/String;
//      11: invokevirtual #5                  // Method java/io/PrintStream.println:(Ljava/lang/String;)V
    private void addStartupPrintout(MethodVisitor mv) {
        mv.visitMethodInsn(INVOKESTATIC, "java/time/Instant", "now", "()Ljava/time/Instant;", false);
        mv.visitVarInsn(ASTORE, 1);
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/time/Instant", "toString", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
    }

}
