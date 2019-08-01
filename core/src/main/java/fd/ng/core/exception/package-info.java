/**
 * 所有异常的构造函数的用法，先看 RevitalizedCheckedException
 * 1）顶级根类：RevitalizedCheckedException
 * 一般不需要直接继承本类，它是对 RuntimeException 的封装，用于把原始异常信息嵌套进来。
 *
 * 2）一级根类：BaseInternalRuntimeException
 * 提供了自动生成错误代码的能力，该错误代码一般被返回前端，并打印到日志中，用于方便查找定位
 * 项目开发中，如果需要定制自己的异常，建议使用本类作为父类。
 *
 * 3）fdcore内部使用的两个异常，在internal包里面。不建议直接使用，不建议继承使用。
 *
 * 4）给项目提供的两个业务处理异常
 * 4.1）BusinessProcessException
 *      用于中断业务处理流程并返回信息给前端。
 *      各个项目可继承它，创建自用的异常（比如旧版fdcore里面的 BusinessPException）
 *
 * 4.2）BusinessSystemException
 *      用于业务层catch到的系统内部异常的包装后抛出。
 *      各个项目可继承它，创建自用的异常（比如旧版fdcore里面的 AppSystemPException）
 *
 * 如果这两个不够用，如果需要定制自己的异常，建议继承BaseInternalRuntimeException作为父类。
 *
 */
package fd.ng.core.exception;