/**
 * 
 */
package gov.nyc.dsny.smart.opsboard.commands.interfaces;

import gov.nyc.dsny.smart.opsboard.domain.personnel.BoardPerson;

/**
 * @author nasangameshwaran
 *
 */
public interface IBoardPersonCommand {
	void setBoardPerson(BoardPerson boardPerson);
	BoardPerson getBoardPerson();

}
