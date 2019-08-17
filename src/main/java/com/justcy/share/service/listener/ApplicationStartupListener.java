package com.justcy.share.service.listener;

import com.justcy.share.service.ShadowSocksCrawlerService;
import com.justcy.share.service.ShadowSocksSerivce;
import com.justcy.share.service.V2rayCrawlerService;
import com.justcy.share.service.V2raySerivce;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * 系统启动 监听事件
 */
@Slf4j
@Component
// @Profile("prod")
public class ApplicationStartupListener {
	@Autowired
	private ShadowSocksSerivce shadowSocksSerivce;

	@Autowired
	private V2raySerivce v2raySerivce;

	@Autowired
	private Set<ShadowSocksCrawlerService> crawlerSet;

	@Autowired
	private Set<V2rayCrawlerService> crawlerV2raySet;


	/**
	 * 系统启动 监听事件
	 */
	@Async
	@EventListener
	public void handleOrderStateChange(ContextRefreshedEvent contextRefreshedEvent) {
//		crawlerSet.parallelStream()/*.filter((service) -> (service instanceof Free_ssCrawlerServiceImpl))*/.forEach((service) -> shadowSocksSerivce.crawlerAndSave(service));
		crawlerV2raySet.parallelStream()/*.filter((service) -> (service instanceof Free_ssCrawlerServiceImpl))*/.forEach((service) -> v2raySerivce.crawlerAndSave(service));
		log.debug("================>{}", "初始扫描完成...");
	}
}
