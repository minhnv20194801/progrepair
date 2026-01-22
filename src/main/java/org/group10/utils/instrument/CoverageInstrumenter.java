package org.group10.utils.instrument;

import org.objectweb.asm.*;

public class CoverageInstrumenter {

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
