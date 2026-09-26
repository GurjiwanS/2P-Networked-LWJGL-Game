# CounteredStrike

A two-player networked multiplayer shooter, built in Java using Slick2D/LWJGL for graphics and raw sockets for networking. University project, built in 2021.

## How it works

- `Server.java` runs the authoritative game server — tracks player positions, health, and weapons, and resolves movement/collision.
- `Client.java` is the game client — renders the game, reads input, and sends/receives JSON state over a socket each frame.
- Communication is JSON over TCP sockets (via `org.json`), one message per frame.

## Running it

Requires Java 11 (LWJGL 2 doesn't run reliably on very new Java versions).

From the project root:

**Start the server:**

java -cp "bin:lib/jars/*" cs.Server



**Start two clients (in separate terminals), one per player:**

java -Djava.library.path=lib/natives/macosx -cp "bin:lib/jars/*" cs.Game

(swap `macosx` for `windows` or `linux` depending on OS)

The game starts once both clients have connected.

**Controls:** WASD to move, arrow keys to aim, Space to shoot, 1/2 to switch weapon, F to pick up a weapon.

## Known issues

This was an early project and there's a lot I'd do differently now:
- Server game state is held in static fields rather than being properly encapsulated.
- Client threads are invoked via `.run()` instead of `.start()`, so they don't actually run concurrently.
- No proper game loop separation between networking and rendering.
- Cleaner code and different structure.
