package com.justcy.share.service.impl.v2ray;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.justcy.share.domain.*;
import com.justcy.share.service.ShadowSocksCrawlerService;
import com.justcy.share.service.ShadowSocksSerivce;
import com.justcy.share.service.V2rayCrawlerService;
import com.justcy.share.service.V2raySerivce;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

/**
 * 1. 爬取 目标网站 SS 信息
 * 2. SS 信息入库
 * 3. 前台页面展示信息
 */
@Slf4j
@Service
public class V2raySerivceImpl implements V2raySerivce {
	@Autowired
	private V2rayRepository v2rayRepository;
	@Autowired
	private V2rayDetailsRepository v2rayDetailsRepository;

	/**
	 * 1. 爬取 SS 并入库
	 * 2. SS 信息入库
	 */
	@Override
	@Transactional
	public void crawlerAndSave(V2rayCrawlerService service) {
		// 1. 爬取 目标网站 SS 信息
		long begin = System.currentTimeMillis();
		V2rayEntity v2rayEntity = service.getV2ray();
		long end = System.currentTimeMillis();
		log.debug("抓取执行时间： {} 毫秒", end - begin);

		// 2. SS 信息入库
		if (v2rayEntity != null) {
			// shadowSocksRepository.deleteByTargetURL(socksEntity.getTargetURL());
			V2rayEntity entity = v2rayRepository.findByTargetURL(v2rayEntity.getTargetURL());
			if (entity != null)
				v2rayRepository.delete(entity);

			v2rayRepository.save(v2rayEntity);
			log.debug("入库执行时间： {} 毫秒", System.currentTimeMillis() - end);
		}
	}

	/**
	 * 3. 查询 SS 信息
	 */
	@Override
	public List<V2rayEntity> findAll(Pageable pageable) {
		Page<V2rayEntity> entities = v2rayRepository.findAll(pageable);
		return entities.getContent();
	}

	/**
	 * 随机查询一条可用 ss 信息
	 */
	@Override
	public V2rayDetailsEntity findFirstByRandom() {
		return v2rayDetailsRepository.findFirstByValidByRandomAsc();
	}

	/**
	 * 3. 生成 SSR 订阅连接
	 * 生成规则：https://github.com/ssrbackup/shadowsocks-rss/wiki/SSR-QRcode-scheme
	 */
	@Override
	public String toVmessLink(List<V2rayEntity> entities, boolean valid) {
		if (!entities.isEmpty()) {
			StringBuilder link = new StringBuilder();
			for (V2rayEntity entity : entities) {
				for (V2rayDetailsEntity detailsEntity : entity.getV2raySet()) {
					if (valid) {
						if (detailsEntity.isValid()) {
							link.append(detailsEntity.getLink()).append("\n");
						}
					} else {
						link.append(detailsEntity.getLink()).append("\n");
					}
					// log.debug("link ------>{}\n{}", detailsEntity, detailsEntity.getLinkNotSafe());
				}
			}
			// log.debug(link.toString());
			return Base64.encodeBase64String(link.toString().getBytes(StandardCharsets.UTF_8));
		}
		return "";
	}

	/**
	 * SS 有效性检查，获取 SS 信息，判断端口有效性，并更新数据
	 */
	@Override
	@Transactional
	public void checkValid() {
		// 查询 一小时前 更新的数据（即：一小时内更新过的数据，不测试有效性）
		Date qDate = DateUtils.addHours(new Date(), -1);
		PageRequest pageRequest = PageRequest.of(0, 10000, Sort.Direction.ASC, "id");
		Page<V2rayDetailsEntity> entityList = v2rayDetailsRepository.findByValidTimeLessThanEqual(qDate, pageRequest);

		for (V2rayDetailsEntity v2rayDetailsEntity : entityList.getContent()) {
			boolean _valid = V2rayCrawlerService.isReachable(v2rayDetailsEntity);
			// 如果检测结果与库中数据 不一致，则更新数据
			if (_valid != v2rayDetailsEntity.isValid()) {
				v2rayDetailsEntity.setValid(_valid);
				v2rayDetailsEntity.setValidTime(new Date());
				v2rayDetailsRepository.save(v2rayDetailsEntity);
			}
		}
	}

	/**
	 * 生成二维码
	 */
	@Override
	public byte[] createQRCodeImage(String text, int width, int height) throws WriterException, IOException {
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

		ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
		MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
		byte[] pngData = pngOutputStream.toByteArray();
		return pngData;
	}
}
