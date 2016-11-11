package gov.nyc.dsny.smart.opsboard.viewmodels.tasks;

import gov.nyc.dsny.smart.opsboard.domain.reference.Category;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ShiftCategory implements Serializable {

	private static final long serialVersionUID = 1L;

	private String id;

	private Category category;
	
	private String selectedCategoryId;

	private Map<String, SubcategoryTask> subcategoryTasks = new HashMap<String, SubcategoryTask>();
	
	private Map<String, String> addedSubcategories = new HashMap<String, String>();

	public ShiftCategory(gov.nyc.dsny.smart.opsboard.domain.tasks.ShiftCategory domain, Location boardLocation) {
		super();
		this.id = domain.getId();
		category = domain.getCategory();
		selectedCategoryId = Long.toString(category.getId());
		for (gov.nyc.dsny.smart.opsboard.domain.tasks.SubcategoryTask st : domain.getSubcategoryTasks()) {
			subcategoryTasks.put(st.getId(), new SubcategoryTask(st, boardLocation));
			addedSubcategories.put(Long.toString(st.getSubcategory().getId()), st.getId());
		}
	}

	@JsonIgnore
	public Category getCategory() {
		return category;
	}
	
	public long getCategoryId(){
		return category.getId();
	}

	public String getId() {
		return id;
	}

	public Map<String, SubcategoryTask> getSubcategoryTasks() {
		return subcategoryTasks;
	}

	public String getSelectedCategoryId() {
		return selectedCategoryId;
	}

	public void setSelectedCategoryId(String selectedCategoryId) {
		this.selectedCategoryId = selectedCategoryId;
	}

	public Map<String, String> getAddedSubcategories() {
		return addedSubcategories;
	}

	public void setAddedSubcategories(Map<String, String> addedSubcategories) {
		this.addedSubcategories = addedSubcategories;
	}
}
