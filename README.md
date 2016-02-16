# Tic-Tac-Toe (NGES Demo)

This is a complete web application demonstrating NGES, applying it in the somewhat unusual domain of playing
tic-tac-toe against the computer.

## Code Walthrough

The client side is implemented in AngularJS.

The server side uses Spring Boot, with Spring Web MVC and Jackson. Code specific to tic-tac-toe is in the
com.oasisdigital.nges.sample.tictactoe. Everything else (e.g. c.o.n.s.eventstream) is generic support code
that has nothing specific to this app.

There is one web controller: GameController. Client can POST commands to create game and perform move. There
are also two GET endpoints returning Server-Sent Event (SSE) streams: One with basic information about all the
games (start/stop), another with all events for a particular game.

Whenever the client POSTs a command, server creates a new game or restores game state from the event store.
All changes to the game are persisted by appending new events to the store. It is a simplistic approximation
of the "domain side" of DDD/CQRS. The point of this project is to demonstrate the event store and not a CQRS
engine, so the latter is reduced to easily digestible minimum.

As noted above, the GET queries are implemented with SSE streams. The streams simply emit unchanged events
from the event store, which the client needs to interpret to determine game list or state of a particular
game. Each SSE stream is backed by a thread, querying NGES and emitting new events via SSE channel. This
approach has some obvious drawbacks and isn't the best idea for thousands of concurrent clients... But
remember, it's just a demo tic-tac-toe game! There is no point introducing too much complexity.

A realistic application will most likely have some kind of persistence for the read model, but it's a
well-understood area. Implementing it in this project would only add complexity for little value, so it is
omitted in this demo in favor of streaming the events directly.

## Running

In order to build the project:

1. Install PostgreSQL and create a new database.
2. Copy config/SAMPLEapplication.properties to config/application.properties and adjust it to match your
setup.
3. Run com.oasisdigital.nges.sample.tictactoe.TicTacToe from your IDE, or run the following Gradle task in the
parent project:

```
./gradlew bootRun
```

The schema will be installed automatically with Flyway when the app starts.

4. Open http://localhost:8080 in your browser.
