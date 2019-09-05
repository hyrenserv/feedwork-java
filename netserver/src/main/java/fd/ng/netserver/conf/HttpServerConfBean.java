package fd.ng.netserver.conf;

/**
 * @program: feedwork
 * @description: serverbane
 * @author: xchao
 * @create: 2019-09-03 18:07
 */
public class HttpServerConfBean {
    private String Host;            // 绑定的主机。如果是null或0.0.0.0，绑定到所有接口；
    private int HttpPort;
    private int IdleTimeout;
    private int HttpsPort;
    private String WebContext;
    private String ActionPattern;
    //	// CORS 跨域配置
    private boolean CORS_Allow;
    private String  CORS_acao;  // Access-Control-Allow-Origin
    private String  CORS_acam;  // Access-Control-Allow-Methods
    private String  CORS_acac;  // Access-Control-Allow-Credentials

    // session
    public int Session_MaxAge_Default = 300; // 默认的session过期时间：5分钟。
    // session过期时间，单位是秒。这是指不活动的过期时间，也就是说，如果一直在页面做操作，就一直有效。如果5分钟都没有操作，则失效
    private int Session_MaxAge;
    private boolean Session_HttpOnly; // true：通过程序(JS脚本、Applet等)将无法读取到Cookie信息，这样能有效的防止XSS攻击。

//	// cookie
    private int     Cookie_MaxAge; // cookie过期时间，单位是秒，默认为8小时
    private boolean Cookie_HttpOnly;
    private String  Cookie_Path; // 默认不设置

    public String getHost() {
        return Host;
    }

    public void setHost(String host) {
        Host = host;
    }

    public int getHttpPort() {
        return HttpPort;
    }

    public void setHttpPort(int httpPort) {
        HttpPort = httpPort;
    }

    public int getIdleTimeout() {
        return IdleTimeout;
    }

    public void setIdleTimeout(int idleTimeout) {
        IdleTimeout = idleTimeout;
    }

    public int getHttpsPort() {
        return HttpsPort;
    }

    public void setHttpsPort(int httpsPort) {
        HttpsPort = httpsPort;
    }

    public String getWebContext() {
        return WebContext;
    }

    public void setWebContext(String webContext) {
        WebContext = webContext;
    }

    public String getActionPattern() {
        return ActionPattern;
    }

    public void setActionPattern(String actionPattern) {
        ActionPattern = actionPattern;
    }

    public boolean isCORS_Allow() {
        return CORS_Allow;
    }

    public void setCORS_Allow(boolean CORS_Allow) {
        this.CORS_Allow = CORS_Allow;
    }

    public String getCORS_acao() {
        return CORS_acao;
    }

    public void setCORS_acao(String CORS_acao) {
        this.CORS_acao = CORS_acao;
    }

    public String getCORS_acam() {
        return CORS_acam;
    }

    public void setCORS_acam(String CORS_acam) {
        this.CORS_acam = CORS_acam;
    }

    public String getCORS_acac() {
        return CORS_acac;
    }

    public void setCORS_acac(String CORS_acac) {
        this.CORS_acac = CORS_acac;
    }

    public int getSession_MaxAge_Default() {
        return Session_MaxAge_Default;
    }

    public void setSession_MaxAge_Default(int session_MaxAge_Default) {
        Session_MaxAge_Default = session_MaxAge_Default;
    }

    public int getSession_MaxAge() {
        return Session_MaxAge;
    }

    public void setSession_MaxAge(int session_MaxAge) {
        Session_MaxAge = session_MaxAge;
    }

    public boolean isSession_HttpOnly() {
        return Session_HttpOnly;
    }

    public void setSession_HttpOnly(boolean session_HttpOnly) {
        Session_HttpOnly = session_HttpOnly;
    }

    public int getCookie_MaxAge() {
        return Cookie_MaxAge;
    }

    public void setCookie_MaxAge(int cookie_MaxAge) {
        Cookie_MaxAge = cookie_MaxAge;
    }

    public boolean isCookie_HttpOnly() {
        return Cookie_HttpOnly;
    }

    public void setCookie_HttpOnly(boolean cookie_HttpOnly) {
        Cookie_HttpOnly = cookie_HttpOnly;
    }

    public String getCookie_Path() {
        return Cookie_Path;
    }

    public void setCookie_Path(String cookie_Path) {
        Cookie_Path = cookie_Path;
    }

    public boolean isCookie_Secure() {
        return Cookie_Secure;
    }

    public void setCookie_Secure(boolean cookie_Secure) {
        Cookie_Secure = cookie_Secure;
    }

    public boolean Cookie_Secure;
}
