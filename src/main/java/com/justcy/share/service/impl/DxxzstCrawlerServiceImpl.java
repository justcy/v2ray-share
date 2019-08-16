package com.justcy.share.service.impl;

import com.justcy.share.domain.ShadowSocksDetailsEntity;
import com.justcy.share.service.ShadowSocksCrawlerService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * https://github.com/dxxzst/Free-SS-SSR
 */
@Slf4j
@Service
public class DxxzstCrawlerServiceImpl extends ShadowSocksCrawlerService {

	// 目标网站 URL
	private static final String TARGET_URL = "https://github.com/dxxzst/Free-SS-SSR";

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
		Elements ssList = document.select("td[align]:contains(ssr:)");

		Set<ShadowSocksDetailsEntity> set = new HashSet<>(ssList.size());

		for (int i = 0; i < ssList.size(); i++) {
			try {
				Element element = ssList.get(i);
				// 取 a 信息，为 ss 信息
				String ssURL = element.text();

				ShadowSocksDetailsEntity ss = parseLink(ssURL);
				ss.setValid(false);
				ss.setValidTime(new Date());
				ss.setTitle(document.title());
				ss.setRemarks("justcy_D");
				ss.setGroup("Dxxzst");

				// 测试网络
				if (isReachable(ss))
					ss.setValid(true);

				// 无论是否可用都入库
				set.add(ss);

				log.debug("*************** 第 {} 条 ***************{}{}", i + 1, System.lineSeparator(), ss);
				// log.debug("{}", ss.getLink());
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
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
