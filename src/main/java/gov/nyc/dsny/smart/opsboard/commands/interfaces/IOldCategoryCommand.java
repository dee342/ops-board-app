/**
 * 
 */
package gov.nyc.dsny.smart.opsboard.commands.interfaces;

import gov.nyc.dsny.smart.opsboard.domain.reference.Category;

/**
 * @author nasangameshwaran
 *
 */
public interface IOldCategoryCommand {
	void setOldCategory(Category category);
	Category getOldCategory();
}
