'use strict';

angular.module('OpsBoard')
  .factory('PersonModel', function (states, groups, BoardDataService) {
    var canvas = document.createElement("canvas");
    function isDetachmentInFuture(detachment) {
      return detachment.displayStatus === 'Future';
    }
    function isDetachmentCompleted(detachment) {
      return detachment.displayStatus === 'Completed';
    }

    return {
      getFormattedName: function() {
        var lastName = this.lastName,
          firstName = this.firstName ? this.firstName.substring(0, 1).toUpperCase() : '';

        var name = firstName ? firstName + '. ' + lastName : lastName;
        var context = canvas.getContext("2d");
        context.font = "400 12px 'Open Sans', sans-serif";
        var metrics = context.measureText(name)
        if (metrics && metrics.width > 80) return this.lastName.substring(0,12);
        return name;
      },

      getIndicatorText: function() {
      if (this.grounded) {
          return 'G';
        }
        
        if (!this.isActiveMDA() && this.activeMdaCodes && this.activeMdaCodes.length && this.activeMdaCodes.length > 0)
          return this.activeMdaCodes[0].subType;
        
        if(this.availableNextDay)
          return 'N';

        if (this.activeSpecialPositions && this.activeSpecialPositions.length > 0)
          return this.activeSpecialPositions[0].code;

      
        return '';
      },
      
      getIndicatorBox: function() {
        //var suspended = false;

        if ((!this.activeMdaCodes || this.activeMdaCodes.length === 0)
          && (!this.activeSpecialPositions || this.activeSpecialPositions.length === 0)
          && (!this.grounded)) {

          return 'nobox';
        }

        if (this.activeSpecialPositions && this.activeSpecialPositions.length > 0) {
          /*angular.forEach(this.activeUnavailabilityReasons, function (reason) {
            if (reason.code === 'SUSPENDED') {
              suspended = true;
            }
          });*/

          if (!this.grounded) { // && suspended === false
            return 'nobox';
          } else {
            return 'redbox';
          }
        } else {
          if (!this.grounded) {
            if (!this.activeMdaCodes || !this.activeMdaCodes.length || this.activeMdaCodes.length === 0) {
              return 'nobox same';
            } else if (!this.isActiveMDA()) {
              return 'nobox same mda';
            }

          } else if(this.grounded) {
            var showBox = false;
            var groundingHistory = this.groundingHistory;
            Object.keys(groundingHistory).forEach(function(key){
              if(groundingHistory[key].removedFlag == false && this.grounded){
                showBox = true;
              }
            }.bind(this))
            if(showBox)
              return 'nobox red';
            else 
                return 'nobox';
          }else{

              if (!this.activeMdaCodes || this.activeMdaCodes.length === 0) {
                return 'nobox red';
              } else {
                return 'nobox red mda';
              }
            
          }

          return 'nobox';
        }
      },

      getFormattedDateObj: function(){
          var pathElements = window.location.pathname.split('/');
          var boardDate = pathElements[3];
          var year = parseInt(boardDate.substring(0,4));
          var month = parseInt(this.boardDate.substring(4,6))-1;
          var date = parseInt(this.boardDate.substring(6,8));
          var formattedDateObj = {
            now: new Date(),
            boardDateDt: new Date(year, month, date, 0, 0, 0)
          }
          return formattedDateObj;
        },

      getFormattedUnavailableReasons: function(list) {
        var reasons = [];
        var action = "";
        var formattedDateObj = this.getFormattedDateObj();

        if (!this.unavailabilityHistory || this.unavailabilityHistory.length <= 0)
          return reasons;
        var unavailableReasons = this.getSortedUnavailabilityHistory(list);
        for (var i = 0; i < unavailableReasons.length; i++) {
          var canceleditable=false;
          var undocanceleditable = false;
          var sdt = unavailableReasons[i].start,
            edt = unavailableReasons[i].end,
            sd = '',
            st = '',
            ed = '',
            et = '';
          if (sdt && moment(sdt).isValid()) {
            sd = moment(sdt).format('MM/DD/YYYY');
            st = moment(sdt).format('HH:mm');
          }
          if (edt && moment(edt).isValid()) {
            ed = moment(edt).format('MM/DD/YYYY');
            et = moment(edt).format('HH:mm');
          }
          var displayStatus = "";
          var isActive = false;
          var startDate = new Date(sdt);
          var endDate = null;
          if(edt != null)
            endDate = new Date(edt);

          var start = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate());
          var editable = true;

          if (unavailableReasons[i].status == 'R') {
            action = "Removed";
          } else if (unavailableReasons[i].action == 'C') {
            action = 'Cancelled';
          } else if (unavailableReasons[i].replaces) {
            if (unavailableReasons[i].action == 'R') {
              action = 'Reinstated';
            } else {
              action = "Updated";
            }
          } else {
            action = "Added";
          }

          var displayStatus = "";

          //if (unavailableReasons[i].replacedBy) {
          //     displayStatus = "";

          if (unavailableReasons[i].status == 'I') {
            displayStatus='';
          } else if (unavailableReasons[i].status == 'R') {
            displayStatus = "Deleted";
          } else {
            if (start.valueOf() > formattedDateObj.boardDateDt.valueOf()) {
              displayStatus = 'Future';
            } else if (endDate != null && (moment(formattedDateObj.boardDateDt).startOf('day').diff(moment(edt).startOf('day'), 'days') > 0 || (moment(edt).diff(moment(formattedDateObj.now), 'days') == 0 && moment(edt).diff(moment(formattedDateObj.now), 'minutes') == 0))) {
              displayStatus = 'Completed';
              if ((moment(edt).diff(moment(formattedDateObj.now), 'days') == 0 && moment(edt).diff(moment(formattedDateObj.now), 'minutes') == 0)) {
                for (var j = 0; j < this.activeUnavailabilityReasons.length; j++) {
                  //console.log('this.activeUnavailabilityReasons[j].id' + this.activeUnavailabilityReasons[j].id + 'unavailableReasons[j].id' + unavailableReasons[j].id)
                  if (this.activeUnavailabilityReasons[j].id == unavailableReasons[i].id) {
                    this.activeUnavailabilityReasons[j].completed = true;
                  }
                }
              }
            } else  {
              displayStatus = 'Active';
              isActive = true;
            }
          }

          /* editable=true;*/

          if(displayStatus !== 'Active')
            editable = false;

          if(displayStatus === 'Future')
            editable = true;

          if(action !== 'Added' && action != 'Updated'){
            editable = false;
          }

          if(editable && this.isTypeValidForCancelButton(unavailableReasons[i].code)) {
            canceleditable = true;
          }

          if (action === 'Cancelled' && (displayStatus === 'Active' || displayStatus === 'Future') ) {
            undocanceleditable = true;
          }

          if (action === 'Reinstated') {
            editable = true;
            canceleditable = true;
          }

          reasons.push({
            code: unavailableReasons[i].code,
            startDate: sd,
            startTime: st,
            endDate: ed,
            endTime: et,
            id: unavailableReasons[i].id,
            status: unavailableReasons[i].status,
            dispStatus: displayStatus,
            action: action,
            isActive: isActive,
            comments: unavailableReasons[i].comments,
            reasonForChange : unavailableReasons[i].reasonForChange,
            editable: editable,
            canceleditable: canceleditable,
            undocanceleditable: undocanceleditable
          })
        }

        reasons = _.uniq(reasons);

        return reasons;
      },
      isTypeValidForCancelButton:function(code){
        if(code === 'CHART' || code === 'VACATION' || code === 'JURY DUTY'){
          return true;
        }else{
          return false;
        }
      },

      getFormattedSpecialPositions: function() {
        var formattedSpecialPositions = [];
        var action = "";
        var formattedDateObj = this.getFormattedDateObj();

        if (!this.specialPositionsHistory || this.specialPositionsHistory.length <= 0)
          return formattedSpecialPositions;
        var specialPositions = this.getSortedSpecialPositionsHistory();
        for (var i = 0; i < specialPositions.length; i++) {
          var sdt = specialPositions[i].startDate,
            edt = specialPositions[i].endDate,
            sd = '',
            ed = '';
          if (sdt && moment(sdt).isValid()) {
            sd = moment(sdt).format('MM/DD/YYYY');
          }
          if (edt && moment(edt).isValid()) {
            ed = moment(edt).format('MM/DD/YYYY');
          }
          var displayStatus = "";
          var action = "";
          var isActive = false;

          var startDate = new Date(sdt);
          var endDate = null;
          if(edt != null)
            endDate = new Date(edt);

          var start = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate());
          var end = null;
          if(endDate != null)
            end = new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate());
          var editable=true;

          if (specialPositions[i].status == 'R'){
            action = "Removed";
          }else if (specialPositions[i].replaces){
            action = "Updated";
          }else{
            action = "Added";
          }

          var displayStatus = "";
          if (specialPositions[i].replacedBy){
            displayStatus = "";
          }else if (specialPositions[i].status == 'R'){
            displayStatus = "Deleted";
          }else if (start.valueOf() > formattedDateObj.boardDateDt.valueOf()){
            displayStatus = 'Future';
          }else if (endDate != null && formattedDateObj.boardDateDt.getTime() > end.getTime()){
            displayStatus = 'Completed';
          }else{
            displayStatus = 'Active';
            isActive = true;
          }

          if (this.civilServiceTitle === groups.personnel.supervisors ||
            this.civilServiceTitle === groups.personnel.superintendents ||
            specialPositions[i].status !== 'A' || specialPositions[i].officerPosition ||
            displayStatus === 'Completed') {
            editable = false;
          }

          formattedSpecialPositions.push({
            code: specialPositions[i].code,
            description : specialPositions[i].description,
            startDate: sd,
            endDate: ed,
            action: action,
            isActive: isActive,
            id: specialPositions[i].id,
            status: specialPositions[i].status,
            dispStatus: displayStatus,
            comments: specialPositions[i].comments,
            reasonForChange : specialPositions[i].reasonForChange,
            editable:editable
          })
        }

        return formattedSpecialPositions;
      },

      getSortedSpecialPositionsHistory: function() {
        return this.specialPositionsHistory.sort(function(me, that) {

            if (that == null)
              return 1;

            // Now sort by start date
            if (me.startDate == null && that.startDate != null)
              return -1;
            else if (me.startDate != null && that.startDate == null)
              return 1;

            if (me.startDate != null && that.startDate != null) {
              if (me.startDate < that.startDate)
                return 1;
              else if (me.startDate > that.startDate)
                return -1;
            }

            // Handle infinite Special Position period
            if (me.endDate == null && that.endDate != null)
              return -1;
            else if (me.endDate != null && that.endDate == null)
              return 1;

            //Handle change type (same day)
            if(me.lastModifiedSystem<that.lastModifiedSystem)
              return 1;
            else
              return -1;

          }
        )
      },

      getFormattedDetachmentHistory: function() {
        var detachments = [];

        var boardData = BoardDataService.getBoardData();
        var boardLocation = boardData.boardLocation; 
        
        //Amanda
        var formattedDateObj = this.getFormattedDateObj();

        if (!this.detachmentHistory || this.detachmentHistory.length <= 0)
          return detachments;
        var detachmentHist = this.getSortedDetachmentHistory();
        
        for (var i = 0 ; i < detachmentHist.length; i++) {
          
          var displayStatus = "";
          var status = "";
          var editable = false;
          var action = "";          
          var from = detachmentHist[i].from;
          var to = detachmentHist[i].to;          
            var sdt = detachmentHist[i].startDate,
            edt = detachmentHist[i].endDate,
            sd,
            ed;

          if (sdt && moment(sdt).isValid())
            sd = moment(sdt).format('MM/DD/YYYY');

          if (edt && moment(edt).isValid())
            ed = moment(edt).format('MM/DD/YYYY');
          /* var boardDate = new Date($scope.board.displayDate);*/
       
          var startDate = new Date(sdt);
          var endDate = null;
          var endDate = new Date(edt);
          var start = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate());
          var end = null;
          var end = new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate());
  
          status = detachmentHist[i].status;

          if(status=='R'){
           displayStatus='Deleted';
          }else if(start.valueOf()>formattedDateObj.boardDateDt.valueOf()){
            displayStatus='Future';
          }else if(edt != null && formattedDateObj.boardDateDt.valueOf()>end.valueOf()){
            displayStatus='Completed';
          }else if(formattedDateObj.boardDateDt.getTime()>=start.getTime() && (edt == null || formattedDateObj.boardDateDt.getTime()<=end.getTime())){
            displayStatus='Active';
          }
          
          if(status == 'R'){
              action = "Removed";
          }else{
              action = "Detached";
          }  

           
           var hist = {
             action:action,
             code: detachmentHist[i].code,
             from: from,
             to: to,
             start: sd,
             end: ed,
             shift: detachmentHist[i].shiftId,
             comments: detachmentHist[i].comments,
             id:detachmentHist[i].id,
             status: status,
             displayStatus:displayStatus,
             reasonForChange:detachmentHist[i].reasonForChange,
             editable: false,
             removable: false
           }           
           
          detachments.push(hist);
        }
        
        var today = new Date();
        var todayAM = new Date(today.getFullYear(), today.getMonth(), today.getDate());
     
        //sort it based on last modified
        detachmentHist = this.getSortedDetachmentHistoryOnLastModified();
        for (var i = 0, len = detachmentHist.length; i < len; i++) {
          //pick the very first active or future
          var detachment = detachments.filter(function(d){
            return d.id == detachmentHist[i].id;
          });

          if (detachment.length) {

              var detachStart = (detachmentHist[i].startDate != null) ? new Date(detachmentHist[i].startDate) : null;
              var detachEnd = (detachmentHist[i].endDate != null) ? new Date(detachmentHist[i].endDate) : null;        	  
        	  
              //if it is open as in Active/Future ( not completed) and not a removed record
              if((detachEnd == null || detachEnd >= todayAM.getTime()) &&  detachment[0].status !== 'R'){
            	  //if it is future
            	  if(detachStart > todayAM.getTime()){
            		  //from, to or homelocation
                      if ( detachment[0].from === boardLocation || detachment[0].to === boardLocation  || this.homeLocation === boardLocation){
                    	  detachment[0].removable = true;
                    	  // not interested past this
                    	  break; 
                       }            		  
            		  
            	  } else { //if it is Active SingleDay/Multiple
            		  //receiving location (to location)
                      if (detachment[0].to === boardLocation){
                    	  
                    	  // single day current only show remove
                          if ( detachStart.getTime() === todayAM.getTime() && ( detachEnd &&  detachEnd.getTime() === todayAM.getTime())){
                        	  detachment[0].removable = true;
                           } else { //muliple - show edit
                        	   detachment[0].editable = true;
                           }
                            // not interested past this
                            break;
                       } else {
                    	   //home or from location
                    	  if(detachment[0].from === boardLocation || this.homeLocation === boardLocation) {
                    		  // ends later than today
                    			if(  detachEnd == null || detachEnd.getTime() > todayAM.getTime() )  {
                    				detachment[0].editable = true;
                    			}
                    			//if it is home location or receiving location and it we are past a single day detachment
                    			//or we have made it editable already
                    			// not interested past this
                    			break; 
                    	  }
                      }
            	  }
            	  
            	  
              }        	  
            } // fi

        }
        return detachments;
      },

      getHomeAddress: function(info) {
        if (!this.addresses || this.addresses.length <= 0) return '';
        var addrArr = this.addresses;
        if (!info) info = 'address1';
        if (info === 'full' && addrArr[0]) {
          return addrArr[0].address1 + ', ' + addrArr[0].city + ', ' + addrArr[0].state;
        }
        for (var i = 0; i < addrArr.length; i++) {
          if (addrArr[i] && addrArr[i].type && addrArr[i].type.toLowerCase() === 'home') {
            if (addrArr[i][info]) return addrArr[i][info];
          }
        }
        return '';
      },

      getFormattedMdaStatus: function() {
        var mdaStatusRecords = [];
        var action = "";
        var formattedDateObj = this.getFormattedDateObj();

        if (!this.mdaStatusHistory || this.mdaStatusHistory.length <= 0)
          return mdaStatusRecords;
        var mdaRecords = this.getSortedmdaHistory();
        for(var i=0; i < mdaRecords.length; i++){
          var mdaRecord = mdaRecords[i];
          if (mdaRecord && mdaRecord.type === 'MDA') {
            var sdt = mdaRecord.startDate,
              edt = mdaRecord.endDate,
              hadt = mdaRecord.appointmentDate,
              status = mdaRecord.status,
              sd = '',
              ed = '',
              et = '',
              haptd = '',
              mdas = '';
            if (sdt && moment(sdt).isValid()) {
              sd = moment(sdt).format('MM/DD/YYYY');
            }
            if (edt && moment(edt).isValid()) {
              ed = moment(edt).format('MM/DD/YYYY');
              et = moment(edt).format('HH:mm');

            }
            if (hadt && moment(hadt).isValid()) {
              haptd = moment(hadt).format('MM/DD/YYYY');

            }
            if(status != undefined){
              mdas = mdaRecord.status;
            }

            var startDate = new Date(sdt);
            var endDate = null;
            if(edt != null)
              endDate = new Date(edt);

            var start = new Date(startDate.getFullYear(), startDate.getMonth(), startDate.getDate());
            var end = null;
            if(endDate != null)
              end = new Date(endDate.getFullYear(), endDate.getMonth(), endDate.getDate());
            var editable=true;

            if (mdaRecord.status == 'R'){
              action = "Removed";
            }else if (mdaRecord.replaces){
              action = "Updated";
            }else{
              action = "Added";
            }

            var displayStatus = "";
            if (mdaRecord.replacedBy) {
              displayStatus = "";
            } else if (mdaRecord.status == 'R') {
              displayStatus = "Deleted";
            } else {
              if (start.valueOf() > formattedDateObj.boardDateDt.valueOf()){
                displayStatus = 'Future';
              }else if (endDate != null && (formattedDateObj.boardDateDt.getTime() > end.getTime() || formattedDateObj.now.getTime() > endDate.getTime())){
                displayStatus = 'Completed';
              }else{
                displayStatus = 'Active';
              }
            }

            if (mdaRecord.status != 'A'|| displayStatus == 'Completed')
              editable=false;



            var hist={
              subType: mdaRecord.subType,
              startDate: sd,
              endDate: ed,
              endTime: et,
              appointmentDate: haptd,
              //changed from sorid to id
              id: mdaRecord.id,
              dispStatus: displayStatus,
              comments: mdaRecord.comments,
              reasonForChange : mdaRecord.reasonForChange,
              action:action,
              status:status,
              editable:editable
            };
            mdaStatusRecords.push(hist);
          }
        }
        return mdaStatusRecords;
      },

      getPhone: function(type) {
        if (!this.phones || this.phones.length <= 0) return '';
        var phones = this.phones;
        for (var i = 0; i < phones.length; i++) {
          if (phones[i] && phones[i].type && phones[i].type.toLowerCase() === type) {
            if (!phones[i].phone) return '';
            var formattedPhone = phones[i].phone.replace(/[\D]+/g, '')
              .replace(/(\d{3})(\d{3})(\d{4})/, '($1) $2-$3');
            return formattedPhone;
          }
        }
        return '';
      },

      getProbationEndDate: function () {
        // superintendents and supervisors have a one year probation date from their promotion
        // sani workers have 18 months from their title entry
        var result = {};
        if (this.civilServiceTitle === groups.personnel.supervisors || this.civilServiceTitle === groups.personnel.superintendents) {
          result.endDate = this.promotionDate && moment(this.promotionDate).add(1, 'y');
        } else if (this.civilServiceTitle === groups.personnel.sanitationWorkers) {
          result.endDate = this.promotionDate && moment(this.promotionDate).add(18, 'M');
        }
        if (!result.endDate) return;
        // compare end date to now
        result.active = result.endDate.isAfter(new Date());
        result.endDate = result.endDate.valueOf();
        return result;
      },

      getEmergencyContactInformation: function () {
        return this.emergencyContacts
      },

      // sort grounding history by start date
      getFormattedGroundedHistory: function () {
        if (!this.groundingHistory) return [];
        return this.groundingHistory.sort(function (a, b) {
          return a.startDate < b.startDate;
        });
      },

      getSortedDetachmentHistory: function() {
        return this.detachmentHistory.sort(function(me, that) {

            if (that == null)
              return -1;

            // Handle permanent detachments
            if (me.endDate == null && that.endDate != null)
              return -1;
            else if (me.endDate != null && that.endDate == null)
              return 1;

            if (me.endDate != null && that.endDate != null) {
              if (me.endDate < that.endDate)
                return 1;
              else if (me.endDate > that.endDate)
                return -1;
            }

            // Now sort by start date
            if (me.startDate == null && that.startDate != null)
              return -1;
            else if (me.startDate != null && that.startDate == null)
              return 1;

            if (me.startDate != null && that.startDate != null) {
              if (me.startDate < that.startDate)
                return 1;
              else if (me.startDate > that.startDate)
                return -1;
            }

            // Now check time detachment was created
            if (me.lastModifiedSystem == null && that.lastModifiedSystem != null)
              return -1;
            else if (me.lastModifiedSystem != null && that.lastModifiedSystem == null)
              return 1;
            else if (me.lastModifiedSystem == null && that.lastModifiedSystem == null)
              return 0;

            if (me.lastModifiedSystem < that.lastModifiedSystem)
              return 1;
            else if (me.lastModifiedSystem > that.lastModifiedSystem)
              return -1;
            else
              return 0;
          }
        )

      },
      getSortedDetachmentHistoryOnLastModified: function() {
          return this.detachmentHistory.sort(function(me, that) {

              if (that == null)
                return -1;

              if (me.lastModifiedSystem == null && that.lastModifiedSystem != null)
                return -1;
              else if (me.lastModifiedSystem != null && that.lastModifiedSystem == null)
                return 1;
              else if (me.lastModifiedSystem == null && that.lastModifiedSystem == null)
                return 0;

              if (me.lastModifiedSystem < that.lastModifiedSystem)
                return 1;
              else if (me.lastModifiedSystem > that.lastModifiedSystem)
                return -1;
              else
                return 0;
            }
          )

        },      
      
      getSortedUnavailabilityHistory: function(list) {
        var ar = list ? list : this.unavailabilityHistory;
        return ar.sort(function(me, that) {

            if (that == null)
              return 1;

            // Now sort by start date
            if (me.start == null && that.start != null)
              return -1;
            else if (me.start != null && that.start == null)
              return 1;

            if (me.start != null && that.start != null) {
              if (me.start < that.start)
                return 1;
              else if (me.start > that.start)
                return -1;
            }

            // Now sort by start time
            if (me.startTime == null && that.startTime != null)
              return -1;
            else if (me.startTime != null && that.startTime == null)
              return 1;

            if (me.startTime != null && that.startTime != null) {
              if (me.startTime < that.startTime)
                return 1;
              else if (me.startTime > that.startTime)
                return -1;
            }

            // Handle infinite unavailability period
            /*                    if (me.end == null && that.end != null)
             return -1;
             else if (me.end != null && that.end == null)
             return 1;*/

            //Handle change type (same day)
            if(me.lastModifiedSystem<that.lastModifiedSystem)
              return 1;
            else
              return -1;

          }
        )
      },

      getSortedmdaHistory: function() {
        return this.mdaStatusHistory.sort(function(me, that) {

            if (that == null)
              return 1;

            // Now sort by start Date
            if (me.startDate == null && that.startDate != null)
              return -1;
            else if (me.startDate != null && that.startDate == null)
              return 1;

            if (me.startDate != null && that.startDate != null) {
              if (me.startDate < that.startDate)
                return 1;
              else if (me.startDate > that.startDate)
                return -1;
            }

            // Handle no end date
            if (me.endDate == null && that.endDate != null)
              return -1;
            else if (me.endDate != null && that.endDate == null)
              return 1;

            //Handle change type (same day)
            if(me.lastModifiedSystem<that.lastModifiedSystem)
              return 1;
            else
              return -1;

          }
        )
      },

      isPartiallyAvailable: function() {
        if (this.partialAvailability)
          return true;


        if (this.state == states.personnel.partiallyAvailable || this.isPartialMdaAvailability()) {
          this.partialAvailability = true;
        } else {
          this.partialAvailability = false;
        }

        return this.partialAvailability;
      },

      isPartialMdaAvailability:function(){
        var result=false;
        if(this.activeMdaCodes.length>0){
          var now=new Date();
          for(var i = 0; i < this.activeMdaCodes.length; i++){
            if(moment(this.activeMdaCodes[i].endDate).diff(now, 'days') == 0 && moment(this.activeMdaCodes[i].endDate).diff(now, 'minutes') <= 0){
              result=true;
            }
          }
        }
        return result;
      },
      isActiveMDA: function () {
        if(this.civilServiceTitle && this.civilServiceTitle === groups.personnel.civilians){
          return false;
        }
        var now = new Date();
        //var pathElements = window.location.pathname.split('/');
        var pathElements = window.location.pathname.split('/'),
          pathStart = '/' + pathElements[1];
//                pathElements = pathElements[1].split('/');
        var boardLocation = pathElements[2],
          boardDate = pathElements[3];
        var year = boardDate.substring(0,4);
        var month = boardDate.substring(4,6);
        var date = boardDate.substring(6,8);
        var boardDateDt = new Date(year, month-1, date, 0, 0, 0);
        var boardStart = new Date(boardDateDt.getHours()-2);

        if(this.activeMdaCodes && this.activeMdaCodes.length > 0 ){
          var show = true;
          var activeMdaCodes = this.activeMdaCodes;
          for (var i = 0; i < activeMdaCodes.length; i++) {
            if(now.getTime() > boardDateDt.getTime()) {// Exclude future boards
              if (activeMdaCodes[i].status == "A" && (activeMdaCodes[i].startDate == null ||
                (moment(activeMdaCodes[i].startDate).diff(now, 'days') <= 0 && moment(activeMdaCodes[i].startDate).diff(now, 'minutes') <= 0))) {
                // record start date <= now
                if (activeMdaCodes[i].endDate == null ||
                  (moment(activeMdaCodes[i].endDate).diff(now, 'days') >= 0 && moment(activeMdaCodes[i].endDate).diff(now, 'minutes') > 0)) {
                  // record end date > now
                  show = false;
                  break;
                }
              }
            }else{ // For future boards

              if (activeMdaCodes[i].status == "A" && (activeMdaCodes[i].startDate == null ||
                (moment(activeMdaCodes[i].startDate).diff(boardDateDt, 'days') <= 0 && moment(activeMdaCodes[i].startDate).diff(boardDateDt, 'minutes') <= 0))) {
                // record start date <= board date
                if (activeMdaCodes[i].endDate == null ||
                  (moment(activeMdaCodes[i].endDate).diff(boardDateDt, 'days') >= 0 && moment(activeMdaCodes[i].endDate).diff(boardDateDt, 'minutes') > 0)) {
                  // record end date > board date
                  show = false;
                  break;
                }
                if (activeMdaCodes[i].endDate == null ||
                  (moment(activeMdaCodes[i].endDate).diff(boardStart, 'minutes') >= 0 && moment(activeMdaCodes[i].endDate).diff(boardStart, 'minutes') > 0)) {
                  // record end date > board date
                  show = false;
                  break;
                }
              }
            }
          }

          return show;
        }

        return true;
      },
      updateState : function(assigned, assignedAnywhere, state) {
        this.assigned = assigned;
        this.assignedAnywhere = assignedAnywhere;
        this.state = state;
      },
      getIsFutureMda: function () {
        if(this.activeMdaCodes && this.activeMdaCodes.length>0) {
          var lastMdaCode = this.activeMdaCodes[this.activeMdaCodes.length-1];
          if(lastMdaCode.status === 'A') {
            return true;
          }
        }
        return false;
      }
    }
  });