# Capn's Bee Addon

## First feature: Temporal Focus Upgrade

Install the upgrade in the **Industrial Apiary running the Temporal effect**.

While installed, that source Temporal effect:

- accelerates only other loaded Gendustry Industrial Apiaries inside its exact Career Bees territory;
- never accelerates its own source apiary;
- ignores every other TileEntity;
- does not schedule random-ticking blocks;
- does not load chunks;
- scans loaded chunk TileEntity maps instead of every block in the volume.

Industrial Apiaries targeted by another normal or focused Temporal bee can still be accelerated. Normal Temporal bees without this upgrade are completely unchanged.

### Test item

No crafting recipe is included yet. For development:

```text
/give @p capnsbeeaddons:temporal_focus_upgrade
```

Place it in one of the Industrial Apiary's upgrade slots. Maximum installed count is one.

## Build

The wrapper scripts download Gradle's official 9.2.1 wrapper JAR from the Gradle GitHub tag on first use, then the wrapper verifies the 9.2.1 distribution checksum.

Windows:

```text
gradlew.bat build
```

Run client:

```text
gradlew.bat runClient
```

## Exact development versions

- Minecraft 1.12.2
- Forge 14.23.5.2860 through RetroFuturaGradle
- Forestry 5.8.2.427
- BDLib 1.14.4.1
- Gendustry 1.6.5.8
- Career Bees 0.4.0
- MixinBooter 10.7

Runtime metadata currently pins these bee-mod versions exactly because the mixin targets Career Bees implementation details.

## Important behavior

Career Bees stores accelerated positions in one shared per-world map. The focused path only adds valid Industrial Apiaries; it never removes entries belonging to ordinary Temporal bees. If the upgrade is inserted immediately after that same apiary previously ran unfocused, old targets can remain accelerated for roughly one second until Career Bees expires them normally.

## Planned extension

A future `Effect Territory Restrictor` can be implemented as another normal `IApiaryUpgrade` by multiplying `ApiaryModifiers.territory`. It does not need another Career Bees mixin.
