\# reset advancement
advancement revoke @s only lock_and_key:lock

\# new key
execute unless data entity @s SelectedItem.components.minecraft:custom_data.key_id run function lock_and_key:keys/increment_id
execute unless data entity @s SelectedItem.components.minecraft:custom_data.key_id run data modify storage lock_and_key:key_id new_key_id set from storage lock_and_key:key_id count
execute unless data entity @s SelectedItem.components.minecraft:custom_data.key_id run data modify storage lock_and_key:key_id new_key_item_id set from entity @s SelectedItem.id
execute unless data entity @s SelectedItem.components.minecraft:custom_data.key_id run function lock_and_key:keys/assign_id with storage lock_and_key:key_id

\# existing key
execute if data entity @s SelectedItem.components.minecraft:custom_data.key_id run data modify storage lock_and_key:key_id new_key_id set from entity @s SelectedItem.components.minecraft:custom_data.key_id

\# summon the lock entity
execute at @s as @e[type=minecraft:interaction, tag=lock_and_key.key_interact] if score @s lock_and_key.uuid_0 = @p lock_and_key.uuid_0 at @s unless entity @n[type=minecraft:interaction,tag=lock_and_key.lock,distance=..0.5] run function lock_and_key:lock/summon_lock with storage lock_and_key:key_id