\# remove when unlocked
\# check if player interacted while holding the correct key
execute store result score @s lock_and_key.key_id_compare on target run data get entity @s SelectedItem.components.minecraft:custom_data.key_id

\# particle effects when nearby player has matching key
execute store result storage lock_and_key:key_id compare int 1 run scoreboard players get @s lock_and_key.key_id
execute at @s positioned ~ ~.005 ~ run function lock_and_key:lock/lock_particles with storage lock_and_key:key_id

execute if score @s lock_and_key.key_id = @s lock_and_key.key_id_compare run execute at @s run playsound minecraft:block.vault.insert_item block @a
execute if data entity @s interaction unless score @s lock_and_key.key_id = @s lock_and_key.key_id_compare run execute at @s run playsound minecraft:block.vault.insert_item_fail block @a
execute if data entity @s interaction unless score @s lock_and_key.key_id = @s lock_and_key.key_id_compare run execute at @s run particle minecraft:smoke ~ ~ ~ 0 0.75 0 0 10

\# open other chest half
execute at @s if score @s lock_and_key.key_id = @s lock_and_key.key_id_compare if block ~ ~.005 ~ minecraft:chest[facing=south,type=left] positioned ~-1 ~.005 ~ run kill @n[type=minecraft:interaction,tag=lock_and_key.lock]
execute at @s if score @s lock_and_key.key_id = @s lock_and_key.key_id_compare if block ~ ~.005 ~ minecraft:chest[facing=south,type=right] positioned ~1 ~.005 ~ run kill @n[type=minecraft:interaction,tag=lock_and_key.lock]

execute at @s if score @s lock_and_key.key_id = @s lock_and_key.key_id_compare if block ~ ~.005 ~ minecraft:chest[facing=north,type=left] positioned ~1 ~.005 ~ run kill @n[type=minecraft:interaction,tag=lock_and_key.lock]
execute at @s if score @s lock_and_key.key_id = @s lock_and_key.key_id_compare if block ~ ~.005 ~ minecraft:chest[facing=north,type=right] positioned ~-1 ~.005 ~ run kill @n[type=minecraft:interaction,tag=lock_and_key.lock]

execute at @s if score @s lock_and_key.key_id = @s lock_and_key.key_id_compare if block ~ ~.005 ~ minecraft:chest[facing=east,type=left] positioned ~ ~.005 ~1 run kill @n[type=minecraft:interaction,tag=lock_and_key.lock]
execute at @s if score @s lock_and_key.key_id = @s lock_and_key.key_id_compare if block ~ ~.005 ~ minecraft:chest[facing=east,type=right] positioned ~ ~.005 ~-1 run kill @n[type=minecraft:interaction,tag=lock_and_key.lock]

execute at @s if score @s lock_and_key.key_id = @s lock_and_key.key_id_compare if block ~ ~.005 ~ minecraft:chest[facing=west,type=left] positioned ~ ~.005 ~-1 run kill @n[type=minecraft:interaction,tag=lock_and_key.lock]
execute at @s if score @s lock_and_key.key_id = @s lock_and_key.key_id_compare if block ~ ~.005 ~ minecraft:chest[facing=west,type=right] positioned ~ ~.005 ~1 run kill @n[type=minecraft:interaction,tag=lock_and_key.lock]

\# open self
execute if score @s lock_and_key.key_id = @s lock_and_key.key_id_compare run kill @s

\# reset interaction state
execute if data entity @s interaction run data remove entity @s interaction

\# remove when block is broken
execute at @s if block ~ ~.005 ~ minecraft:air run kill @s

\# stop hoppers from pulling items out of locked blocks
execute at @s positioned ~ ~-.995 ~ if block ~ ~ ~ minecraft:hopper run data modify block ~ ~ ~ TransferCooldown set value 8