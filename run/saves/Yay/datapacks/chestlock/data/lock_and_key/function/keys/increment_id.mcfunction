\# increment the key_id in storage with a scoreboard
execute store result score @s lock_and_key.key_id_increment run data get storage lock_and_key:key_id count
scoreboard players add @s lock_and_key.key_id_increment 1
execute store result storage lock_and_key:key_id count int 1 run scoreboard players get @s lock_and_key.key_id_increment