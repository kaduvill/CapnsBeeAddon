# Temporal Focus design

## Why this is an upgrade plus a mixin

Gendustry intentionally lets addons implement `IApiaryUpgrade`, but `ApiaryModifiers` contains only numeric and boolean housing modifiers. It has no target predicate.

The item is therefore a normal Gendustry upgrade while the target restriction is implemented at Career Bees' actual acceleration registration points.

## Invariants

1. No-upgrade path returns immediately from the mixin and executes untouched Career Bees code.
2. Focused sources only register `IIndustrialApiary & ITickable` targets.
3. The source position is never registered by its own focused effect.
4. Focused scans never call a chunk-loading method.
5. Random-ticking blocks are never scheduled by a focused source.
6. Entries from normal Temporal sources are never removed or filtered.
7. Career Bees keeps ownership of the shared update map and manual update cadence.

## Why the source marker is not reused

Career Bees inserts the source position with duration zero as a scan marker. Its world-tick handler adds a tickable tile to the update list before removing a zero-duration entry, so that marker still manually updates the source once.

The focused path instead tracks its own last-scan world time and only reads Career Bees' shared map to preserve its anti-cascade behavior. This keeps the source completely unaccelerated by itself.

## Focused scan complexity

Original scan:

```text
O((2Rx + 1) × (2Ry + 1) × (2Rz + 1))
```

Focused scan:

```text
O(loaded chunks intersecting the box + TileEntities in those chunks)
```

No blocks are inspected and no collections are rebuilt every tick. Scanning occurs only on Career Bees' existing 20-tick refresh boundary.
