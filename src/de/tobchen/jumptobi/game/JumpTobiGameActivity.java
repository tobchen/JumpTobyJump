package de.tobchen.jumptobi.game;

import android.os.Bundle;
import de.tobchen.android.game.manager.GameActivity;
import de.tobchen.jumptobi.game.state.ConfigState;
import de.tobchen.jumptobi.game.state.IngameState;
import de.tobchen.jumptobi.game.state.MenuState;
import de.tobchen.jumptobi.game.state.MinimapState;

public class JumpTobiGameActivity extends GameActivity {
	private IngameState ingame;
	private MinimapState minimap;
	private MenuState menu;
	private ConfigState config;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ingame = new IngameState("ingame");
		minimap = new MinimapState("minimap");
		menu = new MenuState("menu");
		config = new ConfigState("config");
		manager.addState(ingame);
		manager.addState(minimap);
		manager.addState(menu);
		manager.addState(config);

		manager.changeState("menu", null);
	}
}