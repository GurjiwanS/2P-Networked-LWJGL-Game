package cs;

import java.util.ArrayList;
import java.util.List;

import org.newdawn.slick.*;


public class AkImages{
	List<Image> akdirs = new ArrayList<>();
	float scaling = 0.22222222f; 
	
	public AkImages() throws SlickException {
		akdirs.add(new Image("res/ak_r.png"));
		akdirs.add(new Image("res/ak_d.png"));
		akdirs.add(new Image("res/ak_l.png"));
		akdirs.add(new Image("res/ak_u.png"));
	}
}
