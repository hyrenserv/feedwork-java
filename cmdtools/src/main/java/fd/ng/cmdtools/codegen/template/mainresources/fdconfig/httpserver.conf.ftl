#host : localhost 启动App时，如果明确指定localhost，将只有本机能访问这个服务！
#idleTimeout : 30000
port : 8080
httpsPort : 38443
webContext : /fdwebtest
actionPattern : /action/*

session :
  maxage: 60    # session过期时间，单位是秒。这是指不活动的过期时间，也就是说，如果一直在页面做操作，就一直有效。如果5分钟都没有操作，则失效
  httponly: no  # true：通过程序(JS脚本、Applet等)将无法读取到Cookie信息，这样能有效的防止XSS攻击。
