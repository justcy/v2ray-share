package com.justcy.share.domain;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * 数据库操作
 */
@Repository
public interface V2rayDetailsRepository extends JpaRepository<V2rayDetailsEntity, Long> {

	/**
	 * 随机查询一条可用 ss 信息
	 */
	@Query(value = "SELECT * FROM V2RAY_DETAILS_ENTITY where valid = true ORDER BY RAND() limit 1", nativeQuery = true)
	V2rayDetailsEntity findFirstByValidByRandomAsc();

	/**
	 * <= validTime
	 */
	Page<V2rayDetailsEntity> findByValidTimeLessThanEqual(Date validTime, Pageable pageable);
}
