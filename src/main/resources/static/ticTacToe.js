; (function() {
  'use strict';

  angular
      .module('ticTacToe', [ 'ngRoute', 'ngSSE' ])

      .config(function moduleConfig($routeProvider) {
        $routeProvider.when('/game/:gameId', {});
        $routeProvider.otherwise({
          redirectTo: '/game/new'
        });
      })

      // Subscribes to SSE events for game created and finished and builds an in-memory list of all the games.
      // The list is exposed as GameList.gameList array, for use in a $watch or to put on scope/controller.
      .service('GameList', function GameList(ServerApi) {
        var self = this,
            eventSource;

        self.gameList = [];

        eventSource = ServerApi.gameEvents();
        eventSource.addListener("GameCreated", function onGameCreated(e) {
          self.gameList.unshift(e);
        });
        eventSource.addListener("GameFinished", function onGameFinished(e) {
          var i;
          for (i in self.gameList) {
            if (self.gameList[i].gameId == e.gameId) {
              self.gameList[i].winner = e.winner || 'DRAW';
              break;
            }
          }
        });
      })

      // Subscribes to SSE events for a particular game and exposes a ready-to-use "current state" view.
      .service('GameState',
          function GameState($rootScope, $route, $timeout, ServerApi) {
            var self = this,
                eventSource,
                eventHandlers = {};

            this.isGameRunning = function isGameRunning() {
              return !!self.currentGame;
            }

            eventHandlers['GameCreated'] = function(eventData) {
              self.currentGame = {
                id: eventData.gameId,
                name: eventData.meta.name,
                humanPlayer: eventData.meta.humanPlayer,
                cells: [ 
                          null, null, null,
                          null, null, null,
                          null, null, null
                        ],
                winner: null,
                winningAxis: []
              };
            };
            eventHandlers['MovePerformed'] = function onMovePerformed(eventData) {
              self.currentGame.cells[eventData.cell] = eventData.player;
            };
            eventHandlers['GameFinished'] = function onGameFinished(eventData) {
              self.currentGame.winner = eventData.winner || 'DRAW';
              self.currentGame.winningAxis = eventData.winningAxis || [];
            };

            $rootScope.$on('$locationChangeStart', function onLocationChangeStart(e) {
              if (eventSource) {
                eventSource.close();
              }
            });
            $rootScope.$on('$locationChangeSuccess', function onLocationChangeSuccess(e) {
              $timeout(function() {
                var gameId = $route.current.params.gameId,
                    eventType;
                self.currentGame = null;
                if (gameId != 'new') {
                  eventSource = ServerApi.gameEvents(gameId);
                  for (eventType in eventHandlers) {
                    eventSource.addListener(eventType, eventHandlers[eventType]);
                  }
                }
              });
            });
          })

      .service('ServerApi', function ServerApi($http, EventSourceWrapperFactory) {
        this.gameEvents = function gameEvents(gameId) {
          if (gameId) {
            return EventSourceWrapperFactory.make('/api/game/' + gameId);
          } else {
            return EventSourceWrapperFactory.make('/api/game');
          }
        }

        this.startGame = function startGame(name) {
          return $http.post('/api/game/create', {
            humanPlayer: 'O',
            name: name
          }).then(function unwrapGameId(resp) {
            var streamUrl = resp.headers('Location');
            return streamUrl.substring(streamUrl.lastIndexOf('/') + 1);
          });
        };

        this.move = function move(gameId, cell, player) {
          return $http.post('/api/game/' + gameId + "/move", {
            cell: cell,
            player: player
          });
        };
      })

      .directive(
          'tttCell',
          function tttCell(GameState, ServerApi) {
            return {
              scope: true,
              templateUrl: 'ttt-cell.html',
              link: function link(scope, element, attrs) {
                var game = GameState.currentGame;

                scope.cells = game.cells;
                scope.cellNumber = parseInt(attrs['tttCell']);

                scope.isOnWinningAxis = function isOnWinningAxis() {
                  return game.winningAxis.indexOf(scope.cellNumber) >= 0;
                }

                scope.humanWon = function humanWon() {
                  return game.winner == game.humanPlayer;
                }

                scope.onCellClicked = function onCellClicked() {
                  if(!scope.cells[scope.cellNumber] && !game.winner) {
                    ServerApi.move(game.id, scope.cellNumber, game.humanPlayer);
                  }
                }
              }
            }
          })

      .controller('TicTacToeController', function TicTacToeController(GameState) {
        this.gameState = GameState;
      })

      .controller(
          'GameListController',
          function GameListController($location, ServerApi, GameList) {
            var self = this;

            self.games = GameList.gameList;

            function getRandomInt(min, max) {
              return Math.floor(Math.random() * (max - min)) + min;
            }

            this.randomizeNewGameName = function randomizeNewGameName() {
              self.newGameName = 'Game ' + getRandomInt(1, 10000);
            }

            this.startGame = function startGame() {
              ServerApi.startGame(self.newGameName).then(function openGame(gameId) {
                $location.path('/game/' + gameId);
                self.newGameName = null;
              });
            }

            this.join = function join(game) {
              $location.path('/game/' + game.gameId);
            }

            this.randomizeNewGameName();
  });
})();
