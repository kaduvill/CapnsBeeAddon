# Capn's Bee Addon

Capn's Bee Addon is a focused Minecraft 1.12.2 addon for Forestry, Gendustry, and Career Bees.

It adds Industrial Apiary upgrades, accurate Career Bee territory visualization, and targeted performance improvements for large apiary setups.

## Features

### Temporal Focus upgrades

Temporal Focus upgrades change what a Career Bees Temporal effect targets. Only one Temporal Focus can be installed at a time.

* **Temporal Apiary Focus Upgrade**

    * Accelerates tickable Gendustry Industrial Apiaries only.
    * Skips unrelated TileEntities and random-ticking blocks.

* **Temporal TileEntity Focus Upgrade**

    * Accelerates tickable TileEntities only.
    * Skips all random-ticking block work.

* **Temporal Growth Focus Upgrade**

    * Accelerates random-ticking blocks only.
    * Skips TileEntity targeting.

Focused scans operate on loaded chunks only and reuse Career Bees' existing target scheduler. They do not load or generate chunks.

### Territory Restrictor Upgrade

Reduces an Industrial Apiary's effective bee territory while slightly increasing its energy use.

Each installed upgrade applies multiplicatively:

* Territory: `×0.70`
* Energy use: `×1.05`
* Maximum installed: `6`

### Product Nullifier Upgrade

Prevents Forestry's normal bee-product stacks from being generated in an Industrial Apiary.

Bee effects and the normal queen lifecycle continue to run. The upgrade does not add a filter, GUI, inventory, or packet system.

### Career Bee territory overlay

Adds a compact button to supported bee-housing GUIs when the active Career Bee effect has a meaningful territory.

The overlay:

* shows the active effect name and area size;
* renders the effect area as an in-world wireframe box;
* accounts for the bee genome, housing modifiers, beekeeping mode, and Territory Restrictors;
* refreshes from the live client housing once per second;
* does not scan or load chunks;
* requires no custom networking.

### Performance improvements

* Reuses Forestry's Industrial Apiary `canWork` result during repeated updates in the same world tick.
* Skips Gendustry's indirect-redstone lookup when the selected apiary redstone mode does not use it.
* Skips BDLib's six-sided cover traversal after an apiary has been conclusively verified to have no covers.
* Invalidates the empty-cover fast path whenever covers change and falls back to BDLib's original behavior for covered or unexpected states.
* Uses loaded-chunk TileEntity maps for focused TileEntity scans instead of scanning every block position.

## Item IDs

```text
capnsbeeaddon:temporal_focus_apiary
capnsbeeaddon:temporal_focus_tile_entity
capnsbeeaddon:temporal_focus_growth
capnsbeeaddon:territory_restrictor
capnsbeeaddon:product_nullifier
```


## Development versions

Capn's Bee Addon is developed against these exact Minecraft 1.12.2 dependencies:

* Forestry 5.8.2.427
* BDLib 1.14.4.1
* Gendustry 1.6.5.8
* Career Bees 0.4.0, internally reporting version 1.0
* MixinBooter 10.7
* Mixin 0.8.7

Other versions are not currently tested.

## License

Capn's Bee Addon is licensed under the MIT License. See [LICENSE](LICENSE).
