package com.justcy.share.service.impl.v2ray;

import com.justcy.share.domain.ShadowSocksDetailsEntity;
import com.justcy.share.domain.V2rayDetailsEntity;
import com.justcy.share.service.V2rayCrawlerService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * doub
 * https://doub.io
 */
@Slf4j
@Service
public class NetlifyCrawlerServiceImpl extends V2rayCrawlerService {

	// 目标网站 URL
	private static final String TARGET_URL = "https://practical-ptolemy-8af3ec.netlify.com";

	// 协议
	private final static Map<String, String> protocolMap = new HashMap<>();

	// 访问目标网站，是否启动代理
	@Value("${proxy.enable}")
	@Getter
	private boolean proxyEnable;
	// 代理地址
	@Getter
	@Value("${proxy.host}")
	private String proxyHost;
	// 代理端口
	@Getter
	@Value("${proxy.port}")
	private int proxyPort;

	/**
	 * 网页内容解析 信息
	 */
	@Override
	protected Set<V2rayDetailsEntity> parse(Document document) {
		//v2ray 订阅地址内容
		String base64ssrLinks = document.text();
		String vmessLinks = StringUtils.toEncodedString(Base64.decodeBase64(base64ssrLinks), StandardCharsets.UTF_8);
		String[] vmessLinkList = vmessLinks.split("\n");

		Set<V2rayDetailsEntity> set = Collections.synchronizedSet(new HashSet<>(vmessLinkList.length));

		Arrays.asList(vmessLinkList).parallelStream().forEach((str) -> {
			try {
				if (StringUtils.isNotBlank(str)) {
					V2rayDetailsEntity vmess = parseLink(str.trim());
					vmess.setValidTime(new Date());

					// 测试网络
					if (isReachable(vmess))
						vmess.setValid(true);
					// 无论是否可用都入库
					set.add(vmess);

					log.debug("*************** 第 {} 条 ***************{}{}", set.size(), System.lineSeparator(), vmess);
					// log.debug("{}", ss.getLink());
				}
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		});

		return set;
	}
	/**
	 * 目标网站 URL
	 */
	@Override
	protected String getTargetURL() {
		return TARGET_URL;
	}
}
