# Capn's Bee Addon


```text
/give @p capnsbeeaddons:temporal_focus_upgrade
```

Place it in one of the Industrial Apiary's upgrade slots. Maximum installed count is one.


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
