//william: add directive to loop, focus, and delete the interval.
angular.module('OpsBoard.directives', ['ui.bootstrap', 'ui.select2'])
    .directive('downEquipmentFocus', ["$interval", function ($interval) {
        return {
            restrict: 'A',
            link: function (scope, element) {

                var stop = $interval(function () {
                    if (element.length) {
                        element.select2('focus');
                        $interval.cancel(stop);
                        stop = undefined;
                    }
                }, 500);
            }
        }
    }])
    .directive('modelOnBlur', [function () {
        return {
            restrict: 'A',
            require: 'ngModel',
            link: function (scope, element, attr, modelCtrl) {
                if (attr.type === 'radio' || attr.type === 'checkbox') return;
                element
                    .off('input keydown change')
                    .on('blur', function () {
                        scope.$apply(function () {
                            modelCtrl.$setViewValue(element.val());
                        });
                    });
            }
        };
    }])
    .directive('inputFocus', ['$interval', function ($interval) {
        return {
            restrict: 'A',
            link: function (scope, element) {
                var stop = $interval(function () {
                    if (element.length) {
                        element[0].focus();
                        $interval.cancel(stop);
                        stop = undefined;
                    }
                }, 500);
            }
        }
    }]);

