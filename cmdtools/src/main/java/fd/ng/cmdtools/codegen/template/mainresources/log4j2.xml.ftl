<?xml version="1.0" encoding="UTF-8"?>
<!--Configuration中可以设置status="warn"，用于设置log4j2自身内部的信息输出-->
<!--monitorInterval：Log4j能够自动检测修改配置并重载配置，间隔为秒数，最小为5 -->
<Configuration monitorInterval="180">

    <!-- 文件路径和文件名称，方便后面引用 -->
    <Properties>
        <Property name="backupFilePatch">/tmp/log/</Property>
        <Property name="fileName">backupLog4jTest.log</Property>
    </Properties>
    <!--先定义所有的appender-->
    <Appenders>
        <!-- （1）输出到控制台的配置-->
        <Console name="Console" target="SYSTEM_OUT">
            <!--只输出level及以上级别的信息（onMatch），其他的直接拒绝（onMismatch）-->
            <ThresholdFilter level="trace" onMatch="ACCEPT" onMismatch="DENY" />
            <!-- 输出日志的格式-->
            <PatternLayout pattern="[%-5level] %msg%xEx%n" />
        </Console>

        <!-- （2）覆盖输出到文件。这个log每次运行程序会自动清空，适合临时测试用-->
        <File name="TempLogFile" fileName="/tmp/log/web.log" append="false">
            <PatternLayout
                    pattern="%d{HH:mm:ss.SSS} %-5level %class{36} %L %M - %msg%xEx%n" />
        </File>

        <!-- （3）这个会打印出所有的信息，每次大小超过size，则这size大小的日志会自动存入按年份-月份建立的文件夹下面并进行压缩存档-->
        <RollingFile name="RollingFile" fileName="${r'${backupFilePatch}'}${r'${fileName}'}"
                     filePattern="${r'${backupFilePatch}'}$${r'${date:yyyy-MM}'}/app-%d{yyyyMMddHHmmssSSS}.log.gz">
            <PatternLayout
                    pattern="%d{yyyy.MM.dd 'at' HH:mm:ss.SSS z} %-5level %class{36} %L %M - %msg%xEx%n" />
            <!-- 日志文件大小 -->
            <SizeBasedTriggeringPolicy size="64MB" />
            <!-- 最多保留文件数 DefaultRolloverStrategy属性如不设置，则默认为最多同一文件夹下7个文件 -->
            <DefaultRolloverStrategy max="20"/>
        </RollingFile>
    </Appenders>

    <!--定义logger，只有定义了logger并引入的appender，appender才会生效-->
    <Loggers>
        <!--过滤掉其他软件包中的无用的DEBUG信息-->
        <Logger name="org.apache" level="WARN"></Logger>
        <Root level="trace">
            <AppenderRef ref="Console"/>
        </Root>
    </Loggers>
</Configuration>