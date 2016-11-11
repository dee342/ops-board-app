'use strict';

angular.module('OpsBoard').factory('EquipmentModel', function(detachmentHistory) {
  return {

    total : function() {
      return this.detachmentHistory.length;
    },
    getKey : function() {
      return this.id.split('_')[0];
    },

    getFormattedPendingLoadStatus : function(status) {
      var formattedStatus = '';
      if (status === 'L') {
        formattedStatus = 'Empty';
      } else {
        formattedStatus = status;
      }
      return formattedStatus;
    },

    getFormattedBins : function(materials) {
      var results = []

      if (!this.bins || this.bins.length == 0 || !materials)
        return results;

      for (var i = 0; i < this.bins.length; i++) {
        var materialDetails = {};
        for (var j in materials) {
          if (materials.hasOwnProperty(j)) {
            if (this.bins[i].material && this.bins[i].material == materials[j].materialType) {
              materialDetails = materials[j];
            }  
          }   
        }


        var displayName = this.bins[i].name === 'BIN 1' ? 'Large Side' : 'Small Side';

        results[i] = {
          status : this.getFormattedPendingLoadStatus(this.bins[i].status),
          name : this.bins[i].name,
          displayName : displayName,
          descr : materialDetails.descr,
          materialType : materialDetails.materialType,
          uniqueId : materialDetails.uniqueId,
          lastUpdated : moment(this.bins[i].lastModifiedSystem).format('MM/DD/YYYY HH:mm:ss'),
          lastUpdatedBy : this.bins[i].systemUser
        }
      }
      return results;
    },
    getFormattedDetachments : function() {
      var formattedHist = [];

      if (!this.detachmentHistory || this.detachmentHistory.length == 0) {
        return formattedHist;
      }
      var hist = this.detachmentHistory;

      for (var i = 0; i < hist.length; i++) {
        var history = {
          id: hist[i].id,
          lastModifiedSystemNonFormatted : hist[i].lastModifiedSystem,
          lastModifiedSystem : moment(hist[i].lastModifiedSystem).format('MM/DD/YYYY HH:mm:ss'),
          lastModifiedActual : hist[i].lastModifiedActual,
          lastSystemDate : hist[i].lastModifiedActual,
          status : detachmentHistory.equipment[hist[i].status],
          remarks : hist[i].comments,
          reporter : hist[i].systemUser,
          from : hist[i].from,
          to : hist[i].to,
          driver : hist[i].driver
        };
        if(hist[i].status !== 'P') {
          history.from = '';
          history.to = '';
          history.driver = ''; 
        }

        formattedHist.push(history);
      }
      return this.getSortedDetachmentHistory(formattedHist);
    },

    getUpDownCondition : function() {
      var results = {
        condition : "Up",
        lastUpdated : null,
        lastUpdatedBy : ""
      };

      if (!this.upDownHistory || this.upDownHistory.length == 0) {
        return results;
      }

      var upDown = this.getFormattedUpDownHistory()[0];
      if (upDown.action)
        results.condition = "Down";
      else
        results.condition = "Up";

      if (upDown) {
        results.lastUpdated = upDown.time;
        results.lastUpdatedBy = upDown.systemUser;
      }

      return results;
    },

    getFormattedUpDownHistory : function() {
      var upDown = [];

      if (!this.upDownHistory || this.upDownHistory.length == 0)
        return upDown;

      var oriUpDown = this.upDownHistory;
      for (var i = 0; i < oriUpDown.length; i++) {
        var conditions = oriUpDown[i].conditions;
        var downStatus = oriUpDown[i].down;
        if (conditions.length == 1) {
          var singleConditions = {
            action : downStatus,
            date : conditions[0].lastModifiedActual,
            time : conditions[0].lastModifiedSystem,
            downCode : conditions[0].downCode,
            location : conditions[0].repairLocation,
            reporter : conditions[0].actualUser,
            mechanic : conditions[0].mechanic,
            remarks : conditions[0].comments,
            id: conditions[0].id,
            systemUser : conditions[0].systemUser
          };
          upDown.push(singleConditions);

        } else {
          for (var j = 0; j < conditions.length; j++) {
            var multiConditions = {
              action : downStatus,
              date : conditions[j].lastModifiedActual,
              time : conditions[j].lastModifiedSystem,
              downCode : conditions[j].downCode,
              location : conditions[j].repairLocation,
              reporter : conditions[j].actualUser,
              mechanic : conditions[j].mechanic,
              remarks : conditions[j].comments,
              id: conditions[j].id,
              systemUser : conditions[j].systemUser

            };
            upDown.push(multiConditions);
          }
        }

      }

      return this.getSortedUpDownHistory(upDown);
    },

    getFormattedUpDownConditions: function (items) {
      var conditions = [];
      for (var i = 0; i < items.length; i++) {
        conditions.push({
          action : items[i].down,
          date : items[i].lastModifiedActual,
          time : items[i].lastModifiedSystem,
          downCode : items[i].downCode,
          location : items[i].repairLocation,
          reporter : items[i].actualUser,
          mechanic : items[i].mechanic,
          remarks : items[i].comments,
          id: items[i].id,
          systemUser : items[i].systemUser
        })
      }
      return this.getSortedUpDownHistory(conditions);
    },

    getSortedUpDownHistory : function(upDown) {
      return upDown.sort(function(me, that) {

        if (that == null)
          return 1;

        if (me.id > that.id)
          return -1;
        
        if (me.id < that.id)
          return 1;

        if (me.id == that.id)
          return -1;

        return 0;

/*

        // Sort by system time
        if (me.time != null && that.time != null) {
          if (me.time < that.time)
            return 1;
          else if (me.time > that.time)
            return -1;
        }

        if (me.time != null && that.time != null) {
          if (me.time < that.time)
            return 1;
          else if (me.time > that.time)
            return -1;
        }

        // Sort by actual date
        if (me.date == null && that.date != null)
          return -1;
        else if (me.date != null && that.date == null)
          return 1;
        
        if (me.date != null && that.date != null) {
         if (me.date < that.date)
          return 1;
        else if (me.date > that.date)
          return -1;
          }

        // Sort by status (up or down)
        if (!me.action && that.action)
          return -1;
        else if (me.action && !that.action)
          return 1;

        return 0;*/
      })
    },

    getSortedDetachmentHistory : function(detachment) {
      return detachment.sort(function(me, that) {

        if (that == null)
          return 1;

        // Now sort by lastModifiedSystem
        if (me.lastModifiedSystemNonFormatted == null && that.lastModifiedSystemNonFormatted != null)
          return -1;
        else if (me.lastModifiedSystemNonFormatted != null && that.lastModifiedSystemNonFormatted == null)
          return 1;

        if (me.lastModifiedSystemNonFormatted != null && that.lastModifiedSystemNonFormatted != null) {
          if (me.lastModifiedSystemNonFormatted < that.lastModifiedSystemNonFormatted)
            return 1;
          else if (me.lastModifiedSystemNonFormatted > that.lastModifiedSystemNonFormatted)
            return -1;
        }

        return 0;

      })
    },

    updateState : function(assigned, states) {
      this.assigned = assigned;
      this.states = states;
    }

  }
});