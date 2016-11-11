package gov.nyc.dsny.smart.opsboard.commands.admin;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;

import java.util.Date;
import java.util.LinkedHashMap;

/**
 * Command to add an CommandAdminClearCache to a board.
 */
public class CommandAdminClearCache extends AbstractMultiBoardAdminCommand {

	public static final String COMMAND_NAME = "AdminClearCache";
	
	private Date newEffectiveEndDate;

	private static final long serialVersionUID = 1L;

	public CommandAdminClearCache() {
	}

	public CommandAdminClearCache(Date newEffectiveEndDate) {
		this.newEffectiveEndDate = newEffectiveEndDate;
	}
	
	public CommandAdminClearCache(LinkedHashMap<String, Object> map) {
		super(map);
	}

	@Override
	public void execute(Board board) throws OpsBoardError 
	{
//		locationCache.changeEffectiveEndDate(new Date(), newEffectiveEndDate));
	}

	@Override
	public String getName() {
		return COMMAND_NAME;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CommandAdminClearCache []");
		return builder.toString();
	}

	/**
	 * @return the newEffectiveEndDate
	 */
	public Date getNewEffectiveEndDate() {
		return newEffectiveEndDate;
	}

	/**
	 * @param newEffectiveEndDate the newEffectiveEndDate to set
	 */
	public void setNewEffectiveEndDate(Date newEffectiveEndDate) {
		this.newEffectiveEndDate = newEffectiveEndDate;
	}
	
}