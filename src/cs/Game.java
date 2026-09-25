package cs;


import org.newdawn.slick.*;
import org.newdawn.slick.state.*;

public class Game extends StateBasedGame{
	public static final String gamename = "Countered-Strike";
	public static final int game = 1;
	

	public Game(String gamename) {
		super(gamename);
		this.addState(new Client(game));
	}

	/**
	 * Initializes the added states
	 */
	public void initStatesList(GameContainer gc) throws SlickException{

	}
	
	public static void main(String[] args) {
		AppGameContainer agc;
		try {
			agc = new AppGameContainer(new Game(gamename));
			agc.setDisplayMode(672, 672, false);
			agc.setTargetFrameRate(250);
			agc.start();		
		}catch(SlickException e) {
			e.printStackTrace();
		}
	}

}
