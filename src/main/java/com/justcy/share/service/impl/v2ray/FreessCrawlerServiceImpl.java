package com.justcy.share.service.impl.v2ray;

import com.justcy.share.domain.V2rayDetailsEntity;
import com.justcy.share.service.V2rayCrawlerService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;

/**
 * doub
 * https://doub.io
 */
@Slf4j
@Service
public class FreessCrawlerServiceImpl extends V2rayCrawlerService {

	// 目标网站 URL
	private static final String TARGET_URL = "https://v2.freev2ray.org";

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
		Elements buttons = document.select("button[data-clipboard-text]");

		Set<V2rayDetailsEntity> set = new HashSet<>(buttons.size() - 1);
		for (int i = 0; i < buttons.size(); i++) {
			try {
				Element element = buttons.get(i);
				String vmessLink = element.attr("data-clipboard-text");

				V2rayDetailsEntity v2rayDetailsEntity = parseLink(vmessLink);
				// 测试网络
				if (isReachable(v2rayDetailsEntity))
					v2rayDetailsEntity.setValid(true);

				set.add(v2rayDetailsEntity);

				log.debug("*************** 第 {} 条 ***************{}{}", i + 1, System.lineSeparator(), v2rayDetailsEntity);
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		return set;
	}
	@Override
	protected Connection getConnection(String url) {
		@SuppressWarnings("deprecation")
		Connection connection = Jsoup.connect(url)
				.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.108 Safari/537.36")
				.ignoreContentType(true)
				.followRedirects(true)
				.ignoreHttpErrors(true)
				.validateTLSCertificates(true)
				.timeout(TIME_OUT);
		if (isProxyEnable())
			connection.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(getProxyHost(), getProxyPort())));
		return connection;
	}
	/**
	 * 目标网站 URL
	 */
	@Override
	protected String getTargetURL() {
		return TARGET_URL;
	}
}
