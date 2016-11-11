package gov.nyc.dsny.smart.opsboard.commands.equipment.detach;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.equipment.AbstractMultiBoardEquipmentCommand;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.reference.Location;

import java.util.Date;
import java.util.LinkedHashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Represents the base class for all multi-board commands for equipment detachment/attachment operations. The class
 * extends base AbstractMultiBoardEquipmentCommand with detachment/attachment fields.
 */
public abstract class AbstractEquipmentAttachDetachCommand extends AbstractMultiBoardEquipmentCommand {

	private static final long serialVersionUID = 1L;

	private String comments;
	private Date date;
	private String driver;
	private transient Location from;
	private String reporter;
	private String status;
	private transient Location to;
	private Long detachmentId;

	public AbstractEquipmentAttachDetachCommand() {
		super();
	}

	public AbstractEquipmentAttachDetachCommand(LinkedHashMap<String, Object> map) {
		super(map);
		if (map.get("date") == null) {
			date = null;
		} else {
			date = new Date((Long) map.get("date"));
		}

		driver = (String) map.get("driver");
		reporter = (String) map.get("reporter");
		comments = (String) map.get("comments");
		status = (String) map.get("status");
		detachmentId = Long.valueOf( map.get("detachmentId").toString());

		ObjectMapper mapper = new ObjectMapper();
		try {
			from = mapper.readValue(map.get("from").toString(), Location.class);
		} catch (Exception e) {
			from = null;
		}
		try {
			to = mapper.readValue(map.get("to").toString(), Location.class);
		} catch (Exception e) {
			to = null;
		}
	}

	public AbstractEquipmentAttachDetachCommand(String boardId, String systemUser, Date systemDateTime,
			String boardEquipmentId, String equipmentId, Date date, Location from, Location to, String driver,
			String reporter, String comments, String status, Long detachmentId) {
		this(boardId, systemUser, systemDateTime, boardEquipmentId, equipmentId, date, from, to, 
				driver, reporter, comments, status, detachmentId, false);
	}
	
	public AbstractEquipmentAttachDetachCommand(String boardId, String systemUser, Date systemDateTime,
			String boardEquipmentId, String equipmentId, Date date, Location from, Location to, String driver,
			String reporter, String comments, String status, Long detachmentId, boolean fromIntegration) {
		super(boardId, systemUser, systemDateTime, boardEquipmentId, equipmentId, fromIntegration);
		this.date = date;
		this.from = from;
		this.to = to;
		this.driver = driver;
		this.reporter = reporter;
		this.comments = comments;
		this.status = status;
		this.detachmentId = detachmentId;
	}

	@Override
	public abstract void execute(Board board) throws OpsBoardError;

	public String getComments() {
		return comments;
	}

	public Date getDate() {
		return date;
	}

	public String getDriver() {
		return driver;
	}

	@JsonIgnore
	public Location getFrom() {
		return from;
	}

	public String getFromCode() {
		return from != null ? from.getCode() : null;
	}

	public String getReporter() {
		return reporter;
	}

	public String getStatus() {
		return status;
	}

	@JsonIgnore
	public Location getTo() {
		return to;
	}

	public String getToCode() {
		return to != null ? to.getCode() : null;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}

	public void setFrom(Location from) {
		this.from = from;
	}

	public void setReporter(String reporter) {
		this.reporter = reporter;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public void setTo(Location to) {
		this.to = to;
	}

	public Long getDetachmentId() {
		return detachmentId;
	}

	public void setDetachmentId(Long detachmentId) {
		this.detachmentId = detachmentId;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("comments", comments);
		builder.append("date", date);
		builder.append("driver", driver);
		builder.append("from", from);
		builder.append("reporter", reporter);
		builder.append("status", status);
		builder.append("to", to);
		builder.append(super.toString());

		return builder.toString();
	}
}
