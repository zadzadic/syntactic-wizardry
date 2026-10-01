# Syntactic Wizardry — Canonical Design and Implementation Document

Version basis: v0.1.134  
Minecraft: 1.21.1  
NeoForge: 21.1.252  
Java: 21  
ModDevGradle: 2.0.147  
Parchment mappings: 2024.11.17  
Mod id: `syntacticwizardry`  
Package: `com.proxpero.syntacticwizardry`

## 1. Purpose and authority

This document describes the current Syntactic Wizardry implementation.

The v0.1.134 source is authoritative when this document and older notes disagree.

The Arcane Builder is current functionality.

Its proven implementation is carried as prebuilt classes from Arcane 0.4.267 and connected to Syntactic Wizardry through compatibility bridges.

The core spell architecture remains modular.

Shapes define where a spell resolves.

Effects define what happens to that resolution.

Modifiers alter one nearby Shape or Effect.

Rendering does not define mechanical geometry.

Shared mechanics belong in reusable services and runtimes.

## 2. Build and source-control baseline

The current mod version is `0.1.134`.

The project targets Minecraft 1.21.1.

The project targets NeoForge 21.1.252.

The project uses Java 21.

The Gradle plugin is `net.neoforged.moddev` 2.0.147.

The Parchment mapping version is 2024.11.17.

The project group is `com.proxpero.syntacticwizardry`.

The mod license is All Rights Reserved.

The GitHub repository is `zadzadic/syntactic-wizardry`.

The authoritative development branch is `main`.

The source tree includes `prebuilt-builder` on the compile classpath, runtime classpath, and output.

The Builder classes in that directory are the proven Arcane Builder implementation.

Only the subscriber mod-id constant was adapted so those classes attach to `syntacticwizardry`.

The current source provides registry, Wand, Mana, event, and networking bridges for the Builder.

## 3. Registered game content

### Blocks

Current blocks include:

- Scribe's Lectern.
- Rune.
- Temporary Block.
- Mature Crystal.
- Pylon Lower.
- Pylon Middle.
- Pylon Upper.
- Power Core Center.
- Power Core Crystal.
- Eight directional Power Core ring blocks.
- Magic Light.
- Arcane Builder.

### Items

Current gameplay and editor items include:

- Scribe's Lectern.
- Temporary Block.
- Mature Crystal.
- Written Spell.
- High Mana Compass.
- Wand.
- Wood Staff.
- Iron Staff.
- Diamond Staff.
- Netherite Staff.
- Magic Light.
- Arcane Builder.
- Spell component icon items used by the Lectern UI.

### Entity types

Current spell-render and spell-runtime entities include:

- Spell Missile.
- Sphere Visual.
- Box Visual.
- Cone Visual.
- Point Visual.

The area visual entities are render carriers.

They are not authoritative spell geometry.

### Menus

Current menus include:

- Scribe's Lectern.
- Dimensional Storage.
- High Mana Compass.
- Focus Spell menu.

### Creative tab

The Creative Mode tab is named Syntactic Wizardry.

It exposes the principal blocks, spell icon items, High Mana Compass, Wand, and four Staff tiers.

Magic Light and Arcane Builder add themselves to the same tab through their own registration paths.

## 4. Written Spell data model

A spell plan is a 5 by 5 grid.

There are 25 cells.

Each plan cell stores three integers:

1. Component type.
2. Style.
3. Visual.

The plan array therefore contains `25 * 3` integers.

Settings are stored separately.

Each cell currently reserves 22 setting integers.

The settings array therefore contains `25 * 22` integers.

The setting keys are:

- Radius.
- Damage Kind.
- Potence.
- Width.
- Height.
- Depth.
- Target Type.
- Distance.
- Move Mode.
- Impact Direction.
- Siphon Resource.
- Siphon Mode.
- Duration Seconds.
- Gravity Mode.
- Sphere Mode.
- Protection Kind.
- Alter Strength.
- Alter Speed.
- Alter Toughness.
- Teleport Mode.
- Range Value.
- Split Pattern.

Style and Visual are also property keys.

They remain stored in the three-integer plan stride.

Written Spell data is stored in item custom NBT.

The principal fields are `sw_plan` and `sw_settings`.

Mana-cost data is also stored on the Written Spell.

The current dense settings format remains functional.

Sparse property-id/value persistence remains a desirable future refactor.

That refactor is not part of current behavior.

## 5. Component type identifiers

The current component ids are:

