package wtf.s1.android.thread

import com.android.SdkConstants
import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.objectweb.asm.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry


class ThreadInspectorTransform : Transform() {

    companion object{
        private const val TRANSFORM_NAME = "thread-inspector"
    }

    override fun getName(): String =
        TRANSFORM_NAME

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> =
        mutableSetOf(QualifiedContent.DefaultContentType.CLASSES)

    override fun isIncremental(): Boolean = false

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> =
        mutableSetOf(
            QualifiedContent.Scope.PROJECT,
            QualifiedContent.Scope.EXTERNAL_LIBRARIES,
            QualifiedContent.Scope.SUB_PROJECTS
        )

    override fun getReferencedScopes(): MutableSet<in QualifiedContent.Scope> =
        mutableSetOf(
            QualifiedContent.Scope.PROJECT,
            QualifiedContent.Scope.EXTERNAL_LIBRARIES,
            QualifiedContent.Scope.SUB_PROJECTS
        )


    override fun transform(transformInvocation: TransformInvocation?) {
        super.transform(transformInvocation)
        transformInvocation?.let {
            val contentLocation =
                it.outputProvider.getContentLocation(name, outputTypes, scopes, Format.DIRECTORY)
             contentLocation.delete()
             contentLocation.mkdir()
            println("content location ${contentLocation.absolutePath}")

            it.inputs.forEach {input->
                input.directoryInputs.forEach {directoryInput ->
                    val dirPath = directoryInput.file.absolutePath
                    println("dirinput $dirPath")

                    directoryInput.file.walkTopDown().forEach { file ->
                        val relativeFilePath = file.absolutePath.substring((dirPath.length))
                        // println("walk to down $relativeFilePath")
                        if (!file.isDirectory) {
                            val fileName = file.name
                            val outputFile = File(contentLocation, relativeFilePath)
                            if (needManipulate(fileName)) {
                                val fis =  FileInputStream(file.absolutePath)
                                val fos = FileOutputStream(outputFile.absolutePath)
                                fos.write(manipulate(fis.readBytes()))
                                fos.close()
                                fis.close()
                                println("$fileName write bytes")
                            } else {
                                outputFile.writeBytes(file.readBytes())
                            }
                        } else {
                            val newDir = File(contentLocation, relativeFilePath)
                            newDir.mkdir()
                            println("new dir ${newDir.absolutePath}")
                        }
                    }
                }


                input.jarInputs.forEach {jarInput ->

                    println("jarName = ${jarInput.name}")

                    var jarName = jarInput.name
                    val md5Name = DigestUtils.md5Hex(jarInput.file.absolutePath)
                    if (jarName.endsWith(".jar")) {
                        jarName = jarName.substring(0, jarName.length - 4)
                    }

                    //生成输出路径
                    val dest = it.outputProvider.getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)

                    val jarFile = JarFile(jarInput.file)

                    val tempFile = File(jarInput.file.parent + File.separator + "temp.jar")
                    //避免上次的缓存被重复插入
                    if (tempFile.exists()) {
                        tempFile.delete()
                    }

                    val jarOutputStream = JarOutputStream(FileOutputStream(tempFile))

                    jarFile.entries().iterator().forEach {jarEntry ->
                        val entryInputStream = jarFile.getInputStream(jarEntry)
                        val nextZipEntry = ZipEntry(jarEntry.name)
                        println("jarEntry name = ${jarEntry.name}")
                        jarOutputStream.putNextEntry(nextZipEntry)
                        jarOutputStream.write(
                            if (needManipulate(jarEntry.name)) {
                                manipulate(entryInputStream.readBytes())
                            } else {
                                entryInputStream.readBytes()
                            }
                        )
                        jarOutputStream.closeEntry()
                    }

                    jarOutputStream.close()
                    jarFile.close()

                    FileUtils.copyFile(tempFile, dest)
                    tempFile.delete()
                }
            }
        }

        println("transform end")
    }

    private fun needManipulate(fileName: String): Boolean {
        return (fileName.endsWith(SdkConstants.DOT_CLASS)
                && !fileName.endsWith("R.class")
                && !fileName.endsWith("BuildConfig.class")
                && !fileName.contains("R\$"))
    }

    private fun manipulate(byteArray: ByteArray): ByteArray {
        val classReader = ClassReader(byteArray)
        val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
        classReader.accept(getClassVisitor(classWriter), ClassReader.EXPAND_FRAMES)
        return classWriter.toByteArray()
    }

    private fun getClassVisitor(cw: ClassWriter): ClassVisitor {
        return ThreadManipulateClassVisitor(
            cw
        )
    }
}