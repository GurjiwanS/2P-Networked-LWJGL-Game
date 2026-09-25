package cs;

import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.*;


public class PistolImages{
	List<Image> pistoldirs = new ArrayList<>();
	float scaling = 0.17679f;
	
	public PistolImages() throws SlickException {
		pistoldirs.add(new Image("res/p_r.png"));
		pistoldirs.add(new Image("res/p_d.png"));
		pistoldirs.add(new Image("res/p_l.png"));
		pistoldirs.add(new Image("res/p_u.png"));
	}
}