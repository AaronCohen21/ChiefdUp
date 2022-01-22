# Chief'd Up!
 This project is a game similar to Jackbox’s Champ’d Up. The multiplayer game will run from a server on one player’s phone,
 utilizing the java.net library. Multiple games may be run on the same network, and players will connect to different games with a
 game pin, that being the port number of the server. The server code will be directly integrated into the application, and run from
 a host client on a different Thread automatically when creating a new game. The game works as such: each player receives a unique
 prompt to draw “the CEO of ________”. The players will have a set amount of time to draw their CEO, and give it a name. After the
 CEO’s have been drawn, each player will receive another player’s drawing, and will be required to draw a new CEO to rival against
 it. However, when drawing the rival CEO, the player will not have the prompt, they will only have the CEO and its name; this makes
 the player have to guess what the prompt is as best as they can. After all CEOs and rivals have been drawn, players will vote on
 which ones are the best, and get points if their drawings win. This game will be uniquely called “Chief’d Up!”

# Port Setup
This tool is intended to speed up the development process
for Chief'd Up! by eliminating the need to set up game ports
individually using Telnet. Instead, this tool automatically
sets up ports with Telnet whenever a new game is created.

Normally when testing the app on an Android VM
each time you create a game, you must also run the
following commands to enable other VM's to join:
1. `telnet localhost *device port*`
2. `auth *token*`
3. `redir add tcp:*game pin*:*game pin*`

With this tool you only need to run `python3 PortSetup.py *device port*`
once per "host device" and all telnet redirects will be handled automatically when each socket is created.
To stop the tool from running press `ctrl+c` in the terminal environment where the script is running,
or close the emulator that the script is connected to.

To set up the tool run `pip3 install -U pure-python-adb` to install the project dependencies. Next edit
`.emulator_console_auth_token` in your home directory leaving the file blank to remove the emulator auth token.
The script cannot run if there is an auth token because for reasons beyond my understanding
`tn.write("auth *token*\n".encode('ascii'))` does not work in the python script to log in with the token.