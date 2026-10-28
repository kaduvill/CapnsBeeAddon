0.2.2
- Automatically disable redstone and empty-cover optimizations when using Gendustry CEu

0.2.1
- Add AccurateEffectDescriptions boolean in config (default true): corrects Fortunate Miner, Police, and Ore Crusher effect descriptions
- Add Career Bee territory visualization to Forestry Apiaries
- Add Career Bee Territory config category with ForestryApiary, GendustryIndustrialApiary, and AdvancedTemporalOverlay switches
- Added debug config to render player's actual hitbox as a red wireframe
- Limit requests per player for Forestry and advanced Temporal overlays
- Add maps from Quark and Charms to trading bees exclusion list (expands optimization tweak)


0.2.0
- Add config
- Add configoptions: RevealDireBee, RevealSoaringBee, and RevealBensenBee in Career Bees Tweaks configsection
- Add optional The One Probe integration for attached Yente princesses and Student drones, with separate toggles in the TOP config category (extended view only)
- Fixed Miner, Fortune Miner and Silky Miner used in Bee-Bee gun could break bedrock (hardness -1)
- Fixed Honey-Smelter Bee-Bee Gun deleting stacked ingots. Convert one ingot per action and preserve the remainder
- Fixed Assassin Bee disappearing and crashing when used in bee housing. Bee-Bee Gun behavior is unchanged
- Fixed Energy Bee overflow causing negative energy transfers
- Fixed Energy Bee compatibility with receivers requiring sided energy input
- Skips trade generation for Business/Junk Seller Bees (these trades was always rejected later in Career Bees code path):
  - Junk Seller trade generation without extractable emeralds in adjacent inventory
  - Vanilla treasure-map
  - Immersive Engineering ore-vein map/revolver-part (Immersive Engineering is optional)
  - Forestry pollen/saplings
- Add JEI pages for Career Bees
  - Ore Crusher Bee
  - Honey-Smelter Bee
  - Chef Bee (honey glazing)
  - Lumber Bee (tree bark)
  - Mason Bee
  - Miner / Fortunate Miner / Silky Miner Bees
  - Armorer Bee (armor)
  - Sharpening Bee (attack damage)
  - Smelter Bee (furnace recipes)
  - Police Bee
  - Taxcollector Bee
  - Thief Bee
  - Assassin Bee
  - Repair Bee
  - Graduate / PHD Bees (retraining)
  - Priest Bee (random enchanting)
  - Husbandry Bee (animal breeding)
  - Butcher Bee (animal loot)
  - Business Bee
  - Junk Seller Bee
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
  - Dire Bee
  - Artistic Bee
  - N.C.A.
  - Yente Bee
  - Student Bee
- Territory Restrictors now reduce territory by 27% per upgrade (previously 30%), fixing missed neighboring flowers when six restrictors are installed due to a bug in Forestry Flower spiral-scan (Forestry 5.8.2.427)
- Added tooltips for Bee-Bee Gun, Career Bees frames, Alveary Frame Holder, Flower Pedestal, Bee Armor and Triquetra

0.1.1
- Add Energy usage to all upgrades
- Some clean up in advanced overlay
- Added chinese langfile (ZY)

0.1.0
 
- Added Temporal Apiary, TileEntity and Growth Focus upgrades
- Added Territory Restrictor upgrade
- Added Product Nullifier upgrade
- Added Career Bee territory visualization
- Added targeted Industrial Apiary and Temporal effect performance improvements
