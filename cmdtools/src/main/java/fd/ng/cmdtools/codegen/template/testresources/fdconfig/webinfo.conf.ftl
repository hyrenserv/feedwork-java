action.longtime : 10           # 超过这个时间(毫秒)的action被log出来

cookie :
  maxage   : 28800
  path     : /
  httponly : no    # true：通过程序(JS脚本、Applet等)将无法读取到Cookie信息，这样能有效的防止XSS攻击。
  secure   : no    # true：表示创建的 Cookie 会被以安全的形式向服务器传输。也就是只能在 HTTPS 连接中被浏览器传递到服务器端进行会话验证，如果是 HTTP 连接则不会传递该信息，所以不会被窃取到Cookie 的具体内容

cors :
  allow : yes
  acao  : null           # Access-Control-Allow-Origin
  acam  : "POST, GET"    # Access-Control-Allow-Methods
  acac  : true           # Access-Control-Allow-Credentials

fileupload :
  # SizeThreshold :
  # FilesTotalSize : 1024
  # Repository : /tmp
  # SavedDir : /tmp/upfiles/temp
  SavedDir : d:\tmp\upfiles\temp\
