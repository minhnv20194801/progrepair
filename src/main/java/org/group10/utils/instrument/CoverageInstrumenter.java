package org.group10.utils.instrument;

import org.objectweb.asm.*;

/**
 * Utility class for instrumenting Java bytecode to track code coverage. <br>
 *
 * This class uses the ASM library to modify the bytecode of a given class
 * such that every line number in the class triggers a call to
 * {@link CoverageTracker#hit(int)} when executed.
 */
public class CoverageInstrumenter {
     /**
     * Instruments the given class byte array to add coverage tracking.
     * <p>
     * For every line number in the class, this method injects a call to
     * {@link CoverageTracker#hit(int)}, allowing coverage tools to record
     * which lines have been executed.
     * </p>
     *
     * @param originalClass the original class as a byte array
     * @return a new byte array representing the instrumented class
     */
    public static byte[] instrument(byte[] originalClass) {
        ClassReader cr = new ClassReader(originalClass);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        ClassVisitor cv = new ClassVisitor(Opcodes.ASM9, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc,
                                             String signature, String[] exceptions) {
                MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
                return new MethodVisitor(Opcodes.ASM9, mv) {
                    @Override
                    public void visitLineNumber(int line, Label start) {
                        mv.visitLdcInsn(line);
                        mv.visitMethodInsn(Opcodes.INVOKESTATIC,
                                "org/group10/utils/instrument/CoverageTracker",
                                "hit",
                                "(I)V",
                                false);
                        super.visitLineNumber(line, start);
                    }
                };
            }
        };

        cr.accept(cv, ClassReader.EXPAND_FRAMES);
        return cw.toByteArray();
    }
}
