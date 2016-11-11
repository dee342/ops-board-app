'use strict';

angular.module('OpsBoard')
  .service(
  'DisplayBoardHelperFormattingLarge',
  function($filter, BoardValueService, $window, DisplayBoardHelperService, DisplayBoardHelperCommon, DisplayBoardHelperFormatting, durations) {
    return {
      unavailable: function (data, refData) {
        var id = Math.random();
        var unavailablecodes = [{code: 'VACATION/CHART', title: 'Vacation/Chart'}, {
          code: 'CHART',
          title: 'Charts'
        }, {code: 'XWP', title: 'XWP'}, {code: 'XWOP', title: 'XWOP'}, {code: 'SICK', title: 'Sick'}, {
          code: 'VACATION',
          title: 'Vacation'
        }, {code: 'LODI', title: 'LODI'}, {code: 'APP', title: 'APP'}, {code: 'AWOL', title: 'AWOL'}, {
          code: 'DIF',
          title: 'DIF'
        }, {code: 'SUSPENDED', title: 'Suspended'}, {
          code: 'TERMINAL LEAVE',
          title: 'Terminal Leave'
        }, {code: 'JURY DUTY', title: 'Jury Duty'}, {
          code: 'Mil Dty w/Pay',
          title: 'Mil Dty w/Pay'
        }, {code: 'Mil Dty w/o Pay', title: 'Mil Dty w/o Pay'}, {
          code: 'HONOR GUARD',
          title: 'Honor Guard'
        }, {code: 'FMLA', title: 'FMLA'},
          {code: 'MATERNITY LEAVE', title: 'Matern. Leave'}, {code: 'MULTIPLE', title: 'Multiple'}, {code: 'Civil Service Exam', title: 'Civ. Svc. Exam'}];

        var results = {};
        var unavailables = {};
        var namelen = 8;

        _.each(unavailablecodes, function (codeobject) {
          results = $filter('unavailableCodeFilter')(data.personnel, codeobject.code, refData.boardLocation);
          results = results.results;
          if (results.length > 0) {
            results.forEach(function (result) {
              if (!unavailables[codeobject.code]) {
                unavailables[codeobject.code] = [];
              }
              unavailables[codeobject.code].push(result);
            });

          }
        });

        refData = DisplayBoardHelperFormatting.checkHeight(74, 'unavailableheader', refData);
        refData.lastBlockType = 'unavailableheader';
        //refData.previousColLastBlock = 'unavailableheader';
        var shiftBlock = '<div id="uall' + id + '" class="firstheader unavailable"><span id="cl' + id + '">Unavailable</span></div>'
        refData.cols['col' + refData.col].push({type: 'unavailableheader', html: shiftBlock});
        refData.lastShiftBlockType = 'unavailableheader';
        refData.lastShiftBlock = shiftBlock;

        var firstInitial = '';

        var unavailable = '';
        var automationstring = '';
        var colcount = 0;
        var mdastring = '';
        var reason = '';

        unavailablecodes.forEach(function (code) {
          var persons = unavailables[code.code];
          if (persons) {
            unavailable = '<span class="unavailableName unavailableTitle">' + code.title + ':</span>';
            colcount = 0;

            persons.forEach(function (person) {

              colcount = colcount + 1;

              if (colcount === 3) {
                refData = DisplayBoardHelperFormatting.checkHeight(74, 'unavailableheader', refData);
                refData.lastBlockType = 'unavailableperson';
                refData.cols['col' + refData.col].push({type: 'unavailableperson', html: unavailable});
                unavailable = '';
                colcount = 1;
                unavailable = unavailable + '<span class="unavailableName unavailableTitle">&nbsp;</span>';

              }
              refData.person2 = person;
              firstInitial = $filter('firstInitial')(refData.person2.firstName);
              reason = '';

              if (refData.person2.activeUnavailabilityReasons[0]) {
                reason = ' (' + refData.person2.activeUnavailabilityReasons[0].code + ')';
              }

              if (refData.person2.mdaStatusHistory[0]) {
                reason = ' (' + refData.person2.mdaStatusHistory[0].type + ')';
              }

              automationstring = "{group: 'unavailable', code: '" + code.code + "', name: '" + firstInitial + ' ' + refData.person2.lastName + "'}";

              if (refData.person2.activeMdaCodes && refData.person2.activeMdaCodes.length > 0) {
                mdastring = '<span class="mda ' + refData.person2.activeMdaCodes[0].subType + '">' + refData.person2.activeMdaCodes[0].subType + '</span>';
                namelen = 9;
              } else {
                mdastring = '';
                namelen = 11;
              }

              var detachedSW = DisplayBoardHelperCommon.getDetachedSWStatus(refData.person2);
              var detachedSup = DisplayBoardHelperCommon.getDetachedSupStatus(refData.person2);
              var detachedOSPayroll = DisplayBoardHelperCommon.getDetachedOSPayrollStatus(refData.person2);
              var attachedToPayrollLoc = DisplayBoardHelperCommon.getAttachedToPayrollLocStatus(refData.person2);
              var hiddenFromDisplay = DisplayBoardHelperCommon.getPersonnelHiddenStatus(refData.person2);


              if (refData.person2.grounded) {
                mdastring = '<span class="mda G">G</span>';
                namelen = 10;
              }

              if (refData.person2.formattedName) {
                if (refData.person2.formattedName.length > namelen) {
                  refData.person2.formattedName = refData.person2.formattedName.slice(0, namelen).trim() + '.';
                }
              } else {
                refData.person2.formattedName = '';
              }

              unavailable = unavailable + '<span data-automation="' + automationstring + '" class="unavailableName"><span class="namemask namemarker ' + detachedSup + ' ' + detachedSW + ' ' + detachedOSPayroll + ' ' + attachedToPayrollLoc + ' ' + hiddenFromDisplay + ' ' + refData.person2.departmentType + ' ' + refData.person2.civilServiceTitle.replace(' ', '-') + ' }}"><span class="namespan">' + mdastring + refData.person2.formattedName + '</span></span></span>';
            });

            unavailable = unavailable + '<div style="clear: both;"></div><div class="unavailableFooter"></span>';

            refData = DisplayBoardHelperFormatting.checkHeight(74, 'unavailableperson', refData);
            refData.lastBlockType = 'unavailableperson';
            refData.cols['col' + refData.col].push({type: 'unsvailableperson', html: unavailable});
          }
        });
        return refData
      },
      detached: function (data, refData) {
        var id = Math.random();
        var firstInitial = '';
        var automationstring = '';
        var mdastring = '';
        var namelen = 8;
        var to = '';
        var indicator1 = '';
        var indicatorString1 = '';
        var flip1 = '';
        var lastShift = '';
        var detachheader = '<div id="dil' + id + '" class="firstheader greyback">Detached</div>';

        refData = DisplayBoardHelperFormatting.checkHeight(74, 'detachedheader', refData);
        refData.lastBlockType = 'detachedheader';
        refData.cols['col' + refData.col].push({type: 'detachedheader', html: detachheader});
        refData.lastShiftBlock = detachheader;
        refData.lastShiftBlockType = 'detachedheader';

        var persons = $filter('detachedWithinZonePersonnel')(data.personnel);

        var taskspersons = $filter('detachedZoneTasks')(data.taskContainers, 263, data.personnel, refData);
        persons = persons.concat(taskspersons);

        taskspersons = $filter('detachedOutsideZonePersonnel')(data.personnel);
        persons = persons.concat(taskspersons);

        taskspersons = $filter('detachedZoneTasks')(data.taskContainers, 264, data.personnel, refData);
        persons = persons.concat(taskspersons);

        taskspersons = $filter('detachedZoneTasks')(data.taskContainers, 258, data.personnel, refData);
        persons = persons.concat(taskspersons);

        taskspersons = $filter('detachedZoneTasks')(data.taskContainers, 592, data.personnel, refData);
        persons = persons.concat(taskspersons);

        var personsSorted = sortPersonnelPanel(persons, 'Location Seniority', data.location);
        personsSorted.forEach(function (person) {
          if(person.activeDetachment.taskName) {
            person.sortgroup = 1;
          } else {
            person.sortgroup = 0;
          }
          person.sortsequence = refData.shiftsObject[person.activeDetachment.shift].sequence;
        });

        personsSorted = $filter('orderBy')(personsSorted, ['sortsequence', 'sortgroup', 'activeDetachment.to']);

        personsSorted.forEach(function (person2) {
          firstInitial = $filter('firstInitial')(person2.firstName);
          automationstring = "{group: 'Detached - Within Zone', shift: '" + person2.activeDetachment.shift + "', to: '" + person2.activeDetachment.to + "', name: '" + firstInitial + ' ' + person2.lastName + "'}";
          flip1 = '';

          if (lastShift !== '' && lastShift !== person2.activeDetachment.shift) {
            refData = DisplayBoardHelperFormatting.checkHeight(74, 'unavailablefooter', refData);
            refData.lastBlockType = 'unavailablefooter';
            refData.cols['col' + refData.col].push({type: 'unavailablefooter', html: '<div style="clear: both;"></div><div class="detachedFooter"></span>'});
          }

          if (person2.departmentType) {

            if (person2.activeMdaCodes && person2.activeMdaCodes.length > 0) {
              indicatorString1 = '<span class="mda ' + person2.activeMdaCodes[0].subType + '">' + person2.activeMdaCodes[0].subType + '</span>';
              namelen = 9;
            } else {
              indicatorString1 = '';
              namelen = 11;
            }

            if (person2.grounded) {
              indicatorString1 = '<span class="mda G">G</span>';
              namelen = 9;
            }

            if (person2.lastName && person2.lastName.length > namelen) {
              person2.lastName = person2.lastName.slice(0, namelen - 1).trim() + '.';
            }

            to = person2.activeDetachment.to;
            if (person2.activeDetachment.taskName) {
              to = person2.activeDetachment.taskName;
            }

            if (person2.state === 'Detached') {
              indicator1 = ''; // when detached, no 'C' class display.
            } else {
              indicator1 = DisplayBoardHelperCommon.visualIndicator(person2.id, true);
            }

            if (indicator1 !== '') {
              if (indicator1 === 'C') {
                indicatorString1 = '<span class="workingChart">' + indicator1 + '</span>';
              } else if (indicator1 === 'V') {
                indicatorString1 = '<span class="workingVacation">' + indicator1 + '</span>';
              } else if (indicator1 === 'OV') {
                flip1 = 'flip';
              } else if (indicator1 === 'OC') {
                flip1 = 'flip';
              } else if (indicator1 === 'OX') {
                flip1 = 'flip';
              } else if (indicator1 === 'OP') {
                flip1 = 'flip';
              }
            }

            if (person2.formattedName) {
              if (person2.formattedName.length > namelen) {
                person2.formattedName = person2.formattedName.slice(0, namelen).trim() + '.';
              }
            } else {
              person2.formattedName = '';
            }

            refData = DisplayBoardHelperFormatting.checkHeight(74, 'detachedperson', refData);
            refData.lastBlockType = 'detachedperson';
            refData.cols['col' + refData.col].push({type: 'detachedperson', html: '<div data-automation="' + automationstring + '" class="detatt"><span class="detattloc">' + person2.activeDetachment.shift + '</span><span class="detattsec">' + to + '</span><span class="detattcat">' +
              '<span class="' + flip1 + ' namemarker ' + person2.departmentType + ' ' + person2.civilServiceTitle.replace(' ', '-') + '">' + indicatorString1 + person2.formattedName + '</span></span></div>'});

            lastShift = person2.activeDetachment.shift;
          }
        });
        return refData;
      },
      multiroute: function (multiroutes, refData) {
        var id = Math.random();
        var route = '';
        var to = '';
        var task2 = {};
        var duration = '';
        var section = '';
        var mcount = 0;

        refData = DisplayBoardHelperFormatting.checkHeight(74, 'multiheader', refData);
        var lastColBlockIndex = refData.cols['col' + refData.col].length - 1;
        if(refData.cols['col' + refData.col][0].lastshiftblock === 'detachedheader' && refData.cols['col' + refData.col][lastColBlockIndex].type !== 'detachedperson') {
          refData = DisplayBoardHelperFormatting.checkHeight(-74, 'detachedheader', refData);
          refData.cols['col' + refData.col].pop();
        }

        refData.lastBlockType = 'multiheader';

        var multirouteheader = '<div id="mrl' + id + '" class="firstheader greyback">Multi-Route Recap</div>';
        refData.cols['col' + refData.col].push({type: 'multiheader', html: multirouteheader});
        refData.lastShiftBlockType = 'multiheader';
        refData.lastShiftBlock = multirouteheader;

        multiroutes.forEach(function (multiroute) {
          route = '';
          to = '';
          section = '';
          var segmentcount = 0;
          try {
            multiroute.forEach(function (s) {
              duration = '';
              task2 = s;

              mcount = mcount + 1;

              if (mcount < multiroute.length - 1) {
                to = ' to ';

                durations.forEach(function (j) {
                  if (j.duration === task2.hours) {
                    duration = ' ' + j.label + ' ';
                  }
                });

              } else {
                durations.forEach(function (k) {
                  if (k.duration === task2.hours) {
                    duration = ' ' + k.label + ' ';
                  }
                });
                to = '';
              }

              if (task2.section) {
                section = parseInt(task2.location.match(/[0-9]+/)[0]) + task2.section + ' ';
              }

              segmentcount = segmentcount + 1;


              route = route + '<span class="segment"><span class="routemarker"><i class="fa fa-map-marker partial group' + task2.multigroup + '"></i></span><span class="segmenttitle">' + section + task2.taskName + duration + '</span><span class="to">' + to + '</span></span>';


            });
          }
          catch (err) {
            console.info(err);
          }
          refData = DisplayBoardHelperFormatting.checkHeight(74 * Math.ceil(segmentcount / 2), 'multiroute', refData);
          refData.lastBlockType = 'multiroute';
          refData.cols['col' + refData.col].push({type: 'multiroute', html: '<div class="multirouteroute">' + route + '</div>'});
        });
        return refData;
    },
    makeBlocks: function(orderByLocation, data, refData) {
      var dataAutomation = {};
      refData.mapgroupobject = DisplayBoardHelperCommon.mapgroups(orderByLocation, refData);
      refData = DisplayBoardHelperFormatting.checkHeight(174, 'datetime', refData);
      refData = DisplayBoardHelperFormatting.dateTime(data, refData);

      refData.mapgroupobject.orderByLocation.forEach(function(location) {;
        var multi = 0;
        refData = DisplayBoardHelperFormatting.checkHeight(80, 'location', refData);
        refData = DisplayBoardHelperFormatting.headerLocation(location, refData);
        if(Object.keys(data.taskContainers).length > 1) {
          multi = 1;
        }

        var shifts = location.locationShifts;
        shifts = $filter('orderByShiftId')(shifts);

        shifts.forEach(function(shift2) {
          refData = DisplayBoardHelperFormatting.checkHeight(77, 'shift', refData);
          DisplayBoardHelperFormatting.formatShiftName(shift2, refData);
          refData.shiftCategories = $filter('orderByCategoryId')(shift2.shiftCategories, refData.categories);
          refData.shiftCategories.forEach(function(shiftcategory2) {
            refData.subcategorytasks = $filter('orderBySubcategorySequence')(shiftcategory2.subcategoryTasks, refData.allSubcategories);
            refData.subcategorytasks.forEach(function(categorytask2) {
              if(categorytask2.subcategoryId !== 263 && categorytask2.subcategoryId !== 264 && categorytask2.subcategoryId !== 258 && categorytask2.subcategoryId !== 592) {
                refData = DisplayBoardHelperFormatting.checkHeight(74, 'subcategory', refData);
                refData = DisplayBoardHelperFormatting.formatSubcategoryName(shiftcategory2, categorytask2, refData);
                refData.tasksObject = $filter('orderByTaskId')(categorytask2.tasks);

                refData.tasksObject.forEach(function(task2) {
                  dataAutomation = {
                    loc: location.locationCode,
                    shift: refData.shifts[shift2.shiftId].name,
                    cat: refData.categoriesObject[shiftcategory2.categoryId].name,
                    sub: refData.allSubcategoriesObject[categorytask2.subcategoryId].name,
                    sec: 0,
                    taskno: task2.sequence
                  };

                  refData = DisplayBoardHelperFormatting.checkHeight(58, 'task', refData);
                  refData.lastBlockType = 'task';
                  refData.cols['col' + refData.col].push({type: 'task', html: DisplayBoardHelperService.formatNames(task2, dataAutomation)});
                });

                refData.sections = $filter('orderBySection')(categorytask2.sections);

                refData.sections.forEach(function(section2) {
                  refData = DisplayBoardHelperFormatting.checkHeight(53, 'section', refData);
                  refData = DisplayBoardHelperFormatting.formatSectionName(section2, refData);
                  refData.tasksObject = $filter('orderByTaskId')(section2.tasks);

                  refData.tasksObject.forEach(function(task2) {
                    dataAutomation = {
                      loc: location.locationCode,
                      shift: refData.shifts[shift2.shiftId].name,
                      cat: refData.categoriesObject[shiftcategory2.categoryId].name,
                      sub: refData.allSubcategoriesObject[categorytask2.subcategoryId].name,
                      sec: section2.sectionId,
                      taskno: task2.sequence
                    };

                    refData = DisplayBoardHelperFormatting.checkHeight(58, 'task', refData);
                    refData.lastBlockType = 'task';
                    refData.cols['col' + refData.col].push({type: 'task', html: DisplayBoardHelperService.formatNames(task2, dataAutomation)});
                  });
                });
              }
            });
          });
        });
      });

      return refData;
    }
  }
});