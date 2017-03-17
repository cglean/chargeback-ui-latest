package com.chargeback.rest.client;

import java.util.List;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.chargeback.vo.UsageRecord;

@FeignClient(name= "CHARGEBACK-API", url="https://chargeback-api-service.cfapps.io")
public interface ChargeBackApiClient {

	@RequestMapping(value = "/metrics/getInstanceMetrics", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<UsageRecord> getAllApplicationInstanceData();

	@RequestMapping(value = "/metrics/getFreeResource/{resourceType}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public String getFreeResourceForResourceType(@PathVariable("resourceType") final String resourceType);

	@RequestMapping(value = "/metrics/getOrgList", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<String> getOrgList();

	@RequestMapping(value = "/metrics/getSpaceList/{orgName}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<String> getSpaceList(@PathVariable("orgName") final String orgName);
}
