package com.justcy.share.service.impl;

import com.justcy.share.domain.ShadowSocksDetailsEntity;
import com.justcy.share.service.ShadowSocksCrawlerService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * https://www.liesauer.net/yogurt/subscribe?ACCESS_TOKEN=DAYxR3mMaZAsaqUb
 */
@Slf4j
@Service
public class LiesauerCrawlerServiceImpl extends ShadowSocksCrawlerService {

	// 目标网站 URL
	private static final String TARGET_URL = "https://www.liesauer.net/yogurt/subscribe?ACCESS_TOKEN=DAYxR3mMaZAsaqUb";

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


	// 解析 连接方式
	@Override
	protected Set<ShadowSocksDetailsEntity> parse(Document document) {

		// SSR 订阅地址内容
		String base64ssrLinks = document.text();
		String ssrLinks = StringUtils.toEncodedString(Base64.decodeBase64(base64ssrLinks), StandardCharsets.UTF_8);
		String[] ssrLinkList = ssrLinks.split("\n");

		// log.debug("---------------->{}={}", ssrLinkList.length + "", ssrLinkList);
		Set<ShadowSocksDetailsEntity> set = Collections.synchronizedSet(new HashSet<>(ssrLinkList.length));

		Arrays.asList(ssrLinkList).parallelStream().forEach((str) -> {
			try {
				if (StringUtils.isNotBlank(str)) {
					ShadowSocksDetailsEntity ss = parseLink(str.trim());
					ss.setValid(false);
					ss.setValidTime(new Date());
					ss.setTitle("免费账号 | 云端框架");
					ss.setRemarks(ss.getServer()+":"+ss.getServer_port()+"(liesauser)");
					ss.setGroup("liesauser");

					// 测试网络
					if (isReachable(ss))
						ss.setValid(true);

					// 无论是否可用都入库
					set.add(ss);

					log.debug("*************** 第 {} 条 ***************{}{}", set.size(), System.lineSeparator(), ss);
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
