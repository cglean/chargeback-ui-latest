/*package com.chargeback.rest.client;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.chargeback.vo.CostVO;

@FeignClient("INFRASTRUCTURE-API")
public interface InfraApiClient {

	@RequestMapping(value = "/cost", method = RequestMethod.GET)
	public CostVO getCost(@RequestParam("start") String start, @RequestParam("end") String end);
}
*/