| Id | Component | Category |
|---:|---|---|
| 0 | Empty | — |
| 1 | Missile | Shape |
| 2 | Sphere | Shape |
| 3 | Box | Shape |
| 4 | Damage | Effect |
| 5 | Target | Shape |
| 6 | Cone | Shape |
| 8 | Touch | Shape |
| 9 | Dig | Effect |
| 10 | Channel | Modifier |
| 11 | Stream | Modifier |
| 12 | Restore | Effect |
| 13 | Move | Effect |
| 14 | Self | Shape |
| 15 | Siphon | Effect |
| 16 | Gravity | Effect |
| 17 | Duration | Modifier |
| 18 | Block Interaction | Modifier |
| 19 | Rune | Shape |
| 20 | Relative | Shape |
| 21 | Chain | Shape |
| 22 | Protection | Effect |
| 23 | Temporary Block | Effect |
| 24 | Alter | Effect |
| 25 | Flight | Effect |
| 26 | Dimensional Storage | Effect |
| 27 | Teleportation | Effect |
| 28 | Mark | Effect |
| 29 | Range | Modifier |
| 30 | Split | Modifier |
| 31 | Ricochet | Modifier |
| 32 | Piercing | Modifier |
| 33 | Homing | Modifier |

## 6. Row grammar

The 5 by 5 grid is executed by rows.

Rows are sequential spell stages.

Columns hold components belonging to the same stage.

Execution begins at the first occupied row.

Every non-Modifier component in a row receives the same incoming `ShapeResolution`.

Effects in one row therefore consume the incoming resolution.

A Shape in that same row does not automatically become the parent of an Effect later in the row.

Shape-to-Effect and Shape-to-Shape continuation is expressed by placing the next stage on the next row.

A row may contain several Effects.

Those Effects all operate on the same incoming resolution.

A row may contain several Shapes.

Each Shape may create its own continuation into the next row.

That produces branching.

Asynchronous Shapes such as Missile and Rune can postpone continuation until their runtime event resolves.

Immediate Shapes resolve their continuation immediately.

If a row does not spawn a Shape branch, the incoming resolution is passed to the next occupied row unchanged.

## 7. Modifier binding

Modifiers are row-local.

A Modifier binds to the nearest non-Modifier component to its left in the same row.

Empty cells are skipped during this search.

Other Modifiers are skipped during this search.

The search stops when it reaches another Shape or Effect.

Modifiers do not create a broad scope around later components.

Modifier ownership does not carry into the next row.

Modifier ownership does not automatically carry through projectile impact continuation.

Duration and Block Interaction are recalculated for each owning component.

Channel applies to Effects.

Stream applies to Shapes.

Projectile Modifiers apply to compatible projectile Shapes.

## 8. ShapeResolution

`ShapeResolution` is the mechanical boundary between Shapes and Effects.

It stores:

- `origin`.
- `direction`.
- `up`.
- `surfaceNormal`.
- `voxels`.
- `directEntity`.
- `vectorPolicy`.

The current vector policies are:

- `FORWARD`.
- `RADIAL`.
- `SURFACE_NORMAL`.

Area mechanics consume discrete voxel occupancy.

Rendering may look smoother than that voxel set.

Rendering does not replace the voxel set.

Effects should consume the resolution they receive.

Effects should not identify the preceding Shape and implement separate Shape-specific branches.

## 9. Orientation and inheritance

A parent resolution may provide a surface frame.

When a surface frame exists, following Shapes can inherit that orientation.

When no surface frame exists, root Shapes generally use the original cast direction.

Non-root Shapes generally use the cast yaw when they require a stable horizontal frame.

Box uses the inherited surface normal when one exists.

Otherwise Box uses cast yaw.


Root area Shapes use an area origin shifted one block along the horizontal cast yaw.

Non-root area Shapes use the parent origin directly.

Block impact resolutions can distinguish Outward and Inward continuation.

## 10. Current Shapes

### Missile

Missile is a physical projectile Shape.

Its base speed is 1.5 blocks per tick unit used by the projectile runtime.

It has no gravity.

Its base travel range is 16 blocks.

Range modifies that travel limit.

Missile can use Default, Arc, and Spiral Styles.

Arc and Spiral affect presentation.

They do not replace the collision trajectory.

Missile supports the current Visual set.

On entity impact it produces a direct-entity resolution.

On block impact it produces a surface-oriented resolution.

Its Impact Direction can be Outward or Inward.

Missile supports Split, Ricochet, Piercing, and Homing.

