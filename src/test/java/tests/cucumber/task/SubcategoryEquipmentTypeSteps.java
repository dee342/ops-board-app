package tests.cucumber.task;

import gov.nyc.dsny.smart.opsboard.domain.reference.SubCategorySubType;
import gov.nyc.dsny.smart.opsboard.domain.reference.Subcategory;

/*may be changed in the future if research on test on Java bean (DB) */
public class SubcategoryEquipmentTypeSteps {

	private Subcategory subcategory;
	private SubCategorySubType subEquipAssociation;

//	@Then("^subcategory has equipmentType$")
//	public void subcategory_has_equipmentType() {
//		/*
//		 * assertThat(subcategory.getEquipmentSubTypes().get(0).getEquipmentSubType().getEquipmentType(),
//		 * equalTo("Four By Four"));
//		 */
//
//	}
//
//	@Given("^subcategory id with equipType$")
//	public void subcategory_id_with_equipType() {
//		subcategory = new Subcategory("District Superintendent", false, 1, 1, "D/S");
//		subEquipAssociation = new SubCategoryEquipmentSubTypeAssociation();
//		subEquipAssociation.setId((long) 1);
//		Subtype subType = new Subtype();
//		subType.setEquipmentType("Four By Four");
//		subEquipAssociation.setEquipmentSubType(subType);
//		Set<SubCategoryEquipmentSubTypeAssociation> equipmentSubTypes = new HashSet<SubCategoryEquipmentSubTypeAssociation>();
//		equipmentSubTypes.add(subEquipAssociation);
//		subcategory.setEquipmentSubTypes(equipmentSubTypes);
//	}
}
