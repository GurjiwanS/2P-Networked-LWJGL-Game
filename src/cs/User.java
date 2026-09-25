package cs;

public class User{
    public int xpos;
    public int direction;
    public int ypos;
    public int[] backpack = new int[2];
    public int liv = 100;
    public int score = 0;
    public int currentSelection = 0;
    public int firerate = 0;
    public int shoot = 0;
    public int id;
    public int pickup = 0;
    

    public User(int id, int xpos, int ypos){
    	this.id = id;
        this.xpos = xpos;
        this.ypos = ypos;
        backpack[0] = 0;
        backpack[1] = 0;
    }
}
