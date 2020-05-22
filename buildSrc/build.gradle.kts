plugins {
    `kotlin-dsl`
}
repositories {
    google()
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation(gradleApi())
    implementation(localGroovy())
    implementation("com.android.tools.build:gradle:3.5.1")
    implementation("com.android.tools.build:gradle-api:3.5.1")
    implementation("org.ow2.asm:asm:8.0.1")
    implementation("org.ow2.asm:asm-util:8.0.1")
    implementation("org.ow2.asm:asm-commons:8.0.1")
}
