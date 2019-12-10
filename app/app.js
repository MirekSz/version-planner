'use strict';

function refreshUserName() {
    var appElement = document.querySelector('[ng-app=myApp]');
    var $scope = angular.element(appElement).scope();
    $scope.$apply(function () {
        $scope.user = localStorage.getItem('user');
    });
}

function editUserName() {
    $("#user-name").val(localStorage.getItem('user'));
    $('#exampleModal').modal('toggle');
}

var app = angular.module('myApp', ['ngAnimate']);
setTimeout(function () {
    if (!localStorage.getItem('user')) {
        editUserName();
    } else {
        refreshUserName();
    }
}, 500)

function closeModal() {
    localStorage.setItem('user', $("#user-name").val());
    refreshUserName();

    $('#exampleModal').modal('hide');
}

app.component('version', {
    templateUrl: '/version.html',
    controller: function VersionController() {
        this.$onInit = function () {
            var user = this.user;
            this.voted = _.findIndex(this.data.watchers, (o) => o.name == user) == -1;
        };

        this.addWatcher = function () {
            var user = this.user;
            if (_.findIndex(this.data.watchers, (o) => o.name == user) == -1) {
                this.data.watchers.push({name: this.user, date: new Date()});
                this.callback(this.data);
            }
        };
        this.removeWatcher = function (index) {
            this.data.watchers.splice(index, 1);
            this.callback(this.data);
        };
        this.removeCurrentWatcher = function (index) {
            this.data.watchers.splice(index, 1);
            this.callback(this.data);
        };
    }, bindings: {
        role: '@',
        data: '=',
        user: '=',
        callback: '<'
    }
});

app.controller('versionManager', function () {
    this.avaliable = [{name: 175, watchers: []}, {name: 177, watchers: []}, {name: 178, watchers: []}];
    this.planned = [];
    var self = this;
    this.release = function () {
        this.planned = [];
        this.avaliable = [{name: 175, watchers: []}, {name: 177, watchers: []}, {name: 178, watchers: []}];
    };
    this.scoreChange = function (version) {
        if (version.watchers.length > 0) {
            _.remove(self.avaliable, {
                name: version.name
            });
            if (_.findIndex(self.planned, function (o) {
                return o.name == version.name;
            }) == -1)
                self.planned.push(version);
        } else {
            _.remove(self.planned, {
                name: version.name
            });
            self.avaliable.push(version);
        }
    };
});
app.directive('avaliable', function () {
    return {restrict: 'E', templateUrl: '/avaliable.html', replace: true};
});
app.directive('planned', function () {
    return {restrict: 'E', templateUrl: '/planned.html', replace: true};
});
