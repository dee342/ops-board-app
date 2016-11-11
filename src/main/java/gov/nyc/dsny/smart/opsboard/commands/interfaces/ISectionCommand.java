/**
 * 
 */
package gov.nyc.dsny.smart.opsboard.commands.interfaces;

import gov.nyc.dsny.smart.opsboard.domain.reference.Section;

/**
 * @author nasangameshwaran
 *
 */
public interface ISectionCommand {
	void setSection(Section section);
	Section getSection();
}
