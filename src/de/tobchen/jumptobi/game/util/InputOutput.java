package de.tobchen.jumptobi.game.util;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

import android.content.res.AssetManager;
import de.tobchen.jumptobi.game.model.MinimapLevel;
import de.tobchen.jumptobi.model.ActiveChar;
import de.tobchen.jumptobi.model.Char;
import de.tobchen.jumptobi.model.Gamemap;
import de.tobchen.jumptobi.model.JumpChar;

public class InputOutput {
	public static Gamemap loadGameMap(String name, AssetManager assets)
			throws IOException {
		InputStream mapfile = assets.open("maps/" + name);
		DataInputStream input = new DataInputStream(mapfile);

		// Enemy lists
		LinkedList<JumpChar> horizontalEnemy = new LinkedList<JumpChar>();
		LinkedList<JumpChar> horizontalSpikedEnemy = new LinkedList<JumpChar>();
		LinkedList<Char> spikedEnemy = new LinkedList<Char>();
		LinkedList<JumpChar> verticalEnemy = new LinkedList<JumpChar>();
		LinkedList<JumpChar> verticalHorizontalEnemy = new LinkedList<JumpChar>();
		LinkedList<ActiveChar> fallingEnemy = new LinkedList<ActiveChar>();
		LinkedList<ActiveChar> shootingEnemy = new LinkedList<ActiveChar>();

		// Tilemap
		int width = input.readShort();
		Gamemap map = new Gamemap(width);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < 14; y++) {
				map.tiles[x][y] = input.readByte();
			}
		}

		boolean eof = false;
		while (!eof) {
			JumpChar chGrav;
			Char ch;
			ActiveChar chAct;

			try {
				byte id = input.readByte();
				switch (id) {
				case 0: // player start
					map.startX = input.readInt();
					map.startY = input.readInt();
					break;
				case 1: // background
					map.background = input.readByte();
					break;
				case 2: // weather
					map.weather = input.readByte();
					break;
				case 3: // type
					map.type = input.readByte();
					break;
				case 4: // Horizontal moving enemy
					chGrav = new JumpChar(input.readInt(), input.readInt(), 16,
							16);
					horizontalEnemy.add(chGrav);
					break;
				case 5: // Horizontal moving spiked enemy
					chGrav = new JumpChar(input.readInt(), input.readInt(), 16,
							16);
					horizontalSpikedEnemy.add(chGrav);
					break;
				case 6: // Standing spiked enemy
					ch = new Char(input.readInt(), input.readInt(), 16, 16);
					spikedEnemy.add(ch);
					break;
				case 7: // On the spot jumping enemy
					chGrav = new JumpChar(input.readInt(), input.readInt(), 16,
							16);
					verticalEnemy.add(chGrav);
					break;
				case 8: // Horizontal moving jumping enemy
					chGrav = new JumpChar(input.readInt(), input.readInt(), 16,
							16);
					verticalHorizontalEnemy.add(chGrav);
					break;
				case 9: // Falling enemy
					chAct = new ActiveChar(input.readInt(), input.readInt(),
							16, 16);
					fallingEnemy.add(chAct);
					break;
				case 10: // Shooting enemy
					chAct = new ActiveChar(input.readInt(), input.readInt(),
							16, 16);
					shootingEnemy.add(chAct);
					break;
				}
			} catch (EOFException e) {
				eof = true;
			}
		}

		// Close file
		input.close();
		mapfile.close();

		// Save characters
		map.horizontalEnemy = horizontalEnemy.toArray(map.horizontalEnemy);
		map.horizontalSpikedEnemy = horizontalSpikedEnemy
				.toArray(map.horizontalSpikedEnemy);
		map.spikedEnemy = spikedEnemy.toArray(map.spikedEnemy);
		map.verticalEnemy = verticalEnemy.toArray(map.verticalEnemy);
		map.verticalHorizontalEnemy = verticalHorizontalEnemy
				.toArray(map.verticalHorizontalEnemy);
		map.fallingEnemy = fallingEnemy.toArray(map.fallingEnemy);
		map.shootingEnemy = shootingEnemy.toArray(map.shootingEnemy);

		// Set destination sign
		map.setDestinationSign();

		return map;
	}

	public static MinimapLevel[] loadLevelList(AssetManager assets)
			throws IOException {
		// Storage for level
		LinkedList<MinimapLevel> levels = new LinkedList<MinimapLevel>();

		// Open file
		InputStream file = assets.open("maps/map.minimap");
		DataInputStream input = new DataInputStream(file);

		// Read
		boolean eof = false;
		while (!eof) {
			try {
				int x = input.readShort();
				int y = input.readShort();
				String mapfile = input.readUTF();
				levels.add(new MinimapLevel(x, y, mapfile));
			} catch (EOFException e) {
				eof = true;
			}
		}

		// Close file
		input.close();
		file.close();

		return levels.toArray(new MinimapLevel[] {});
	}
}