### Chain

Chain uses the projectile runtime.

Its base cost and projectile behavior begin like Missile.

After a valid impact it can continue to another eligible target.

The chain target search range is 10 blocks.

Already visited targets are tracked to avoid immediate repetition.

Downstream Effect Potence is reduced by one for each chain continuation.

The chain stops when the reduced Potence no longer supports continuation.

Chain supports Default, Arc, and Spiral Styles.

Chain supports the current Visual set.

Chain supports the projectile Modifiers.

### Sphere

Sphere is a voxel area Shape.

Radius ranges from 1 to 10.

Height ranges from 1 to 10.

The modes are:

- Sphere.
- Upper Hemisphere.
- Lower Hemisphere.

The current implementation generates a filled discrete ellipsoid from horizontal radius and vertical height.

Sphere supports Default, Inner, and Outer Styles.

Inner and Outer matter for directional rendered Visuals.

Mechanical occupancy remains the voxel set.

### Box

Box is a voxel area Shape.

Width ranges from 1 to 10.

Height ranges from 1 to 10.

Depth ranges from 1 to 10.

The current implementation does not permit zero-sized dimensions.

Box uses the Shape frame for orientation.

Box supports the Default Style and current Visual set.

### Cone

Cone is a voxel area Shape.

Width ranges from 1 to 10.

Height ranges from 1 to 10.

Depth ranges from 1 to 10.

It generates a discrete layered cone oriented from the current Shape frame.

Cone supports the Default Style and current Visual set.

### Touch

Touch uses a short ray.

Its range is 5 blocks.

Its Target Type can be Blocks or Entities.

Entity selection can win before a farther blocking block.

Touch produces either a block resolution or a direct-entity resolution.

Touch does not expose Style or Visual controls.

### Target

Target uses a long ray.

Its range is 64 blocks.

Its Target Type can be Blocks, Entities, or All.

Entity eligibility is determined by the Effects in the next row.

Target therefore remains generic.

Block targets support Outward and Inward direction modes.

Target does not expose Style or Visual controls.

### Self

Self resolves the caster as the direct entity.

It uses `RADIAL` vector policy.

It does not expose Style or Visual controls.

### Relative

Relative resolves from the parent Shape frame.

Distance ranges from 1 to 32 blocks.

Directions are:

- Front.
- Back.
- Left.
- Right.
- Up.
- Down.

The line stops at the first block or entity collision.

If nothing is hit, the requested endpoint is used.

The output facing remains based on cast yaw.

### Rune

Rune is a one-shot placed Shape.

A root Rune raycasts up to 5 blocks for a surface.

A chained Rune inherits the parent spatial frame.

The Rune is placed in adjacent air.

It receives one of six glyphs.

Its block entity stores the continuation state.

The spell branch resumes when the Rune is triggered.

## 11. Current Effects

### Damage

Damage affects living targets.

Potence ranges from 1 to 10.

Damage amount equals Potence.

Damage types are:

- Fire.
- Frost.
- Force.
- Physical.
- Arcane.
- Entropic.
- Holy.

Direct living targets are supported.

Area targeting uses living entities intersecting the incoming voxel occupancy.

Damage classification is exposed to Protection through `SpellProtectionService`.

### Dig

Dig breaks eligible blocks in the incoming voxel set.

Potence ranges from 1 to 5.

Mining eligibility is delegated to `MiningTierService`.

Area breaking uses the bulk runtime.

Drops are calculated and merged before spawning.

### Restore

Restore heals living targets.

Potence ranges from 1 to 10.

Healing is:

`0.5 + 0.5 * Potence`

Restore also processes reversible blocks in the incoming voxel set.

Block restoration is delegated to `BlockRestorationService`.

Current restoration includes selected copper oxidation states and damaged masonry transformations.

Shared blockstate properties are preserved where possible.

### Move

Move affects Blocks, Entities, or All.

Potence ranges from 1 to 10.

Modes are Push and Pull.

Entity force is delegated to `ForceService` and the movement runtime.

Block selection uses the incoming voxel set.

Block eligibility uses `MiningTierService`.

Moved blocks are represented as a rigid runtime group.

Terrain collision can detach obstructed members.

### Siphon

Siphon affects living targets.

Potence ranges from 1 to 10.

Resources are Health and Mana.

Modes are Drain and Send.

The requested damage amount is:

`1 + Potence`

Drain damages the target.

Health Drain returns 50% of successful damage to the caster as health.

Mana Drain returns 25% of successful damage to the caster as Mana.

