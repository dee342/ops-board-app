/**
 * 
 */
package gov.nyc.dsny.smart.opsboard.commands.interfaces;

import gov.nyc.dsny.smart.opsboard.domain.reference.Subcategory;

/**
 * @author nasangameshwaran
 *
 */
public interface ISubCategoryCommand {
	void setSubcategory(Subcategory subcategory);
	Subcategory getSubcategory();
}
