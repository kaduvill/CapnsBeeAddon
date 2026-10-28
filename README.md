# Capn's Bee Addon

Capn's Bee Addon is a focused Minecraft 1.12.2 addon for Forestry, Gendustry, and Career Bees.

It adds Industrial Apiary upgrades, accurate Career Bee territory visualization, and targeted performance improvements for large/sped up apiary setups.

## Features

### Bug fixes

- Miner, Fortunate Miner, and Silky Miner Bees fired from the Bee Gun respect negative block hardness, preventing them from mining unbreakable blocks such as bedrock. Apiary mining is unchanged.
- Honey-Smelter Bees fired from the Bee Gun converts 1 ingot, and preserves the rest. Apiary processing are unchanged.
- Assassin Bee crash fix. Automation safely targets other living, different-species queens in Forestry/Gendustry housings, using loaded chunks only. Bee Gun behavior is unchanged.
- Prevents Energy Bee overflow from causing negative energy transfers.
- Energy Bee compatibility with receivers requiring sided energy input.

### Career Bees tweaks (configurable)
- Accurate effect descriptions: (focusing on matching description to what career bees effects actually does, and removing untrue claims)
  - Fortunate Miner effect from Fortune 3 to Fortune 2
  - Police effect: removes the high-luck claim and clarifies that kills count as player kills
  - Ore Crusher effect: corrects the yield to 2–4 dust per ore
- Reveal secret bees: Dire, Bensen and Soaring (disabled by default)

### Item tooltips

Adds concise tooltips for Career Bees frames, the Alveary Frame Holder, Flower Pedestal, Bee Armor and Triquetra, plus Bee-Bee Gun controls.

### The One Probe integration (optional)

- Show attached Yente princess or Student drone on Forestry and Gendustry bee housings. Alvearies show these lines on the exact block the helper was attached to.
- Extended view only. The `TOP` category in `capnsbeeaddon.cfg` contains the `TopIntegration` master switch and separate `YenteBee` and `StudentBee` toggles (default `true`; restart required).


### Career Bees JEI

Adds JEI pages for Career Bees. These can be deactivated in config (default=true)
Currently implemented:

- Crusher Bee
- Honey-Smelter Bee
- Chef Bee
- Lumber Bee
- Mason Bee
- Miner / Fortunate Miner / Silky Miner Bees
- Armorer Bee
- Sharpening Bee
- Smelter Bee
- Police Bee
- Taxcollector Bee
- Thief Bee
- Assassin Bee
- Repair Bee
- Graduate / PHD Bees (retraining)
- Priest Bee
- Husbandry Bee
- Butcher Bee
- Business Bee (all sellable items)
- Junk Seller Bee (all purchasable goods)
- Clockwork Bee
- Doctor Bee (healing and regeneration)
- Plague Bee (cleansing and zombie-villager curing)
- Electrician Bee (lightning damage and mob conversions)
- Devil Bee
- Mad Scientist Bee
- Quantum Charming / Quantum Strange Bees (shared Teleposition overview)
- Collecting Bee
- Energy Bee
- Temporal Bee
- Bensen Bee
- Dire Bee (house building; cobblestone, glass, and torch outputs)
- Artistic Bee
- N.C.A.
- Yente Bee
- Student Bee

### Temporal Focus upgrades (Gendustry)

Temporal Focus upgrades change what a Career Bees Temporal effect targets. Only one Temporal Focus can be installed at a time.

* **Temporal Apiary Focus Upgrade**

    * Accelerates tickable Gendustry Industrial Apiaries only.
    * Skips unrelated TileEntities and random-ticking blocks.
    * Energy use: `×1.10`

* **Temporal TileEntity Focus Upgrade**

    * Accelerates tickable TileEntities only.
    * Skips all random-ticking block work.
    * Energy use: `×1.10`

* **Temporal Growth Focus Upgrade**

    * Accelerates random-ticking blocks only.
    * Skips TileEntity targeting.
    * Energy use: `×1.10`

Focused scans operate on loaded chunks only and reuse Career Bees' existing target scheduler. They do not load or generate chunks.

### Territory Restrictor Upgrade

Reduces an Industrial Apiary's effective bee territory while slightly increasing its energy use.

Each installed upgrade applies multiplicatively:

* Territory: `×0.73`
* Energy use: `×1.05`
* Maximum installed: `6`

### Product Nullifier Upgrade

Prevents Forestry's normal bee-product stacks from being generated in an Industrial Apiary.
Bee effects and the normal queen lifecycle continue to run
* Energy use: `×1.10`

### Career Bee territory overlay

Adds a GUI toggle to Forestry Apiaries and Gendustry Industrial Apiaries that displays the active Career Bee effect territory as an in-world wireframe.

Normal click shows the territory boundary, including housing/frame/upgrade territory modifiers.This calculation does not scan the territory or load chunks.
Sneak-click with a Temporal bee shows advanced target counts.

* `ForestryApiary`: enables the button and overlays for Forestry Apiaries (Bee Houses and Alvearies are excluded).
* `GendustryIndustrialApiary`: enables the button and overlays for Gendustry Industrial Apiaries.
* `AdvancedTemporalOverlay`: enables advanced Temporal targets and diagnostics for either enabled apiary.



### Performance improvements


* Reuses Forestry's Industrial Apiary `canWork` result during repeated updates in the same world tick.
* Skips Gendustry's indirect-redstone lookup when the selected apiary redstone mode does not use it.
* Skips BDLib's six-sided cover traversal after an apiary has been conclusively verified to have no covers.
  * Invalidates the empty-cover fast path whenever covers change and falls back to BDLib's original behavior for covered or unexpected states.
* Uses loaded-chunk TileEntity maps for focused TileEntity scans instead of scanning every block position.
* Skips trade generation for Business/Junk Seller Bees (these trades was always rejected later in Career Bees code path):
  * Junk Seller trade generation without extractable emeralds in adjacent inventory
  * Vanilla treasure-map
  * Quark Pathfinder Maps and Charm Structure Maps (optional mods)
  * Immersive Engineering ore-vein map/revolver-part (Immersive Engineering is optional)
  * Forestry pollen/saplings

Gendustry optimizations are automatically skipped when Gendustry CEu is installed.


## Development versions

Capn's Bee Addon is developed against these exact Minecraft 1.12.2 dependencies:

* Forestry 5.8.2.427
* BDLib 1.14.4.1
* Gendustry 1.6.5.8
* Career Bees 0.4.0, internally reporting version 1.0
* MixinBooter 10.7
* Mixin 0.8.7
* JEI 4.16.1.301 API (optional)
* The One Probe 1.12-1.4.28 API (optional)