Send reverses the source and recipient roles.

### Gravity

Gravity affects entities and can affect blocks.

Potence ranges from 1 to 10.

Modes are Attract and Repel.

Gravity uses `TICK` replay policy.

Its center comes from the incoming resolution.

Potence progressively suppresses ordinary target movement.

Active force begins at the higher effective Potence levels.

At high Potence the force reaches 0.08 per tick in the current runtime.

Entity height is considered when deriving effective Potence.

With Block Interaction attached, Gravity also creates a gravity-block runtime over the incoming voxels.

### Alter

Alter changes three signed attributes:

- Strength: -2 to +2.
- Speed: -2 to +2.
- Toughness: -2 to +2.

The base effect duration is 30 seconds.

Attached Duration extends that lifetime.

The current implementation maps those settings onto vanilla status effects.

Alter uses `CREATE_ONCE` replay policy.

### Flight

Flight targets players.

Potence ranges from 1 to 8.

The base duration is 30 seconds.

Attached Duration extends that lifetime.

The current levels progressively improve aerial movement and protection.

Potence 1–3 reduces fall damage.

Potence 4 grants Slow Falling.

Potence 5–7 enables glide behavior.

Potence 6 reduces wall-crash damage.

Potence 7 removes fall and wall-crash damage.

Potence 8 grants creative-style flight permission while the effect is active.

Existing legitimate flight permission is restored safely when the magical grant ends.

Flight uses `STATEFUL` replay policy.

### Dimensional Storage

Dimensional Storage targets server players.

Potence ranges from 1 to 10.

Capacity is 10 slots per Potence.

The maximum current capacity is therefore 100 slots.

The menu presents 50 slots per page.

Storage belongs to the target UUID.

It is persistent server data.

The Effect opens the target-owned storage rather than creating inventory data inside the spell.

Dimensional Storage uses `CREATE_ONCE` replay policy.

### Teleportation

Teleportation uses Potence 1 to 10.

Range is 10 blocks per Potence where the selected mode uses range.

Modes are:

- Directional.
- Blink.
- Home.
- Recall.

Directional searches backward from the maximum requested destination until a safe fit is found.

Blink searches randomized safe destinations inside the allowed radius.

Home uses the player's home or world-spawn logic in the Overworld.

Home searches for a nearby portal in the Nether.

Recall uses the caster's stored Mark.

Cross-dimensional Recall is supported when a safe destination exists.

Teleportation uses `CREATE_ONCE` replay policy.

### Mark

Mark records a spell destination for the caster.

The recorded position is the block space above the resolved struck surface.

Marks are keyed to the caster UUID.

Teleportation Recall consumes this current stored destination conceptually as its target source.

Mark uses `CREATE_ONCE` replay policy.

### Protection

Protection targets living entities.

Potence ranges from 1 to 10.

The protected damage category can be All or one specific spell damage type.

The base duration is 30 seconds.

Attached Duration extends that lifetime.

All-damage Protection reduces damage by 10% per Potence.

Specific-type Protection reduces matching damage by 20% per Potence.

Reduction is clamped to 100%.

Protection uses `STATEFUL` replay policy.

### Temporary Block

Temporary Block creates real temporary solid blocks over the incoming voxel occupancy.

It can replace Air or Water.

Water is restored when the temporary field is removed.

With Duration attached, the runtime restores the replaced state when the lifetime expires.

Without Duration, the current runtime does not assign a timed expiry.

The Temporary Block itself has no loot.

It does not suffocate entities.

It does not block view or conduct redstone as an ordinary full block would.

Temporary Block uses `CREATE_ONCE` replay policy.

### Light

Light costs 1 Mana.

On a block or point resolution, Light places the mod's existing Magic Light block.

The placed Light is permanent when no Duration Modifier is attached.

When Duration is attached, the placed Light remains for that Duration and then restores the replaced air or water state.

On a direct entity resolution, Light highlights the entity and makes it carry a moving Magic Light for 30 seconds.

The moving light uses the existing Magic Light block, which emits light level 15.

The moving light follows the entity and is cleaned up when the effect expires.

The entity highlight is removed after 30 seconds unless the entity was already glowing before Light was applied.

Light uses CREATE_ONCE replay semantics.

## 12. Current Modifiers

### Duration

Duration ranges from 1 to 60 seconds.

The default is 5 seconds.

It belongs to the Shape or Effect immediately to its left under the row-local binding rule.

