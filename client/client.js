const WebSocket = require("ws");

const argv = require("minimist")(process.argv.slice(2));

// constants
const N = 20;
const PORTS = [31415, 31416, 31417];
let silentMode = argv.s;

// a few utils
function uuidv4() {
	return "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx".replace(/[xy]/g, function (c) {
		var r = (Math.random() * 16) | 0,
			v = c == "x" ? r : (r & 0x3) | 0x8;
		return v.toString(16);
	});
}
const sleep = (n) => new Promise((res) => setTimeout(res, n));
const log = (...args) => {
	if (!silentMode) console.log(...args);
};

//*********************************************************
//                      BOT CLASS                         *
//*********************************************************
class Bot {
	constructor(port) {
		// state setup
		this.ID = uuidv4();
		this.ws = new WebSocket(`ws://localhost:${port || PORTS[0]}`);
		this.positions = {}; // maps player id to position {x, y}

		log("my id:", this.ID);
		// init
		this.ws.on("open", () => {
			this.ws.send("join " + this.ID);
			log("sent connect message");
		});

		this.ws.on("message", (data) => {
			log("received: %s", data);
			if (data.startsWith("pos ")) {
				// position update
				const [_, uid, xstr, ystr] = data.split(" ");
				const [x, y] = [parseInt(xstr), parseInt(ystr)];

				// some validation
				if (x >= N || y >= N || x < 0 || y < 0) {
					throw new Error("out of map boundary");
				}

				this.positions[uid] = { x: x, y: y };
			} else if (data.startsWith("dc ")) {
				const [_, uid] = data.split(" ");
				delete this.positions[uid];
			}
			this.printState();
		});
	}

	move(dir) {
		this.ws.send("move " + dir);
	}

	close() {
		this.ws.close();
	}

	printState() {
		if (silentMode) return; // silent mode

		for (let y = 0; y < N; y++) {
			for (let x = 0; x < N; x++) {
				let pids = []; // players at this loc, if any
				for (let [id, pos] of Object.entries(this.positions)) {
					if (pos.x === x && pos.y === y) {
						pids.push(id);
					}
				}
				if (pids.length === 0) {
					process.stdout.write(" .. ");
				} else {
					pids.forEach((id) => {
						process.stdout.write(id.slice(32));
					});
				}
			}
			process.stdout.write("\n");
		}
	}
}

//*********************************************************
//                      TESTS                             *
//*********************************************************
const testMoveBasic = async () => {
	let b = new Bot();

	await sleep(1500);

	const startx = b.positions[b.ID].x;
	const starty = b.positions[b.ID].y;

	if (startx !== 0) {
		b.move("left");
	} else {
		b.move("right");
	}

	await sleep(1500);

	let fail = false;
	if (startx !== 0) {
		fail = b.positions[b.ID].x !== startx - 1;
	} else {
		fail = b.positions[b.ID].x !== startx + 1;
	}
	if (fail) throw new Error("failed basic move");

	// test map boundary
	for (let i = 0; i < N; i++) {
		b.move("left");
		await sleep(100);
	}

	await sleep(1500);

	if (b.positions[b.ID].x !== 0) throw new Error("failed edge of map test");

	b.close();
};

// returns two bots on the top row, in order (left, right)
const collisionSetup = async () => {
	let left = new Bot();
	let right = new Bot();

	await sleep(1500);

	if (left.positions[left.ID].x > right.positions[right.ID].x) {
		// need to swap
		[left, right] = [right, left];
	}
	// also move left left and right right just to be safe in case same column
	left.move("left");
	right.move("right");

	await sleep(1000);
	// ok now left is definitely to the left and right is definitely to the right

	// move both to top
	for (let i = 0; i < N; i++) {
		left.move("up");
		right.move("up");
		await sleep(100);
	}
	await sleep(1500);

	return [left, right];
};

const testPlayerCollision = async () => {
	let [left, right] = await collisionSetup();

	// move right all the way left. should collide at some point
	for (let i = 0; i < N; i++) {
		right.move("left");
		await sleep(100);
	}
	await sleep(1500);

	// confirm right is now one space right of left
	if (right.positions[right.ID].x !== left.positions[left.ID].x + 1)
		throw new Error("collision check failed");
	// the position data being out of sync here is possible but tested elsewhere

	left.close();
	right.close();
};

