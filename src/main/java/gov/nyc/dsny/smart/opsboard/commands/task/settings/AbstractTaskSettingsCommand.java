package gov.nyc.dsny.smart.opsboard.commands.task.settings;

import gov.nyc.dsny.smart.opsboard.commands.AbstractBoardCommand;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.ICategoryCommand;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.ILocationCommand;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.ISectionCommand;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.IShiftCommand;
import gov.nyc.dsny.smart.opsboard.commands.interfaces.ISubCategoryCommand;
import gov.nyc.dsny.smart.opsboard.domain.StateAndLocation;
import gov.nyc.dsny.smart.opsboard.domain.StatesAndAssignment;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.equipment.BoardEquipment;
import gov.nyc.dsny.smart.opsboard.domain.reference.Category;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;
import gov.nyc.dsny.smart.opsboard.domain.reference.Section;
import gov.nyc.dsny.smart.opsboard.domain.reference.Shift;
import gov.nyc.dsny.smart.opsboard.domain.reference.Subcategory;
import gov.nyc.dsny.smart.opsboard.domain.tasks.Task;

import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Represents the base class for all task settings commands.
 */
public abstract class AbstractTaskSettingsCommand extends AbstractBoardCommand implements ILocationCommand,
IShiftCommand, ICategoryCommand, ISubCategoryCommand, ISectionCommand {

	private static final long serialVersionUID = 1L;

	private transient Category category;
	private transient Section section;
	private transient Location serviceLocation;
	private transient Shift shift;
	private transient Subcategory subcategory;
	private transient Comparator<Entry<String, Task>> taskSeqComparator = (e1, e2) -> e1.getValue().getSequence()
			- e2.getValue().getSequence();

	public AbstractTaskSettingsCommand() {
	}

	public AbstractTaskSettingsCommand(String boardId, LinkedHashMap<String, Object> map) {
		super(boardId, map);
	}

	public AbstractTaskSettingsCommand(String boardId, String systemUser, Date systemDateTime, Location serviceLocation) {
		super(boardId, systemUser, systemDateTime);
		this.serviceLocation = serviceLocation;
	}

	@Override
	@JsonIgnore
	public Category getCategory() {
		return category;
	}

	public Long getCategoryId() {
		return category == null ? null : category.getId();
	}

	@Override
	@JsonIgnore
	public Location getLocation() {
		return serviceLocation;
	}

	@Override
	public Section getSection() {
		return section;
	}

	@JsonIgnore
	public Location getServiceLocation() {
		return serviceLocation;
	}

	public String getServiceLocationCode() {
		return serviceLocation == null ? null : serviceLocation.getCode();
	}

	@Override
	@JsonIgnore
	public Shift getShift() {
		return shift;
	}

	public Long getShiftId() {
		return shift == null ? null : shift.getId();
	}

	public Map<String, Task> getSortedTaskMap(Map<String, Task> unsortedTasksMap) {
		return unsortedTasksMap.entrySet().stream().sorted(getTaskSeqComparator())
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
	}

	@Override
	@JsonIgnore
	public Subcategory getSubcategory() {
		return subcategory;
	}

	public Long getSubcategoryId() {
		return subcategory == null ? null : subcategory.getId();
	}

	public Comparator<Entry<String, Task>> getTaskSeqComparator() {
		return taskSeqComparator;
	}

	@Override
	public void setCategory(Category category) {
		this.category = category;
	}

	@Override
	public void setLocation(Location location) {
		serviceLocation = location;
	}

	@Override
	public void setSection(Section section) {
		this.section = section;
	}

	public void setServiceLocation(Location serviceLocation) {
		this.serviceLocation = serviceLocation;
	}

	@Override
	public void setShift(Shift shift) {
		this.shift = shift;
	}

	@Override
	public void setSubcategory(Subcategory subcategory) {
		this.subcategory = subcategory;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("categoryId", getCategoryId());
		builder.append("section", section);
		builder.append("serviceLocationCode", getServiceLocationCode());
		builder.append("shiftid", getShiftId());
		builder.append("subcategoryId", getSubcategoryId());
		builder.append(super.toString());

		return builder.toString();
	};

	protected StatesAndAssignment wrapEquipmenStatesAndAssignment(Board board, BoardEquipment be) {
		Location loc = board.getLocation();
		Map<String, String> states = new HashMap<String, String>();

		if (loc.isServicesEquipmentLocations()) {
			for (Location el : loc.getServiceLocations()) {
				StateAndLocation sl = be.getState(el);
				states.put(el.getCode(), sl.getState());
			}
		} else {
			StateAndLocation sl = be.getState(loc);
			states.put(loc.getCode(), sl.getState());
		}

		return new StatesAndAssignment(states, be.isAssigned());
	}
}