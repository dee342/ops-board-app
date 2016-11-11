(function () {

    'use strict';

    angular
        .module('lvl.directives.dragdrop', ['lvl.services'])
        .directive('lvlDraggable', ['$rootScope', '$log', 'uuid',
        function ($rootScope, $log, uuid) {

            var callbacks = (function () {
                return {
                    dragstart: function (scope, attrs, event) {

                    	if ( (scope.person && scope.person.state === 'Hidden') || (scope.pieceOfEquipment && scope.pieceOfEquipment.state === 'Hidden')) {
                            return;
                        }
                    	
                        var id = attrs.id,
                            pos = attrs.pos,
                            elementId = scope.pieceOfEquipment ? scope.pieceOfEquipment.id : scope.person ? scope.person.id : '',
                            task = scope.item && scope.item.task,
                            cls = event.target.classList;

                        // personnel will have `pos` as 1 or 2
                        if (pos) {
                            elementId = scope.item.task['assignedPerson' + pos].personId;
                            event.dataTransfer.setData('position', pos);
                        }

                        // dragging from a task
                        if (task) {
                            // personnel in a task
                            if (!pos) {
                                // dragging equipment from partial tasks is not supported
                                //if (task.groupId) return;
                                elementId = task.assignedEquipment.equipmentId;
                            }
                            //$log.info('dnd : move : %s from %s', elementId, task.id);
                            event.dataTransfer.setData('taskId', task.id);
                        }

                        event.dataTransfer.setData('text', id);
                        event.dataTransfer.setData('context', attrs.context);
                        event.dataTransfer.setData('taskIndicator', attrs.taskIndicator);
                        event.dataTransfer.setData('elementId', elementId);

                        cls.add('dnd-drag-target');

                        $rootScope.$emit('LVL-DRAG-START');
                        $rootScope.dragging = {
                            type: cls.contains('equipment') ? 'equipment' : cls.contains('equipment-container') ? 'equipment' : 'person'
                        };

                    },
                    dragend: function (event) {
                        event.target.classList.remove('dnd-drag-target');
                        var el = $(event.target).find('.coverup');
                        el.html('').css('top', 'auto');
                        $rootScope.$emit('LVL-DRAG-END');
                        $rootScope.dragging = null;
                    }
                }
            }());
    
            var checkHiddenState = function (scope) {
                return (scope.person && scope.person.state === 'Hidden') || (scope.pieceOfEquipment && scope.pieceOfEquipment.state === 'Hidden');
            };

            return {
                restrict: 'A',
                link: function (scope, el, attrs) {

                    // ignore hidden entities
                    if (checkHiddenState(scope)) return;

                    var id = attrs.id;
                    
                    // add tracking id if not present
                    if (!id) {
                        id = uuid.new();
                        attrs.$set('id', id);
                    }

                    attrs.$set('draggable', 'true');

                    el.bind('dragstart', callbacks.dragstart.bind(null, scope, attrs));
                    el.bind('dragend', callbacks.dragend);

                    scope.$on('$destroy', function () {
                        el.unbind();
                        el = null;
                    });

                }
            };
        }])
        .directive('lvlDropTarget', ['$rootScope', 'uuid',
        function ($rootScope, uuid) {

        var callbacks = (function () {
            return {
                drop: function (scope, event) {
                    event.preventDefault && event.preventDefault();
                    event.stopPropogation && event.stopPropogation();
                    scope.onDrop({
                        elementId: event.dataTransfer.getData('elementId'),
                        context: event.dataTransfer.getData('context'),
                        taskIndicator: event.dataTransfer.getData('taskIndicator'),
                        taskId: event.dataTransfer.getData('taskId'),
                        fromPosition: event.dataTransfer.getData('position')
                    });
                    event.target.classList.remove('dnd-over');
                },
                dragstart: function (id) {
                    var el = document.getElementById(id);
                    el && el.classList.add('dnd-target');
                },
                dragover: function (event) {
                    event.preventDefault && event.preventDefault(); // Necessary. Allows us to drop.
                    event.dataTransfer.dropEffect = 'move'; // See the section on the DataTransfer object.
                    return false;
                },
                dragleave: function (event) {
                    event.target.classList.remove('dnd-over', 'dnd-invalid');
                },
                dragenter: function (event) {
                    if (!$rootScope.dragging) return;
                    var type = event.target.classList.contains('equipment') ? 'equipment' : 'person';
                    var validClass;
                    // provide a cue when dragging a person to equipment or vice versa
                    if (($rootScope.dragging.type === 'equipment' && type !== 'equipment') || ($rootScope.dragging.type === 'person' && type !== 'person')) {
                        validClass = 'dnd-invalid';
                    }
                    event.target.classList.add('dnd-over', validClass);
                },
                dragend: function (id) {
                    var el = document.getElementById(id);
                    el && el.classList.remove('dnd-target', 'dnd-over', 'dnd-invalid');
                }
            }
        }());

        return {
            restrict: 'A',
            scope: {
                onDrop: '&'
            },
            link: function (scope, el, attrs) {

                var id = attrs.id;
                
                // add tracking id if not present
                if (!id) {
                    id = uuid.new();
                    attrs.$set('id', id);
                }

                el.bind('dragenter', callbacks.dragenter);
                el.bind('dragover', callbacks.dragover);
                el.bind('dragleave', callbacks.dragleave);
                el.bind('drop', callbacks.drop.bind(null, scope));

                $rootScope.$on('LVL-DRAG-START', callbacks.dragstart.bind(null, id));
                $rootScope.$on('LVL-DRAG-END', callbacks.dragend.bind(null, id));

                scope.$on('$destroy', function () {
                    el.unbind();
                    el = null;
                });

            }
        };
    }]);

}());