For replayable Effects, `DurationRuntime` schedules repeated application.

`INSTANT` and `TICK` Effects can be replayed by Duration.

`STATEFUL` and `CREATE_ONCE` Effects use duration as state lifetime or internal runtime lifetime instead of being recreated every tick.

### Channel

Channel attaches to an Effect.

It turns the spell into held-use behavior.

The attached Effect is replayed while the cast remains held.

The runtime is cleared when use stops.

### Stream

Stream attaches to a Shape.

It turns the Shape into held-use behavior.

The Shape is recast every 10 ticks while held.

Root Stream Shapes use the caster's current look direction on replay.

### Block Interaction

Block Interaction is a generic Modifier flag.

The Effect decides whether it supports the flag.

Gravity currently declares Block Interaction support.

Unsupported Effects ignore the flag.

### Range

Range is a projectile Modifier.

The stored setting is displayed as a signed value from -10 to +10.

Zero is the default.

Each Range point changes projectile range by 2 blocks.

Missile and Chain have a base range of 16 blocks.

Projectile range is never allowed below 1 block.

### Split

Split is a projectile Modifier.

Potence controls the number of additional projectiles.

Patterns are Polygonal and Linear.

The current split offset is 0.65.

### Ricochet

Ricochet is a projectile traversal Modifier.

A compatible projectile can reflect from a block impact and continue when travel range remains.

### Piercing

Piercing is a projectile traversal Modifier.

A compatible projectile can continue through hit entities.

Already pierced entities are tracked.

### Homing

Homing is a projectile steering Modifier.

It acquires eligible targets according to the Effects in the next row.

The projectile continuously adjusts its travel direction toward the selected target.

## 13. Effect replay policies

The replay-policy enum contains:

- `INSTANT`.
- `TICK`.
- `DELTA_VOXELS`.
- `CREATE_ONCE`.
- `STATEFUL`.

Most immediate Effects use `INSTANT`.

Gravity uses `TICK`.

Alter uses `CREATE_ONCE`.

Flight uses `STATEFUL`.

Dimensional Storage uses `CREATE_ONCE`.

Teleportation uses `CREATE_ONCE`.

Mark uses `CREATE_ONCE`.

Protection uses `STATEFUL`.

Temporary Block uses `CREATE_ONCE`.

The policy belongs to Effect execution semantics.

It is not a Shape-specific rule.

## 14. Styles and Visuals

Styles and Visuals are presentation settings.

They do not replace mechanical geometry.

Current Styles are:

- Default.
- Arc.
- Spiral.
- Inner.
- Outer.

Current Visuals are:

- Default.
- Large Chunk.
- Sword.
- Axe.
- Trident.
- Flames.

Missile and Chain support Default, Arc, and Spiral.

Sphere supports Default, Inner, and Outer.

Box and Cone currently use Default Style.

Directional Visuals can use Inner or Outer orientation on Sphere.

The rendered carrier may interpolate motion or presentation.

Mechanical targeting remains based on the resolution and its voxel occupancy.

## 15. Target extraction

Target eligibility is Effect-driven.

Shape code should not contain a list of every Effect type.

`acceptsDirectEntity()` defines whether an Effect can consume a direct target.

Target and projectile homing use that capability to determine eligible entities.

Damage, Restore, and Siphon require living entities.

Flight requires players.

Dimensional Storage requires server players.

Move, Gravity, and Teleportation accept broader entity targets according to their runtime rules.

This keeps Shape targeting generic as Effects are added or changed.

## 16. Shared runtime services

Shared mechanics are separated from component definitions.

Current examples include:

- `AreaDigBulkRuntime`.
- `BlockBreakService`.
- `MiningTierService`.
- `BlockRestorationService`.
- `ForceService`.
- `MoveTravelRuntime`.
- `HealthService`.
- `ManaService`.
- `SpellProtectionService`.
- `FlightService`.
- `DimensionalStorageService`.
- `TemporaryBlockRuntime`.
- `GravityBlockRuntime`.
- `WandBindingService`.
- `ManaGridSupport`.


## Robe armor rules

The robe families use the following protection values.

| Family | Hood | Upper Robe | Lower Robe |
|---|---:|---:|---:|
| Cloth | 0 | 0 | 0 |
| Heavy | 1 | 3 | 2 |
| Leather | 2 | 5 | 3 |
| Wizard | 2 | 5 | 4 |

Heavy protection matches the corresponding vanilla Leather pieces.

Leather Robe protection matches the corresponding vanilla Gold pieces.

