package wtf.s1.android.thread

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

class ThreadConstructMethodAdapter(methodVisitor: MethodVisitor?,
                                   access: Int,
                                   name: String?,
                                   desc: String?) :
    AdviceAdapter(Opcodes.ASM8, methodVisitor, access, name, desc) {

    override fun onMethodEnter() {
        super.onMethodEnter()
        println("method name $name")
    }

    override fun onMethodExit(opcode: Int) {
        super.onMethodExit(opcode)
        mv.visitVarInsn(ALOAD, 0)
        mv.visitMethodInsn(INVOKESTATIC,
            "wtf/s1/android/thread/ThreadLog",
            "create",
            "(Ljava/lang/Thread;)V",
            false)
        mv.visitInsn(RETURN)
    }
}