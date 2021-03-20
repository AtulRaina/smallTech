package com.smalltech;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PubsubPublish {
 @Autowired 
  PubsubService ps;
	@RequestMapping("/publish")
	public String publishMessage() throws Exception {
		

		return ps.publishMessage();
	}
	
}
