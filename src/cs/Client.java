package cs;

import java.io.*;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.newdawn.slick.*;
import org.newdawn.slick.state.*;
import org.newdawn.slick.tiled.TiledMap;

public class Client extends BasicGameState {
	AkImages aks;
	AwpImages awps;
	PistolImages pistols;
	Socket sckt;
	Image bullet;
	boolean win = false;
	boolean lose = false;
	int currentSelection = 0;
	int playerCount;
	int keyW;
	int keyS;
	int keyD;
	int keyA;
	int keyF;
	boolean fire = false;
	int shoot = 0;
	int direction = 0;
	boolean canmove;
	int currentBullet = 0;
	List<Object> soldierGuns = new ArrayList<>();
	List<Soldier> soldiers = new ArrayList<>();
	List<Bullet> bullets = new ArrayList<>();
	List<Weapon> wpns = new ArrayList<>();
	List<Weapon> gunTypes = new ArrayList<>();
    int bounds = 672;
    private TiledMap map;
    SendThread str;
    RecieveThread rtr;
    private int fireR = 700;

    private int timer = 0;

    public Client(int state) {

    }

	/**
	 * Initializes the added variables
	 */
    public void init(GameContainer gc, StateBasedGame sbg) throws SlickException {
    	aks = new AkImages();
    	awps = new AwpImages();
    	pistols = new PistolImages();
    	bullet  = new Image("res/bullet.png");
    	playerCount = 2;
    	soldierGuns.add(pistols);
    	soldierGuns.add(aks);
    	soldierGuns.add(awps);
        for(int i = 0; i < playerCount; i++) {
        	soldiers.add(new Soldier());
        }
    	try {
    		sckt = new Socket("localhost", 3235);
    		str = new SendThread();
    		rtr = new RecieveThread();
    		str.start();
    		rtr.start();
    	} catch(IOException e) {
    		System.out.println("CLIENT: cant initially connect");
    	}
    	keyW = 0;
    	keyD = 0;
    	keyA = 0;
    	keyS = 0;
    	keyF = 0;
    	canmove = true;
        map = new TiledMap("Map/cs1.tmx");
        placeGuns();
        try {
			defineGuns();
		} catch (IOException e) {
			e.printStackTrace();
		}
        
    }


    /**
     * renders added elements to display on screen
     * @param gc, game container that manages the game loop, fps recording and input
     * @param sbg, isolates stages of the game into different states for management
     * @param g, renders primitives to the LWJGL canvas
     */
    public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException {
        if(win == true) {
        	g.drawString("YOU WIN", 300, 100);
        }
        else if(lose == true) {
        	g.drawString("YOU LOSE", 300, 100);
        }else {
        map.render(0, 0);
        for(int i = 0; i < wpns.size(); i++) {
        	Image img;
        	if(wpns.get(i).weaponID == 1)
        		img = new Image("res/tak47.png");
        	else if(wpns.get(i).weaponID == 2)
        		img = new Image("res/tawp.png");
        	else
        		img = new Image("res/bullet.png");
        	
        	img.draw(wpns.get(i).xpos, wpns.get(i).ypos, wpns.get(i).scaling);
        }
        for(int i = 0; i < playerCount; i++) {
        soldiers.get(i).currentImg.draw(soldiers.get(i).x, soldiers.get(i).y, soldiers.get(i).scaling);
        }
        
        for(int i = 0; i < bullets.size(); i++) {
        	bullet.draw(bullets.get(i).xpos, bullets.get(i).ypos, 0.003f);
        }

        }
    }
    

    /**
     * updates elements and variables, for instance updating bullets coordinates
     * and keepin track of keypresses to bring about movement
     */
    public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException {
    	if(win == true || lose == true) {
    		try {
	    			JSONObject j = new JSONObject();
					j.put("done", 1);
		    		sendToServer(j);
				} catch (JSONException e) {
					e.printStackTrace();
				}
	    		releaseKeys();
    		}
    	else {
        timer -= delta;
    	checkBulletCollision();
    	bulletTravel();
        Input movement = gc.getInput();
        //keepInsideBounds(i);
        try {
			updateMovement(movement);
		} catch (JSONException e) {
			e.printStackTrace();
		}
        //}
        	str.run();
        	rtr.run();
        
        releaseKeys();
    	}
       
    }
    
