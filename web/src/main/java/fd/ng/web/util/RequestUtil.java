package fd.ng.web.util;

import com.alibaba.fastjson.JSONArray;
import fd.ng.core.annotation.Param;
import fd.ng.core.exception.internal.FrameworkRuntimeException;
import fd.ng.core.exception.internal.RawlayerRuntimeException;
import fd.ng.core.utils.*;
import fd.ng.web.conf.WebinfoConf;
import fd.ng.web.helper.HttpDataHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RequestUtil {
	private static final Logger logger = LogManager.getLogger(RequestUtil.class.getName());

	private RequestUtil() {
		throw new AssertionError("No RequestUtil instances for you!");
	}

	// 存储每个javabean的属性信息。key为bean的class，value为属性信息Map。该Map的key为属性名字，value为属性的Field对象
	private static final Map<Class<?>, Map<String, Field>> _beanInfoBox = new ConcurrentHashMap<>();

	/**
	 * 从 request 中，构造bean对象。
	 * bena对象中的属性只支持主类型和JavaBean对象，不支持集合类型（比如List, Map)
	 *
	 * @param request
	 * @param beanClass
	 * @param <T>
	 * @return
	 */
	public static <T> T buildBeanFrom(final HttpServletRequest request, final Class<T> beanClass, final String para) {
		String json = request.getParameter(para);
		Map<String, Object>[] parameterMap = JsonUtil.toObjectSafety(json, Map[].class).get();
		T paramsValueArry = (T) Array.newInstance(beanClass, parameterMap.length);
		for (int i = 0; i < parameterMap.length; i++) {
			Map<String, Object> stringMap = parameterMap[i];
			Map<String, String[]> valueMap = new HashMap<>();
			for (String key : stringMap.keySet()) {
				Object obj = stringMap.get(key);
				if (obj == null) continue;
				valueMap.put(key, JsonUtil.toJsonString(obj));
			}
			T paramsValue = buildBeanFromRequest(valueMap, beanClass);
			Array.set(paramsValueArry, i, paramsValue);
		}
		return paramsValueArry;
	}

	public static <T> T buildBeanFromRequest(final HttpServletRequest request, final Class<T> beanClass) {
		//request.getParameter()
		return buildBeanFromRequest(request.getParameterMap(), beanClass);
	}

	public static <T> T buildBeanFromRequest(final Map<String, String[]> parameterMap, final Class<T> beanClass) {
		try {
//			Map<String, Field> beanFields = BeanUtil.getAllVisibleFields(beanClass);
			Map<String, Field> beanFields = _beanInfoBox.get(beanClass);
			if (beanFields == null) {
				final Map<String, Field> tmpBeanFields = new HashMap<>();
				ClassUtil.getAllVisibleFields(beanClass).forEach(field -> {
					if (field != null) {
						Annotation[] annos = field.getDeclaredAnnotations();
						if (!ArrayUtil.isEmpty(annos)) // 只缓存定义了注解的属性
							tmpBeanFields.put(field.getName(), field);
					}
				});
				beanFields = Collections.unmodifiableMap(tmpBeanFields);
				_beanInfoBox.putIfAbsent(beanClass, beanFields);
			}

			T bean = beanClass.newInstance();
			BeanInfo beanInfo = Introspector.getBeanInfo(beanClass);
			PropertyDescriptor[] beanPropArr = beanInfo.getPropertyDescriptors();
			for (PropertyDescriptor beanProp : beanPropArr) {
				Class<?> propType = beanProp.getPropertyType(); // 该bean的属性的类型: String, int, 等等
				if (Class.class.isAssignableFrom(propType)) continue;
				Method propSetter = beanProp.getWriteMethod();
				if (propSetter == null) continue; // 没有 set 方法就无法为其赋值，所以没必要继续了

				String propName = beanProp.getName();           // 该bean的属性名字
				String reqFieldName = propName;  // 默认使用属性名作为request中的参数名
				// 获取bean属性对象的注解信息
				Field field = beanFields.get(propName);
				if (field != null) {
					Param propAnno = field.getAnnotation(Param.class);
					if (propAnno != null) {
						if (propAnno.isBean()) {// 该属性被被定义为RequestBean
							Object innerBean = buildBeanFromRequest(parameterMap, propType);
							propSetter.invoke(bean, innerBean);
							continue;
						} else {
							String alias = propAnno.alias();
							if (StringUtil.isNotEmpty(alias))
								reqFieldName = alias;
						}
					}
				}

				String[] valArr = parameterMap.get(reqFieldName);   // 用当前属性名字到request中取值
				if (valArr == null) continue; // 该属性不在 request 中

				try {
					Object val = BeanUtil.castStringToClass(valArr, propType);
					propSetter.invoke(bean, val);
				} catch (BeanUtil.ArgumentNullvalueException e) {
					throw new FrameworkRuntimeException(String.format("argument:%s(%s) must not be null!", propName, propType.getSimpleName()));
				} catch (BeanUtil.ArgumentUnsupportedTypeException e) {
					throw new FrameworkRuntimeException(String.format("argument:%s 's type:%s is Unsupported!", propName, propType.getSimpleName()));
				}
			}
			return bean;
		} catch (IllegalAccessException | InstantiationException | IntrospectionException | InvocationTargetException e) {
			throw new FrameworkRuntimeException(String.format("create bean [%s] instance fail!", beanClass.getName()), e);
		}
	}

	/**
	 * 唯一代表一次HTTP请求的标识 id
	 *
	 * @return String
	 */
	public static String getBizId() {
		return HttpDataHolder.getBizId();
	}

	public static String getJson() {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(getRequest().getInputStream(), CodecUtil.UTF8_CHARSET))) {
			StringBuilder content = new StringBuilder();
			String line;
			while ((line = br.readLine()) != null) {
				content.append(line);
			}
			return content.toString();
		} catch (IOException e) {
			throw new RawlayerRuntimeException("System Error! bizid=" + getBizId(), e);
		}
	}

	// ----------------------- request, session, cookie 工具方法 -----------------------

	public static HttpServletRequest getRequest() {
		return HttpDataHolder.getRequest();
	}

	public static HttpSession getSession() {
		return getRequest().getSession();
	}

	/**
	 * 从session中按照名字取值后自动以原始值的类型造型并返回。
	 * 只能造型成与存入值一直的类型，比如：存入了数字[123] ，那么只能取出数字，不能造型成字符串返回。
	 * <p>
	 * 如果存入了主类型（比如int，long，double等），那么取值时为了避免null无法造型成主类型，应该使用主类型对象：
	 * Long id = RequestUtil.getSessValue("id");
	 *
	 * @param name 获取 session 数据的的key
	 * @param <T>
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getSessValue(String name) {
		return (T) getSession().getAttribute(name);
	}

	public static void putSessValue(String name, Object value) {
		getSession().setAttribute(name, value);
	}

	// ----------------------- Cookie 处理
	public static void putCookieObject(String name, Object value) {
		try {
			String strValue = value == null ? StringUtil.EMPTY : URLEncoder.encode(JsonUtil.toJson(value), StringUtil.UTF_8);

			Cookie cookie = new Cookie(name, strValue);
			if (logger.isTraceEnabled())
				logger.trace("orgn : path={}, domain={}, secure={}, ver={}, httponly={}"
						, cookie.getPath(), cookie.getDomain(), cookie.getSecure(), cookie.getVersion(), cookie.isHttpOnly());
			cookie.setMaxAge(WebinfoConf.Cookie_MaxAge); // 设置为0，即为删除
			//设置路径，这个路径即该工程下都可以访问该cookie 如果不设置路径，那么只有设置该cookie路径及其子路径可以访问
			cookie.setPath(WebinfoConf.Cookie_Path);
			cookie.setHttpOnly(WebinfoConf.Cookie_HttpOnly);
			cookie.setSecure(WebinfoConf.Cookie_Secure);
			if (logger.isTraceEnabled())
				logger.trace("now  : path={}, domain={}, secure={}, ver={}, httponly={}"
						, cookie.getPath(), cookie.getDomain(), cookie.getSecure(), cookie.getVersion(), cookie.isHttpOnly());
			HttpDataHolder.getResponse().addCookie(cookie);
		} catch (UnsupportedEncodingException e) {
			throw new RawlayerRuntimeException(e);
		}
	}

	public static <T> T getCookieObject(String name, Class<T> type) {
		if (name == null) return null;
		try {
			Cookie[] cookieArray = getRequest().getCookies();
			if (ArrayUtil.isNotEmpty(cookieArray)) {
				for (Cookie cookie : cookieArray) {
					if (name.equals(cookie.getName())) {
						String val0 = cookie.getValue();
						String val1 = URLDecoder.decode(val0, StringUtil.UTF_8);
						return JsonUtil.toObject(val1, type);
//						return (T) URLDecoder.decode(cookie.getValue(), StringUtil.UTF_8);
					}
				}
			}
			return null;
		} catch (UnsupportedEncodingException e) {
			throw new RawlayerRuntimeException(e);
		}
	}

	public static <T> T getCookieObject(String name, Type type) {
		if (name == null) return null;
		try {
			Cookie[] cookieArray = getRequest().getCookies();
			if (ArrayUtil.isNotEmpty(cookieArray)) {
				for (Cookie cookie : cookieArray) {
					if (name.equals(cookie.getName())) {
						String val0 = cookie.getValue();
						String val1 = URLDecoder.decode(val0, StringUtil.UTF_8);
						return JsonUtil.toObject(val1, type);
//						return (T) URLDecoder.decode(cookie.getValue(), StringUtil.UTF_8);
					}
				}
			}
			return null;
		} catch (UnsupportedEncodingException e) {
			throw new RawlayerRuntimeException(e);
		}
	}
}
