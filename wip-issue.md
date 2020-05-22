
Q: 上传了 bintray 无法找到这个plugin
A problem occurred configuring root project 'android-thread-inspector'.
> Could not resolve all artifacts for configuration ':classpath'.
   > Could not find wtf.s1.pudge:thread-gradle-plugin:0.1.0.
     Searched in the following locations:
       - https://dl.google.com/dl/android/maven2/wtf/s1/pudge/thread-gradle-plugin/0.1.0/thread-gradle-plugin-0.1.0.pom
       - https://dl.google.com/dl/android/maven2/wtf/s1/pudge/thread-gradle-plugin/0.1.0/thread-gradle-plugin-0.1.0.jar
       - https://jcenter.bintray.com/wtf/s1/pudge/thread-gradle-plugin/0.1.0/thread-gradle-plugin-0.1.0.pom
       - https://jcenter.bintray.com/wtf/s1/pudge/thread-gradle-plugin/0.1.0/thread-gradle-plugin-0.1.0.jar
     Required by:
         project :

A: 插件使用中，apply 的 id 对应了 META-INF.gradle-plugins 下的 .properties 文件的文件名。
   如果没有用对，就会出现无法找到这个 plugin 的问题