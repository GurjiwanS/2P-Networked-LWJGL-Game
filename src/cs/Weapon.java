package cs;
import org.newdawn.slick.*;

public class Weapon{
    boolean isPickedUp;
    float scaling;
    int xpos;
    int ypos;
    int height;
    int bulletSpeed;
    int ammo; //sniper 5, ak 15, pistol 100
    int dmg;  //sniper 100, 34, pistol 20
    int weaponID; //sniper 2, ak 1, pistol 0
    int uniqueID;
    String name;

    public Weapon(int weaponID, int xpos, int ypos, boolean isPickedUp, int qid) throws SlickException {
        this.isPickedUp = isPickedUp;
        this.xpos = xpos;
        this.ypos = ypos;
        this.uniqueID = qid;

        if (weaponID == 0) {
        	bulletSpeed = 3;
            this.ammo = 100;
            this.dmg = 20;
            this.weaponID = 0;
            this.name = "Pistol";
            
        }
        if (weaponID == 1) {
        	bulletSpeed = 6;
            this.ammo = 30;
            this.dmg = 5;
            this.weaponID = 1;
            this.name = "AK-47";
            height = 19;
            scaling = 0.106f;//64w, 19.2h
        }
        if (weaponID == 2) {
        	bulletSpeed = 30;
            this.ammo = 5;
            this.dmg = 100;
            this.weaponID = 2;
            height = 27;
            this.name = "AWP";
            scaling = 0.05f;//64w, 12h
        }
    }
    
}