    private void  bulletTravel() {
    	for(int i = 0; i < bullets.size(); i++) {
    		int newx = bullets.get(i).xpos;
    		int newy = bullets.get(i).ypos;
    		int dir = bullets.get(i).dir;
    		int id = bullets.get(i).bid;
    		int wid = bullets.get(i).weaponID;
    		int bulletSpeed = gunTypes.get(bullets.get(i).weaponID).bulletSpeed;
    		
    		if(dir == 0)
    			newx = bullets.get(i).xpos + bulletSpeed;
    		else if(dir == 2) 
    			newx = bullets.get(i).xpos - bulletSpeed;	
    		else if(dir == 3)
    			newy = bullets.get(i).ypos - bulletSpeed;
    		else 
    			newy = bullets.get(i).ypos + bulletSpeed;
    		
    		bullets.set(i, new Bullet(newx, newy, dir, id, wid));
    		
    		if(newx > 672 || newx < 0 || newy < 0 || newy > 672) {
    			bullets.remove(i);	
    		}
    	}
    }
    private void checkBulletCollision() {
  	  
    	  for(int i = 0; i < soldiers.size(); i++) {
    		  int sRoof = soldiers.get(i).y;
    		  int sFloor = soldiers.get(i).y+32;
    		  int sLeft = soldiers.get(i).x;
    		  int sRight = soldiers.get(i).x+32;
    		  for(int k = 0; k < bullets.size(); k++) {
        		  int bRoof = bullets.get(k).ypos;
        		  int bFloor = bullets.get(k).ypos+6;
        		  int bLeft = bullets.get(k).xpos;
        		  int bRight = bullets.get(k).xpos+6;
    			  
	    		  if (bRoof > sRoof && bRoof < sFloor)
	    				 if(bFloor < sFloor && bFloor > sRoof) 
	    					 if(bRight < sRight && bRight > sLeft) 
	    						 if(bLeft > sLeft && bLeft < sRight) {
	    			 try {
	    			  JSONObject jsn = new JSONObject();
	    			  jsn.put("done", 0);
	    			  jsn.put("bullethit", 1);
	    			  jsn.put("whogotshot", soldiers.get(i).id);
	    			  jsn.put("weaponID", bullets.get(k).weaponID);
	    			  sendToServer(jsn);
	    			 } catch(JSONException e) {
	    				 System.out.println("Cant pack bullet-hit info");
	    			 }
	    		  }
    		  }
    	  }
    }
    		/*
    		 * WASD to move, Space to shoot, F to pick up weapon, 
    		 * arrows to look in different direction, 
    		 * 1 and 2 to switch between weapons
    		 */
    private void updateMovement(Input movement) throws JSONException {
        if (movement.isKeyDown(Input.KEY_S)) {
        	keyS = 1;
        }
        if (movement.isKeyDown(Input.KEY_W)) {
        	keyW = 1;
        }
        if (movement.isKeyDown(Input.KEY_D)) {
        	keyD = 1;
        }
        if (movement.isKeyDown(Input.KEY_A)) {
        	keyA = 1;
        }//-------------------------------------
        if (movement.isKeyDown(Input.KEY_SPACE)) {
        	shoot = 1;
        }//-------------------------------------
        if (movement.isKeyDown(Input.KEY_1)) {
        	currentSelection = 0;
        }
        if (movement.isKeyDown(Input.KEY_2)) {
        	currentSelection = 1;
        }if (movement.isKeyDown(Input.KEY_F)) {
        	keyF = 1;
        }//-------------------------------------
        
        if(movement.isKeyDown(Input.KEY_UP))
        	direction = 3;
        else if(movement.isKeyDown(Input.KEY_LEFT))
        	direction = 2;
        else if(movement.isKeyDown(Input.KEY_DOWN))
        		direction = 1;
        else if(movement.isKeyDown(Input.KEY_RIGHT))
        	direction = 0;
    }
    
   
    private void releaseKeys() {
    	keyW = 0;
    	keyD = 0;
    	keyA = 0;
    	keyS = 0;
    	keyF = 0;
    	shoot = 0;
    }
    