const testGhostCollision = async () => {
	let [left, right] = await collisionSetup();

	// dc left, so it shouldn't be in the way any more
	left.close();
	await sleep(100);

	// move right all the way left. should NOT collide
	for (let i = 0; i < N; i++) {
		right.move("left");
		await sleep(100);
	}
	await sleep(1500);

	// confirm right is now all the way left
	if (right.positions[right.ID].x !== 0)
		throw new Error("ghost collision check failed");

	right.close();
};

// many bots, all three ports, many random moves, all state synced
const testAllPortsNPlayers = async (numPlayers, ports) => {
	const bots = [...Array(numPlayers).keys()].map(
		() => new Bot(ports[Math.floor(Math.random() * ports.length)])
	);

	// helper func to make sure all the bots have the same state
	const checkStateMatches = () => {
		const failError = new Error("bot state didn't match");

		const s1 = bots[0].positions;
		bots.forEach((b) => {
			const s2 = b.positions;

			if (Object.keys(s1).length !== Object.keys(s2).length) {
				console.error("two bots know different numbers of players");
				throw failError;
			}

			// now, since they have the same number of keys,
			//	we just have to check that all of s1's keys match
			Object.keys(s1).forEach((k) => {
				if (s1[k].x !== s2[k].x || s1[k].y !== s2[k].y) {
					console.error("different locations for", k);
					console.error(s1[k], s2[k]);
					throw failError;
				}
			});
		});
	};

	await sleep(5000);

	checkStateMatches(); // should all be the same after init

	for (let i = 0; i < 100; i++) {
		bots.forEach((b) => {
			const dir = ["up", "down", "left", "right"][
				Math.floor(Math.random() * 4)
			];
			b.move(dir);
		});
		await sleep(300);
	}

	await sleep(3000); // give things a bit to settle down

	// check that all state matches
	checkStateMatches();

	// do it again!
	for (let i = 0; i < 100; i++) {
		bots.forEach((b) => {
			const dir = ["up", "down", "left", "right"][
				Math.floor(Math.random() * 4)
			];
			b.move(dir);
		});
		await sleep(300);
	}

	await sleep(3000); // give things a bit to settle down

	// check that all state matches
	checkStateMatches();
};

//*********************************************************
//                      MAIN                              *
//*********************************************************
const main = async () => {
	if (argv.t) {
		// run tests
		console.log("testing basic movement");
		await testMoveBasic();
		console.log("passed testMoveBasic");
		console.log("testing player collision");
		await testPlayerCollision();
		console.log("passed testPlayerCollision");
		console.log("testing for collision with dc'ed players");
		await testGhostCollision();
		console.log("passed testGhostCollision");

		// bigger tests, too many prints
		silentMode = true;
		let numPlayers = parseInt(argv.t);
		numPlayers = isNaN(numPlayers) ? 25 : numPlayers;
		console.log(
			`----- beginning random movement test with ${numPlayers} bots on just port ${PORTS[0]} -----`
		);
		//await testAllPortsNPlayers(numPlayers, [PORTS[0]]);
		console.log("passed single port loadtest");

		console.log();
		console.log();
		console.log("----- PHASE 1 TESTS COMPLETE -----");
		console.log(
			">> please send your current code to your interviewer before attempting the distributed version"
		);
		console.log();
		console.log();

		await sleep(5000);

		console.log(
			`----- beginning random movement test with ${numPlayers} bots on ports ${PORTS} -----`
		);
		await testAllPortsNPlayers(numPlayers, PORTS);
		console.log("passed multi-port loadtest");

		console.log("all tests passed :)");
	} else {
		// init
		let b = new Bot();
		await sleep(2500);

		if (argv.r) {
			// move randomly
			const numMoves = parseInt(argv.r);
			if (isNaN(numMoves))
				throw new Error("-r flag required to be followed by num moves");

			for (let i = 0; i < numMoves; i++) {
				const dir = ["up", "down", "left", "right"][
					Math.floor(Math.random() * 4)
				];

				b.move(dir);

				await sleep(500);
			}
		} else {
			// accept user input
			const readline = require("readline");
			const rl = readline.createInterface({
				input: process.stdin,
				output: process.stdout,
			});

			while (true) {
				const input = await new Promise((res) =>
					rl.question("next move? ", res)
				);

				if (input === "q") {
					b.close();
					return;
				}

				b.move(input);
				await sleep(500);
			}
		}
	}
};
main();
