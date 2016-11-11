'use strict';
angular.module("angular-flex-splitter", []).directive("splitter", ['$window', '$timeout', '$rootScope',
    function ($window, $timeout, $rootScope) {
        return {
            restrict: 'AC',
            template: '<div class="ghost noSelect"><div class="expander noSelect"><i class="fa fa-angle-left"></i><i class="fa fa-angle-right"></i></div></div>',
            link: function (scope, iElement, iAttr) {
                if (iAttr.fixed === 'fixed') return;
                var isActive = false;
                var isPerv = /^(up|left)$/.test(iAttr.splitter);
                var target = isPerv ? iElement.prev() : iElement.next();
                var style = /^(up|down)$/.test(iAttr.splitter) ? 'height' : 'width';
                var ghostStyle = /^(up|down)$/.test(iAttr.splitter) ? 'top' : 'left';
                var eventType = /^(up|down)$/.test(iAttr.splitter) ? 'clientY' : 'clientX';
                var sourceSize, targetSize;
                var body = angular.element('body');
                var content = iElement.parents(".flexbox-content");

                var ghost = iElement.children(".ghost");
                var lastSize = 0;

                jQuery.fn.docEventsUnbind = function (events_ary) {
                    var $self = jQuery(this);
                    var docEvents = jQuery._data(document, "events");

                    //keydown, contextmenu, click, slectionchange, mouseup, mousemove
                    events_ary.forEach(function (evt) {
                        if (docEvents[evt] && docEvents[evt].length > 0) {
                            $self.unbind(evt);
                        }
                    });
                };

                ghost.on('mousedown', function (ev) {
                    ev.preventDefault()
                    isActive = true;
                    targetSize = parseInt(target.css(style));
                    body.addClass("flexbox-active");
                    ghost.addClass("active");
                }).parent().parent().on('mousemove', function (ev) {
                    if (!isActive) return;
                    lastSize = (ev[eventType] - sourceSize);
                    ghost.css(ghostStyle, (ev[eventType] - sourceSize));
                }).on('mousedown', function (ev) {
                    sourceSize = ev[eventType];

                    jQuery(document).docEventsUnbind(['mouseup', 'mousemove']);

                }).on('mouseup', function (ev) {

                    // originally 'task-settings' button runs 2 times per-click; using stopImmediatePropagation to prevent it.
                    ev.stopImmediatePropagation();

                    jQuery(document).docEventsUnbind(['mouseup', 'mousemove']);

                    isActive = false;
                    body.removeClass("flexbox-active");
                    ghost.removeClass("active");
                    ghost.css(ghostStyle, 0);

                    var taskSettingFlag = false;
                    var navigator = angular.element('div.menu-item.active').find('div.nav-pane-link-selenium').text();
                    if (/(Task Settings|Tasks)/.test(navigator)) {
                        taskSettingFlag = true;
                    }

                    if (isPerv) {
                        target.css(style, targetSize + lastSize);
                        $rootScope.$broadcast('RESIZE-TASK-PANE', {
                            width: targetSize + lastSize,
                            taskSettingFlag: taskSettingFlag
                        });
                    } else {
                        target.css(style, targetSize - lastSize);
                        $rootScope.$broadcast('RESIZE-TASK-PANE', {
                            width: targetSize + -lastSize,
                            taskSettingFlag: taskSettingFlag
                        });
                    }
                });

                var wEl = angular.element($window);
                var pWidth = 0;

                function resize() {
                    if (pWidth != content.parent().width()) {
                        pWidth = content.parent().width();
                        content.width(pWidth);
                        //fix animation delay
                        $timeout(resize, 50);
                    }
                }

                resize();
                //fix parent full width issue
                wEl.off('resize.splitter').on('resize.splitter', resize);
                var newScope = scope.$new();
                newScope.$on("resize", resize);
            }
        };
    }
]);
