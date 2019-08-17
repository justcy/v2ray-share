package com.justcy.share.service;

import com.alibaba.fastjson.JSON;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.justcy.share.domain.V2rayDetailsEntity;
import com.justcy.share.domain.V2rayEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * SSR 爬虫 抽象类
 * 本类只负责 爬取 SS 信息，入库操作由 ShadowSocksSerivce 处理
 * 1. 请求 目标地址 获取 Document
 * 2. 构造 ShadowSocksEntity 对象
 * 3. 解析 Document 构造 ShadowSocksSet
 */
@Slf4j
public abstract class V2rayCrawlerService {
	// 目标网站请求超时时间（60 秒）
	protected static final int TIME_OUT = 60 * 1000;
	// 测试网络超时时间（3 秒）
	protected static final int SOCKET_TIME_OUT = 300;

	/**
	 * 网络连通性测试
	 */
	public static boolean isReachable(V2rayDetailsEntity v2ray) {
		try (Socket socket = new Socket()) {
			socket.connect(new InetSocketAddress(v2ray.getAddress(), v2ray.getPort()), SOCKET_TIME_OUT);
			return true;
		} catch (IOException e) {
		}
		return false;
	}


	/**
	 * 请求目标 URL 获取 Document
	 */
	protected Document getDocument() throws IOException {
		Document document;
		try {
			document = getConnection(getTargetURL()).get();
		} catch (IOException e) {
			throw new IOException("请求[" + getTargetURL() + "]异常：" + e.getMessage(), e);
		}
		return document;
	}

	protected Connection getConnection(String url) {
		@SuppressWarnings("deprecation")
		Connection connection = Jsoup.connect(url)
				.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.108 Safari/537.36")
//				.referrer("https://www.google.com/")
				.ignoreContentType(true)
				.followRedirects(true)
				.ignoreHttpErrors(true)
				.validateTLSCertificates(false)
				.timeout(TIME_OUT);
		if (isProxyEnable())
			connection.proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(getProxyHost(), getProxyPort())));
		return connection;
	}

	/**
	 * 爬取 ss 账号
	 */
	public V2rayEntity getV2ray() {
		try {
			Document document = getDocument();
			V2rayEntity entity = new V2rayEntity(getTargetURL(), document.title(), true, new Date());
			entity.setV2raySet(parse(document));
			return entity;
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return new V2rayEntity(getTargetURL(), "", false, new Date());
	}

	/**
	 * 连接解析
	 */
	protected V2rayDetailsEntity parseLink(String link) {
		if (StringUtils.isNotBlank(link) && StringUtils.startsWithIgnoreCase(link, "vmess")) {
			String v2rayInfoStr = new String(Base64.decodeBase64(StringUtils.remove(link, "vmess://").getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
			try {
				Map maps = (Map) JSON.parse(v2rayInfoStr.trim());
				V2rayDetailsEntity enity = new V2rayDetailsEntity(maps);
				return enity;
			} catch (Exception e) {
				throw new RuntimeException("Vmess 连接[" + v2rayInfoStr + "]解析异常：" + e.getMessage(), e);
			}
		} else {
			throw new IllegalArgumentException("Vmess 连接[" + link + "]解析异常：协议类型错误");
		}
	}

	/**
	 * 图片解析
	 */
	protected V2rayDetailsEntity parseURL(String imgURL) throws IOException, NotFoundException {
		Connection.Response resultImageResponse = getConnection(imgURL).execute();

		Map<DecodeHintType, Object> hints = new LinkedHashMap<>();
		// 解码设置编码方式为：utf-8，
		hints.put(DecodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
		//优化精度
		hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
		//复杂模式，开启PURE_BARCODE模式
		hints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);

		try (BufferedInputStream bytes = resultImageResponse.bodyStream()) {
			BufferedImage image = ImageIO.read(bytes);
			Binarizer binarizer = new HybridBinarizer(new BufferedImageLuminanceSource(image));
			BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
			Result res = new MultiFormatReader().decode(binaryBitmap, hints);
			return parseLink(res.toString());
		}
	}

	/**
	 * 图片解析
	 */
	protected V2rayDetailsEntity parseImg(String imgURL) throws IOException, NotFoundException {
		String str = StringUtils.removeFirst(imgURL, "data:image/png;base64,");

		Map<DecodeHintType, Object> hints = new LinkedHashMap<>();
		// 解码设置编码方式为：utf-8，
		hints.put(DecodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
		//优化精度
		hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
		//复杂模式，开启PURE_BARCODE模式
		hints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);

		try (ByteArrayInputStream bis = new ByteArrayInputStream(Base64.decodeBase64(str))) {
			BufferedImage image = ImageIO.read(bis);
			Binarizer binarizer = new HybridBinarizer(new BufferedImageLuminanceSource(image));
			BinaryBitmap binaryBitmap = new BinaryBitmap(binarizer);
			Result res = new MultiFormatReader().decode(binaryBitmap, hints);
			return parseLink(res.toString());
		}
	}

	/**
	 * 网页内容解析
	 */
	protected abstract Set<V2rayDetailsEntity> parse(Document document);

	/**
	 * 目标网站 URL
	 */
	protected abstract String getTargetURL();

	/**
	 * 访问目标网站，是否启动代理
	 */
	protected abstract boolean isProxyEnable();

	/**
	 * 代理地址
	 */
	protected abstract String getProxyHost();

	/**
	 * 代理端口
	 */
	protected abstract int getProxyPort();
}
