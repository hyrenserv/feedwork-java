package fd.ng.web.action;

public enum ActionResultEnum {
	SUCCESS(200, "OK"),
	BIZ_ERROR(220, "Business Exception"),
	UNAUTHORIZED_ERROR(401, "Unauthorized"),
	FORBIDDEN_ERROR(403, "Forbidden"),
	NOTFOUND_ERROR(404, "Not Found"),
	SYSTEM_ERROR(500, "System Internal Error");

	private Integer code;
	private String message;

	ActionResultEnum(Integer code, String message) {
		this.code = code;
		this.message = message;
	}

	public Integer getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}
}
