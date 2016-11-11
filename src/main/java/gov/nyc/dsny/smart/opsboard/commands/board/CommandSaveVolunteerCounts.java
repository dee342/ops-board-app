package gov.nyc.dsny.smart.opsboard.commands.board;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.AbstractBoardCommand;
import gov.nyc.dsny.smart.opsboard.commands.IBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.persistence.services.board.BoardPersistenceService;

import java.util.LinkedHashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Command to save Add Volunteer Counts
 */
@IBoardCommandAnnotation(commandName = "SaveVolunteerCounts")
public class CommandSaveVolunteerCounts extends AbstractBoardCommand {

	private static final long serialVersionUID = 1L;

	private int chartVolunteers;
	private int mandatoryChart;
	private int vacationVolunteers;

	public CommandSaveVolunteerCounts(String boardId, LinkedHashMap<String, Object> content) {
		super(boardId, content);
		chartVolunteers = (Integer) content.get("chartVolunteers");
		mandatoryChart = (Integer) content.get("mandatoryChart");
		vacationVolunteers = (Integer) content.get("vacationVolunteers");
	}

	@Override
	public void execute(Board board) throws OpsBoardError {

		// Execute logic
		board.saveVolunteerCounts(chartVolunteers, mandatoryChart, vacationVolunteers);

		// Create audit message
		createAuditMessage(board);

		// Add command to history
		board.addCommandToHistory(this);
	}

	public int getChartVolunteers() {
		return chartVolunteers;
	}

	public int getMandatoryChart() {
		return mandatoryChart;
	}

	public int getVacationVolunteers() {
		return vacationVolunteers;
	}

	@Override
	public void persist(Board board, BoardPersistenceService persistService) throws OpsBoardError {
		persistService.save(board);
	}

	public void setChartVolunteers(int chartVolunteers) {
		this.chartVolunteers = chartVolunteers;
	}

	public void setMandatoryChart(int mandatoryChart) {
		this.mandatoryChart = mandatoryChart;
	}

	public void setVacationVolunteers(int vacationVolunteers) {
		this.vacationVolunteers = vacationVolunteers;
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("chartVolunteers", chartVolunteers);
		builder.append("mandatoryChart", mandatoryChart);
		builder.append("vacationVolunteers", vacationVolunteers);
		builder.append(super.toString());

		return builder.toString();
	}

	@Override
	protected void createAuditMessage(Board board) {
		StringBuilder sb = new StringBuilder();
		sb.append("Added volunter counts.");

		setAuditMessage(sb.toString());
	};
}
