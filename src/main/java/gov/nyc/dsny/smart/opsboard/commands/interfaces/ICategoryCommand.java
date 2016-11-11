/**
 * 
 */
package gov.nyc.dsny.smart.opsboard.commands.interfaces;

import gov.nyc.dsny.smart.opsboard.domain.reference.Category;

/**
 * @author nasangameshwaran
 *
 */
public interface ICategoryCommand {
	void setCategory(Category category);
	Category getCategory();
}
