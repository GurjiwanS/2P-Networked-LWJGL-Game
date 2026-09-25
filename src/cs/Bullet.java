package cs;

public class Bullet{
    int xpos;
    int ypos;
    int dir;
    int bid;
    int weaponID;
    
    public Bullet(int x, int y, int dir, int bid, int wid) {
    	xpos = x;
    	ypos = y;
    	this.dir = dir;
    	this.bid = bid;
    	weaponID = wid;
    }
}
