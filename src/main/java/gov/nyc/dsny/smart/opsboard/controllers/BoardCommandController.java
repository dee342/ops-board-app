package gov.nyc.dsny.smart.opsboard.controllers;

import static net.logstash.logback.marker.Markers.appendEntries;
import gov.nyc.dsny.smart.opsboard.IgnoreException;
import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.cache.factories.BoardKeyFactory;
import gov.nyc.dsny.smart.opsboard.cache.gf.board.BoardCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.BoardContainer;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.LocationCache;
import gov.nyc.dsny.smart.opsboard.commands.AbstractBoardCommand;
import gov.nyc.dsny.smart.opsboard.commands.CommandMessage;
import gov.nyc.dsny.smart.opsboard.commands.ReflectionBoardCommandFactory;
import gov.nyc.dsny.smart.opsboard.commands.board.CommandErrorMessage;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;
import gov.nyc.dsny.smart.opsboard.utils.LogContext;






import java.security.Principal;
import java.util.Date;
import java.util.concurrent.CompletableFuture;





import net.logstash.logback.encoder.org.apache.commons.lang.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

/**
 * This controller is the server entry point for all browser commands sent over Web Sockets. It has a single method
 * named receiveCommand to receive commands and process them against a single board.
 */
@Controller
public class BoardCommandController {

	private static final Logger log = LoggerFactory.getLogger(BoardCommandController.class);

	@Autowired
	private BoardCacheService boardsCache;

	@Autowired
	private ApplicationContext ctx;

	@Autowired
	private LocationCache locationCache;

	@Autowired
	private LogContext logContext;

	@Autowired
	private SimpMessagingTemplate messenger;

	@Autowired
	private ReflectionBoardCommandFactory reflectionBoardCommandFactory;
	
	@Autowired
	private BoardKeyFactory boardKeyFactory;

	@MessageExceptionHandler(OpsBoardError.class)
	@SendToUser(value = "/queue/notifications", broadcast = true)
	public CommandMessage handleException(OpsBoardError obe, CommandMessage message, Principal principal) {
		CommandErrorMessage errorCommand = new CommandErrorMessage(message.getCommandContent());
		message.setUser(principal.getName());
		message.setCommandContent(errorCommand);
		message.setCommandName(errorCommand.getName());
		return message;
	}

	/**
	 * Receives a command from the browser, processes against the supplied board (i.e. in message mapping) on the server
	 * and then broadcasts the command to all board clients for processing on the fronted.
	 *
	 * Commands are guaranteed to execute serially on on a board.
	 *
	 * Prior to broadcasting the command to all board clients, the server updates the command with server information
	 * such as the verified user who sent the command and a sequence # indicating the the order in which command was run
	 * on the board.
	 *
	 * @param message
	 *            command message
	 * @param principal
	 *            user submitting the command
	 *
	 * @return if the command is successfully executed on the server, the command is augmented with server data and sent
	 *         to all board clients; if it fails, null is returned.
	 * @throws OpsBoardError
	 */
	@MessageMapping("/commands.{district}.{date}")
	public CommandMessage receiveCommand(CommandMessage message, Principal principal) throws OpsBoardError {

		logContext.initContext(message, principal);

		log.info(appendEntries(logContext), "receiveCommand - User '{}' invokes command '{}'.", principal.getName(),
				message.toString());
		AbstractBoardCommand command = null;
		BoardKey key = null;
		try {

			// Convert message to actual command
			command = reflectionBoardCommandFactory.createCommand(message);
			Date boardDate = DateUtils.toBoardDateNoNull(message.getDate());
			
			// Get board
			key = boardKeyFactory.createBoardKey(boardDate, message.getLocation());
			BoardContainer boardContainer = boardsCache.get(key, false, principal);
			if (boardContainer == null || boardContainer.getBoard() == null) {
				log.error(appendEntries(logContext), "receiveCommand - Board '{}'does not exist.", key);

				// Stop forwarding of message to other clients
				return null;
			}

			// Execute command on board
			synchronized (boardContainer) {
				// Set mandatory fields
				command.setSystemUser(principal.getName());
				command.setSystemDateTime(new Date());

				// Execute command
				command.execute(boardContainer.getBoard());

				// Update command message
				message.setServerSequence(boardContainer.getAutoIncrementSequence());
				message.setUser(command.getSystemUser());
				message.setCommandContent(command);

				// Forward command to queue (and clients)
				log.info(appendEntries(logContext),
						"receiveCommand - Execution completed successfully.  Command forwarded to Queue. Command: {}",
						command);
				
				return message;
			}

		} catch (IgnoreException userException) {
			// This exception is created to take no action in scenarios where exception needs to be ignored eg. AddShift
			// - same shift added by two users on same board.
			log.error(appendEntries(logContext), "OpsBoardError: '{}' ", userException.getMessage(), userException);
			return null;
		} catch (OpsBoardError obe) {
			// Error is already logged.
			log.error(appendEntries(logContext), "OpsBoardError: '{}' ", obe.getMessage(), obe);
			// create error command
			CommandErrorMessage errorCommand = new CommandErrorMessage(command);
			errorCommand.setErrorCode(obe.getCode());
			errorCommand.setErrorMessage(obe.getMessage());
			message.setUser(command.getSystemUser());
			message.setCommandContent(errorCommand);
			message.setCommandName(errorCommand.getName());
			// messenger.convertAndSendToUser(principal.getName(), "/queue/notifications", message);
			messenger.convertAndSend("/topic/user-queue-notifications." + principal.getName() + "."
					+ key.getLocation().getCode() + "." + key.getDate(), message);
			return null;
		} catch (Exception e) {
			log.error(appendEntries(logContext), "Uncaught exception '{}' ", e.getMessage(), e);
			return null;
		}
	}
}
