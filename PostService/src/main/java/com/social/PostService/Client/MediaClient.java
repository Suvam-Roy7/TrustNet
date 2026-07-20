package com.social.PostService.Client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "MEDIASERVICE")
public interface MediaClient {

	@DeleteMapping("/api/media/{objectName}")
	void deleteFile(@PathVariable("objectName") String objectName);
}