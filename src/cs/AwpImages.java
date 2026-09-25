package cs;

import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.*;


public class AwpImages{
	List<Image> awpdirs = new ArrayList<>();
	float scaling = 0.237f;
	
	public AwpImages() throws SlickException {
		awpdirs.add(new Image("res/s_r.png"));
		awpdirs.add(new Image("res/s_d.png"));
		awpdirs.add(new Image("res/s_v.png"));
		awpdirs.add(new Image("res/s_u.png"));
	}
}