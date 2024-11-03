\# setup UUID storage
scoreboard objectives add lock_and_key.uuid_0 dummy
scoreboard objectives add lock_and_key.key_id dummy
scoreboard objectives add lock_and_key.key_id_compare dummy

\# setup key index storage
execute unless data storage lock_and_key:key_id count run data modify storage lock_and_key:key_id count set value 1
\# used to increment the key storage when a new lock is made
scoreboard objectives add lock_and_key.key_id_increment dummy

\# setup player UUIDs
execute as @a store result score @s lock_and_key.uuid_0 run data get entity @s UUID[0]

\# setup interaction entity to detect right click while sneaking with a key
execute as @a[tag=!lock_and_key.key_interact_setup] if predicate lock_and_key:is_sneaking run function lock_and_key:lock/key_interact_add

execute at @a[tag=lock_and_key.key_interact_setup] as @e[type=minecraft:interaction, tag=lock_and_key.key_interact] if score @s lock_and_key.uuid_0 = @p lock_and_key.uuid_0 positioned ~ ~1.27 ~ run function lock_and_key:lock/key_interact_update

execute as @a[tag=lock_and_key.key_interact_setup] unless predicate lock_and_key:is_sneaking at @s as @e[type=minecraft:interaction, tag=lock_and_key.key_interact] if score @s lock_and_key.uuid_0 = @p lock_and_key.uuid_0 run kill @s
execute as @a[tag=lock_and_key.key_interact_setup] unless predicate lock_and_key:is_sneaking run tag @s remove lock_and_key.key_interact_setup

execute as @e[type=minecraft:interaction,tag=lock_and_key.lock] run function lock_and_key:lock/lock_update