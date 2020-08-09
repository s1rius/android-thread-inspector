
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


npm 和 yarn 的源及代理

npm和yarn转换淘宝源和官方源
npm config set registry http://registry.npm.taobao.org/
npm config set registry https://registry.npmjs.org/

yarn config set registry http://registry.npm.taobao.org/
yarn config set registry https://registry.npmjs.org/

npm 设置代理
npm config set proxy http://127.0.0.1:8080
npm config set https-proxy http://127.0.0.1:8080

npm 删除代理
npm config delete proxy
npm config delete https-proxy

yarn 设置代理
yarn config set proxy http://127.0.0.1:8080
yarn config set https-proxy http://127.0.0.1:8080

yarn 删除代理
yarn config delete proxy
yarn config delete https-proxy

export http_proxy="http://127.0.0.1:8001";
export HTTP_PROXY="http://127.0.0.1:8001";
export https_proxy="http://127.0.0.1:8001";
export HTTPS_PROXY="http://127.0.0.1:8001";