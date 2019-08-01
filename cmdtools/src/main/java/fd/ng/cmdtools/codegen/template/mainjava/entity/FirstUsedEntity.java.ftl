package ${basePackage}.${subPackage};

import fd.ng.db.entity.TableEntity;
import fd.ng.db.entity.anno.Table;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

<#assign varTableName="${tableName?lower_case}">
@Table(tableName = "${varTableName}")
public class ${className} extends TableEntity {
    private static final long serialVersionUID = 1L;
    public static final String TableName = "${varTableName}";

<#-- 【设置属性】 不做驼峰转换，使用原始名字，但是必须小写。因为JavaBean属性名要求：前两个字母要么都大写，要么都小写 -->
<#list columnMetaList as columnInfo>
	private ${columnInfo.typeOfJava} ${columnInfo.name?lower_case};
</#list>

<#-- 方法 -->
<#list columnMetaList as columnInfo>
	public ${columnInfo.typeOfJava} get${columnInfo.name?lower_case?cap_first}() { return ${columnInfo.name?lower_case}; }
	public void set${columnInfo.name?lower_case?cap_first}(${columnInfo.typeOfJava} ${columnInfo.name?lower_case}) {
		if(${columnInfo.name?lower_case}==null) addNullValueField("${columnInfo.name?lower_case}");
		this.${columnInfo.name?lower_case} = ${columnInfo.name?lower_case};
	}

</#list>
}