**Maps**\
Required regions:
* `waiting_spawn` - one waiting spawn
* `spawn` - one at each island
* `spawn_chest` - region for each chest on spawn islands
* `center_chest` - region for each chest on center island

**Config**
* `dimension` - dimension id (*optional*)
* `map`
    * `id` - map id
    
* `spawn_loot_table` - spawn islands loot table (*optional*)
* `center_loot_table` - center island loot table (*optional*)
* `refills` - amount of refills to do (default loot table only supports 2)
* `refill_mins` - time between each refill in minutes
* `time_limit_mins` - maximum game duration if no one wins

**Loot Tables**\
Vanilla loot table system\
Title your loot table the identifier you defined in config + fill number.\
EX: `insane1`, `insane2`, `insane3`