    //write json object formatted to UTF-8 binary to server, flush to send even small bits of information
    public void sendToServer(JSONObject jsn){
		OutputStreamWriter writer; 
		try {
			writer = new OutputStreamWriter(sckt.getOutputStream(), "UTF-8");

	        writer.write(jsn.toString() + "\n");
	        writer.flush();
		} catch (UnsupportedEncodingException e) {
			System.out.println("CLIENT: cant send to server pga encoding STS");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("CLIENT: cant send to server pga io STS");
			e.printStackTrace();
		}

    }
    
    public JSONObject jsnToSend() {
    	JSONObject jsn = new JSONObject();
        try {
        	jsn.put("done", 0);
        	jsn.put("keyF", keyF);
        	if(keyF == 1) {
        		jsn.put("wpncount", wpns.size());
        	for(int i = 0; i < wpns.size(); i++) {
        		jsn.put("wx".concat(Integer.toString(i)), wpns.get(i).xpos);
        		jsn.put("wy".concat(Integer.toString(i)), wpns.get(i).ypos);
        		jsn.put("wh".concat(Integer.toString(i)), wpns.get(i).height);
        		jsn.put("wu".concat(Integer.toString(i)), wpns.get(i).uniqueID);
        		jsn.put("wid".concat(Integer.toString(i)), wpns.get(i).weaponID);
        		
        	}
        	}
        	jsn.put("bullethit", 0);
            jsn.put("keyW", keyW);
            jsn.put("keyA", keyA);
            jsn.put("keyS", keyS);
            jsn.put("keyD", keyD);
            jsn.put("currentSelection", currentSelection);
            jsn.put("direction", direction);
            jsn.put("shoot", shoot);
            
    } catch (JSONException e) {
    	System.out.println("CLIENT: cant create keys jsn pga jsn");
        e.printStackTrace();
    }
    	return jsn;
    }
    
    /*
     * recieve UTF-8 formatted json from server using bufferReader
     */
    public JSONObject recieveFromServer() {
    	JSONObject jsn = new JSONObject();
		BufferedReader reader;
		try {
			reader = new BufferedReader(new InputStreamReader(sckt.getInputStream(), "UTF-8"));
	        String line = reader.readLine();
	        jsn = new JSONObject(line);
	        System.out.println("Received from Server: " + jsn.toString(2));
		} catch (UnsupportedEncodingException | JSONException e) {
			System.out.println("CLIENT: cant recieve from server pga encoding elr jsn");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("CLIENT: cant recieve from server pga io");
			e.printStackTrace();
		}
        return jsn;
    }
    //om causing socket 1 skicka t 2 att playerid1 solider ska bort
    public void evaluate(JSONObject jsn) throws SlickException {
    	try {
    		if(jsn.getInt("gameOver") == 1) {
    			win = true;
    		}
    		else if(jsn.getInt("gameOver") == -1){
    			lose = true;
    		}else {
    		fireR = jsn.getInt("fr");
    		if(jsn.getInt("removegun") == 1 || jsn.getInt("removegun") == 2) {
    			for(int i = 0; i < wpns.size(); i++)
    				if(jsn.getInt("removeid") == wpns.get(i).uniqueID)
    					wpns.remove(i);
    		}
    		
    			placement(jsn);
    		}
		} catch (JSONException e) {
			System.out.println("CLIENT: cant evaluate");
			e.printStackTrace();
		}

    }
    
