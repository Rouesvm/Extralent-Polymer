execute store result storage lock_and_key:uuid uuid_0 int 1 run data get entity @s UUID[0]

execute anchored eyes at @s positioned ^ ^ ^0.1 run function lock_and_key:lock/key_interact_summon with storage lock_and_key:uuid
tag @s add lock_and_key.key_interact_setup