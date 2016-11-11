/**
 * 
 */
package gov.nyc.dsny.smart.opsboard.commands.interfaces;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.cache.gf.BoardPersonnelCacheService;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;

/**
 * @author nasangameshwaran
 *
 */
public interface IRemovePersonCommand {
	void removePersonFromCache(BoardPersonnelCacheService boardPersonnelCache, BoardKey boardKey, String personId) throws OpsBoardError;
}
