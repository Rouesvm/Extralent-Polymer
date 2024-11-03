#[[\$]]#execute summon minecraft:interaction run scoreboard players set @s lock_and_key.uuid_0 #[[\$]]#(uuid_0)
#[[\$]]#execute as @e[type=minecraft:interaction] if score @s lock_and_key.uuid_0 matches #[[\$]]#(uuid_0) run tag @s add lock_and_key.key_interact
#[[\$]]#execute as @e[type=minecraft:interaction] if score @s lock_and_key.uuid_0 matches #[[\$]]#(uuid_0) run data modify entity @s width set value 1.01
#[[\$]]#execute as @e[type=minecraft:interaction] if score @s lock_and_key.uuid_0 matches #[[\$]]#(uuid_0) run data modify entity @s height set value 1.01