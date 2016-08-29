package com.chargeback.controller;

import static com.chargeback.constants.ChargeBackConstants.CPU;
import static com.chargeback.constants.ChargeBackConstants.DISK;
import static com.chargeback.constants.ChargeBackConstants.GET_COST_DETAILS;
import static com.chargeback.constants.ChargeBackConstants.GET_ORG_LIST;
import static com.chargeback.constants.ChargeBackConstants.GET_SPACE_LIST;
import static com.chargeback.constants.ChargeBackConstants.GET_USAGE_DETAILS;
import static com.chargeback.constants.ChargeBackConstants.MEMORY;
import static com.chargeback.constants.ChargeBackConstants.SUMMARY;
import static com.chargeback.constants.ChargeBackConstants.UNUSED;
import static com.chargeback.constants.ChargeBackConstants.UNUTILISED;

import java.io.UnsupportedEncodingException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.chargeback.vo.ChartVO;
import com.chargeback.vo.CostVO;
import com.chargeback.vo.PriceValueSummary;
import com.chargeback.vo.UsageRecord;
/**
 * This Controller gives the Chart Data after calling the metrics service to the
 * UI.
 * 
 * @author ambansal
 *
 */
@RestController
public class ChargeBackController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChargeBackController.class);

	/*@Autowired
	private InfraApiClient infraApiClient;
	
	@Autowired
	private ChargeBackApiClient chargeBackApiClient;
	*/
	
	
	
	private static final String INSTANCE_SUMMARY_URL = "http://localhost:8080/chargeback/getResourceDetailsSummary";
	// TODO :: Need to fetch this from Eureka Server Client Id by just giving application name 
	private static final String INSTANCE_METRICS_URL = "http://chargeback-api.cglean.com/metrics/getInstanceMetrics";
	private static final String FREERESOURRCE_URL = "http://chargeback-api.cglean.com/metrics/getFreeResource";
	
	private static final String ORG_LIST_URL = "http://chargeback-api.cglean.com/metrics/getOrgList";
	private static final String SPACELIST_URL = "http://chargeback-api.cglean.com/metrics/getSpaceList";
	private static final String INFRA_API="http://infrastructure-api.cglean.com/cost";


	@Autowired  private RestTemplate restTemplate; 
	
	private List<PriceValueSummary> getSummary(final String startDate, final String endDate) throws ParseException {
		String url = INFRA_API;
		url = url + "?" +"start=" + startDate + "&" + "end=" + endDate; 
		
		final ResponseEntity<CostVO> infraApiResponse = restTemplate.exchange(INFRA_API, HttpMethod.GET, HttpEntity.EMPTY,
				new ParameterizedTypeReference<CostVO>() {
				});
		final CostVO costVO = infraApiResponse.getBody();
				//infraApiClient.getCost(startDate, endDate);
		
		final ResponseEntity<List<String>> response = restTemplate.exchange(ORG_LIST_URL, HttpMethod.GET, HttpEntity.EMPTY,
				new ParameterizedTypeReference<List<String>>() {
				});
		
		final ResponseEntity<List<UsageRecord>> instanceMetrics = restTemplate.exchange(INSTANCE_METRICS_URL, HttpMethod.GET, HttpEntity.EMPTY,
				new ParameterizedTypeReference<List<UsageRecord>>() {
				});
		final List<String> orgList = response.getBody();
				//chargeBackApiClient.getOrgList();

		final List<PriceValueSummary> priceValueSummaryList = new ArrayList<>();
		final List<UsageRecord> instanceData =  instanceMetrics.getBody();
				//chargeBackApiClient.getAllApplicationInstanceData();

		final NumberFormat format = NumberFormat.getCurrencyInstance();

		final double allOrgsCpuSum = instanceData.stream()
				.mapToDouble(usageRecord -> Double.valueOf(usageRecord.getCpu())).sum();
		final double allOrgsDiskSum = instanceData.stream()
				.mapToDouble(usageRecord -> Double.valueOf(usageRecord.getDisk())).sum();
		final double allOrgMemorySum = instanceData.stream()
				.mapToDouble(usageRecord -> Double.valueOf(usageRecord.getMemory())).sum();
		for (final String orgName : orgList) {
			// Sum up for Memory
			final double orgMemorySum = instanceData.stream()
					.filter(usageRecord -> usageRecord.getOrgName().equals(orgName))
					.mapToDouble(usageRecord -> Double.valueOf(usageRecord.getMemory())).sum();
			final double pctMemoryUsed = (Double.valueOf(orgMemorySum) / Double.valueOf(allOrgMemorySum));
			final double amtForMemory = (format.parse(costVO.getMemory()).doubleValue()) * pctMemoryUsed;
			// Sum up for CPU
			final double orgCpuSum = instanceData.stream()
					.filter(usageRecord -> usageRecord.getOrgName().equals(orgName))
					.mapToDouble(usageRecord -> Double.valueOf(usageRecord.getCpu())).sum();
			final double pctCpuUsed = (Double.valueOf(orgCpuSum) / Double.valueOf(allOrgsCpuSum));
			final double amtForCPU = (format.parse(costVO.getCpu()).doubleValue()) * pctCpuUsed;
			// SUM for DISK
			final double orgDiskSum = instanceData.stream()
					.filter(usageRecord -> usageRecord.getOrgName().equals(orgName))
					.mapToDouble(usageRecord -> Double.valueOf(usageRecord.getDisk())).sum();
			final double pctDiskUsed = (Double.valueOf(orgDiskSum) / Double.valueOf(allOrgsDiskSum));
			final double amtForDisk = (format.parse(costVO.getDisk()).doubleValue()) * pctDiskUsed;
			priceValueSummaryList.add(new PriceValueSummary(amtForDisk + amtForCPU + amtForMemory, amtForCPU, amtForDisk, amtForMemory, orgName));
		}
		return priceValueSummaryList;
	}

	@RequestMapping(value = GET_COST_DETAILS, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	private ChartVO getSummaryVal(@PathVariable String infoType, @PathVariable String resourceType, @PathVariable final String startDate, @PathVariable final String endDate)
			throws ParseException {

		final List<PriceValueSummary> priceValueSummaryList = getSummary(startDate,endDate);
		Function<List<PriceValueSummary>, List<String>> usedResourceFunction = null;
		Function<List<PriceValueSummary>, List<String>> appLabelFunction = null;
		if (resourceType.equals(SUMMARY)) {
			usedResourceFunction = summary -> priceValueSummaryList.stream()
					.map(usageRecord -> String.valueOf(usageRecord.summary)).collect(Collectors.toList());
			appLabelFunction = appLabel -> priceValueSummaryList.stream().map(usageRecord -> usageRecord.orgName
					.concat(" $").concat(String.valueOf(usageRecord.summary))).collect(Collectors.toList());

		} else if (resourceType.equals(MEMORY)) {
			usedResourceFunction = usedMemory -> priceValueSummaryList.stream()
					.map(usageRecord -> String.valueOf(usageRecord.memory)).collect(Collectors.toList());
			appLabelFunction = appLabel -> priceValueSummaryList.stream().map(
					usageRecord -> usageRecord.orgName.concat(" $").concat(String.valueOf(usageRecord.memory)))
					.collect(Collectors.toList());

		} else if (resourceType.equals(CPU)) {
			usedResourceFunction = usedCPU -> priceValueSummaryList.stream()
					.map(usageRecord -> String.valueOf(usageRecord.cpu)).collect(Collectors.toList());
			appLabelFunction = appLabel -> priceValueSummaryList.stream().map(
					usageRecord -> usageRecord.orgName.concat(" $").concat(String.valueOf(usageRecord.cpu)))
					.collect(Collectors.toList());

		} else if (resourceType.equals(DISK)) {
			usedResourceFunction = usedCPU -> priceValueSummaryList.stream()
					.map(usageRecord -> String.valueOf(usageRecord.disk)).collect(Collectors.toList());
			appLabelFunction = appLabel -> priceValueSummaryList.stream().map(
					usageRecord -> usageRecord.orgName.concat(" $").concat(String.valueOf(usageRecord.disk)))
					.collect(Collectors.toList());
		} else {
			throw new RuntimeException("Please Select Resource Type from : CPU, DISK, MEM");
		}
		return getParameterizedUsageDetails(priceValueSummaryList, usedResourceFunction, appLabelFunction);

	}

	/**
	 * 
	 * @param usageType
	 * @param resourceType
	 * @param orgName
	 * @param space
	 * @return
	 */
	@RequestMapping(value = GET_USAGE_DETAILS, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ChartVO getResourceUsage(@PathVariable final String usageType, @PathVariable final String resourceType,
			@PathVariable final String orgName, @PathVariable final String space) {
		final ResponseEntity<List<UsageRecord>> response = restTemplate.exchange(INSTANCE_METRICS_URL, HttpMethod.GET, HttpEntity.EMPTY,
				new ParameterizedTypeReference<List<UsageRecord>>() {
				});
		
		final List<UsageRecord> instanceData =  response.getBody();
		//chargeBackApiClient.getAllApplicationInstanceData();

		Function<List<UsageRecord>, List<String>> usedResourceFunction = null;
		Function<List<UsageRecord>, List<String>> appLabelFunction = null;
		if (resourceType.equals(MEMORY)) {
			usedResourceFunction = usedMemory -> instanceData.stream()
					.filter(usageRecord -> (usageRecord.getOrgName().equals(orgName)
							&& usageRecord.getSpaceName().equals(space)))
					.map(usageRecord -> usageRecord.getMemory()).collect(Collectors.toList());

		} else if (resourceType.equals(CPU)) {
			usedResourceFunction = usedCPU -> instanceData.stream()
					.filter(usageRecord -> (usageRecord.getOrgName().equals(orgName)
							&& usageRecord.getSpaceName().equals(space)))
					.map(usageRecord -> usageRecord.getCpu()).collect(Collectors.toList());

		} else if (resourceType.equals(DISK)) {
			usedResourceFunction = usedCPU -> instanceData.stream()
					.filter(usageRecord -> (usageRecord.getOrgName().equals(orgName)
							&& usageRecord.getSpaceName().equals(space)))
					.map(usageRecord -> usageRecord.getDisk()).collect(Collectors.toList());
		} else {
			throw new RuntimeException("Please Select Resource Type from : CPU, DISK, MEM");
		}

		appLabelFunction = appLabel -> instanceData.stream().filter(
				usageRecord -> (usageRecord.getOrgName().equals(orgName) && usageRecord.getSpaceName().equals(space)))
				.map(usageRecord -> usageRecord.getAppname().concat(" - ").concat(usageRecord.getInstanceIndex()))
				.collect(Collectors.toList());

		if (usageType.equals(UNUSED)) {
			if (!resourceType.equals(DISK)) {
				//final String freeResource = chargeBackApiClient.getFreeResourceForResourceType(resourceType);
				final ResponseEntity<String> freeResourceResponse = restTemplate.exchange(FREERESOURRCE_URL + "/" + resourceType, HttpMethod.GET, HttpEntity.EMPTY,
						new ParameterizedTypeReference<String>() {
						});
				return getUnUsedResource(instanceData, freeResourceResponse.getBody(), usedResourceFunction, appLabelFunction);
			} else {
				throw new RuntimeException("Not able to get total disk usage as of now");
			}
		}
		return getUsageDetails(instanceData, usedResourceFunction, appLabelFunction);
	}

	@RequestMapping(value = GET_ORG_LIST, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<String> getOrganizationNames() {
		
		final ResponseEntity<List<String>> response = restTemplate.exchange(ORG_LIST_URL, HttpMethod.GET, HttpEntity.EMPTY,
				new ParameterizedTypeReference<List<String>>() {
				});
		return response.getBody();
		//return chargeBackApiClient.getOrgList();
	}

	@RequestMapping(value = GET_SPACE_LIST, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<String> getSpaceList(@PathVariable String orgName) throws UnsupportedEncodingException {
		
		final ResponseEntity<List<String>> response = restTemplate.exchange(SPACELIST_URL + "/" + orgName , HttpMethod.GET, HttpEntity.EMPTY,
				new ParameterizedTypeReference<List<String>>() {
				});
		return response.getBody();
		
		//return chargeBackApiClient.getSpaceList(URLEncoder.encode(orgName, "UTF-8"));
	}

	private ChartVO getUnUsedResource(final List<UsageRecord> response,
			final String freeResourceVal,
			Function<List<UsageRecord>, List<String>> freeResourceFunction,
			Function<List<UsageRecord>, List<String>> appLabelFunction) {
		final List<String> freeResource = freeResourceFunction.apply(response);
		final List<String> appLabel = appLabelFunction.apply(response);
		freeResource.add(freeResourceVal);
		appLabel.add(UNUTILISED);
		return new ChartVO(appLabel,freeResource);
	}

	private ChartVO getUsageDetails(final List<UsageRecord> response,
			final Function<List<UsageRecord>, List<String>> resourceUsedFunction,
			final Function<List<UsageRecord>, List<String>> appLabelFunction) {

		final List<String> resourceUsed = resourceUsedFunction.apply(response);
		final List<String> appLabel = appLabelFunction.apply(response);
		return new ChartVO(appLabel,resourceUsed);
	}

	private ChartVO getParameterizedUsageDetails(final List<PriceValueSummary> summaryList,
			final Function<List<PriceValueSummary>, List<String>> resourceUsedFunction,
			final Function<List<PriceValueSummary>, List<String>> appLabelFunction) {

		final List<String> resourceUsed = resourceUsedFunction.apply(summaryList);
		final List<String> appLabel = appLabelFunction.apply(summaryList);
		return  new ChartVO(appLabel, resourceUsed);
	}
}