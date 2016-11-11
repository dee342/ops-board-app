package gov.nyc.dsny.smart.opsboard.misc;

import gov.nyc.dsny.smart.opsboard.OpsBoardError;
import gov.nyc.dsny.smart.opsboard.cache.factories.BoardKeyFactory;
import gov.nyc.dsny.smart.opsboard.cache.gf.board.BoardCacheService;
import gov.nyc.dsny.smart.opsboard.cache.gf.reference.BoardContainer;
import gov.nyc.dsny.smart.opsboard.commands.AbstractBoardCommand;
import gov.nyc.dsny.smart.opsboard.commands.AbstractMultiBoardCommand;
import gov.nyc.dsny.smart.opsboard.commands.CommandMessage;
import gov.nyc.dsny.smart.opsboard.commands.IMultiBoardCommandAnnotation;
import gov.nyc.dsny.smart.opsboard.commands.ReflectionBoardCommandFactory;
import gov.nyc.dsny.smart.opsboard.domain.board.Board;
import gov.nyc.dsny.smart.opsboard.domain.board.BoardKey;
import gov.nyc.dsny.smart.opsboard.util.DateUtils;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SubCommandAspect {

	@Autowired
	private BoardCacheService boardsCache;

	@Autowired
	private BoardKeyFactory boardKeyFactory;

	@Autowired
	private SimpMessagingTemplate messenger;

	@Autowired
	private ReflectionBoardCommandFactory reflectionBoardCommandFactory;

	@AfterReturning(pointcut = "execution(* gov.nyc.dsny.smart.opsboard.controllers.BoardCommandController.receiveCommand(..))", returning = "message")
	public void postProcess(JoinPoint jp, CommandMessage message) throws Throwable {
		if(message == null)
			return;
		
		AbstractBoardCommand command = (AbstractBoardCommand) message.getCommandContent();

		if (!command.getSubCommands().isEmpty()) {
			for (AbstractBoardCommand cmd : command.getSubCommands()) {
				BoardKey commandBasedKey = boardKeyFactory.createBoardKey(
						DateUtils.toBoardDate(Board.boardIdToBoardDate(cmd.getBoardId())),
						Board.boardIdToLocation(cmd.getBoardId()));

				if (boardsCache.isInCache(commandBasedKey)) {
					BoardContainer commandBasedContainer = boardsCache.get(commandBasedKey);
					synchronized (commandBasedContainer) {
						cmd.execute(commandBasedContainer.getBoard());
						CommandMessage boardCommandMessage = new CommandMessage();
						boardCommandMessage.setCommandName(cmd.getName());
						boardCommandMessage.setCommandContent(cmd);
						boardCommandMessage.setDate(commandBasedKey.getDate());
						boardCommandMessage.setLocation(commandBasedKey.getLocation().getCode());
						boardCommandMessage.setServerSequence(commandBasedContainer.getAutoIncrementSequence());
						boardCommandMessage.setUser(message.getUser());
						// Send to clients
						String topic = "/topic/commands." + boardCommandMessage.getLocation() + "."
								+ commandBasedKey.getDate();
						messenger.convertAndSend(topic, boardCommandMessage);
					}				
				}
			}
		}
	}

	public static void postProcessMulti(BoardCacheService boardsCache, BoardKeyFactory boardKeyFactory, SimpMessagingTemplate messenger, AbstractMultiBoardCommand command) throws OpsBoardError {

		if (!command.getSubCommands().isEmpty()) {
			for (AbstractBoardCommand cmd : command.getSubCommands()) {
				BoardKey commandBasedKey = boardKeyFactory.createBoardKey(
						DateUtils.toBoardDate(Board.boardIdToBoardDate(cmd.getBoardId())),
						Board.boardIdToLocation(cmd.getBoardId()));

				if (boardsCache.isInCache(commandBasedKey)) {
					BoardContainer commandBasedContainer = boardsCache.get(commandBasedKey);
					synchronized (commandBasedContainer) {
						cmd.execute(commandBasedContainer.getBoard());
						CommandMessage boardCommandMessage = new CommandMessage();
						boardCommandMessage.setCommandName(cmd.getName());
						boardCommandMessage.setCommandContent(cmd);
						boardCommandMessage.setDate(commandBasedKey.getDate());
						boardCommandMessage.setLocation(commandBasedKey.getLocation().getCode());
						boardCommandMessage.setServerSequence(commandBasedContainer.getAutoIncrementSequence());
						boardCommandMessage.setUser(command.getSystemUser());
						// Send to clients
						String topic = "/topic/commands." + boardCommandMessage.getLocation() + "."
								+ commandBasedKey.getDate();
						messenger.convertAndSend(topic, boardCommandMessage);
					}				
				}
			}
		}
	}

}
