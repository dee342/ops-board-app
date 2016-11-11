'use strict';

angular.module('OpsBoard')
    .factory(
        'TaskModel',
        function() {

            return {

                isCompleted : function() {
                    return this.assignedEquipment.completed === true || this.assignedPerson1.completed === true
                        || this.assignedPerson2.completed === true;
                },

                clearEquipmentAssignment : function() {
                    var assignment = this.assignedEquipment;
                    if (assignment) {
                        assignment.assignmentTime = null;
                        assignment.completed = false;
                        assignment.equipment = null;
                        assignment.equipmentId = null;
                    }
                },

                setEquipmentAssignment : function(assignmentTime, completed, equipment) {
                    var assignment = this.assignedEquipment;
                    if (assignment) {
                        this.assignedEquipment.assignmentTime = assignmentTime;
                        this.assignedEquipment.completed = completed;
                        // this.assignedEquipment.equipment = equipment;
                        this.assignedEquipment.equipmentId = equipment.id;
                    }
                },

                clearPersonnelAssignment : function(position) {
                    var assignment = this['assignedPerson' + position];
                    if (assignment) {
                        assignment.assignmentTime = null;
                        assignment.completed = false;
                        assignment.person = null;
                        assignment.personId = null;
                        assignment.type = null;
                    }
                },

                clearPersonnelAssignmentWhenIndexNotKnown : function(personId, task) {
                    if (task) {
                        var assignment = this['assignedPerson' + 1];
                        if (assignment.personId == null
                            || (assignment.personId != null && assignment.personId != personId))
                            assignment = this['assignedPerson' + 2];
                        if (assignment) {
                            assignment.assignmentTime = null;
                            assignment.completed = false;
                            assignment.person = null;
                            assignment.personId = null;
                        }
                    }
                },

                setPersonnelAssignment : function(assignmentTime, completed, position, person) {
                    var assignment = this['assignedPerson' + position];
                    if (assignment) {
                        assignment.assignmentTime = assignmentTime;
                        assignment.completed = completed;
                        // assignment.person = person;
                        assignment.personId = person.id;
                    }
                },
                
                clearSupervisorAssignment : function(supervisorId) {
                   if (this.taskSupervisorAssignments) {
                       var i = this.taskSupervisorAssignments.length;
                       while (i--) {
                           var assignment = this.taskSupervisorAssignments[i];
                           if (assignment.personId == supervisorId) {
                               this.taskSupervisorAssignments.splice(i, 1);
                           }
                       }
                   }
                }

            }
        });