#!/usr/bin/env python3
"""Apply the Undine integration after the existing v0.3.94 source overlays."""
from pathlib import Path
import json
import re

root = Path("src/main")
src = root / "java/com/proxpero/syntacticwizardry/SyntacticWizardry.java"
events = root / "java/com/proxpero/syntacticwizardry/client/ClientEvents.java"
lang = root / "resources/assets/syntacticwizardry/lang/en_us.json"
gradle = Path("gradle.properties")

def insert_once(text, needle, insert, label):
    if text.count(needle) != 1:
        raise RuntimeError(f"{label}: expected one insertion point; got {text.count(needle)}")
    return text.replace(needle, needle + insert, 1)

s = src.read_text()
# The Salamander overlay owns this file. Insert into that definitive source.
lines = s.splitlines(keepends=True)
for marker, extra in (
    ('SALAMANDER_SPAWN_EGG=ITEMS.register', ' public static final DeferredItem<DeferredSpawnEggItem> UNDINE_SPAWN_EGG=ITEMS.register("undine_spawn_egg",()->new DeferredSpawnEggItem(SyntacticWizardry.UNDINE::get,0x147FCA,0xA6EDFF,new Item.Properties()));\n'),
    ('SALAMANDER=ENTITIES.register', ' public static final DeferredHolder<EntityType<?>,EntityType<UndineEntity>> UNDINE=ENTITIES.register("undine",()->EntityType.Builder.of(UndineEntity::new,MobCategory.CREATURE).sized(0.65F,1.5F).clientTrackingRange(8).build("undine"));\n'),
):
    hits = [i for i,line in enumerate(lines) if marker in line]
    if len(hits) != 1:
        raise RuntimeError(f"{marker}: expected one matching line, found {len(hits)}")
    lines[hits[0]] += extra
s = ''.join(lines)

# Attribute creation is a MOD-bus listener in the original mod class.
attribute_pattern = r'(event\.put\(SALAMANDER\.get\(\),\s*SalamanderEntity\.createAttributes\(\)\.build\(\)\);|e\.put\(SALAMANDER\.get\(\),\s*SalamanderEntity\.createAttributes\(\)\.build\(\)\);)'
matches = list(re.finditer(attribute_pattern, s))
if len(matches) != 1:
    raise RuntimeError(f"Undine attributes: expected Salamander listener, found {len(matches)}")
m = matches[0]
var = m.group(1).split(".put(")[0]
s = s[:m.end()] + f'{var}.put(UNDINE.get(),UndineEntity.createAttributes().build());' + s[m.end():]

# Put the spawn egg beside the Salamander egg in the mod's creative tab.
egg_pattern = r'(\w+)\.accept\(SALAMANDER_SPAWN_EGG\.get\(\)\);'
match = re.search(egg_pattern, s)
if not match:
    raise RuntimeError("Undine egg: no Salamander egg in creative tab")
s = s[:match.end()] + match.group(1) + '.accept(UNDINE_SPAWN_EGG.get());' + s[match.end():]
src.write_text(s)

c = events.read_text()
c = insert_once(c, 'e.registerEntityRenderer(SyntacticWizardry.SALAMANDER.get(),SalamanderRenderer::new);',
    'e.registerEntityRenderer(SyntacticWizardry.UNDINE.get(),UndineRenderer::new);', 'renderer')
events.write_text(c)

d = json.loads(lang.read_text())
d['entity.syntacticwizardry.undine'] = 'Undine'
d['item.syntacticwizardry.undine_spawn_egg'] = 'Undine Spawn Egg'
lang.write_text(json.dumps(d, ensure_ascii=False, indent=2) + '\n')

g = gradle.read_text()
g, count = re.subn(r'(?m)^mod_version=.*$', 'mod_version=0.3.95', g)
if count != 1:
    raise RuntimeError("Could not stamp 0.3.95")
gradle.write_text(g)

# The color and alpha in this optimized atlas are derived from the original
# 256x256 user-authored Undine texture.
import base64
texture = root / "resources/assets/syntacticwizardry/textures/entity/undine.png"
texture.parent.mkdir(parents=True, exist_ok=True)
texture.write_bytes(base64.b64decode(Path(".build/v0.3.95-undine-texture.png.b64").read_text()))
assert texture.read_bytes()[:8] == b'\x89PNG\r\n\x1a\n'
print("Undine integration applied: entity, egg, attributes, renderer, labels, texture, version.")
