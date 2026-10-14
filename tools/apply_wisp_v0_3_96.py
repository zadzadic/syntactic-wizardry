#!/usr/bin/env python3
"""v0.3.96 integration. Run after v0.3.95 Undine and Salamander build stages."""
from pathlib import Path
import json
import re

root = Path("src/main")
java = root / "java/com/proxpero/syntacticwizardry/SyntacticWizardry.java"
client = root / "java/com/proxpero/syntacticwizardry/client/ClientEvents.java"
lang = root / "resources/assets/syntacticwizardry/lang/en_us.json"
properties = Path("gradle.properties")


def replace_once(content, old, new, label):
    matches = content.count(old)
    if matches != 1:
        raise ValueError(f"{label}: expected one insertion target, found {matches}")
    return content.replace(old, new, 1)


s = java.read_text()
entity_needle = 'public static final DeferredHolder<EntityType<?>,EntityType<UndineEntity>> UNDINE=ENTITIES.register'
lines = s.splitlines(keepends=True)
matching = [i for i, line in enumerate(lines) if entity_needle in line]
if len(matching) != 1:
    raise ValueError("Could not locate existing Undine entity registration")
lines[matching[0]] += ' public static final DeferredHolder<EntityType<?>,EntityType<WispEntity>> WISP=ENTITIES.register("wisp",()->EntityType.Builder.<WispEntity>of(WispEntity::new,MobCategory.AMBIENT).sized(0.45F,0.45F).fireImmune().clientTrackingRange(8).updateInterval(3).build("wisp"));\n'
s = "".join(lines)

egg_needle = 'public static final DeferredItem<DeferredSpawnEggItem> UNDINE_SPAWN_EGG=ITEMS.register'
lines = s.splitlines(keepends=True)
matching = [i for i, line in enumerate(lines) if egg_needle in line]
if len(matching) != 1:
    raise ValueError("Could not locate existing Undine spawn egg")
lines[matching[0]] += ' public static final DeferredItem<DeferredSpawnEggItem> WISP_SPAWN_EGG=ITEMS.register("wisp_spawn_egg",()->new DeferredSpawnEggItem(SyntacticWizardry.WISP::get,0xE08B2C,0xFFE08A,new Item.Properties()));\n'
s = "".join(lines)

attr_re = r'(event\.put\(UNDINE\.get\(\),UndineEntity\.createAttributes\(\)\.build\(\)\);)'
s, count = re.subn(attr_re, r'\1event.put(WISP.get(),WispEntity.createAttributes().build());', s, count=1)
if count != 1:
    raise ValueError("Could not locate attribute creation listener")

s = replace_once(
    s, 'output.accept(UNDINE_SPAWN_EGG.get());',
    'output.accept(UNDINE_SPAWN_EGG.get());output.accept(WISP_SPAWN_EGG.get());',
    "Creative tab"
)
java.write_text(s)

r = client.read_text()
r = replace_once(
    r, 'e.registerEntityRenderer(SyntacticWizardry.UNDINE.get(),UndineRenderer::new);',
    'e.registerEntityRenderer(SyntacticWizardry.UNDINE.get(),UndineRenderer::new);e.registerEntityRenderer(SyntacticWizardry.WISP.get(),WispRenderer::new);',
    "Client renderer registration"
)
client.write_text(r)

labels = json.loads(lang.read_text())
labels["entity.syntacticwizardry.wisp"] = "Wisp"
labels["item.syntacticwizardry.wisp_spawn_egg"] = "Wisp Spawn Egg"
lang.write_text(json.dumps(labels, ensure_ascii=False, indent=2) + "\n")

p = properties.read_text()
p, count = re.subn(r'(?m)^mod_version=.*$', 'mod_version=0.3.96', p)
if count != 1:
    raise ValueError("Version property not found")
properties.write_text(p)

assert (root / "resources/assets/syntacticwizardry/geo/wisp.geo.json").is_file()
assert (root / "resources/assets/syntacticwizardry/textures/entity/wisp.png").read_bytes()[:8] == b"\x89PNG\r\n\x1a\n"
assert (root / "resources/assets/syntacticwizardry/textures/particle/wisp_particles.png").read_bytes()[:8] == b"\x89PNG\r\n\x1a\n"
print("Wisp v0.3.96 applied: entity, renderer, attributes, spawn egg, assets, version.")
