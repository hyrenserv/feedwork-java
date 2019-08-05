# feedwork

## 功能介绍

创建 Gradle Java 工程，根据项目要求创建 Module，之后，按照下面介绍，创建业务处理来（Action）即可 

## 1. Action

### 1.1 编写 Action 类

- 方式一：继承 AbstractWebappBaseAction  
每个项目首先编写自己用的 Action 父类，并继承自 AbstractWebappBaseAction;  
重新 _doPreProcess 和 _doPostProcess 两个方法，这两个方法在每个Action函数执行前、后被自动调用。一般情况，pre中做session验证，_doPostProcess做清理操作。AbstractWebappBaseAction的_doPostProcess默认完成了DB清理工作。  

- 方式二：使用 @Action 注解  
如果一个包下面有多个Action类，那么需要给 @Action 注解设置 UriExt 值，该值会追加到包名路径的最后。

- 附加说明  
web请求的映射规则为：使用Action类的包名+方法名，作为URL访问路径。 即：   
1）URL最后一个/符号前面为请求路径，用该值去找对应的 Action 类   
2）URL最后一个/符号后面为请求方法，该该值去找对应的 Action 类方法   

### 1.2 编写 Action 方法

按照普通的Java方法编写程序即可，不需要有任何与WEB程序相关的处理代码。  
如果方法内部必须直接操作request等对象，可通过RequestUtil工具类获取。  
所有 public 非 static 的方法，被作为web请求的响应方法。  
如果有同名方法，需要使用 UrlName 注解为其设置别名。  
使用 Dbo 可以直接操作数据库，不需要 try...catch  
代码中如果需要中断方法执行并返回错误信息给前端，可以直接抛出 BusinessException 异常  
分页查询，需创建分页对象并使用相应的分页查询方法，例如：
```
public Map<String, Object> getPagedUserResult(int currPage, int pageSize) {
    Page page = new DefaultPageImpl(currPage, pageSize);
    Result result0 = SqlOperator.queryPagedResult(page,	"select * from XXX");
    Map<String, Object> result = new HashMap<>();
    result.put("totalSize", page.getTotalSize()); // 总记录数
    result.put("pageCount", page.getPageCount()); // 总页数
    result.put("data", result0.toList());
    return result;
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
```
public boolean addUser(String name, int age, String[] favors) {
    ......
}
```
对应的单元测试方法的样板代码：
```
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
```

### 2.2 对于需要登陆认证的测试用例处理方式
```
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

#### - 测试方法中判断是否出现了预期的异常
在测试方法中的最前面使用如下代码即可
```
    G_ExpectedEx.expect(XXXException.class);
    G_ExpectedEx.expectMessage(containsString("XXX"));
```

#### - 可用的注解
- 使用 Timeout 注解，对测试方法判断执行时间是否小于该时间
- 使用 Retry 注解，对测试方法可以进行反复重试，直到成功为止
- 使用 Repeat 注解，可重复多次执行测试方法
- 使用 Parallel 注解，可对单个测试方法设置成并行多次执行

对测试类使用 @RunWith(ExtendBasalRunner.class) 注解（推荐）  
或者，使用 RetryRule 等规则也可以。  

#### - 对多个测试用例类及里面的测试方法设置为并行执行

样例代码如下：
```
public static void main(String[] args) {
    Class[] cls = { TestCase1.class, TestCase2.class };
    Result rt = JUnitCore.runClasses(new ParallelRunner(true, true), cls); // 这两个类及类里面的方法会被并行执行
    // 观察执行结果
    System.out.println("wasSuccessful=" + rt.wasSuccessful() + ", getIgnoreCount=" + rt.getIgnoreCount());
    System.out.println("getRunCount=" + rt.getRunCount() + ", getRunTime=" + rt.getRunTime());
    System.out.println("getFailureCount=" + rt.getFailureCount() + ", getRunTime=" + rt.getFailures().stream().map(Failure::toString).collect(Collectors.joining(" | ")));
}
```

## 3. 搭建新项目的步骤

- 新建 Gradle java 工程（不需要任何和Web相关的插件）
- 给工程新建 Module
- 在工程根目录下创建'libs'目录(含两个子目录runtime,testcase) ，把fdcore的jar文件分别拷贝进去
- 执行自动生成代码的命令
```
java -Dfdconf.dbinfo=./dbinfo.conf -jar fdcmdtools-2.0.jar codegen codedir=代码生成的根目录（需指定到Module的全路径名） basepkg=项目包前缀名(如：hmfms) ftldir=.\template -E
```
- 把该Module目录下自动生成的 build.gradle 拷贝到项目根目录，并清空该Module下build.gradle的内容
- 修改test下的dbinfo.conf（配置DB连接），执行 SqlOperatorTest 看运行状况
- 修改main下的dbinfo.conf（配置DB连接），启动 main.AppMain ，执行 SysParaActionTest 看运行情况（项目库表必须包含sys_para表才能运行）

注意： conf 配置文件中的缩进必须是“2个空格”！

## 4. Howto

### - 文件上传

Action 方法使用UploadFile注解，参数名与上传页面中的 file 元素名对应上，参见如下代码：
```
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
```
@UploadFile
public void upload(String desc, String[] files) throws IOException {
    for(String curFileinfo : files) {
        File uploadedFile = FileUploadUtil.getUploadedFile(curFileinfo);       // 已经上传到服务器的文件
        String orgnFilename = FileUploadUtil.getOriginalFileName(curFileinfo); // 原始文件名
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
```
    <servlet>
      <servlet-name>bizController</servlet-name>
      <servlet-class>fd.ng.web.handler.WebServlet</servlet-class>
    </servlet>

    <servlet-mapping>
        <servlet-name>bizController</servlet-name>
        <url-pattern>/action/*</url-pattern>
    </servlet-mapping>
```
记得要配置成UTF-8，比如tomcat9支持servlet 4了，可以在web.xml增加一句：
```
<request-character-encoding>UTF-8</request-character-encoding>
```