package cs;

import org.json.JSONException;
import org.json.JSONObject;
import org.newdawn.slick.SlickException;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server{ 
  static ServerSocket serverSckt;
  static Socket sckt1;
  static Socket sckt2;
  static Threaded tr1;
  static Threaded tr2;
  static int remove = 0;
  static int removeid = 0;
  static int playerCount = 0;
  static int score = 2;
  static int donesignal = 0;
  static List<Socket> sockets = new ArrayList<>();
  static List<User> users = new ArrayList<>();
  static List<Threaded> trs = new ArrayList<>();
  static List<Bullet> bullets = new ArrayList<>();
  static List<Weapon> gunTypes = new ArrayList<>();
  static List<Walls> walls = new ArrayList<>();

  public static void main(String[] args) throws Exception{
      startRunning();
  }
//------------------------------------------------------------------------------
public static void startRunning() throws JSONException, SlickException{
  try{
    serverSckt = new ServerSocket(3235);
    definingGuns();
    placingWalls();
    while(true){
            waitForConnection();
            while(users.get(0).liv > 0 && users.get(1).liv > 0) {
            	for(int i = 0; i < trs.size(); i++) {
            		trs.get(i).run();
            		remove--;
            		if(remove < 0)
            			remove = 0;
            	}

            }
        	
        	while(donesignal < 2) {	
            JSONObject jsn = new JSONObject();
            JSONObject jsn2 = new JSONObject();
            //jsn.put("fr", 0);
            jsn.put("gameOver", -1);
            //jsn2.put("fr", 0);
            jsn2.put("gameOver", 1);
            	if (users.get(0).liv < 1) {
                    if(users.get(0).id == 0) {
                    	send(sckt1, jsn); // s1 = id 0
                    	send(sckt2, jsn2);
                    }else {
                    	send(sckt2, jsn);
                    	send(sckt1, jsn2);
                    }
            	}
                else if (users.get(1).liv < 1) {
                    if(users.get(1).id == 0) {
                    	send(sckt1, jsn);
                    	send(sckt2, jsn2);
                    }else {
                    	send(sckt2, jsn);
                    	send(sckt1, jsn2);   
                    }
            	}
	
            		donesignal = rdr(sckt1) + rdr(sckt2);
            		System.out.println("----------DONE SIGNAL--------" + donesignal + " " +rdr(sckt1));
            	}
        	tr1.stop();
        	tr2.stop();
            sockets.get(0).close();
            sockets.get(1).close();
            serverSckt.close();
          
      }
    }catch (IOException ioException) {
    	System.out.println("SERVER: hela sR");
      ioException.printStackTrace();
  }
}

private static int rdr(Socket s) throws JSONException, IOException {
	BufferedReader read = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
	String l = read.readLine();            
	JSONObject jn = new JSONObject(l);
    return jn.getInt("done");
}
//------------------------------------------------------------------------------
private static void send(Socket s, JSONObject update) throws UnsupportedEncodingException, IOException{
	OutputStreamWriter writer;
	writer = new OutputStreamWriter(s.getOutputStream(), "UTF-8");
    try{
        writer.write(update.toString() + "\n");
        System.out.println("SERVER: jag skickar " + update.toString());
        writer.flush();
    }catch(IOException ioe){
        System.out.println("SERVER: nagot fel intraffade! SEND"+ ioe);
    }
}
//------------------------------------------------------------------------------
private static void waitForConnection() throws IOException, JSONException {
    System.out.println("SERVER: Waiting for someone to connect...");

    if ((sckt1 = serverSckt.accept()) != null) {
        sockets.add(sckt1);
        users.add(new User(0,32*2, 32*2));
        playerCount++;
        tr1 = new Threaded(sckt1);
        tr1.start();
        trs.add(tr1);
        System.out.println("SERVER: 1 " + sckt1.getInetAddress().getHostName() + " connected.");
    }
    if ((sckt2 = serverSckt.accept()) != null) {
        sockets.add(sckt2);
        users.add(new User(1, 32*17, 32*17));
        playerCount++;
        tr2 = new Threaded(sckt2);
        tr2.start();
        trs.add(tr2);
        System.out.println("SERVER: 2 " + sckt2.getInetAddress().getHostName() + " connected.");
    }
}

//------------------------------------------------------------------------------
/* a thread that listens to a client as well as sends json object with changes back
 * one thread for each player
 */
private static class Threaded extends Thread implements Runnable{
	Socket s;
	BufferedReader reader;
	OutputStreamWriter writer;
	String line;
	JSONObject jasn = new JSONObject();
	JSONObject newjsn;
	JSONObject jsn;
 public Threaded(Socket s) throws UnsupportedEncodingException, IOException {
	this.s = s;
	reader = new BufferedReader(new InputStreamReader(s.getInputStream(), "UTF-8"));
	writer = new OutputStreamWriter(s.getOutputStream(), "UTF-8");
	System.out.println("THREAD CREATED---");
 }

    public void run() {
        try {
            line = reader.readLine();            
            jsn = new JSONObject(line);
            System.out.println("SERVER: Recieved from Client: " + jsn.toString());
            	newjsn = evaluate(jsn, s);
            	send(newjsn);
            	jasn.put("gameOver", 0);
            
        } catch (IOException e) { //tar bort causing socket och thread och sänker playercount, tex om man DC:ar
        	if (s==sckt1) {
                for(int i = 0; i < sockets.size(); i++) {
                	if(sockets.get(i) == sckt1)
                	sockets.remove(i);
                	
                   	if (trs.get(i).s == sckt1)
                		trs.remove(i);
                	
                }
                
				playerCount--;
				for(int i = 0; i < users.size(); i++) {
					if(users.get(i).id == 0)
					users.remove(i);	
				}
				
			}
			else if (s==sckt2){
                for(int i = 0; i < sockets.size(); i++) {
                	if(sockets.get(i) == sckt2)
                	sockets.remove(i);
                	
                   	if (trs.get(i).s == sckt2)
                		trs.remove(i);
                	
                }
				playerCount--;
				for(int i = 0; i < users.size(); i++) {
					if(users.get(i).id == 1)
					users.remove(i);	
				}
				
			}
        } catch (JSONException e) {
        	System.out.println("SERVER: JSON send fail " + newjsn.toString());
        }
    }
    
    private void send(JSONObject update){
        try{
            writer.write(update.toString() + "\n");
            System.out.println("SERVER: jag skickar");
            writer.flush();
        }catch(IOException ioe){
            System.out.println("SERVER: nagot fel intraffade! SEND"+ ioe);
        }
    }

}
  //----------------------------------------------------------------------------
  private static void definingGuns() throws IOException, SlickException {
      gunTypes.add(new Weapon(0, 10, 16, false, 0));
      gunTypes.add(new Weapon(1, 10, 16, false, 0));
      gunTypes.add(new Weapon(2, 10, 16, false, 0));
  }
//------------------------------------------------------------------------------
  /*
   * Evauluates the json object received from client and puts it into effect
   * @param jsn, json object that was recieved
   * @oaram sckt, the socket of the thread to identify who moves etc
   * @return json object that needs to be sent to client
   */
  private static JSONObject evaluate(JSONObject jsn, Socket sckt){
      int i;
      if(sckt == sckt1)
          i = 0;
      else if(sckt == sckt2 && sockets.size() < 2)
    	  i = 0;
      else
          i = 1;

      try {
	      if(jsn.getInt("bullethit") == 1) {
			  for(int k = 0; k < users.size(); k++) {
				  if(users.get(k).id == jsn.getInt("whogotshot"))
				    users.get(k).liv = users.get(k).liv - gunTypes.get(jsn.getInt("weaponID")).dmg;
			  }
	      } else {

	      movement(jsn, i);	      
	      
	      if(jsn.getInt("keyF") == 1) {
	    	  pickup(jsn, i);
	      }
	      users.get(i).currentSelection = jsn.getInt("currentSelection");
	      users.get(i).shoot = jsn.getInt("shoot");
	      users.get(i).direction = jsn.getInt("direction");
	      }
	} catch (JSONException e) {
		System.out.println("SERVER: cant evaluate");
		e.printStackTrace();
	}

  return jsonToSend();
  }
  
  //check what if youre standing on weapon if so pick it up.
  private static void pickup(JSONObject jsn, int i) throws JSONException {
      int wpncount = jsn.getInt("wpncount");
      int[] wx = new int[wpncount];
      int[] wy = new int[wpncount];
      int[] wh = new int[wpncount];
      int[] wu = new int[wpncount];
      int[] wid = new int[wpncount];
      
      for(int w = 0; w < wpncount; w++) {
    	  wx[w] = jsn.getInt("wx".concat(Integer.toString(w)));
      	  wy[w] = jsn.getInt("wy".concat(Integer.toString(w)));
      	  wh[w] = jsn.getInt("wh".concat(Integer.toString(w)));
      	wu[w] = jsn.getInt("wu".concat(Integer.toString(w)));
      	wid[w] = jsn.getInt("wid".concat(Integer.toString(w)));
      }
    		  int sRoof = users.get(i).ypos;
    		  int sFloor = users.get(i).ypos+32;
    		  int sLeft = users.get(i).xpos;
    		  int sRight = users.get(i).xpos+32;
    		  for(int k = 0; k < wx.length; k++) {
        		  int wRoof = wy[k];
        		  int wFloor = wy[k]+wh[k];
        		  int wLeft = wx[k];
        		  int wRight = wx[k]+64;
    			  
	    		  if (wRoof > sRoof && wRoof < sFloor)
	    				 if(wFloor < sFloor && wFloor > sRoof) 
	    					 if(wRight > sRight && wRight > sLeft) 
	    						 if(wLeft < sLeft && wLeft < sRight) {
				    			  remove = 2;
				    			  removeid = wu[k];
				    		      users.get(i).backpack[users.get(i).currentSelection] = wid[k];
	    						 }
    		  }
  }
  
//------------------------------------------------------------------------------
  //JSON objekt som ska skickas till clienten
  private static JSONObject jsonToSend(){
      JSONObject jsn = new JSONObject();
      try {
    	  jsn.put("fr", 500);
    	  jsn.put("gameOver", 0);
    	  jsn.put("removegun", remove);
    	  jsn.put("removeid", removeid);
    	  jsn.put("wpnInit", 0);
    	  jsn.put("playerCount", playerCount);
          for(int i = 0; i < users.size(); i++) {
              jsn.put("xpos".concat(Integer.toString(i+1)), users.get(i).xpos);
              jsn.put("ypos".concat(Integer.toString(i+1)), users.get(i).ypos);
              jsn.put("direction".concat(Integer.toString(i+1)), users.get(i).direction);
              jsn.put("shoot".concat(Integer.toString(i+1)), users.get(i).shoot);
              jsn.put("currentSelection".concat(Integer.toString(i+1)), users.get(i).backpack[users.get(i).currentSelection]);
          }
      } catch (JSONException e) {
    	  System.out.println("SERVER: cant pack send json");
          e.printStackTrace();
      }
      return jsn;
  }
  
//------------------------------------------------------------------------------
  //create a list of walls, the coordinates of these walls are non-enterable
  private static void placingWalls() {
	  walls.add(new Walls(2*32, 3*32));
	  walls.add(new Walls(2*32, 4*32));
	  walls.add(new Walls(3*32, 3*32));
	  walls.add(new Walls(8*32, 2*32));
	  walls.add(new Walls(14*32, 4*32));
	  walls.add(new Walls(18*32, 2*32));
	  walls.add(new Walls(6*32, 7*32));
	  walls.add(new Walls(17*32, 8*32));
	  walls.add(new Walls(9*32, 8*32));
	  walls.add(new Walls(12*32, 8*32));
	  walls.add(new Walls(9*32, 11*32));
	  walls.add(new Walls(12*32, 11*32));
	  walls.add(new Walls(14*32, 14*32));
	  walls.add(new Walls(2*32, 18*32));
	  walls.add(new Walls(3*32, 15*32));
	  walls.add(new Walls(16*32, 18*32));
	  walls.add(new Walls(17*32, 18*32));
	  walls.add(new Walls(6*32, 14*32));
  }
//------------------------------------------------------------------------------
  private static void movement(JSONObject jsn, int q) throws JSONException {
	  int m;
	  if (q == 1)
		  m = 0;
	  else 
		  m = 1;
	  
		if(jsn.getInt("keyW") == 1)
		      users.get(q).ypos--;
	      if(jsn.getInt("keyS") == 1)
	          users.get(q).ypos++;
	      if(jsn.getInt("keyD") == 1)
	          users.get(q).xpos++;
	      if(jsn.getInt("keyA") == 1)
	          users.get(q).xpos--;
	  
	      //vägg collision
	  for(int i = 0; i < users.size(); i++) {
		  for(int k = 0; k < walls.size(); k++) {
		  if(users.get(i).xpos == walls.get(k).x2 && ((users.get(i).ypos <= walls.get(k).y2 && users.get(i).ypos+32 >= walls.get(k).y1)))
			  users.get(i).xpos++;
		  if(users.get(i).xpos+32 == walls.get(k).x1 && ((users.get(i).ypos <= walls.get(k).y2 && users.get(i).ypos+32 >= walls.get(k).y1)))
			  users.get(i).xpos--;
		  if(users.get(i).ypos+32 == walls.get(k).y1 && ((users.get(i).xpos <= walls.get(k).x2 && users.get(i).xpos+32 >= walls.get(k).x1)))
			  users.get(i).ypos--;
		  if(users.get(i).ypos == walls.get(k).y2 && ((users.get(i).xpos <= walls.get(k).x2 && users.get(i).xpos+32 >= walls.get(k).x1)))
			  users.get(i).ypos++;
		  }
		  if(users.get(i).xpos < 0)
			  users.get(i).xpos = 0;
		  if(users.get(i).xpos+32 > 672)
			  users.get(i).xpos = 672-32;
		  if(users.get(i).ypos < 0)
			  users.get(i).ypos = 0;
		  if(users.get(i).ypos+32 > 672)
			  users.get(i).ypos = 672-32;
	  }
	if(users.size() > 1) {
		  if(users.get(q).xpos == users.get(m).xpos+32 && ((users.get(q).ypos <= users.get(m).ypos+32 && users.get(q).ypos+32 >= users.get(m).ypos))) {
				  if(jsn.getInt("keyA") == 1)
					  users.get(q).xpos++;	  
		  	  }
		  if(users.get(q).xpos+32 == users.get(m).xpos && ((users.get(q).ypos <= users.get(m).ypos+32 && users.get(q).ypos+32 >= users.get(m).ypos))) {
				  if(jsn.getInt("keyD") == 1)
					  users.get(q).xpos--;	  
			  }
		  if(users.get(q).ypos+32 == users.get(m).ypos && ((users.get(q).xpos <= users.get(m).xpos+32 && users.get(q).xpos+32 >= users.get(m).xpos))) {
				  if(jsn.getInt("keyS") == 1)
					  users.get(q).ypos--;	  
			  }
		  if(users.get(q).ypos == users.get(m).ypos+32 && ((users.get(q).xpos <= users.get(m).xpos+32 && users.get(q).xpos+32 >= users.get(m).xpos))) {
				  if(jsn.getInt("keyW") == 1)
					  users.get(q).ypos++;	  
			  }
				  
	}
	  
  }


}
