'use strict';

angular.module('OpsBoard')
  .service(
    'RecentActivityLoggingService',
    function(UtilityService) {

      var _logAction = function(actions, user, action, entityType, entityId, entityName, direction, task, commandDate) {
        actions[actions.length] = {
          'user': user,
          'action': action,
          'entity': {
            'type': entityType,
            'id': entityId,
            'name': entityName
          },
          'direction': direction,
          'task': task,
          'date': moment(commandDate).format('MM/DD/YYYY HH:mm'),
          'day': moment(commandDate).format('MM/DD/YYYY'),
          'time': moment(commandDate).format('HH:mm')
        }
      }

      var _writeAuditMessage = function(log, datetime, user, message, referenceData) {
        log[log.length] = {
            'user': user,
            'date': moment(datetime).format('MM/DD/YYYY HH:mm'),
            'day': moment(datetime).format('MM/DD/YYYY'),
            'time': moment(datetime).format('HH:mm'),
            'message': deconstruct(message, referenceData)
        }
      }
        
      var deconstruct = function(message, referenceData) {
          var results = [];
          
          var parts = message.split(' ');
          for (var i = 0; i < parts.length; i++) {
            var part = parts[i];
            if (part.length > 0) {
              var link = part.match(/^\[.*\]/);
              var info;
              if (!link) {
                info = UtilityService.populateReference('message', part, referenceData);
                results.push({'type': 'message', 'data' : part, 'text': info.data, 'msgType': info.msgType});
              } else {
                var trimmedLink = link[0].substring(1, link[0].length - 1);
                var temp = trimmedLink.split(':');
                info = UtilityService.populateReference(temp[0], temp[1], referenceData)
                results.push({'type': temp[0], 'data' : temp[1], 'text': info.data, 'msgType': info.msgType});
                
                if (part.length > link[0].length) {
                  var additionalMessage = part.substring(link[0].length, part.length);
                  info = UtilityService.populateReference('message', additionalMessage, referenceData)
                  results.push({'type': 'message', 'data' : additionalMessage, 'text': info.data, 'msgType': info.msgType});
                }
              }
            }
          }
          
          return results;
      }
      
      return {
        
        writeAuditMessage: function(log, datetime, user, message, referenceData) {
          _writeAuditMessage(log, datetime, user, message, referenceData);
        },

        logAction: function(actions, user, action, entityType, entityId, entityName, direction, task, commandDate) {
          //_logAction(actions, user, action, entityType, entityId, entityName, direction, task, commandDate)
        },

        getTaskFromTaskCommand: function(command) {
          return command.commandContent.commandHistoryMessage && command.commandContent.commandHistoryMessage.actionTaken;
        },
        
        getTaskNameFromTaskCommand: function(command) {
            return command.commandContent.commandHistoryMessage && command.commandContent.commandHistoryMessage.actionInfo;
          },

        formattedHist: function(commandHistory, referenceData) {
          var log = [];
          angular.forEach(commandHistory, function(command, key) {
            if (command.commandContent.auditMessage)
              _writeAuditMessage(log, command.commandContent.systemDateTime, command.commandContent.systemUser, command.commandContent.auditMessage, referenceData);
          });
          
          return log;
        },

        processAddOnlineUser: function(onlineUsers, command) {
          var value = command.commandContent.session;
          if (onlineUsers[value.httpSessionId]) {
            onlineUsers[value.httpSessionId].sessions = Number(onlineUsers[value.httpSessionId].sessions) + 1;
          } else {
            if (value.httpSessionId)
              onlineUsers[value.httpSessionId] = {
                userId: value.loggedUserId,
                remoteAddr: value.remoteAddr,
                sessions: '1'
              }
          }
        },

        processRemoveOnlineUser: function(onlineUsers, command) {
          var value = command.commandContent;
          if (onlineUsers[value.httpSessionId]) {
            if (Number(onlineUsers[value.httpSessionId].sessions) - 1 <= 0)
              delete onlineUsers[value.httpSessionId]
            else
              onlineUsers[value.httpSessionId].sessions = Number(onlineUsers[value.httpSessionId].sessions) - 1;
          }
        }
      }

    })