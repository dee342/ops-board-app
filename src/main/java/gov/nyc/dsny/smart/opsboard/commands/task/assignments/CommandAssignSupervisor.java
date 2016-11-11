package gov.nyc.dsny.smart.opsboard.commands.task.assignments;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;


@IBoardCommandAnnotation(commandName = "AssignSupervisor")
public class CommandAssignSupervisor extends AbstractTaskPersonCommand {

	private static final long serialVersionUID = 1L;

	private String sectionId;
	private int sequenceNumber;
	private String subcategoryId;
	private String taskIndicator;

	private Map<String, Integer> taskSequenceNumberMap = new HashMap<String, Integer>();

	public CommandAssignSupervisor(String boardId, LinkedHashMap<String, Object> content) {
		super(boardId, content);
		taskIndicator = (String) content.get("taskIndicator");
		sequenceNumber = content.get("sequenceNumber") == null
				? new Integer(0)
				: Integer.parseInt(content.get("sequenceNumber").toString());
		sectionId = (String) content.get("sectionId");
		subcategoryId = (String) content.get("subcategoryId");
	}

	@Override
	public void execute(Board board) throws OpsBoardError {

		// Execute logic
		setTaskSequenceNumberMap(board.assignSupervisor(getTaskId(), getSectionId(), getSubcategoryId(), getTaskIndicator(), getPersonId(),
				getSequenceNumber()));

		// Create audit message
		createAuditMessage(board);

		// Add command to history
		board.addCommandToHistory(this);
	}

	public String getSectionId() {
		return sectionId;
	}

	public int getSequenceNumber() {
		return sequenceNumber;
	}

	public String getSubcategoryId() {
		return subcategoryId;
	}

	public String getTaskIndicator() {
		return taskIndicator;
	}

	public Map<String, Integer> getTaskSequenceNumberMap() {
		return taskSequenceNumberMap;
	}

	@Override
	public void persist(Board board, BoardPersistenceService persistService) throws OpsBoardError {
		persistService.save(board);

	}

	public void setSectionId(String sectionId) {
		this.sectionId = sectionId;
	}

	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public void setSubcategoryId(String subcategoryId) {
		this.subcategoryId = subcategoryId;
	}

	public void setTaskIndicator(String taskIndicator) {
		this.taskIndicator = taskIndicator;
	}

	public void setTaskSequenceNumberMap(Map<String, Integer> taskSequenceNumberMap) {
		this.taskSequenceNumberMap = taskSequenceNumberMap;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("sectionId", sectionId);
		builder.append("sequenceNumber", sequenceNumber);
		builder.append("taskIndicator", taskIndicator);
		builder.append("taskSequenceNumberMap", taskSequenceNumberMap);
		builder.append(super.toString());

		return builder.toString();
	}

	@Override
	protected void createAuditMessage(Board board) {
		StringBuilder sb = new StringBuilder();
		sb.append("Assigned supervisor ");
		sb.append("[person:" + getPersonId() + "] ");

		if (StringUtils.isNotBlank(getTaskId()))
			sb.append("to [task:" + getTaskId() + "].");
		else if (StringUtils.isNotBlank(getSectionId()))
			sb.append("to [section:" + getSectionId() + "].");

		setAuditMessage(sb.toString());
	};
}
