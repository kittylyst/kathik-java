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
public final class MakeLdcExample {

    public static void main(String[] args) throws IOException {
        final MakeLdcExample ldc = new MakeLdcExample();
        ldc.run();
    }

    private void run() throws IOException {
        final Path out = Paths.get("WeirdLDC.class");
        Files.write(out, dump("WeirdLDC"));
    }

    public byte[] dump(final String outputClassName) {
        final ClassWriter cw = new ClassWriter(0);

        // Setup the basic metadata for the bootstrap class
        // Use a version 8 classfile to allow max chance this works
        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, outputClassName, null, "java/lang/Object", null);

        // Visit the constant pool to set our MethodType constant
        
        // Create a standard void constructor
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        // Create a main method
        mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        mv.visitCode();
        addStaticLdcCall(mv);
        mv.visitInsn(RETURN);
        mv.visitMaxs(3, 3);
        mv.visitEnd();

        cw.visitEnd();

        return cw.toByteArray();
    }

    private void addStaticLdcCall(MethodVisitor mv) {
        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
        Type mt = Type.SHORT_TYPE;
        mv.visitLdcInsn(mt);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/Object;)V", false);
    }
}
