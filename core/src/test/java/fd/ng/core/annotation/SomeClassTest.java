package fd.ng.core.annotation;

import fd.ng.core.utils.StringUtil;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNull;

public class SomeClassTest {
	@Test
	public void testParam() throws IllegalAccessException, InstantiationException {
		Method[] methods = SomeClass.class.getDeclaredMethods();
		for(Method method : methods) {
			if(method.getName().equalsIgnoreCase("say1")) {
				Params paramsAnno = method.getAnnotation(Params.class);
				Param paramAnno = method.getAnnotation(Param.class);

				assertThat(paramsAnno.value().length, Matchers.is(3));
				for(Param param : paramsAnno.value())
					assertThat(param.name(), Matchers.anyOf(is("age"), is("favor"), is("addr")));
				assertNull(paramAnno);
			}

			if(method.getName().equalsIgnoreCase("say2")) {
				Params paramsAnno = method.getAnnotation(Params.class);
				Param paramAnno = method.getAnnotation(Param.class);
				assertNull(paramsAnno);
				assertNull(paramAnno);
			}

			if(method.getName().equalsIgnoreCase("say3")) {
				Params paramsAnno = method.getAnnotation(Params.class);
				Param paramAnno = method.getAnnotation(Param.class);
				assertNull(paramsAnno);
				assertThat(paramAnno.name(), is(StringUtil.EMPTY));
				assertThat(paramAnno.isBean(), is(false));
				assertThat(paramAnno.desc(), is("say3:测试仅有一个注解的情况"));
				assertThat(paramAnno.range(), is("say3"));
			}

			if(method.getName().equalsIgnoreCase("say4")) {
				Params paramsAnno = method.getAnnotation(Params.class);
				Param paramAnno = method.getAnnotation(Param.class);
				assertNull(paramsAnno);
				assertThat(paramAnno.name(), is(StringUtil.EMPTY));
				assertThat(paramAnno.isBean(), is(true));
				assertThat(paramAnno.desc(), is("say4:测试仅有一个注解的情况"));
				assertThat(paramAnno.range(), is("say4"));
			}
		}
	}
}
