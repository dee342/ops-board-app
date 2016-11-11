'use strict';

angular.module('OpsBoard')
  .service(
    'InfoService',
    function() {

      // returns either day or night based on shift sequence
      var calculateShiftType = function (shift) {
        return (shift.sequence > 6 && shift.sequence < 14) ? 'day' : 'night';
      };

      return {

        getDateFormat: function (d, t) {
          return new Date(moment(d).minutes(t.getMinutes()).hours(t.getHours()));
        },
      
        getShiftInfo: function (uuid, shift) {
          if (!shift.hasOwnProperty('type')) {
            shift.type = calculateShiftType(shift);
          }
          return {
            "id": uuid,
            "shift": shift,
            "shiftCategories": {}
          }
        },
        
        getCategoryInfo: function (uuid, category) {
          return {
            "id": uuid,
            "category": {
              "id": category.id,
              "name": category.name,
              "sequence": category.sequence
            },
            allsubcategoryTasks: angular.copy(category.subcategories),
            subcategoryTasks: {}
          }
        },

        getSubcategoryInfo: function (uuid, subcategory, sections) {
          var sub = {
            "id": uuid,
            "subcategory": {
              "containsSections": subcategory.containsSections,
              "id": subcategory.id,
              "name": subcategory.name,
              "taskIndicator": subcategory.taskIndicator,
              "peoplePerTask": subcategory.peoplePerTask,
              "sequence": subcategory.sequence,
              "personnelTitle" : subcategory.personnelTitle,
              "equipmentSubTypes" : subcategory.equipmentSubTypes
            }
          }

          if (subcategory.containsSections ) {
            sub.sections = {};
            sub.allSections = [];
            sub.allSections.numOfTasks = 0;
            for (var k = 0; k < sections.length; k++) {
              sub.allSections.push({
                id: sections[k],
                name: sections[k],
                tasks: [],
                numOfTasks: 0
              })
            }
          } else {
            sub.numOfTasks = 1;
          }
          return sub;
        },

        getSectionInfo: function (uuid, numOfTasks, section) {
          var sectionModel = {
            id: uuid,
            sectionName: section.name,
            sectionId: section.id,
            section: {
              id: section.id,
              name: section.id,
            },
            numOfTasks: numOfTasks,
            tasks: {}
          };
          return sectionModel;
        },

        getTaskInfo: function (uuid) {
          var task = {
            "id": uuid,
            "assignedEquipment": {equipment: null},
            "assignedPerson1": {person: null},
            "assignedPerson2": {person: null},
            "taskName": "T/R"
          }
          return task;
        },
        getShiftType: calculateShiftType

      }
    });