# distributed game service take-home coding challenge

There are two parts to this challenge:

1. Implement a functional single-threaded game server
2. Distribute your game server across three replicas

(The details are explained in **spec**)

The first four tests check your implementation of part 1.
Once you pass all four,
please email your current code to me (so we have a functional snapshot),
and only then move on to part 2.

Part 2 is deliberately harder.
Most people do not finish, and it is not expected that you do--we
want to see how far you can get given limited time.
4.5 hours from the start of the interview, please send me your code,
however far you got and whatever state it's in,
so we have a timestamped snapshot.
If you do pass all five tests before time is up,
please email me your code and feel free to stop early.

## spec

Build a game server in whatever language you like.
It should implement the game described below.

### phase 1

Clients will connect via websockt to `localhost:31415`, issue commands, and expect to receive state updates,
according to the interface below.

### phase 2

Build a replicated game server, in whatever language you like.
You'll run 3 replicas, on `localhost:31415-31417`, as three separate processes.

Clients will connect to any server at random via websocket and issue moves.

It shouldn't matter which server a client connects to — eventually (after 3 seconds), the view each client has of the state of the space should be identical.
Intermediate state may have temporary inconsistencies.
Fault tolerance (one of the three servers becoming unavailable) and scalability are not a concern for this task--it just needs to adhere to this spec and pass the basic test suite included in the client.

### game details

There is one public map, a 20x20 grid that players may join and roam with other players.
The only rules are that two players may not overlap and that players may not leave the boundary of the map.
When players first join, they are assigned a random (or arbitrary) position.

Your game server should track and disseminate player state and enforce these simple rules.
We have provided an example client and documentation on the messages it sends and expects to receive.
This is also the client used to run the tests.

## evaluation

On the second day, we'll meet again and go over your code;
first the single-threaded version and then the replicated version.
You'll show me what you've come up with,
and then we'll talk about your design, why you chose it, and how you might extend it given many more hours.

We will look at a couple aspects of your implementation in particular:

- how far you got in passing the tests
- how you balanced speed and perfection given limited time
- how you chose to structure your code, and code quality (again, under a time constraint)

There are deliberately many valid solutions--the ultimate goal is to see how you approach these problems :)

## install

1. install NodeJS and npm
2. `npm install` in root dir

## running

### client

`node client.js`

Default behavior is to connect to port `31415` and accept moves ("up", "down", "left", or "right") via stdin, then print the resulting map state.

options:

- `-s` for silent mode -- no logging
- `-t` to run tests
- `-r [N]` instead of inputting move commands, make N random moves

(All but the large loadtest will only connect to port 31415, for ease of development)

This example client is provided for your convenience -- feel free to modify it however you please.

### server

Up to you! My example used `node server.js -p [PORT]`, but whatever works for your server.

## interface

There are four different message types:

client --> server:

- `join [player ID]` on initial connection
- `move [up|down|left|right]` to move around the grid

server --> client:

- `pos [player ID] [x location] [y location]` to tell a client the current position of some player
- `dc [player ID]` to tell a client that a player disconnected (and should be removed from current state)

Different servers can of course communicate between themselves however you want.

### questions

Questions are welcome and expected, don't hesitate to ask!

Some common ones:

**Can I use an external library/service?**
Yes.

**Can I tweak the interface slightly?**
For the sake of consistency, no, but write that idea down because I would love to hear how you'd design it differently.

**Having clients self-identify is insecure... should I worry about auth?**
No, this is simplified so you can spend more time thinking about the main problem.

**Who do I email again?**
If you don't have your interviewer's email, feel free to use nate@gather.town.
