package wtf.s1.android.thread
import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class ThreadInspectorPlugin: Plugin<Project> {

    override fun apply(target: Project) {

        // val extension = target.extensions.create("thread-inspector", ThreadExtension::class.java)

        val android = target.extensions.findByName("android") as BaseExtension


        android.registerTransform(ThreadInspectorTransform())
    }

    open class ThreadExtension {
    }
}