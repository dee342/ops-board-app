package gov.nyc.dsny.smart.opsboard.services.sorexecutors;

import gov.nyc.dsny.smart.opsboard.commands.IMultiBoardCommand;
import gov.nyc.dsny.smart.opsboard.commands.LocationCommandMessage;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;

import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class SorExecutor {

	private static final Logger log = LoggerFactory.getLogger(SorExecutor.class);

	@Autowired
	private SimpMessagingTemplate messenger;

	protected void sendCommands(String user, BoardKey boardKey, Map<String, IMultiBoardCommand> commandsMap) {

		for (Entry<String, IMultiBoardCommand> entry : commandsMap.entrySet()) {
			String location = entry.getKey();
			IMultiBoardCommand command = entry.getValue();

			sendCommands(user, boardKey, location, command);
		}
	}

	protected void sendCommands(String user, BoardKey boardKey, String location, IMultiBoardCommand command) {
		LocationCommandMessage message = new LocationCommandMessage(command.getName(), boardKey.getDate(), location,
				user, command);
		messenger.convertAndSend("/topic/tomcat.messages." + location, message);
		log.debug("{} message sent to '{}' board(s).", command.getClass().getName(), location);
	}

	/**
	 * Routing method to send LocationCommandMessages to the owner and current location of command entity (equipment or
	 * person).
	 *
	 * @param user
	 *            user who submitted the command
	 * @param boardKey
	 *            key to uniquely identify a board
	 * @param ownerLocation
	 *            location of the owner on an entity
	 * @param ownerCommand
	 *            command to send to owner location
	 * @param currentLocation
	 *            current location of entity
	 * @param currentCommand
	 *            command to send to current location
	 */
	protected void sendCommands(String user, BoardKey boardKey, String ownerLocation, IMultiBoardCommand ownerCommand,
			String currentLocation, IMultiBoardCommand currentCommand) {
		sendCommands(user, boardKey, ownerLocation, ownerCommand, currentLocation, currentCommand, null, null);
	}

	/**
	 * Routing method to send LocationCommandMessages to the owner, to and from locations for a command entity
	 * (equipment or person).
	 *
	 * @param user
	 *            user who submitted the command
	 * @param boardKey
	 *            key to uniquely identify a board
	 * @param ownerLocation
	 *            location of the owner on an entity
	 * @param ownerCommand
	 *            command to send to owner location
	 * @param fromLocation
	 *            location that the entity is coming from
	 * @param fromCommand
	 *            command to send to "from" location
	 * @param toLocation
	 *            location that the entity is going to
	 * @param toCommand
	 *            command to send to "to" location
	 */
	protected void sendCommands(String user, BoardKey boardKey, String ownerLocation, IMultiBoardCommand ownerCommand,
			String fromLocation, IMultiBoardCommand fromCommand, String toLocation, IMultiBoardCommand toCommand) {

		// Handle owner
		LocationCommandMessage ownerMessage = new LocationCommandMessage(ownerCommand.getName(), boardKey.getDate(),
				ownerLocation, user, ownerCommand);
		messenger.convertAndSend("/topic/tomcat.messages." + ownerLocation, ownerMessage);
		log.debug(ownerCommand.getClass().getName() + " message sent to 'owner' board(s).");

		// Handle "from" locations
		if (fromLocation != null && !ownerLocation.equals(fromLocation)) {
			LocationCommandMessage fromMessage = new LocationCommandMessage(fromCommand.getName(), boardKey.getDate(),
					fromLocation, user, fromCommand);
			messenger.convertAndSend("/topic/tomcat.messages." + fromLocation, fromMessage);
			log.debug(fromCommand.getClass().getName() + " message sent to 'from' board(s).");
		}

		// Handle "to" locations (need to add equipment)
		if (toLocation != null && !ownerLocation.equals(toLocation)) {
			LocationCommandMessage toMessage = new LocationCommandMessage(toCommand.getName(), boardKey.getDate(),
					toLocation, user, toCommand);
			messenger.convertAndSend("/topic/tomcat.messages." + toLocation, toMessage);
			log.debug(toCommand.getClass().getName() + " message sent to 'to' board(s).");
		}

	}

}