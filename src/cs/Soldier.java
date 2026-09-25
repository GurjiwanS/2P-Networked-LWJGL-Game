package cs;

import org.newdawn.slick.*;

public class Soldier{
	int id;
	int x = 32;
	int y = 32;
	int direction = 0; //0 höger, 1 ner, 2 vänster, 3, upp
	float scaling = 1;
	Image currentImg = new Image("res/p_r.png");;
	int currentSelection = 0; //0 pistol, 1 ak, 2 awp
	int firerate = 0;
	
	public Soldier() throws SlickException {
	}
	
}