// 定义全局变量。各子模块使用 rootProject.ext 引用
ext {
    currentDateTime = new Date().format("yyyy-MM-dd HH:mm:ssZ")
}

allprojects {
    group '${basePackage}'  // 修改为本工程的真实 GroupId。必须要修改。后续每个子工程都应该使用这个前缀作为包的根
    version '1.0'
}

subprojects {
    apply plugin: 'java'

    compileJava {
        sourceCompatibility = 1.8
        targetCompatibility = 1.8
        options.encoding = 'UTF-8'
        options.compilerArgs += "-parameters"
    }

    compileTestJava {
        sourceCompatibility = 1.8
        targetCompatibility = 1.8
        options.encoding = 'UTF-8'
        options.compilerArgs += "-parameters"
    }

    repositories {
        mavenLocal()
        maven { url 'http://maven.aliyun.com/nexus/content/groups/public/' }
    }

    configurations {
        // 所有需要忽略的包定义在此
        //all*.exclude group: 'commons-httpclient'
        //all*.exclude group: 'commons-beanutils', module: 'commons-beanutils'
    }

    dependencies {
        compile fileTree(dir: "$rootDir/libs/runtime", include: ['*.jar'])
        compile group: 'org.apache.logging.log4j', name: 'log4j-core', version: '2.11.2'
        compile group: 'org.apache.logging.log4j', name: 'log4j-slf4j-impl', version: '2.11.2'
        compile group: 'com.google.code.gson', name: 'gson', version: '2.8.5'
        compile group: 'com.zaxxer', name: 'HikariCP', version: '3.3.1'
        compile group: 'org.postgresql', name: 'postgresql', version: '42.2.6'

        // 对于非WEB项目，删除以下3个依赖
        compile group: 'javax.servlet', name: 'javax.servlet-api', version: '3.1.0'
        compile group: 'org.eclipse.jetty', name: 'jetty-server', version: '9.4.19.v20190610'
        compile group: 'org.eclipse.jetty', name: 'jetty-servlet', version: '9.4.19.v20190610'
        // 如果项目中不需要 HttpClient 方式的网络通讯，删除okhttp依赖
        compile group: 'com.squareup.okhttp3', name: 'okhttp', version: '3.13.1'

        testCompile fileTree(dir: "$rootDir/libs/testcase", include: ['*.jar'])
        testCompile group: 'junit', name: 'junit', version: '4.12'
        testCompile group: 'org.hamcrest', name: 'hamcrest-all', version: '1.3'
    }

    // -------------  模块打包  -------------
    def appMainClass = "${r'${project.group}'}.${r'${project.name}'}.main.AppMain"
    // 正式发布时，打开注释，目的：不将resources打包到jar文件内
//    processResources {
//        exclude { "**/*.*" }
//    }

    jar {
        archivesBaseName = "${r'${project.name}'}"

        String classpaths = 'resources/' // 存放资源文件的目录
        configurations.runtime.each {classpaths = classpaths + " lib/"+it.name} //遍历项目的所有依赖的jar包赋值给变量someString

        manifest {
            attributes(
                    'Main-Class': appMainClass,
                    'Class-Path': classpaths,
                    "Implementation-Title": archivesBaseName,
                    "Implementation-Version": project.version,
                    'Built-By': System.getProperty('user.name'),
                    'Built-JDK': System.getProperty('java.version'),
                    'Source-Compatibility': project.sourceCompatibility,
                    'Target-Compatibility': project.targetCompatibility,
                    "Build-Time": rootProject.ext.currentDateTime
            )
        }
    }

    task clearJar(type: Delete) {
        delete "$buildDir/libs/lib"
//        followSymlinks = true
    }
    task copyJar(type:Sync){
        from configurations.runtime
        into "$buildDir/libs/lib"
    }
    task copyResource(type:Sync){
        // 因为resources没有打包到jar里面，所以这里自动复制到发布目录下
        sourceSets.main.resources.srcDirs.each {
            from it
            into "$buildDir/libs/resources"
        }
    }
    task release(dependsOn: [build, copyJar, copyResource]) {
        println("release Done.")
    }
}
