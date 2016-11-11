package tests.cucumber.assign.equipment;

import gov.nyc.dsny.smart.opsboard.domain.equipment.Bin;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.equipment.Equipment;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.domain.tasks.Task;

import java.util.Date;

public class AssignEquipmentPendingLoadSteps {
	
	private Equipment equipment;
	private BoardEquipment boardEquipment;
	private Task task;
	private String boardDate;
	private Location location;
	private Date boardDt;
	private Date endDate;
	private Date startDate;
	private Bin bin1;
	
//	@Given("^set equipment \"(.*?)\"$")
//	public void set_equipment(String equipmentId) {
//	    equipment=new Equipment();
//	    equipment.setId(equipmentId);
//	    equipment.setOwner(new Location("BKN01"));
//	    boardDt = new Date();
//		System.out.println("board date is::"+boardDt);
//		int milliSecondsInDay = 1000 * 60 * 60 * 24;
//		long startDateLong = boardDt.getTime() - milliSecondsInDay;
//		startDate = new Date(startDateLong);
//		long endDateLong = boardDt.getTime() + milliSecondsInDay;
//		endDate = new Date(endDateLong);
//		System.out.println("startDate is::"+startDate);
//		System.out.println("end date is::"+endDate);
//		boardEquipment = new BoardEquipment();
//		boardEquipment.setBoardDate(boardDate);
//		boardEquipment.setStartDate(startDate);
//		boardEquipment.setEndDate(endDate);
//		boardEquipment.setEquipment(equipment);
//		boardEquipment.setAssigned(true);
//        	  
//	}
//	
//	@Given("^soonEndingTask \"(.*?)\"$")
//	public void soonendingtask(String taskId) {
//        task=new Task();
//        task.setId(taskId);
//        	  
//	}
//	
//    @Given("^main location \"([^\"]*)\"$")
//	public void main_location(String locationn){
//		location = new Location(locationn);
//	}
///*	
//	@Given("^set homeLocation \"([^\"]*)\"$")
//	public void set_homeLocation(String homeLocationn){
//		homeLocation = new Location(homeLocationn);
//	}
//	
//	@Given("^set boardDate \"([^\"]*)\"$")
//	public void set_boardDate(String boardDatee){
//		boardDate = boardDatee;
//	}
//	*/
//
//	@When("^update equipment pending load status$")
//	public void update_equipment_pending_load_status() {
//	   bin1=new Bin();
//	   bin1.setId(1L);
//	   bin1.setStatus("Relay");
//	   List<Bin> binList=new ArrayList<Bin>();
//	   binList.add(bin1);
//	   equipment.setBinHistory(binList);
//	   bin1.setEquipment(boardEquipment.getEquipment());	  
//	}
//
//	@When("^equipment assigned to soonEndingTask$")
//	public void equipment_assigned_to_soonEndingTask() {
//	    EquipmentAssignment equipmentAssign=new EquipmentAssignment();
//	    equipmentAssign.setId(1L);
//	    equipmentAssign.assign(boardEquipment, new Date());
//	    task.setAssignedEquipment(equipmentAssign);
//	    
//	}
//
//	@When("^equipment back to pending load$")
//	public void equipment_back_to_pending_load()  {	
//		//method copy from autoCompleteAssignedEquipment in EquipmentExecutor, 
//		//need to be changed if this method changes.
//		bin1.setStatus("L");
//		bin1.setMaterial(null);
//		bin1.setLastModifiedActual(new Date());
//		bin1.setLastModifiedSystem(new Date());
//		bin1.setSystemUser("user");
//		bin1.setActualUser("user");
//		boardEquipment.unassign();
//	  
//	}
//
//	@Then("^equipment state is pending load$")
//	public void equipment_state_is_pending_load() {
//		assertThat(equipment.getState(location).getState(), equalTo("Pending Load"));
//	 
//	}
//
//	@Then("^equipment has unchanged load status$")
//	public void equipment_has_unchanged_load_status() {
//		assertThat(equipment.getBinHistory().get(0).getId(), equalTo(1L));
//		assertThat(equipment.getBinHistory().get(0).getStatus(), equalTo("Relay"));
//	   
//	}
}