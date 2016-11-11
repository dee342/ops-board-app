'use strict';

angular.module('OpsBoard')
  .service(
  'DisplayBoardHelperService',
  function($filter, BoardValueService, DisplayBoardHelperCommon) {
      return {
        getAllSubcategories: function (categories) {
          var result = [];

          categories.forEach(function(i) {
            result.push(i.subcategories);
          });

          return _.flatten(result);
      },
      getAllSubcategoriesObject: function (categories) {
        var results = {};

        categories.forEach(function(i) {
          i.subcategories.forEach(function(j) {
            results[j.id] = j;
          });
        });

        return results;
      },
      formatNames: function (task2, dataAutomation, equipmentobject) {
          var equipment = BoardValueService.equipment;
          var personnel = BoardValueService.personnel;
          var name1Marker = '';
          var name2Marker = '';
          var multimarker = '&nbsp;';
          var childclass = '';
          var childmarkerclass = '';
          var automationstring = '';
          var indicator1 = '';
          var indicator2 = '';
          var indicatorString1 = '';
          var indicatorString2 = '';
          var nameLen1 = 11;
          var nameLen2 = 11;

          if(task2.multigroup) {
            multimarker = '<i class="fa fa-map-marker partial group' + task2.multigroup + '"></i>';
            if(parseInt(task2.partialTaskSequence) >1 ) {
              childclass = 'childclass';
              childmarkerclass = 'childmarkerclass';
            }
          }

          var id = Math.random();
          var blankPersonModel = {
            firstName: '',
            lastName: '',
            departmentType: ''
          };
          var detachedSWClass = '';
          var detachedSupClass = '';
          var detachedOSPayrollLoc = '';
          var attachedToPayrollLoc = '';
          var hiddenFromDisplay = '';
          var personModel1;
          var personModel2;
          var html;
          var flip1 = '';
          var flip2 = '';
          var binsclass = '';
          var snowclass = '';
          var equipmentModel = !task2.assignedEquipment.equipmentId ? {name: '', type: '', subType: ''} :  equipment[task2.assignedEquipment.equipmentId];

          if (!task2.assignedPerson1.personId) {
            personModel1 = blankPersonModel;
          } else {
            indicator1 = DisplayBoardHelperCommon.visualIndicator(task2.assignedPerson1.personId);
            personModel1 = personnel[task2.assignedPerson1.personId];

            if(indicator1 !== '' && task2.assignedPerson1.type !== 'NEXT_DAY') {
              nameLen1 = 9;
              if(indicator1 === 'C') {
                indicatorString1 = '<span class="workingChart">' + indicator1 + '</span>';
              } else if(indicator1 === 'V') {
                indicatorString1 = '<span class="workingVacation">' + indicator1 + '</span>';
              } else if(indicator1 === 'OV') {
                flip1 = 'flip';
              } else if(indicator1 === 'OC') {
                flip1 = 'flip';
              } else if(indicator1 === 'OX') {
                flip1 = 'flip';
              } else if(indicator1 === 'OP') {
                flip1 = 'flip';
              }
            }

            detachedSWClass = DisplayBoardHelperCommon.getDetachedSWStatus(personModel1);
            detachedSupClass = DisplayBoardHelperCommon.getDetachedSupStatus(personModel1);
            detachedOSPayrollLoc = DisplayBoardHelperCommon.getDetachedOSPayrollStatus(personModel1);
            attachedToPayrollLoc = DisplayBoardHelperCommon.getAttachedToPayrollLocStatus(personModel1);
            hiddenFromDisplay = DisplayBoardHelperCommon.getPersonnelHiddenStatus(personModel1);
            
            name1Marker = '<span id="tnm' + id + task2.id + '"  class="' + flip1 + ' namemarker ' + detachedSWClass + detachedSupClass + detachedOSPayrollLoc + attachedToPayrollLoc + hiddenFromDisplay;
            name1Marker += personModel1.departmentType + ' ';
            name1Marker += personModel1.civilServiceTitle.replace(' ', '-');
            name1Marker += ' ' + childmarkerclass + '">';
          }

          if(!task2.assignedPerson2.personId) {
            personModel2 = blankPersonModel;
          } else {
            indicator2 = DisplayBoardHelperCommon.visualIndicator(task2.assignedPerson2.personId);
            personModel2 = personnel[task2.assignedPerson2.personId];

            if(indicator2 !== '' && task2.assignedPerson2.type !== 'NEXT_DAY') {
              nameLen2 = 9;
              if(indicator2 === 'C') {
                indicatorString2 = '<span class="workingChart">' + indicator2 + '</span>';
              } else if(indicator2 === 'V') {
                indicatorString2 = '<span class="workingVacation">' + indicator2 + '</span>';
              } else if(indicator2 === 'OV') {
                flip2 = 'flip';
              } else if(indicator2 === 'OC') {
                flip2 = 'flip';
              } else if(indicator2 === 'OX') {
                flip2 = 'flip';
              } else if(indicator2 === 'OP') {
                flip2 = 'flip';
              }
            }

            detachedSWClass = DisplayBoardHelperCommon.getDetachedSWStatus(personModel2);
            detachedSupClass = DisplayBoardHelperCommon.getDetachedSupStatus(personModel2);
            detachedOSPayrollLoc = DisplayBoardHelperCommon.getDetachedOSPayrollStatus(personModel2);
            attachedToPayrollLoc =DisplayBoardHelperCommon.getAttachedToPayrollLocStatus(personModel2);
            hiddenFromDisplay = DisplayBoardHelperCommon.getPersonnelHiddenStatus(personModel2);
            name2Marker = '<span id="tnm' + id + task2.id + '"  class="' + flip2 + ' namemarker ' + detachedSWClass + detachedSupClass + detachedOSPayrollLoc + attachedToPayrollLoc + hiddenFromDisplay;
            name2Marker += personModel2.departmentType + ' ';
            name2Marker += personModel2.civilServiceTitle.replace(' ', '-');
            name2Marker += ' ' +  childmarkerclass + '">';
          }

          personModel1.firstName = $filter('firstInitial')(personModel1.firstName);
          personModel2.firstName = $filter('firstInitial')(personModel2.firstName);
          equipmentobject = equipment[task2.assignedEquipment.equipmentId];

          if(equipmentobject && equipmentobject.bins && equipmentobject.bins.length === 2) {
            binsclass = 'bins';
          } else {
            binsclass = '';
          }

          if(equipmentobject && equipmentobject.snowReadiness && equipmentobject.snowReadiness.plowType !== 'NO_PLOW') {
            snowclass = 'Snow';
          } else {
            snowclass = '';
          }

          if(personModel1.activeMdaCodes && personModel1.activeMdaCodes.length > 0) {
            indicatorString1 = '<span class="mda ' + personModel1.activeMdaCodes[0].subType + '">' + personModel1.activeMdaCodes[0].subType + '</span>';
            nameLen1 = 9;
          }

          if(personModel2.activeMdaCodes && personModel2.activeMdaCodes.length > 0) {
            indicatorString2 = '<span class="mda ' + personModel2.activeMdaCodes[0].subType + '">' + personModel2.activeMdaCodes[0].subType + '</span>';
            nameLen2 = 9;
          }

          if(personModel1.grounded) {
            indicatorString1 = '<span class="mda G">G</span>';
            nameLen1 = 9;
          }
          if(personModel2.grounded) {
            indicatorString2 = '<span class="mda G">G</span>';
            nameLen2 = 9;
          }

          automationstring = "{loc: '" + dataAutomation.loc + "', shift: '" + dataAutomation.shift + "', cat: '" + dataAutomation.cat + "', sub: '" + dataAutomation.sub + "', sec: " + dataAutomation.sec + ", taskno: " + dataAutomation.taskno + ", specialindicator: '" + indicator2 + "', flip: '" + flip2 + "'}";

          html = '<span data-automation="' + automationstring + '" class="taskrow"><span id="m' + id + task2.id + '" class="marker">' + multimarker + '</span><span id="c' + id + task2.id + '" class="code ' + childclass + '">';
          html += task2.taskName.slice(0,4) + '&nbsp;</span><span id="e' + id + task2.id + '"  class="equipment ' + childclass + ' ' + equipmentModel.type + ' ' + equipmentModel.subType + ' ' + binsclass + ' ' + snowclass + '">' + equipmentModel.name;

          if(personModel1.formattedName) {
            if(personModel1.formattedName.length > nameLen1) {
              personModel1.formattedName = personModel1.formattedName.slice(0, nameLen1).trim() + '.';
            }
          } else {
            personModel1.formattedName = '';
          }

          if(personModel2.formattedName) {
            if(personModel2.formattedName && personModel2.formattedName.length > nameLen2) {
              personModel2.formattedName =  personModel2.formattedName.slice(0, nameLen2).trim() + '.';
            }

          } else {
            personModel2.formattedName = '';
          }

          if(task2.assignedPerson2.personId) {
            html += '&nbsp;</span><span id="n1' + id + task2.id + '" class="name1 ' + childclass + '"><span id="n1f' + id + task2.id + '" class="namefull">';
            html += name1Marker + ' ' + indicatorString1 + personModel1.formattedName + '</span>';
            html += '</span></span><span id="n2' + task2.id + '" class="name2 ' + childclass + '"><span class="namefull">' + name2Marker;
            html +=  indicatorString2 + personModel2.formattedName + '</span></span></span></span>';
          } else {
            html += '&nbsp;</span><span id="n1' + id + task2.id + '" class="name1 twocol' + childclass + '"><span id="n1f' + id + task2.id + '" class="namefull">';
            html += name1Marker + ' ' + indicatorString1 + personModel1.formattedName + '</span></span>';
            html += '</span>';
          }

          return html;
      }
  }
});