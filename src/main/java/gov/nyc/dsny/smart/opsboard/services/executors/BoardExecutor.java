package gov.nyc.dsny.smart.opsboard.services.executors;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.commands.CommandMessage;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public abstract class BoardExecutor {

	private static final Logger log = LoggerFactory.getLogger(BoardExecutor.class);

	@Autowired
	private SimpMessagingTemplate messenger;

	protected void sendCommand(String commandName, Object commandContent, Board board, String user)
			throws OpsBoardError {

		CommandMessage message = new CommandMessage();
		message.setCommandName(commandName);
		message.setCommandContent(commandContent);
		message.setDate(board.getDate());
		message.setLocation(board.getLocation().getCode());
		message.setUser(user);
		log.debug("Sending message to topic " + "/topic/commands." + board.getLocation().getCode() + "."
				+ board.getDate() + " " + message);
		messenger.convertAndSend("/topic/commands." + board.getLocation().getCode() + "." + board.getDate(), message);
	}

}