    private void placement(JSONObject jsn) throws JSONException {
		playerCount = jsn.getInt("playerCount");
		for(int i = 0; i < playerCount; i++) {
		soldiers.get(i).x = jsn.getInt("xpos".concat(Integer.toString((i+1))));
    	soldiers.get(i).y = jsn.getInt("ypos".concat(Integer.toString((i+1))));
    	soldiers.get(i).direction = jsn.getInt("direction".concat(Integer.toString((i+1))));
    	soldiers.get(i).currentSelection = jsn.getInt("currentSelection".concat(Integer.toString((i+1))));
    	
    	Object weapon = ((Object) soldierGuns.get(jsn.getInt("currentSelection".concat(Integer.toString((i+1))))));
    	if(weapon.getClass() == AkImages.class) {
    		soldiers.get(i).scaling = ((AkImages) weapon).scaling;
    		soldiers.get(i).currentImg = ((AkImages) weapon).akdirs.get(jsn.getInt("direction".concat(Integer.toString((i+1)))));
    	}else if(weapon.getClass() == PistolImages.class) {
    		soldiers.get(i).scaling =((PistolImages) weapon).scaling;
	    	soldiers.get(i).currentImg = ((PistolImages) weapon).pistoldirs.get(jsn.getInt("direction".concat(Integer.toString((i+1)))));
		}else if(weapon.getClass() == AwpImages.class) {
			soldiers.get(i).scaling =((AwpImages) weapon).scaling;
	    	soldiers.get(i).currentImg = ((AwpImages) weapon).awpdirs.get(jsn.getInt("direction".concat(Integer.toString((i+1)))));
		}
    	
	    if(jsn.getInt("shoot".concat(Integer.toString((i+1)))) == 1) {
	    	if(soldiers.get(i).direction == 0)
	    	bullets.add(new Bullet(soldiers.get(i).x+34, soldiers.get(i).y+24, 0, generateBulletID(), jsn.getInt("currentSelection".concat(Integer.toString((i+1))))));
	    	else if(soldiers.get(i).direction == 1)
		    	bullets.add(new Bullet(soldiers.get(i).x+3, soldiers.get(i).y+32, 1, generateBulletID(), jsn.getInt("currentSelection".concat(Integer.toString((i+1))))));
	    	else if(soldiers.get(i).direction == 2)
		    	bullets.add(new Bullet(soldiers.get(i).x-20, soldiers.get(i).y+6, 2, generateBulletID(), jsn.getInt("currentSelection".concat(Integer.toString((i+1))))));
	    	else if(soldiers.get(i).direction == 3)
		    	bullets.add(new Bullet(soldiers.get(i).x+16, soldiers.get(i).y-17, 3, generateBulletID(), jsn.getInt("currentSelection".concat(Integer.toString((i+1))))));
	    }
	    
		}
    }
    
    private int generateBulletID() {
    	currentBullet++;
    	return currentBullet;
    }
    
    
    private class SendThread extends Thread implements Runnable{
     public SendThread() {
     }

        public void run() {
            sendToServer(jsnToSend());
        }

    }
    
    private class RecieveThread extends Thread implements Runnable{
        public RecieveThread() {
        }

           public void run() {
   			try {
				evaluate(recieveFromServer());
			} catch (SlickException e) {
				e.printStackTrace();
			} 
           }

       }
    private void placeGuns() throws SlickException {
        wpns.add(new Weapon(1, 10*32, 16*32, false, 1));
        wpns.add(new Weapon(1, 10*32, 4*32, false, 2));
        wpns.add(new Weapon(2, 10*32, 10*32, false, 3));
    }

    /**
     * id to identify stage
     * @return int of id
     */
    public int getID() {
        return 1;
    }
    
    private void defineGuns() throws IOException, SlickException {
        gunTypes.add(new Weapon(0, 10, 16, false, 0));
        gunTypes.add(new Weapon(1, 10, 16, false, 0));
        gunTypes.add(new Weapon(2, 10, 16, false, 0));
    }
}