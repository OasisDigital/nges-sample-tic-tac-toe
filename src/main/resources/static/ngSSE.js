; (function() {
  'use strict';

  angular.module('ngSSE', [])

  .service('EventSourceWrapperFactory', function($rootScope) {
    function EventSourceWrapper(url) {
      var _this = this;
      this.url = url;
      this.eventSource = new EventSource(url);

      this.addListener = function(eventType, handler) {
        _this.eventSource.addEventListener(eventType, function(e) {
          $rootScope.$apply(function() {
            handler(JSON.parse(e.data));
          });
        });
      };

      this.close = function() {
        _this.eventSource.close();
        _this.eventSource = null;
      };
    }

    this.make = function(url) {
      return new EventSourceWrapper(url);
    };
  });
})();
