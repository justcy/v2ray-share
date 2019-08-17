package com.justcy.share.domain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.apache.commons.codec.binary.Base64;

import javax.persistence.*;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * vmess 信息
 * 参考：
 * https://www.zfl9.com/ssr.html
 * http://rt.cn2k.net/?p=328
 * https://vxblue.com/archives/115.html
 */
@Entity
@Getter
@Setter
@ToString
// @RequiredArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"id", "address", "port"})
public class V2rayDetailsEntity implements Serializable {
	private static final long serialVersionUID = 952212276705742191L;

	// 必填字段
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Setter
	private long id;

	@Column
	@NonNull
	private String address; // 地址

	@Column
	@NonNull
	private int port;    // 端口

	@Column
	@NonNull
	private String uuid ;  //ID

	@Column
	// @NonNull
	private String host;    // host

	@Column
	// @NonNull
	private String net;    // net

	@Column
	// @NonNull
	private String file;    // file

	@Column
	// @NonNull
	private String path;    // path

	@Column
	// @NonNull
	private int aid;    // aid

	@Column
	// @NonNull
	private String ps;    // ps

	@Column
	// @NonNull
	private String tls;    // tls

	@Column
	// @NonNull
	private int v;    // v
	@Column
	private String type;    // type

	@Column
	private boolean valid;    // 是否有效

	@Column
	private Date validTime;        // 有效性验证时间

	@Column
	private String title;        // 网站名

	public V2rayDetailsEntity(Map<String,Object> v2ray) {
		this.address = (String) v2ray.get("add");
		this.port = Integer.parseInt((String) v2ray.get("port"));
		this.uuid= (String) v2ray.get("id");
		this.aid = (Integer) v2ray.get("aid");
		this.host = (String) v2ray.get("host");
		this.net = (String) v2ray.get("net");
		this.ps = (String) v2ray.get("ps");
		this.type = (String) v2ray.get("type");
		this.host = (String) v2ray.get("host");
		this.path = (String) v2ray.get("path");
		this.tls = (String) v2ray.get("tls");
		this.v = (Integer) v2ray.get("v");
	}

	public String getJsonStr() throws JsonProcessingException {
		Map<String, Object> json = new HashMap<>();
		json.put("add", address);
		json.put("port", port);
		json.put("net", net);
		json.put("ps", ps);
		json.put("id", uuid);
		json.put("aid", aid);
		json.put("type", type);
		json.put("host", host);
		json.put("file", file);
		json.put("path", path);
		json.put("v", v);
		json.put("tls", tls);
		return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(json);
	}


	/**
	 * 生成连接
	 */
	public String getLink() {
		String jsonStr  = "";
		try {
			jsonStr = this.getJsonStr();
		}catch (JsonProcessingException e){

		}
		return "vmess://"+Base64.encodeBase64URLSafeString(jsonStr.getBytes(StandardCharsets.UTF_8));
	}
}
