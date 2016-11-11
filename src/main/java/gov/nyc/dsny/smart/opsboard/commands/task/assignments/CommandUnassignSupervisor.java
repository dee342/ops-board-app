package gov.nyc.dsny.smart.opsboard.commands.task.assignments;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;


@IBoardCommandAnnotation(commandName = "UnassignSupervisor")
public class CommandUnassignSupervisor extends AbstractTaskPersonCommand {

	private static final long serialVersionUID = 1L;

	private String sectionId;
	private String subcategoryId;
	private List<Map<String, String>> supervisorAssignments;

	@SuppressWarnings("unchecked")
	public CommandUnassignSupervisor(String boardId, LinkedHashMap<String, Object> map) {
		super(boardId, map);
		sectionId = (String) map.get("sectionId");
		subcategoryId = (String) map.get("subcategoryId");
		supervisorAssignments = map.get("supervisorAssignments") == null
				? new ArrayList<Map<String, String>>()
				: (List<Map<String, String>>) map.get("supervisorAssignments");
	}

	@Override
	public void execute(Board board) throws OpsBoardError {

		// Execute logic
		board.unassignSupervisor(getTaskId(), getSectionId(), getSubcategoryId(), getPersonId(),
				getSupervisorAssignments());

		// Create audit message
		createAuditMessage(board);

		// Add command to history
		board.addCommandToHistory(this);
	}

	public String getSectionId() {
		return sectionId;
	}

	public String getSubcategoryId() {
		return subcategoryId;
	}

	public List<Map<String, String>> getSupervisorAssignments() {
		return supervisorAssignments;
	}

	@Override
	public void persist(Board board, BoardPersistenceService persistService) throws OpsBoardError {
		persistService.save(board);
	}

	public void setSectionId(String sectionId) {
		this.sectionId = sectionId;
	}

	public void setSubcategoryId(String subcategoryId) {
		this.subcategoryId = subcategoryId;
	}

	public void setSupervisorAssignments(List<Map<String, String>> supervisorAssignments) {
		this.supervisorAssignments = supervisorAssignments;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("sectionId", sectionId);
		builder.append("subcategoryId", subcategoryId);
		builder.append("upervisorAssignments", supervisorAssignments);
		builder.append(super.toString());

		return builder.toString();
	}

	@Override
	protected void createAuditMessage(Board board) {
		StringBuilder sb = new StringBuilder();
		sb.append("Unassigned supervisors ");

		if (StringUtils.isNotBlank(getTaskId()))
			sb.append("from [task:" + getTaskId() + "].");
		else if (StringUtils.isNotBlank(getSectionId()))
			sb.append("from [section:" + getSectionId() + "].");
		else if (StringUtils.isNotBlank(getSubcategoryId()))
			sb.append("from [section:" + getSubcategoryId() + "].");

		setAuditMessage(sb.toString());
	};
}
