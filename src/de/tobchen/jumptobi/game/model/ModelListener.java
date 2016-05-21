package de.tobchen.jumptobi.game.model;

public interface ModelListener {
	public void coinChanged(Model model);

	public void lifeChanged(Model model);

	public void boxDestroyed(Model model, int x, int y);

	public void coinCollected(Model model, int x, int y);

	public void enemyKilled(Model model, int x, int y);

	public void playerKilled(Model model);

	public void levelWon(Model model);
}
