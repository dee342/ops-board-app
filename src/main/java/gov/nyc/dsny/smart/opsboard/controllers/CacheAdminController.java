package gov.nyc.dsny.smart.opsboard.controllers;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.cache.gf.function.GFCacheFunctions;
import gov.nyc.dsny.smart.opsboard.domain.equipment.Equipment;
import gov.nyc.dsny.smart.opsboard.domain.tasks.TaskContainer;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.gemstone.gemfire.cache.EntryNotFoundException;

@RestController
@RequestMapping("/admin/cache")
public class CacheAdminController {

	@Autowired
	private GFCacheFunctions gfFunctionExecutor;
	
	@RequestMapping("boardContainer/{id}/evict")
	public ResponseEntity<String> evictBoard(@PathVariable String id)throws OpsBoardError{
		ResponseEntity<String> response;
		try{
			gfFunctionExecutor.evictBoard(id);
			response = new ResponseEntity<String>(String.format("Board ID:%s is deleted", id), HttpStatus.OK);
		}
		catch(EntryNotFoundException ex){
			response = new ResponseEntity<String>(String.format("Board ID:%s is not found", id), HttpStatus.NOT_FOUND);
		}
		return response;
	}
	
	@RequestMapping("equipment/{id}/evict")
	public ResponseEntity<String> evictEquipment(@PathVariable String id){
		ResponseEntity<String> response;
		try{
			gfFunctionExecutor.evictEquipment(id);
			response = new ResponseEntity<String>(String.format("Equipment ID:%s is refreshed", id), HttpStatus.OK);
		}
		catch(EntryNotFoundException ex){
			response = new ResponseEntity<String>(String.format("Equipment ID:%s is not found", id), HttpStatus.NOT_FOUND);
		}
		return response;
	}

	@RequestMapping("boardEquipment/{id}/evict")
	public ResponseEntity<String> evictBoardEquipment(@PathVariable String id){
		ResponseEntity<String> response;
		try{
			gfFunctionExecutor.evictBoardEquipment(id);
			response = new ResponseEntity<String>(String.format("Board Equipment ID:%s is refreshed", id), HttpStatus.OK);
		}
		catch(EntryNotFoundException ex){
			response = new ResponseEntity<String>(String.format("Board Equipment ID:%s is not found", id), HttpStatus.NOT_FOUND);
		}
		return response;
	}
	
	@RequestMapping("personnel/{id}/evict")
	public ResponseEntity<String> evictPersonnel(@PathVariable String id){
		ResponseEntity<String> response;
		try{
			gfFunctionExecutor.evictPersonnel(id);
			response = new ResponseEntity<String>(String.format("Personnel ID:%s is refreshed", id), HttpStatus.OK);
		}
		catch(EntryNotFoundException ex){
			response = new ResponseEntity<String>(String.format("Personnel ID:%s is not found", id), HttpStatus.NOT_FOUND);
		}
		return response;
	}
	
	@RequestMapping("boardPersonnel/{id}/evict")
	public ResponseEntity<String> evictBoardPersonnel(@PathVariable String id){
		ResponseEntity<String> response;
		try{
			gfFunctionExecutor.evictBoardPersonnel(id);
			response = new ResponseEntity<String>(String.format("Board Personnel ID:%s is refreshed", id), HttpStatus.OK);
		}
		catch(EntryNotFoundException ex){
			response = new ResponseEntity<String>(String.format("Board Personnel ID:%s is not found", id), HttpStatus.NOT_FOUND);
		}
		return response;
	}
	
	@RequestMapping("/getAll/{regionName}")
	public List<Equipment> getAllDataInRegion(@PathVariable String regionName) throws Exception{
		return gfFunctionExecutor.getAllDataInRegion(regionName);
	}
	
	@RequestMapping(value="/query", method=RequestMethod.POST, consumes="text/plain")
	public List<Equipment> selectOnQuery(@RequestBody String query) throws Exception{
		return gfFunctionExecutor.queryDataByRegion(query);
	}
	
	@RequestMapping(value="/region/names")
	public List<String> listOfRegions(){
		return gfFunctionExecutor.listRegionByNames();
	}
	
	@RequestMapping(value="/{regionName}/entrycount")
	public int entryCountForRegion(@PathVariable String regionName){
		return gfFunctionExecutor.getEntryCount(regionName);
	}
	
	@RequestMapping(value="/{regionName}/{id}/taskContainers")
	public  List<TaskContainer> getTaskContainers(@PathVariable String regionName, @PathVariable String id){
		return gfFunctionExecutor.getTaskContainers(regionName,id);
	}
	
	@RequestMapping(value="/{regionName}/{id}")
	public <T> T getDataById(@PathVariable String regionName, @PathVariable String id){
		Optional<T> resultObj = gfFunctionExecutor.getDataById(regionName,id);
		return resultObj.get();
	}
	
	@RequestMapping(value="/location/code")
	public List<String> getLocationCodes(){
		return gfFunctionExecutor.getLocationCode();
	}
	
	@RequestMapping(value="/refdata/region")
	public List<String> refDataRegionList(){
		return gfFunctionExecutor.listRefDataRegionNames();
	}
	
	@RequestMapping(value="/refdata/{regionName}/{startDate}")
	public <T> List<T> getRegionDataByDate(@PathVariable String regionName, @PathVariable @DateTimeFormat(iso=ISO.DATE)Date startDate) throws Exception{
		String queryString = "select * from /" + regionName + " s where $1 >= s.effectiveStartDate and $1 <= s.effectiveEndDate";
		return gfFunctionExecutor.queryDataByRegion(queryString, startDate);
	}
}
