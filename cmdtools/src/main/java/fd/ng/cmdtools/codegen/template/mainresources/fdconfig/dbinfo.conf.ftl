# global 中填写全局通用的配置参数，这些参数会被继承到下面的每一组数据库连接配置中
global :
  show_sql        : yes
  fetch_size      : 200
  max_result_rows : -1        # 结果集最大允许多少条。-1为不受限制
  show_conn_time  : no        # 是否显示数据连接的时间消耗
  show_sql_time   : yes       # 是否显示sql执行时间
  longtime_sql    : 10000     # 10秒。SQL执行时间超过这个值的，显示到日志中

# 每组配置使用 name 进行区别。在程序中，通过 name 获取不同的数据库连接
databases :
  -
    name            : default
    way             : POOL       # 来自枚举 ConnWay
    dbtype          : MYSQL      # 来自枚举 Dbtype
    driver          : com.mysql.jdbc.Driver
    url             : jdbc:mysql://localhost:3306/xxx?useCursorFetch:true
    username        : root
    password        : ""
    show_conn_time  : yes
    show_sql_time   : yes
    longtime_sql    : 1000
    # 连接池参数
    minPoolSize     : 10
    maxPoolSize     : 20
    properties:
      - cachePrepStmts         : true
      - prepStmtCacheSize      : 250
      - prepStmtCacheSqlLimit  : 2048
  -
    name       : pgsql
    way        : POOL
    dbtype     : POSTGRESQL
    driver     : org.postgresql.Driver
    url        : jdbc:postgresql://127.0.0.1:5432/postgres
    username   : fd
    password   : ""
    disable    : yes
