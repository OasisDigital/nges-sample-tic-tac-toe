# Tic-Tac-Toe (NGES Demo)

This is a complete web application demonstrating NGES, applying it in the somewhat unusual domain of playing
tic-tac-toe against the computer.

## Code Walthrough

The client side is implemented in AngularJS.

The server side uses Spring Boot, with Spring Web MVC and Jackson. Code specific to tic-tac-toe is in the
com.oasisdigital.nges.sample.tictactoe. Everything else (e.g. c.o.n.s.eventstream) is generic support code
that has nothing specific to this app.

There is one web controller: GameController. Client can POST commands to create game and perform move. There
are also two GET endpoints returning Server-Sent Event streams: One with basic information about all the games
(start/stop), another with all events for a particular game.

Each SSE stream is backed by a thread, querying NGES and emitting new events via SSE channel. This approach
has some obvious drawbacks and isn't the best idea for thousands of concurrent clients... But remember, it's
just a demo tic-tac-toe game! There is no point introducing too much complexity.

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
