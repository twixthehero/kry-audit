# Krythera Audit

[Minecraft Forge](https://mcforge.readthedocs.io/en/1.15.x/) mod for recording and auditing events.

## Supported Versions

* 1.15.2

## Server Only

This mod only needs to be installed on the server.

## Command Information

Run `/kry` in-game.

## Implementation Details

This mod subscribes to audited events. When events fire, the subscriber creates an event summary, optionally using
[flatbuffers](https://github.com/google/flatbuffers) to store metadata, and queues it for an `EventLogger`.
`EventLogger` classes run in separate threads where they poll events from the queue and batch insert them into the database.

### Database

Multiple H2 file databases store audit event data, one per dimension.

Dimension|Location
---|---
Overworld|`./<worldName>/data/kryaudit.mv.db*`
Nether|`./<worldName>/data/DIM-1/kryaudit.mv.db*`
End|`./<worldName>/data/DIM1/kryaudit.mv.db*`
Other|`./<worldName>/data/<dimensionFolder>/kryaudit.mv.db*`

### Supported Events

* Block break
* Block place
* Item toss
* Item expire (despawn)

## TODO

* Events
  * Entity pickup (P1)
* Config file
  * Dimensions to store audit data for (P1)
  * Batch insert size (P1)
  * Events that should be audited (P1)
