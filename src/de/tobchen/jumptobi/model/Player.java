package de.tobchen.jumptobi.model;

public class Player extends JumpChar {

	public boolean hasGuardian;
	public int invulnerableTime;
	public int reanimateTime;
	public boolean smallJumpToDo;

	public Player(int x, int y, boolean guardian) {
		super(x, y, 14, 24);

		reset();
		hasGuardian = guardian;
	}

	@Override
	public void reset() {
		super.reset();
		state = STATE_ACTIVE;
		invulnerableTime = Integer.MIN_VALUE;
		reanimateTime = Integer.MIN_VALUE;
		hasGuardian = false;
		looksRight = true;
		smallJumpToDo = false;
	}
}
