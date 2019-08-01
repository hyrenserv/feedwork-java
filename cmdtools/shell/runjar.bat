@echo off

rem 命令行参数需要用双引号括起来。因为 = 会被自动分割成两个参数
rem -d0 关闭明细清单。当-n设置的很大时，如果是屏幕输出，可以启用本开关

rem 本机
rem java -Dfile.encoding=UTF-8 -jar ..\build\libs\fdcmdtools-2.0.jar loadrunner -n10 -c5 -w15 url=http://47.103.38.63:38080/fdwebtest/action/hmfms/action/stress/index %*

rem 持续10小时添加数据
java -Dfile.encoding=UTF-8 -jar ..\build\libs\fdcmdtools-2.0.jar loadrunner -n100 -c50 -w36000 url=http://47.103.38.63:38080/fdwebtest/action/hmfms/action/stress/addData %*

rem 持续10小时查询9万开头的1万数据
rem java -Dfile.encoding=UTF-8 -jar ..\build\libs\fdcmdtools-2.0.jar loadrunner -n100 -c50 -w3600 url=http://47.103.38.63:38080/fdwebtest/action/hmfms/action/stress/getDataPage10Number10000 %*

rem 持续10小时随机查询前1000条数据
rem java -Dfile.encoding=UTF-8 -jar ..\build\libs\fdcmdtools-2.0.jar loadrunner -n100 -c50 -w36000 url=http://47.103.38.63:38080/fdwebtest/action/hmfms/action/stress/getRandomEntity %*

rem 深圳VPN
rem java -Dfile.encoding=UTF-8 -jar build\libs\fdcmdtools-2.0.jar loadrunner -n4000 -c8 url=http://10.50.130.91:17456/sdf/yyy?name=123 %*

rem 962121
rem java -Dfile.encoding=UTF-8 -jar build\libs\fdcmdtools-2.0.jar loadrunner -n200 -c8 url=http://www.962121.net

rem DB-pgsql
rem java -Dfile.encoding=UTF-8 -jar build\libs\fdcmdtools-2.0.jar loadrunner sort=threadid -n200 -c8 class=jdbc connType=conn driver=org.postgresql.Driver url=jdbc:postgresql://localhost:5432/postgres user=fd passwd=xxx123 sql="select * from test"
