package fd.ng.netclient.http;

import okhttp3.MediaType;

/**
 * 支持四种常见的 POST 数据提交方式
 * application/x-www-form-urlencoded （默认）
 * multipart/form-data
 * application/json
 * text/xml
 */
public enum SubmitMediaType {
	FORM{
		@Override
		MediaType mediaType() { return null; }
	},
	JSON{
		@Override
		MediaType mediaType() { return JSON_MEDIATYPE; }
	},
	MULTIPART{
		@Override
		MediaType mediaType() { return MULTIPART_MEDIATYPE; }
	},
	XML{
		@Override
		MediaType mediaType() { return XML_MEDIATYPE; }
	};

	// FORM : application/x-www-form-urlencoded
	private static final MediaType JSON_MEDIATYPE = MediaType.parse("application/json; charset=utf-8");
	private static final MediaType MULTIPART_MEDIATYPE = MediaType.parse("multipart/form-data");
	private static final MediaType XML_MEDIATYPE = MediaType.parse("text/xml");

	abstract MediaType mediaType();
}
