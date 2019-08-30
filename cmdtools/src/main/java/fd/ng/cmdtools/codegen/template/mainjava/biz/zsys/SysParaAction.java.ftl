package ${basePackage}.${subPackage};

import fd.ng.core.utils.StringUtil;
import fd.ng.db.jdbc.DefaultPageImpl;
import fd.ng.db.jdbc.Page;
import fd.ng.db.resultset.Result;
import fd.ng.web.annotation.RequestBean;
import fd.ng.web.annotation.RequestParam;
import fd.ng.web.util.Dbo;

import ${basePackage}.biz.zbase.WebappBaseAction;
import ${basePackage}.entity.SysPara;
import ${basePackage}.exception.ExceptionEnum;
import ${basePackage}.exception.BusinessException;
import ${basePackage}.exception.AppSystemException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;

public class SysParaAction extends WebappBaseAction {
	private static final Logger logger = LogManager.getLogger();

	public void add(@RequestBean SysPara sysPara) {
		if(sysPara.add(Dbo.db())!=1)
			throw new BusinessException(ExceptionEnum.DATA_ADD_ERROR);
	}

	public void update(String name, String value) {
		OptionalLong result = Dbo.queryNumber(
				"select count(1) from "+ SysPara.TableName+" where para_name=?", name);
		long val = result.orElseThrow(() -> new AppSystemException("Error select-count"));
		if(val!=1L)
			throw new BusinessException(ExceptionEnum.DATA_NOT_EXIST);
		SysPara sysPara = new SysPara();
		sysPara.setPara_name(name);
		sysPara.setPara_value(value);
		if(sysPara.update(Dbo.db())!=1)
			throw new BusinessException(ExceptionEnum.DATA_UPDATE_ERROR);
	}

	public void delete(String name) {
		int nums = Dbo.execute("delete from " + SysPara.TableName + " where para_name=?", name);
		if(nums!=1) {
			if (nums == 0)
				throw new BusinessException(ExceptionEnum.OTHER_ERROR.getCode(), "没有数据被删除！para_name="+name);
			else
				throw new BusinessException(ExceptionEnum.DATA_DELETE_ERROR);
		}
	}

	public Map<String, Object> getPagedDataList(int currPage, int pageSize, @RequestParam(nullable = true) String value) {
		Page page = new DefaultPageImpl(currPage, pageSize);
		Result rs;
		if(StringUtil.isNotEmpty(value))
			rs = Dbo.queryPagedResult(page,
					"select * from " + SysPara.TableName + " where para_value like ?", "%" + value + "%");
		else
			rs = Dbo.queryPagedResult(page, "select * from " + SysPara.TableName);

		Map<String, Object> result = new HashMap<>();
		result.put("count", page.getTotalSize());
		result.put("sysparaList", rs.toList());
		return result;
	}
}
