package com.justcy.share.domain;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 数据库操作
 */
@Repository
public interface V2rayRepository extends JpaRepository<V2rayEntity, Long> {

	/**
	 * 按目标 URL 删除信息，URL 唯一
	 */

	/**
	 * 按 TargetURL 查询
	 */
	V2rayEntity findByTargetURL(String targetURL);
}
