package wtf.s1.android.thread

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.RecordComponentVisitor
import org.objectweb.asm.commons.AdviceAdapter

class ThreadManipulateClassVisitor(classVisitor: ClassVisitor?) :
    ClassVisitor(Opcodes.ASM8, classVisitor),
    Opcodes {


    private var isVisitThreadChildClass: Boolean = false
    private var hasRunMethod: Boolean = false

    override fun visitSource(source: String?, debug: String?) {
        super.visitSource(source, debug)
        println("source string $source")
    }

    override fun visitRecordComponent(
        name: String?,
        descriptor: String?,
        signature: String?
    ): RecordComponentVisitor {
        return super.visitRecordComponent(name, descriptor, signature)
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<String?>?
    ): MethodVisitor? {
        val mv = super.visitMethod(access, name, descriptor, signature, exceptions)

        return if (isVisitThreadChildClass && name.equals("<init>")) {
            println("find thread <init> method")
            ThreadConstructMethodAdapter(
                mv,
                access,
                name,
                descriptor
            )
        } else if (isVisitThreadChildClass && name.equals("run")) {
            println("find thread run method")
            hasRunMethod = true
            ThreadRunMethodAdapter(
                mv,
                access,
                name,
                descriptor
            )
        } else {
            mv
        }
    }

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        isVisitThreadChildClass = false
        super.visit(version, access, name, signature, superName, interfaces)

        println("super Name $superName")
        if ("java/lang/Thread" == superName) {
            name?.let {
                isVisitThreadChildClass = true
            }
            println("child class $name")
        }

    }

    override fun visitEnd() {
        if (!hasRunMethod && isVisitThreadChildClass) {
            val mv = cv.visitMethod(
                Opcodes.ACC_PUBLIC,
                "run",
                "()V",
                null,
                null)
            mv.visitVarInsn(AdviceAdapter.ALOAD, 0)
            mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "wtf/s1/android/thread/ThreadLog",
                "runBegin",
                "()V",
                false)
            mv.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                "java/lang/Thread",
                "run",
                "()V",
                false)

            mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "wtf/s1/android/thread/ThreadLog",
                "runEnd",
                "()V",
                false)
            mv.visitInsn(Opcodes.RETURN)
            mv.visitMaxs(0,0)
            mv.visitEnd()
        }
        super.visitEnd()

    }
}