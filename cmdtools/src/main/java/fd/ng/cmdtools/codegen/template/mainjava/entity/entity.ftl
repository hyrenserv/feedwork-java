package ${basePackage}.${subPackage};

import fd.ng.db.entity.TableEntity;
import fd.ng.db.entity.anno.Column;
import fd.ng.db.entity.anno.Table;
import ${basePackage}.exception.BusinessException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 实体类中所有属性都应定义为对象，不要使用int等主类型，方便对null值的操作
 */
<#assign varTableName="${tableName?lower_case}">
@Table(tableName = "${varTableName}")
public class ${className} extends TableEntity {
<#if serialVersionUID?exists>
    private static final long serialVersionUID = ${serialVersionUID}L;
</#if>
	private transient static final Set<String> __PrimaryKeys;
	public static final String TableName = "${varTableName}";

<#-- 【设置主键清单】 -->
	static {
<#if pkNameList?exists && (pkNameList?size>0) >
		Set<String> __tmpPKS = new HashSet<>();
<#list pkNameList as pkName>
		__tmpPKS.add("${pkName?lower_case}");
</#list>
		__PrimaryKeys = Collections.unmodifiableSet(__tmpPKS);
<#else>
		__PrimaryKeys = Collections.emptySet();
</#if>
	}
	/**
	 * 检查给定的名字，是否为主键中的字段
	 * @param name String 检验是否为主键的名字
	 * @return
	 */
	public static boolean isPrimaryKey(String name) { return __PrimaryKeys.contains(name); }
	public static Set<String> getPrimaryKeyNames() { return __PrimaryKeys; }

<#-- 【设置属性】 不做驼峰转换，使用原始名字，但是必须小写。因为JavaBean属性名要求：前两个字母要么都大写，要么都小写 -->
<#list columnMetaList as columnInfo>
	private ${columnInfo.typeOfJava} ${columnInfo.name?lower_case};
</#list>

<#-- 方法 -->
<#list columnMetaList as columnInfo>
	public ${columnInfo.typeOfJava} get${columnInfo.name?lower_case?cap_first}() { return ${columnInfo.name?lower_case}; }
	public void set${columnInfo.name?lower_case?cap_first}(${columnInfo.typeOfJava} ${columnInfo.name?lower_case}) {
<#if columnInfo.nullable >
		if(${columnInfo.name?lower_case}==null) addNullValueField("${columnInfo.name?lower_case}");
<#else>
		if(${columnInfo.name?lower_case}==null) throw new BusinessException("Entity : ${className}.${columnInfo.name?lower_case} must not null!");
</#if>
		this.${columnInfo.name?lower_case} = ${columnInfo.name?lower_case};
	}

</#list>
}