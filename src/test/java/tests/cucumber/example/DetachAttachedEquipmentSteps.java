package tests.cucumber.example;

import gov.nyc.dsny.smart.opsboard.domain.equipment.Equipment;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;

public class DetachAttachedEquipmentSteps {
	
	private Equipment equipment;
	private Location owner;
	private Location from;
	private Location to;
	
//	@Given("^equipment \"([^\"]*)\"$")
//	public void equipment(String equipmentId){
//		equipment = new Equipment();
//		equipment.setId(equipmentId);
//	}
//	
//	@Given("^equipment owner \"([^\"]*)\"$")
//	public void equipment_owner( String locationCode){
//		owner = new Location(locationCode);
//		equipment.setOwner(owner);
//	}
//	
//	@When("^detach equipment from owner to \"([^\"]*)\"$")
//	public void detach_equipment_from_owner_to(String toCode){
//		from = owner;
//		to = new Location(toCode);
//		equipment.detach("DETACH ACTUAL USER", "DETACH SYSTEM USER", new Date(), new Date(), from, to, "DRIVER", "COMMENTS");
//	}
//	
//	@Then("^detachment is added to top of equipment detachment history$")
//	public void detachment_added_to_top_of_equipment_detachment_history(){
//		Detachment mostRecent = equipment.getMostRecentDetachment(); 
//		/*assertThat(mostRecent.getActualUser(), equalTo("DETACH ACTUAL USER"));
//		assertThat(mostRecent.getSystemUser(), equalTo("DETACH SYSTEM USER"));
//		assertThat(mostRecent.getFrom(), equalTo(from));
//		assertThat(mostRecent.getTo(), equalTo(to));
//		assertThat(equipment.getDetachmentHistory().get(0), equalTo(mostRecent));*/
//	}
//	
//	@Then("equipment state at owner is \"([^\"]*)\"$")
//	public void equipment_state_at_owner_is(String state){
//		assertThat(equipment.getState(owner).getState(), equalTo(state));
//	}
//	
//	@Then("equipment state at to is \"([^\"]*)\"$")
//	public void equipment_state_at_to_is(String state){
//		assertThat(equipment.getState(to).getState(),  equalTo(state));
//	}
}
