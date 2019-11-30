'use strict';

var app = angular.module('myApp', ['ngAnimate']);


app.component('version', {
      templateUrl: '/version.html',
      controller: function VersionController() {
            this.$onInit = function () {
            }

            this.addWatcher = function () {
                  this.data.watchers.push({ name: "Mirek", date: new Date() });
                  this.callback(this.data);
            }
            this.removeWatcher = function (index) {
                  this.data.watchers.splice(index, 1);
                  this.callback(this.data);
            }
      }, bindings: {
            role: '@',
            data: '=',
            callback: '<'
      }
});

app.controller('versionManager', function () {
      this.avaliable = [{ name: 175, watchers: [] }, { name: 177, watchers: [] }, { name: 178, watchers: [] }];
      this.planned = [];
      var self = this;
      this.scoreChange = function (version) {
            debugger
            if (version.watchers.length > 0) {
                  _.remove(self.avaliable, {
                        name: version.name
                  });
                  if (_.findIndex(self.planned, function (o) { return o.name == version.name; }) == -1)
                        self.planned.push(version);
            } else {
                  _.remove(self.planned, {
                        name: version.name
                  });
                  self.avaliable.push(version);
            }
      }
})
app.directive('avaliable', function () {
      return { restrict: 'E', templateUrl: '/avaliable.html', replace: true };
});
app.directive('planned', function () {
      return { restrict: 'E', templateUrl: '/planned.html', replace: true };
});