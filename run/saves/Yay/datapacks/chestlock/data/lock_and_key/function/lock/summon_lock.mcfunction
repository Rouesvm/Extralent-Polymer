\# door halves
execute if block ~ ~.005 ~ \#minecraft:doors[half=lower] run summon minecraft:interaction ~ ~ ~ {width:1.01, height:2.01, Tags:["lock_and_key.new_lock"]}
execute if block ~ ~.005 ~ \#minecraft:doors[half=upper] run summon minecraft:interaction ~ ~-1 ~ {width:1.01, height:2.01, Tags:["lock_and_key.new_lock"]}

\# chest single, or left/right
execute if block ~ ~.005 ~ minecraft:chest[type=single] if block ~ ~.005 ~ \#lock_and_key:lockable run summon minecraft:interaction ~ ~ ~ {width:1.01, height:1.01, Tags:["lock_and_key.new_lock"]}

execute if block ~ ~.005 ~ minecraft:chest[facing=north,type=left] run summon minecraft:interaction ~ ~ ~ {width:1.01, height:1.01, Tags:["lock_and_key.new_lock"]}
execute if block ~ ~.005 ~ minecraft:chest[facing=north,type=left] run summon minecraft:interaction ~1 ~ ~ {width:1.01, height:1.01, Tags:["lock_and_key.new_lock"]}
execute if block ~ ~.005 ~ minecraft:chest[facing=north,type=right] run summon minecraft:interaction ~ ~ ~ {width:1.01, height:1.01, Tags:["lock_and_key.new_lock"]}
execute if block ~ ~.005 ~ minecraft:chest[facing=north,type=right] run summon minecraft:interaction ~-1 ~ ~ {width:1.01, height:1.01, Tags:["lock_and_key.new_lock"]}

execute if block ~ ~.005 ~ minecraft:chest[facing=south,type=left] run summon minecraft:interaction ~ ~ ~ {width:1.01, height:1.01, Tags:["lock_and_key.new_lock"]}
execute if block ~ ~.005 ~ minecraft:chest[facing=south,type=left] run summon minecraft:interaction ~-1 ~ ~ {width:1.01, height:1.01, Tags:["lock_and_key.new_lock"]}
execute if block ~ ~.005 ~ minecraft:chest[facing=south,type=right] run summon minecraft:interaction ~ ~ ~ {width:1.01, height:1.01, Tags:["lock_and_key.new_lock"]}
execute if block ~ ~.005 ~ minecraft:chest[facing=south,type=right] run summon minecraft:interaction ~1 ~ ~ {width:1.01, height:1.01, Tags:["lock_and_key.new_lock"]}

execute if block ~ ~.005 ~ minecraft:chest[facing=east,type=left] run summon minecraft:interaction ~ ~ ~ {width:1.01, height:1.01, Tags:["lock_and_key.new_lock"]}
execute if block ~ ~.005 ~ minecraft:chest[facing=east,type=left] run summon minecraft:interaction ~ ~ ~1 {width:1.01, height:1.01, Tags:["lock_and_key.new_lock"]}
execute if block ~ ~.005 ~ minecraft:chest[facing=east,type=right] run summon minecraft:interaction ~ ~ ~ {width:1.01, height:1.01, Tags:["lock_and_key.new_lock"]}
execute if block ~ ~.005 ~ minecraft:chest[facing=east,type=right] run summon minecraft:interaction ~ ~ ~-1 {width:1.01, height:1.01, Tags:["lock_and_key.new_lock"]}

execute if block ~ ~.005 ~ minecraft:chest[facing=west,type=left] run summon minecraft:interaction ~ ~ ~ {width:1.01, height:1.01, Tags:["lock_and_key.new_lock"]}
execute if block ~ ~.005 ~ minecraft:chest[facing=west,type=left] run summon minecraft:interaction ~ ~ ~-1 {width:1.01, height:1.01, Tags:["lock_and_key.new_lock"]}
execute if block ~ ~.005 ~ minecraft:chest[facing=west,type=right] run summon minecraft:interaction ~ ~ ~ {width:1.01, height:1.01, Tags:["lock_and_key.new_lock"]}
execute if block ~ ~.005 ~ minecraft:chest[facing=west,type=right] run summon minecraft:interaction ~ ~ ~1 {width:1.01, height:1.01, Tags:["lock_and_key.new_lock"]}

\# execute if block ~ ~.005 ~ minecraft:chest[type=right] run summon minecraft:interaction ~ ~ ~ {width:1.01, height:1.01, Tags:["lock_and_key.new_lock"]}

execute unless block ~ ~.005 ~ \#minecraft:doors unless block ~ ~.005 ~ minecraft:chest if block ~ ~.005 ~ \#lock_and_key:lockable run summon minecraft:interaction ~ ~ ~ {width:1.01, height:1.01, Tags:["lock_and_key.new_lock"]}

execute as @e[type=minecraft:interaction, tag=lock_and_key.new_lock] at @s run playsound minecraft:block.vault.insert_item block @a ~ ~ ~

execute as @e[type=minecraft:interaction, tag=lock_and_key.new_lock] store result score @s lock_and_key.key_id run data get storage lock_and_key:key_id new_key_id
tag @e[type=minecraft:interaction, tag=lock_and_key.new_lock] add lock_and_key.lock
tag @e[type=minecraft:interaction, tag=lock_and_key.new_lock] remove lock_and_key.new_lock