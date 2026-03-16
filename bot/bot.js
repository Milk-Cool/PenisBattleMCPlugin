import mineflayer from "mineflayer";
import pathfinderMod from "mineflayer-pathfinder";
import { Vec3 } from "vec3";
import { parseArgs } from "util";
const { pathfinder, Movements, goals } = pathfinderMod;

const { values } = parseArgs({
    options: {
        ip: { type: "string", default: "localhost" },
        username: { type: "string", default: "Bot01" }
    }
});

const bot = mineflayer.createBot({
    host: values.ip,
    username: values.username
});
bot.loadPlugin(pathfinder);
bot.on("kicked", msg => console.error(JSON.stringify(msg, null, 2)));
bot.on("error", msg => console.error(msg));
bot.on("message", msg => console.error(JSON.stringify(msg, null, 2)));

const sleep = ms => new Promise(resolve => setTimeout(resolve, ms));

/** @argument {Vec3} pos */
const place = async (pos, lookAtPos, blockNameFunc, jump = true) => {
    const movements = new Movements(bot, bot.entity.position);
    movements.canDig = false;
    movements.exclusionAreasStep = [block => pos.equals(block.position)];
    const goal = new goals.GoalLookAtBlock(lookAtPos, bot.world, {});
    bot.pathfinder.stop();
    bot.pathfinder.setMovements(movements);
    bot.pathfinder.setGoal(goal);
    try {
        await bot.pathfinder.goto(goal);
    } catch(_) {}
    const slot = bot.inventory.slots.find((slot, i) => slot && blockNameFunc(slot.name) && slot.slot >= 36);
    if(!slot) return;
    await bot.equip(slot, "hand");
    const ch = async r => {
        bot.updateHeldItem();
        if(!bot.heldItem || !bot.heldItem.name.endsWith("_wool")) setTimeout(ch, 1, r);
        r();
    }
    await new Promise(ch);
    await bot.lookAt(lookAtPos, true);
    if(jump) {
        bot.setControlState("jump", true);
        await sleep(400);
    }
    await bot.placeBlock(bot.blockAt(lookAtPos), pos.clone().subtract(lookAtPos));
    if(jump) bot.setControlState("jump", false);
    await sleep(100);
}

bot.once("login", async () => {
    await sleep(500);

    bot.chat("/register aA3b4829!! aA3b4829!!");
    bot.chat("/login aA3b4829!!");

    await sleep(500);

    bot.on("path_stop", () => null);
    let lock = false;
    let attacking = false;
    let t = 0;
    bot.on("forcedMove", () => {
        lock = false;
        t = 10;
        bot.pathfinder.stop();
    });
    let lastStartedPlaying = 0;
    let m = 0;
    process.on("uncaughtException", e => {
        console.error(e);
        lock = false
    });
    process.on("unhandledRejection", e => {
        console.error(e);
        lock = false
    });
    let block, hasWool, head = null;
    setInterval(async () => {
        if(lock) return;
        if(t > 0) {
            t--;
            return;
        }
        const y0 = new Vec3(bot.entity.position.x, 0, bot.entity.position.z);
        const blockAtY0 = bot.blockAt(y0);
        if(blockAtY0 === null) return;
        if(blockAtY0.name.endsWith("_terracotta") || blockAtY0.name === "smooth_sandstone" || blockAtY0.name === "grass_block" || blockAtY0.name === "spruce_planks" || blockAtY0.name === "stone_bricks") {
            // In-game
            const inv = bot.inventory.slots;
            head = inv.find(x => x !== null && x.slot === 5);
            const entity = bot.nearestEntity((e) => e.type === "player" && e.username !== bot.username && e.position.distanceTo(bot.entity.position) < 10
                && e.equipment.find(x => x && x.name.endsWith("_wool")).name !== head.name);
            hasWool = head !== null && inv.some(x => x !== null && x.name.endsWith("_wool")) && inv.some(x => x !== null && x.name == "pink_wool");
            if(entity) {
                const slot = bot.inventory.slots.find((slot, i) => slot && slot.name === "wooden_sword" && slot.slot >= 36);
                if(!slot) return;
                await bot.equip(slot, "hand");
                const ch = async r => {
                    bot.updateHeldItem();
                    if(!bot.heldItem || !bot.heldItem.name.endsWith("_wool")) setTimeout(ch, 1, r);
                    r();
                }
                await new Promise(ch);

                if(entity.position.distanceTo(bot.entity.position) < 3) {
                    await bot.lookAt(entity.position, true);
                    await bot.attack(entity);
                }
                const movements = new Movements(bot, bot.entity.position);
                const goal = new goals.GoalFollow(entity, 1);
                bot.pathfinder.stop();
                bot.pathfinder.setMovements(movements);
                bot.pathfinder.setGoal(goal);
            } else if(block) { 
                lock = true;
                const movements = new Movements(bot, bot.entity.position);
                const goal = new goals.GoalLookAtBlock(block.position, bot.world, {});
                bot.pathfinder.stop();
                bot.pathfinder.setMovements(movements);
                bot.pathfinder.setGoal(goal);

                await bot.pathfinder.goto(goal);

                const slot = bot.inventory.slots.find((slot, i) => slot && slot.name === "shears" && slot.slot >= 36);
                if(!slot) return;
                await bot.equip(slot, "hand");
                const ch = async r => {
                    bot.updateHeldItem();
                    if(!bot.heldItem || !bot.heldItem.name.endsWith("_wool")) setTimeout(ch, 1, r);
                    r();
                }
                await new Promise(ch);

                await bot.lookAt(block.position, true);
                await bot.dig(block);
                await sleep(500);
                lock = false;
            } else if(hasWool) {
                lock = true;
                const base = new Vec3(bot.entity.position.x > 0 ? 15 : -15, 1, m * 4 - 20);
                await place(base, base.clone().add(new Vec3(0, -1, 0)), name => head.name === name, false);
                await place(base.clone().add(new Vec3(0, 1, 0)), base, name => head.name === name, false);
                await place(base.clone().add(new Vec3(1, 0, 0)), base, name => head.name === name, false);
                await place(base.clone().add(new Vec3(-1, 0, 0)), base, name => head.name === name, false);
                await place(base.clone().add(new Vec3(0, 2, 0)), base.clone().add(new Vec3(0, 1, 0)), name => name === "pink_wool");
                m++;
                lock = false;
            } else {
                bot.swingArm("right");
            }
        } else {
            // Not in-game
            if(Date.now() - lastStartedPlaying < 5000) return;
            bot.chat("/play");
            m = 0;
            lastStartedPlaying = Date.now();
        }
    }, 500);
    setInterval(() => {
        block = bot.findBlock({
            matching: block => block !== null && block.name.endsWith("_wool") && (!head || head.name !== block.name) && (!head || head.name !== bot.blockAt(block.position.clone().subtract(new Vec3(0, 1, 0))).name),
            useExtraInfo: true,
            maxDistance: hasWool ? 24 : 64
        });
        console.log(block);
    }, 2500);
});
bot.on("death", () => {
    bot.respawn();
});
