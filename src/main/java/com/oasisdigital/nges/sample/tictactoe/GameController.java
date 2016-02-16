package com.oasisdigital.nges.sample.tictactoe;

import java.util.Optional;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.springframework.web.util.UriComponentsBuilder;

import com.oasisdigital.nges.sample.tictactoe.domain.Game;
import com.oasisdigital.nges.sample.tictactoe.domain.GameInfo;
import com.oasisdigital.nges.sample.tictactoe.domain.NewMove;

/**
 * Web controller responsible for all API endpoints (write and read side).
 *
 */
@Controller
public class GameController {

    private final EventStreams eventStreams;
    private final GameStore gameStore;

    @Autowired
    public GameController(GameStore gameStore, EventStreams eventStreams) {
        this.gameStore = gameStore;
        this.eventStreams = eventStreams;
    }

    /**
     * @return SSE event stream with all events for the given game.
     */
    @RequestMapping(path = "/api/game/{id}", method = RequestMethod.GET)
    public SseEmitter getGame(@PathVariable("id") UUID gameId,
            @RequestHeader(value = "Last-Event-ID") Optional<String> lastEventId) {
        return eventStreams.makeSingleGameStream(gameId, lastEventId).getSseEmitter();
    }

    /**
     * @return SSE event stream with GameCreated and GameFinished events for all games.
     */
    @RequestMapping(path = "/api/game", method = RequestMethod.GET)
    public SseEmitter getGames(@RequestHeader(value = "Last-Event-ID") Optional<String> lastEventId) {
        return eventStreams.makeGameListStream(lastEventId).getSseEmitter();
    }

    /**
     * Create a new game with given name and human player (circle or cross).
     *
     * @return HTTP Created with Location header set for the event stream for this game.
     */
    @RequestMapping(path = "/api/game/create", method = RequestMethod.POST)
    public ResponseEntity<Void> createGame(UriComponentsBuilder uri, @Valid @RequestBody GameInfo gameInfo) {
        UUID id = UUID.randomUUID();
        Game game = new Game(id);
        game.create(gameInfo);
        gameStore.save(game);

        return ResponseEntity.created(uri.path("/api/game/" + id).build().toUri()).build();
    }

    /**
     * Record a move by the human player and respond by the computer player.
     */
    @RequestMapping(path = "/api/game/{id}/move", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void move(@PathVariable("id") UUID gameId, @Valid @RequestBody NewMove move) {
        Game game = gameStore.load(gameId);
        game.movePlayerAndRespond(move);
        gameStore.save(game);
    }

}
