package wtf.s1.android.thread

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

class ThreadRunMethodAdapter(methodVisitor: MethodVisitor?,
                             access: Int,
                             name: String?,
                             desc: String?):
    AdviceAdapter(Opcodes.ASM8, methodVisitor, access, name, desc) {

    override fun onMethodEnter() {
        super.onMethodEnter()
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "wtf/s1/android/thread/ThreadLog",
            "runBegin",
            "()V",
            false)
    }

    override fun onMethodExit(opcode: Int) {
        super.onMethodExit(opcode)
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "wtf/s1/android/thread/ThreadLog",
            "runEnd",
            "()V",
            false)
        mv.visitInsn(RETURN)
    }
}