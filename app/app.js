'use strict';

function refreshUserName() {
    var appElement = document.querySelector('[id=app]');
    var $scope = angular.element(appElement).scope();
    $scope.$apply(function () {
        $scope.user.name = localStorage.getItem('user');
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
    controller: function VersionController($scope) {
    }, bindings: {
        role: '@',
        data: '=',
        user: '=',
        add: '<',
        remove: '<',
        release: '<'
    }
});

var INIT = [{name: 175, watchers: []}, {name: 177, watchers: []}, {name: 178, watchers: []}, {name: 179, watchers: []}];
app.controller('versionManager', function ($http, $scope, $timeout) {
    var self = this;
    $scope.$watch('user', function () {
        if ($scope.user) {
            var all = [].concat(self.avaliable).concat(self.planned);
            for (let o of all) {
                self.scoreChange(o);
            }
        }
    });
    this.$onInit = function () {
        $scope.user = {"name": localStorage.getItem('user')};
        this.reloadVotes();
    };
    this.reloadVotes = function () {
        self.planned = [];
        self.avaliable = JSON.parse(JSON.stringify(INIT));
        $http.get('http://localhost:8080/vp/vote/list').then(function successCallback(response) {
            debugger;
            for (let d of response.data) {
                let find = _.find([].concat(self.avaliable).concat(self.planned), (o) => o.name == d.version);
                if (find)
                    self.addWatcher(find, d.login)
            }
        }, function errorCallback(response) {
        });
    };

    this.avaliable = JSON.parse(JSON.stringify(INIT));
    this.planned = [];
    this.releaseVersion = function (version) {
        $http.post('http://localhost:8080/vp/vote/releaseVersion', {version: version}).then(self.reloadVotes);
    };
    this.releaseAll = function () {
        $http.post('http://localhost:8080/vp/vote/releaseAll').then(self.reloadVotes);
    };
    this.addWatcher = function (version, userName, voteType) {
        let user = userName != null ? userName : $scope.user.name;
        if (_.findIndex(version.watchers, (o) => o.user.name == user) == -1) {
            version.watchers.push({user: {name: user}, date: new Date()});
            if (version.watchers.length == 1) {
                _.remove(self.avaliable, {
                    name: version.name
                });
                self.planned.push(version);
            }
        }
        self.scoreChange(version, voteType);
    };
    this.removeWatcher = function (version, userName, voteType) {
        let user = userName != null ? userName : $scope.user.name;
        version.watchers.splice(_.findIndex(version.watchers, (o) => o.name == user), 1);
        if (version.watchers.length == 0) {
            _.remove(self.planned, {
                name: version.name
            });
            self.avaliable.push(version);
        }
        self.scoreChange(version, voteType);
    };
    this.scoreChange = function (version, voteType) {
        let vote = _.find(version.watchers, (o) => o.user.name == $scope.user.name);
        version.voted = vote != undefined;
        if (voteType === 'up') {
            $http.post('http://localhost:8080/vp/vote', {version: version.name, login: $scope.user.name});
        } else if (voteType === 'down') {
            $http.post('http://localhost:8080/vp/vote/delete', {version: version.name, login: $scope.user.name});
        }

    };
});
app.directive('avaliable', function () {
    return {restrict: 'E', templateUrl: '/avaliable.html', replace: true};
});
app.directive('planned', function () {
    return {restrict: 'E', templateUrl: '/planned.html', replace: true};
});
