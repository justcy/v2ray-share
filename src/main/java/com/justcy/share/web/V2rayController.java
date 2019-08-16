package com.justcy.share.web;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.zxing.WriterException;
import com.justcy.share.domain.ShadowSocksDetailsEntity;
import com.justcy.share.domain.ShadowSocksEntity;
import com.justcy.share.domain.V2rayDetailsEntity;
import com.justcy.share.domain.V2rayEntity;
import com.justcy.share.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
@RequestMapping("/v2ray")
public class V2rayController {
	@Autowired
	private V2raySerivce v2rayServiceImpl;
	@Autowired
	private CountSerivce countSerivce;
	@Autowired
	private ApplicationContext appContext;

	/**
	 * 首页
	 */
	@RequestMapping("/")
	public String index(@PageableDefault(page = 0, size = 50, sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable, Model model) {
		List<V2rayEntity> v2rayList = v2rayServiceImpl.findAll(pageable);
		List<V2rayDetailsEntity> v2raydList = new ArrayList<>();
		for (V2rayEntity v2ray : v2rayList) {
			v2raydList.addAll(v2ray.getV2raySet());
		}
		// ssr 信息
		model.addAttribute("v2rayList", v2rayList);
		// ssr 明细信息，随机排序
		Collections.shuffle(v2raydList);
		model.addAttribute("v2raydList", v2raydList);
		return "vmess";
	}

	/**
	 * SSR 订阅地址
	 */
	@RequestMapping("/subscribe")
	@ResponseBody
	public ResponseEntity<String> subscribe(boolean valid, @PageableDefault(page = 0, size = 1000, sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable) {
		List<V2rayEntity> vmessList = v2rayServiceImpl.findAll(pageable);
		return ResponseEntity.ok()
				.contentType(MediaType.TEXT_PLAIN)
				.body(v2rayServiceImpl.toVmessLink(vmessList, valid));
	}

	/**
	 * 订阅 一条 有效的 Json
	 */
	@RequestMapping("/subscribeJson")
	@ResponseBody
	public ResponseEntity<String> subscribeJson() throws JsonProcessingException {
		return ResponseEntity.ok()
				.contentType(MediaType.TEXT_PLAIN)
				.body(v2rayServiceImpl.findFirstByRandom().getJsonStr());
	}

	/**
	 * 二维码
	 */
	@RequestMapping(value = "/createQRCode")
	@ResponseBody
	public ResponseEntity<byte[]> createQRCode(long id, String text, int width, int height, WebRequest request) throws IOException, WriterException {
		// 缓存未失效时直接返回
		if (request.checkNotModified(id))
			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType(MediaType.IMAGE_PNG_VALUE))
					.cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
					.eTag(String.valueOf(id))
					.body(null);
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(MediaType.IMAGE_PNG_VALUE))
				.cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS))
				.eTag(String.valueOf(id))
				.body(v2rayServiceImpl.createQRCodeImage(text, width, height));
	}

	@RequestMapping(value = "/count")
	@ResponseBody
	public ResponseEntity<String> count() {
		return ResponseEntity.ok().body(String.valueOf(countSerivce.get()));
	}

	@RequestMapping(value = "/run")
	@ResponseBody
	public ResponseEntity<String> run(String name) {
		if (StringUtils.isNotBlank(name)) {
			Object bean = appContext.getBean(name);

			if (bean instanceof ShadowSocksCrawlerService) {
				v2rayServiceImpl.crawlerAndSave(V2rayCrawlerService.class.cast(bean));
			}
		}
		return ResponseEntity.ok().body("OK...");
	}
}
