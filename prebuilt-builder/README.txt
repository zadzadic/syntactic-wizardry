Arcane Builder transplant
========================

These class files are copied directly from the user's working
arcane-0.4.267-field-conjure-voxel-collision(1).jar.

Only the EventBusSubscriber mod-id UTF8 constant was changed from "arcane"
to "syntacticwizardry" in the copied subscriber classes so NeoForge attaches
them to the current mod container.

Syntactic Wizardry source code provides only registry, wand, mana, event and
network compatibility bridges. The Builder camera, selection, slice, grid,
container linking, inventory UI, placement, copy/move/take, rotation, fluid
handling, hold actions and previews are the original compiled implementation.
