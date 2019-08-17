package com.justcy.share.service;

import com.google.zxing.NotFoundException;
import com.justcy.share.domain.ShadowSocksEntity;
import com.justcy.share.domain.V2rayEntity;
import com.justcy.share.service.impl.WuwRedCrawlerServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import sun.rmi.runtime.Log;

import java.io.IOException;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Transactional
@Rollback
@ActiveProfiles("dev")
public class V2rayCrawlerServiceTest {
	@Autowired
	@Qualifier("freessCrawlerServiceImpl")
	private V2rayCrawlerService iv2rayCrawlerServiceImpl;    // ishadow
	@Autowired

	@Test
	public void parseURL() throws IOException, NotFoundException {
		String url = "https://v2.freev2ray.org";
		iv2rayCrawlerServiceImpl.parseURL(url);
//		log.debug("====>{}",D);
	}

	// http://i.wuw.red/
	@Test
	public void testFressService() {
		V2rayEntity entity = iv2rayCrawlerServiceImpl.getV2ray();
		log.debug("========>{}", entity);
	}
}