Wizard Robe protection matches the corresponding vanilla Chainmail pieces.

All robe pieces use vanilla Leather durability values.

Hoods use 55 durability.

Upper Robes use 80 durability.

Lower Robes use 75 durability.

Robes do not break normally.

Durability damage is capped at one remaining durability.

At one remaining durability, an equipped robe piece is removed from the armor slot and dropped.

A robe at one remaining durability cannot be equipped until repaired.

Spell Mana discounts are additive across equipped, usable robe pieces.

Cloth pieces grant 5% discount each.

Heavy pieces grant 10% discount each.

Leather Robe pieces grant 15% discount each.

Wizard Robe pieces grant 20% discount each.

The robe discount is applied after any focus-specific Mana multiplier.

Casting Experience continues to use the raw undiscounted spell cost.

All robe families use Gold-tier enchantability.

For gameplay enchantment queries, every enchantment present on a robe is treated as one level higher.

This +1 effective level is not clamped to the enchantment's vanilla maximum.

Armor enchantments therefore may function one level above their normal cap while worn or otherwise queried for gameplay.


## Random spell loot

Random Written Spells are injected through a NeoForge Global Loot Modifier.

Ordinary mobs have a 0.5% chance to drop an Apprentice spell.

Witches, Endermen, Pillagers, and Blazes have a 5% chance to drop an Initiate spell.

Evokers, Guardians, and Piglin Brutes have a 15% chance to drop a Wizard spell.

Elder Guardians, Ravagers, Withers, and Wardens always drop a Master spell.

The mob-specific tier replaces the ordinary Apprentice roll.

Supported dungeon and hostile-structure chests have a 10% chance to gain one random Written Spell.

Dungeon spell tier weights are 60% Apprentice, 25% Initiate, 12% Wizard, and 3% Master.

Supported loot tables include simple dungeons, abandoned mineshafts, desert pyramids, jungle temples, woodland mansions, Nether fortresses, End cities, ancient cities, underwater ruins, strongholds, bastions, and trial chambers.

All generated loot spells use RandomSpellGenerator and therefore follow the same spell grammar and tier rules as the Spell Randomizer.


### Carving Station UI

The Carving Station is divided into three visually distinct panels.

The left panel is Runestone creation and input.

The center panel is the carving grid.

The right panel is the Shape and Effect component palette.

Panel boundaries use explicit borders and vertical separators.

All real inventory and station slots are drawn with visible slot borders.

The three Runestone-forming ingredient slots are order-independent.

Iron Ingot + Iron Pickaxe + Red Dye forms an Iron Runestone regardless of ingredient slot order.

Gold Ingot + Golden Pickaxe + Red Dye forms a Gold Runestone regardless of ingredient slot order.

Diamond + Diamond Pickaxe + Red Dye forms a Diamond Runestone regardless of ingredient slot order.

Spell components are placed by dragging from the component palette onto a carving-grid cell.

Existing carving-grid components may be dragged between cells before carving.

Dropping an existing component onto another occupied carving cell swaps the two components.

Right-clicking a carving-grid cell clears it.

The Carve action remains unavailable once a Runestone has been carved.


### Runestone slot layout correction

Runestones do not have Faces.

Each Runestone has one horizontal carving row.

The Runestone material determines the number of component slots in that row.

Iron Runestone: 2 slots.

Gold Runestone: 3 slots.

Emerald Runestone: 4 slots.

Diamond Runestone: 5 slots.

The Carving Station renders only those active slots.

The previous three-row-by-face grid model is removed.


## Runecasters

Runecasters are staff-length magical foci.

Their held model is approximately the length of the player.

Four visual material variants exist: Wood, Iron, Diamond, and Netherite.

All Runecasters deal exactly 1 point of melee attack damage before other external modifiers.

Runecasters use the same 50% mana multiplier as Staves.

Shift-right-click opens the Runecaster mounting interface.

A Runecaster has one mounted Rune slot.

Only carved Runestones can be mounted.

The mounted Runestone's carving row is interpreted from left to right as a spell sequence.

Each non-empty Runestone slot becomes one spell row.

Runestone components use their normal default Style, Visual, and property values when cast from a Runecaster.

The mounted Rune must begin with a Shape.

Right-click casts the mounted Rune using the normal spell runtime.

Channel and Stream behavior, mana consumption, cooldowns, and robe mana discounts are handled the same way as Staff casting.

The Runestone itself is preserved while mounted and can be removed through the mounting interface.
