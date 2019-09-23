# feedwork
# 一、使用样例
### 假设有如下HTML页面：
![page](https://github.com/hyrenserv/resources/raw/master/feedwork-java/images/page-show.png)
### 其HTML代码如下：
![page](https://github.com/hyrenserv/resources/raw/master/feedwork-java/images/page-code.png)
### 对应的处理程序这样写即可：
![page](https://github.com/hyrenserv/resources/raw/master/feedwork-java/images/code-1.png)
### 或者，如果想用SQL完成入库，这样写：
![page](https://github.com/hyrenserv/resources/raw/master/feedwork-java/images/code-2.png)

详细用法参见每个模块中的单元测试代码。    
例如，一个完整的增删改查的WEB应用，及对应的单元测试代码，见：
```text
/web/src/test/java/fd/ng/web/hmfmswebapp/a0101/UserManagerAction.java
/web/src/test/java/fd/ng/web/hmfmswebapp/a0101/UserManagerActionTest.java
```

# 二、搭建新项目的步骤

- 新建 Gradle java 工程（不需要任何和Web相关的插件）
- 给工程新建 Module
- 下载 [libs](https://github.com/hyrenserv/resources/tree/master/feedwork-java) 目录放到在工程根目录下（含libs目录）
- 开始编码
  - __方式一__：自动生成初始代码骨架
    - 使用 [fdcmdtools](https://github.com/hyrenserv/resources/tree/master/feedwork-java) 生成代码骨架
    - 把自动生成的 build.gradle 拷贝到项目根目录。该Module下的build.gradle一般应该清空。
    - 修改test下的 dbinfo.conf（配置自己的DB连接），执行 SqlOperatorTest 看运行状况
    - 修改main下的 dbinfo.conf（配置自己的DB连接），启动 main.AppMain ，执行 SysParaActionTest 看运行情况（项目库表必须包含sys_para表才能运行）
    - 在biz包下，编写具体业务处理的代码
    
  - __方式二__：从零开始
    - (1) 创建目录结构，建议为：
    ```text
    basepackagename.biz.a0101  // 按照业务功能对应创建相应的包：a0101, b0101 ......
                               // 业务层中，所有公共的、全局的代码，用 z 开头的包名
                               // 例如：
    basepackagename.biz.zauth  // 登陆验证
    basepackagename.biz.zbase  // 存放基类的包
    basepackagename.entity     // 存放与DB表对应的实体类
    basepackagename.exception  // 存放整个项目的异常处理基类
    basepackagename.main       // 项目启动类
    basepackagename.util       // 项目工具类
    ```
    - (2) 编写业务处理基类 `zbase.WebappBaseAction` 重载 `_doPreProcess` 方法，完成Session验证等处理代码
    ```java
    package basepackagename.biz.zbase;
    public abstract class WebappBaseAction extends AbstractWebappBaseAction {
        @Override
        protected ActionResult _doPreProcess(HttpServletRequest request) {
            
	    // 完成 session 验证等各种处理

	    return null; // 返回null表示各种验证都通过了
        }
    }
    ```
    - (3) 编写项目启动类
    ```java
    package basepackagename.main;
    public class AppMain extends WebServer{
        public static void main(String[] args) {
            new AppMain().running();
        }
    }
    ```
    - (4) 编写conf文件
    ```text
    appinfo.conf    : 设置 basePackage
    dbinfo.conf     : 设置数据库连接信息
    httpserver.conf : 设置 port, webContext
    webinfo.conf    : 根据实际情况设置（也可以为空，全部使用默认值）
    ```
    - (5) 在biz包下，编写具体业务处理的代码

# 三、功能介绍

## 1. Action

### 1.1 编写 Action 类

- 方式一：继承 AbstractWebappBaseAction  
每个项目首先编写自己用的 Action 父类，并继承自 AbstractWebappBaseAction;  
重写 _doPreProcess 和 _doPostProcess 两个方法，这两个方法在每个Action函数执行前、后被自动调用。一般情况，pre中做session验证，_doPostProcess做清理操作。AbstractWebappBaseAction的_doPostProcess默认完成了DB清理工作。  

- 方式二：使用 @Action 注解  
如果一个包下面有多个Action类，那么需要给 @Action 注解设置 UriExt 值，该值会追加到包名路径的最后。

附加说明：  
web请求的映射规则为：使用Action类的包名+方法名，作为URL访问路径。 即：  
1）URL最后一个/符号前面为请求路径，用该值去找对应的 Action 类  
2）URL最后一个/符号后面为请求方法，该该值去找对应的 Action 类方法  

### 1.2 编写 Action 方法

按照普通的Java方法编写程序即可，不需要有任何与WEB程序相关的处理代码。  
如果方法内部必须直接操作request等对象，可通过RequestUtil工具类获取。  
所有 public 非 static 的方法，被作为web请求的响应方法。  
如果有同名方法，需要使用 UrlName 注解为其设置别名。  
使用 SqlOperator 可以直接操作数据库，不需要 try...catch  
代码中如果需要中断方法执行并返回错误信息给前端，可以直接抛出 BusinessException 异常  
分页查询，需创建分页对象并使用相应的分页查询方法，例如：
```java
public class YoursAction {
    public Map<String, Object> getPagedUserResult(int currPage, int pageSize) {
        Page page = new DefaultPageImpl(currPage, pageSize);
        Result result0 = Dbo.queryPagedResult(page,	"select * from XXX");
        Map<String, Object> result = new HashMap<>();
        result.put("totalSize", page.getTotalSize()); // 总记录数
        result.put("pageCount", page.getPageCount()); // 总页数
        result.put("data", result0.toList());
        return result;
    }
}
```

### 1.3 Action 方法参数

可以是任意名字的参数，如果名字和前端提交的数据名字一致则自动取值，如果不一致，可以使用 @RequestParam 注解。  
如果参数是一个JavaBean，可以使用 @RequestBean 注解，或者继承自 FeedBean。  

### 1.4 Action 返回值

返回值可以是任意对象。  
前端得到JSON串，格式为：{ code: 200, message: "OK", data: 方法的返回值对象 }  

## 2. 测试用例

### 所有测试用例，需要继承 FdBaseTestCase 或其子类

### 2.1 针对每个Action方法编写对应的测试用例

引入 fd-testing 包，继承 WebBaseTestCase，使用 Action 方法相同名字来命名的测试方法。  
把test中的 netclientinfo.conf 配置文件中的 connectTimeout, readTimeout, writeTimeout 设置成250，以免单步跟踪时出现超时  

例如，有 Action 方法如下：
```java
public class YoursAction {
    public boolean addUser(String name, int age, String[] favors) {
        return true;
    }
}
```
对应的单元测试方法的样板代码：
```java
public class YoursActionTest {
    @Test
    public void addUser() {
        // 1）提交数据给Action
        HttpClient.ResponseValue resVal = new HttpClient()
                .addData("name", "张三")          // 每个 addData 为一个"名/值"对
                .addData("age",  25)
                .post(getActionUrl("addUser"));   // getActionUrl中传入“被测试的Action方法名字”
        
        // 2）断言判断返回值
        assertThat(resVal, containsString("hello:张三"));
    }
}
```

### 2.2 对于需要登陆认证的测试用例处理方式
```text
    new HttpClient()
        .buildSession()
        .addData("username", "admin")
        .addData("password", "admin")
        .post("......");
```
如上代码，增加 buildSession() 方法即可在测试用例中获得 session

- 对测试类使用 FixMethodOrder(MethodSorters.NAME_ASCENDING) 注解
- 所有被测试方法按照ASCII顺序编号
- 保证登陆认证的方法是顺序最小的

以上处理方式，是仅登陆一次，然后所有测试方法都可执行。  
也可以对登陆方法设置 Before 注解，这样会导致每个测试方式执行时，都会先登陆一次。  

### 2.3 扩展功能

#### - 可用的注解
- 使用 Timeout 注解，对测试方法判断执行时间是否小于该时间
- 使用 Retry 注解，对测试方法可以进行反复重试，直到成功为止
- 使用 Repeat 注解，可重复多次执行测试方法
- 使用 Parallel 注解，可对单个测试方法设置成并行多次执行

对测试类使用 @RunWith(ExtendBasalRunner.class) 注解（推荐）  
或者，使用 RetryRule 等规则也可以。  

#### - 对多个测试用例类及里面的测试方法设置为并行执行

样例代码如下：
```java
public class SomeTestSuite {
    public static void main(String[] args) {
        Class[] cls = { TestCase1.class, TestCase2.class };
        Result rt = JUnitCore.runClasses(new ParallelRunner(true, true), cls); // 这两个类及类里面的方法会被并行执行
        // 观察执行结果
        System.out.println("wasSuccessful=" + rt.wasSuccessful() + ", getIgnoreCount=" + rt.getIgnoreCount());
        System.out.println("getRunCount=" + rt.getRunCount() + ", getRunTime=" + rt.getRunTime());
        System.out.println("getFailureCount=" + rt.getFailureCount() + ", getRunTime=" + rt.getFailures().stream().map(Failure::toString).collect(Collectors.joining(" | ")));
    }
}
```

## 3. 异常处理
自动生成的代码骨架中，提供两个异常：BusinessException 和 AppSystemException

|  | BusinessException | AppSystemException |
| ------- | ---------- | ---------- |
| 使用场合 | Action类的业务方法中 | 当需要try...catch时，在catch中，<br>对发生的异常进行再包裹后抛出 |
| 注意事项 | __没有异常堆栈__ ！！！ | 就是 RuntimeException |
| 附加特性 | 自动把异常信息写入日志中 | 只把构造这个异常时使用的信息返回前端，<br>被包裹的原始异常信息记入日志。<br>会自动生成的错误代码<br>一并返回前端并记入日志 |
| 附加特性 | 支持给抛出的异常设置数字代码 | - |
| 附加特性 | 支持使用错误分类枚举的方式抛出异常 | - |
| 附加特性 | 支持从i18n配置文件中读取数据 | 不支持。<br>可参照BusinessProcessException改造其父类以支持i18n |
| 性能考虑 | 性能极其优异 | 和各种JAVA异常类一样的性能 |


# 四、Howto

### - 跨域支持

默认支持跨域。  
前端 ajax 提交时，增加： xhrFields: { withCredentials: true }  

### - 文件上传

Action 方法使用UploadFile注解，参数名与上传页面中的 file 元素名对应上，参见如下代码：
```html
<form enctype="multipart/form-data" method="post" name="fileinfo">
  文件描述：<input type="text" name="desc" /><br />
  选择文件：<input type="file" name="files" multiple /><br />
  <input type="submit" value="提  交" />
</form>

<script>
var form = document.forms.namedItem("fileinfo");
form.addEventListener('submit', function(ev) {
  var formData = new FormData(form);
  var oReq = new XMLHttpRequest();
  var url = "http://localhost:8080/......./upload";
  oReq.open("POST", url, true);
  oReq.onload = function(oEvent) {
    if (oReq.status == 200)
      // 成功
  }
  oReq.send(formData);
  ev.preventDefault();
}, false);
</script>
```
对应的后台 Java 处理代码为：
```java
public class YoursAction {
    @UploadFile
    public void upload(String desc, String[] files) throws IOException {
        for(String curFileinfo : files) {
            File uploadedFile = FileUploadUtil.getUploadedFile(curFileinfo);       // 已经上传到服务器的文件
            String orgnFilename = FileUploadUtil.getOriginalFileName(curFileinfo); // 原始文件名
        }
    }
}
```
可以在 UploadFile 注解中设置上传文件的默认存放目录

### - 前端提交的是 Json 数据该如何处理

在Action方法中，使用 RequestUtil.getJson() 即可获取前端提交的整个json串

### - 部署到tomcat中

删除AppMain等所有与netserver.http相关的程序。（因为要使用tomcat代替内嵌的jetty）  
按照普通的web项目部署到tomcat中，WEB-INF\lib需要的jar包为：core, database, web, gson, log4j, HikariCP  
在classes下放fdconfig等资源文件  
web.xml中增加以下配置：
```xml
<web-app>
    <servlet>
      <servlet-name>bizController</servlet-name>
      <servlet-class>fd.ng.web.handler.WebServlet</servlet-class>
    </servlet>

    <servlet-mapping>
        <servlet-name>bizController</servlet-name>
        <url-pattern>/action/*</url-pattern>
    </servlet-mapping>
</web-app>
```
记得要配置成UTF-8，比如tomcat9支持servlet 4了，可以在web.xml增加一句：
```xml
<request-character-encoding>UTF-8</request-character-encoding>
```

### - 命令行解析

使用 ArgsParser 工具类做命令行参数解析。接受两种命令行参数：  
- 名值对参数：name=value
- 无值参数
如果值中有空格、制表符等字符，需使用双引号。  
如果有前后双引号，那么里面双引号需要加转义字符 \   

例如，有如下命令行参数：  
```shell script
java -jar yours.jar type=r -fw file=/tmp/io-jdk.csv
```
对应的解析程序为：  
```java
public class YoursMainClass {
    public static void main(String[] args) {
        // 使用 defOptionPair   定义名值对的命令行参数
        // 使用 defOptionSwitch 定义无值的命令行参数
        // 3个参数为： 参数名、是否必须提供、参数说明
        ArgsParser cmd = new ArgsParser()
                .defOptionPair("file", true, "文件名")
                .defOptionPair("type", true, "[r|w|rw] 读写类型")
                .defOptionSwitch("-fw", false, "是否自动执行flush")
                .parse(args);
        // cmd.usage();  // 显示完整的参数使用说明。（其说明文字来源于上面的各个 addOption）
        // 以上构造了一个命令行对象，之后，可通过如下方式获取各参数的实际输入值
        String filename = cmd.opt("file").value;
        if(cmd.opt("type").is("w")) {
            // do something
        }
        if(cmd.opt("-fw").exist()) {
            // do something
        }
    }
}
```

 