package ru.kirill.chess.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.kirill.chess.Response1;
import ru.kirill.chess.model.Game;
import ru.kirill.chess.model.Player;
import ru.kirill.chess.service.PlayService;

@RestController
public class PlayController {

	@Autowired
	private PlayService playService;

	@GetMapping("/play")
	public Response1 play(@RequestParam String name) throws Exception {
		System.out.printf("Received play request with player name %s\n", name);
		Player player = playService.create(name);
		System.out.printf("Created new player: %s\n", player);
		Response1 response1 = null;
		while (response1 == null) {
			Thread.sleep(1000);
			Game game = playService.getGameByPlayerId(player.getId());
			if (game != null) {
				if (game.getPlayerOne().getId() == player.getId()){
					response1 = new Response1(game.getPlayerTwo().getName(), game.getPlayerOne().getColor(), game.getId(), player.getId());
				}
				else if(game.getPlayerTwo().getId() == player.getId()){
					response1 = new Response1(game.getPlayerOne().getName(), game.getPlayerTwo().getColor(), game.getId(), player.getId());
				}
			}
		}
		return response1;
	}

	@GetMapping("/test")
	public String test(@RequestParam String name){
		return name + ", ты овощ";
	}
}
