package javamag.asm;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.V1_8;

/**
 *
 * @author kittylyst
 */
public final class MakeHelloWorld {

    // The ClassWriter that we'll use to build up the class
    private final ClassWriter cw;

    public MakeHelloWorld() {
        cw = new ClassWriter(0);
    }
    
    public static void main(String[] args) throws IOException {
        final MakeHelloWorld mkhw = new MakeHelloWorld();
        mkhw.run();
    }

    public void run() throws IOException {
        final Path out = Paths.get("MakeHelloWorld.class");
        Files.write(out, dump("MakeHelloWorld"));
    }

    public byte[] dump(String outputClazzName) {
        // Setup the basic metadata for the bootstrap class
        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, outputClazzName, null, "java/lang/Object", null);
        addStandardConstructor();
        addMainMethod();
        cw.visitEnd();
        return cw.toByteArray();
    }

    public void addMainMethod() {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null, null);
        mv.visitCode();

        
        mv.visitInsn(RETURN);
        mv.visitMaxs(3, 3);
        mv.visitEnd();
    }

    public void addStandardConstructor() {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }
}
