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
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.chargeback.rest.client.ChargeBackApiClient;
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

	private Log log = LogFactory.getLog(ChargeBackController.class);

	/*@Autowired
	private InfraApiClient infraApiClient;
*/
	@Autowired
	private ChargeBackApiClient chargeBackApiClient;
	

	@Value("${client.name:NBCU}")
	private String clientName;
	

	@RequestMapping(value = "/getClient", method = RequestMethod.GET, produces = MediaType.TEXT_PLAIN_VALUE)
	public String getClientName() {
		return clientName;

	}

	private List<PriceValueSummary> getSummary(final String startDate, final String endDate) throws ParseException {
		log.info(String.format("GetSummary %s, %s", startDate, endDate));
		
		// HardCoding To be Removed 
		final CostVO costVO = new CostVO();
		
		final String cpu = String.valueOf(Math.random()* 100);
		final String disk = String.valueOf(Math.random()* 100);
		final String memory =  String.valueOf(Math.random()* 100);
		final Double total = Double.valueOf(cpu) +  Double.valueOf(disk) +  Double.valueOf(memory) ;
		costVO.setCpu("$" +cpu);
		costVO.setDisk("$" + disk);
		costVO.setMemory("$" + memory);
		costVO.setStartDate(startDate);
		costVO.setEndDate(endDate);
		costVO.setTotal("$"+String.valueOf(total));
		//final CostVO costVO = infraApiClient.getCost(startDate, endDate);
		final List<String> orgList = chargeBackApiClient.getOrgList();

		final List<PriceValueSummary> priceValueSummaryList = new ArrayList<>();
		final List<UsageRecord> instanceData = chargeBackApiClient.getAllApplicationInstanceData();

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
			priceValueSummaryList.add(new PriceValueSummary(amtForDisk + amtForCPU + amtForMemory, amtForCPU,
					amtForDisk, amtForMemory, orgName));
		}
		return priceValueSummaryList;
	}

	@RequestMapping(value = GET_COST_DETAILS, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ChartVO getSummaryVal(@PathVariable final String infoType, @PathVariable final String resourceType,
			@PathVariable final String startDate, @PathVariable final String endDate, HttpSession session)
					throws ParseException {
		log.info(String.format("GetCostDetails %s, %s", infoType, resourceType));
		if (null != session
				&& null != session.getAttribute("dollarSplit" + startDate + endDate + infoType + resourceType)) {
			return ((ChartVO) session.getAttribute("dollarSplit" + startDate + endDate + infoType + resourceType));
		}

		ChartVO chartVO = null;
		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		final List<PriceValueSummary> priceValueSummaryList = getSummary(startDate, endDate);
		final Function<List<PriceValueSummary>, List<String>> summaryfunction = getSummaryFunction(
				priceValueSummaryList);
		final Function<List<PriceValueSummary>, List<String>> diskfunction = getDiskFunction(priceValueSummaryList);
		;
		final Function<List<PriceValueSummary>, List<String>> cpufunction = getCPUFunction(priceValueSummaryList);
		final Function<List<PriceValueSummary>, List<String>> memoryfunction = getMemoryFunction(priceValueSummaryList);

		final Function<List<PriceValueSummary>, List<String>> summaryLabel = getSummaryLabelFunction(decimalFormat,
				priceValueSummaryList);
		final Function<List<PriceValueSummary>, List<String>> memoryLabel = getMemoryLabelFunction(decimalFormat,
				priceValueSummaryList);
		;
		final Function<List<PriceValueSummary>, List<String>> cpuLabel = getCPULabelFunction(decimalFormat,
				priceValueSummaryList);
		final Function<List<PriceValueSummary>, List<String>> diskLabel = getDiskLabelFunction(decimalFormat,
				priceValueSummaryList);

		if (resourceType.equals(SUMMARY)) {
			chartVO = getParameterizedUsageDetails(priceValueSummaryList, summaryfunction, summaryLabel);
		} else if (resourceType.equals(MEMORY)) {
			chartVO = getParameterizedUsageDetails(priceValueSummaryList, memoryfunction, memoryLabel);
		} else if (resourceType.equals(CPU)) {
			chartVO = getParameterizedUsageDetails(priceValueSummaryList, cpufunction, cpuLabel);
		} else if (resourceType.equals(DISK)) {
			chartVO = getParameterizedUsageDetails(priceValueSummaryList, diskfunction, diskLabel);
		} else {
			throw new RuntimeException("Please Select Resource Type from : CPU, DISK, MEM");
		}
		session.setAttribute("dollarSplit" + startDate + endDate + infoType + SUMMARY,
				getParameterizedUsageDetails(priceValueSummaryList, summaryfunction, summaryLabel));
		session.setAttribute("dollarSplit" + startDate + endDate + infoType + MEMORY,
				getParameterizedUsageDetails(priceValueSummaryList, memoryfunction, memoryLabel));
		session.setAttribute("dollarSplit" + startDate + endDate + infoType + CPU,
				getParameterizedUsageDetails(priceValueSummaryList, cpufunction, cpuLabel));
		session.setAttribute("dollarSplit" + startDate + endDate + infoType + DISK,
				getParameterizedUsageDetails(priceValueSummaryList, diskfunction, diskLabel));

		return chartVO;

	}

	private Function<List<PriceValueSummary>, List<String>> getDiskLabelFunction(final DecimalFormat decimalFormat,
			final List<PriceValueSummary> priceValueSummaryList) {
		return appLabel -> priceValueSummaryList.stream()
				.map(usageRecord -> usageRecord.orgName.concat(" $").concat(decimalFormat.format(usageRecord.disk)))
				.collect(Collectors.toList());
	}

	private Function<List<PriceValueSummary>, List<String>> getDiskFunction(
			final List<PriceValueSummary> priceValueSummaryList) {
		return usedCPU -> priceValueSummaryList.stream().map(usageRecord -> String.valueOf(usageRecord.disk))
				.collect(Collectors.toList());
	}

	private Function<List<PriceValueSummary>, List<String>> getCPULabelFunction(final DecimalFormat decimalFormat,
			final List<PriceValueSummary> priceValueSummaryList) {
		return appLabel -> priceValueSummaryList.stream()
				.map(usageRecord -> usageRecord.orgName.concat(" $").concat(decimalFormat.format(usageRecord.cpu)))
				.collect(Collectors.toList());
	}

	private Function<List<PriceValueSummary>, List<String>> getCPUFunction(
			final List<PriceValueSummary> priceValueSummaryList) {
		return usedCPU -> priceValueSummaryList.stream().map(usageRecord -> String.valueOf(usageRecord.cpu))
				.collect(Collectors.toList());
	}

	private Function<List<PriceValueSummary>, List<String>> getMemoryLabelFunction(final DecimalFormat decimalFormat,
			final List<PriceValueSummary> priceValueSummaryList) {
		return appLabel -> priceValueSummaryList.stream()
				.map(usageRecord -> usageRecord.orgName.concat(" $").concat(decimalFormat.format(usageRecord.memory)))
				.collect(Collectors.toList());
	}

	private Function<List<PriceValueSummary>, List<String>> getMemoryFunction(
			final List<PriceValueSummary> priceValueSummaryList) {
		return usedMemory -> priceValueSummaryList.stream().map(usageRecord -> String.valueOf(usageRecord.memory))
				.collect(Collectors.toList());
	}

	private Function<List<PriceValueSummary>, List<String>> getSummaryLabelFunction(final DecimalFormat decimalFormat,
			final List<PriceValueSummary> priceValueSummaryList) {
		return appLabel -> priceValueSummaryList.stream()
				.map(usageRecord -> usageRecord.orgName.concat(" $").concat(decimalFormat.format(usageRecord.summary)))
				.collect(Collectors.toList());
	}

	private Function<List<PriceValueSummary>, List<String>> getSummaryFunction(
			final List<PriceValueSummary> priceValueSummaryList) {
		Function<List<PriceValueSummary>, List<String>> summaryfunction;
		summaryfunction = summary -> priceValueSummaryList.stream()
				.map(usageRecord -> String.valueOf(usageRecord.summary)).collect(Collectors.toList());
		return summaryfunction;
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
		log.info(String.format("getResourceDetails %s, %s, %s, %s", usageType, resourceType, orgName, space));
		final List<UsageRecord> instanceData = chargeBackApiClient.getAllApplicationInstanceData();
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

		appLabelFunction = appLabel -> instanceData.stream()
				.filter(usageRecord -> (usageRecord.getOrgName().equals(orgName)
						&& usageRecord.getSpaceName().equals(space)))
				.map(usageRecord -> usageRecord.getAppname().concat(" - ").concat(usageRecord.getInstanceIndex()))
				.collect(Collectors.toList());

		if (usageType.equals(UNUSED)) {
			if (!resourceType.equals(DISK)) {
				return getUnUsedResource(instanceData, chargeBackApiClient.getFreeResourceForResourceType(resourceType),
						usedResourceFunction, appLabelFunction);
			} else {
				throw new RuntimeException("Not able to get total disk usage as of now");
			}
		}
		return getUsageDetails(instanceData, usedResourceFunction, appLabelFunction);
	}

	@RequestMapping(value = GET_ORG_LIST, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<String> getOrganizationNames() {
		log.info(String.format("getOrgList"));
		return chargeBackApiClient.getOrgList();
	}

	@RequestMapping(value = GET_SPACE_LIST, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public List<String> getSpaceList(@PathVariable final String orgName) throws UnsupportedEncodingException {
		log.info(String.format("getSpaceList, %s", orgName));		
		return chargeBackApiClient.getSpaceList(URLEncoder.encode(orgName, "UTF-8"));
	}

	private ChartVO getUnUsedResource(final List<UsageRecord> response, final String freeResourceVal,
			final Function<List<UsageRecord>, List<String>> freeResourceFunction,
			final Function<List<UsageRecord>, List<String>> appLabelFunction) {
		final List<String> freeResource = freeResourceFunction.apply(response);
		final List<String> appLabel = appLabelFunction.apply(response);
		freeResource.add(freeResourceVal);
		appLabel.add(UNUTILISED);
		return new ChartVO(appLabel, freeResource);
	}

	private ChartVO getUsageDetails(final List<UsageRecord> response,
			final Function<List<UsageRecord>, List<String>> resourceUsedFunction,
			final Function<List<UsageRecord>, List<String>> appLabelFunction) {

		final List<String> resourceUsed = resourceUsedFunction.apply(response);
		final List<String> appLabel = appLabelFunction.apply(response);
		return new ChartVO(appLabel, resourceUsed);
	}

	private ChartVO getParameterizedUsageDetails(final List<PriceValueSummary> summaryList,
			final Function<List<PriceValueSummary>, List<String>> resourceUsedFunction,
			final Function<List<PriceValueSummary>, List<String>> appLabelFunction) {

		final List<String> resourceUsed = resourceUsedFunction.apply(summaryList);
		final List<String> appLabel = appLabelFunction.apply(summaryList);
		return new ChartVO(appLabel, resourceUsed);
	}
}