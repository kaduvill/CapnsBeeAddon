package com.kaduvill.capnsbeeaddon.compat.jei.TradingBee;

import com.kaduvill.capnsbeeaddon.CapnsBeeAddon;
import com.rwtema.careerbees.effects.EffectBuisness;
import forestry.api.apiculture.EnumBeeType;
import forestry.apiculture.items.ItemBeeGE;
import forestry.core.utils.VillagerTradeLists;
import net.minecraft.entity.IMerchant;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/** Reads the bees' generator pool once, without a world, live inventory or world RNG. */
public final class TradingBeeRecipes {
    private static final int SAMPLES = 150;
    private static final String TREASURE_MAP = "net.minecraft.entity.passive.EntityVillager$TreasureMapForEmeralds";

    private TradingBeeRecipes() { }

    public enum Mode {
        BUSINESS("business_bee", "business"), JUNK_SELLER("junk_seller_bee", "junk_seller");

        public final String uid;
        public final String translation;

        Mode(String uid, String translation) {
            this.uid = CapnsBeeAddon.MODID + "." + uid;
            this.translation = "jei.capnsbeeaddon." + translation;
        }

        public boolean isBusiness() { return this == BUSINESS; }

        public EffectBuisness effect() { return isBusiness() ? EffectBuisness.INSTANCE : EffectBuisness.JUNK; }
    }

    public static Snapshot read() {
        return read(EffectBuisness.INSTANCE.getTrades());
    }

    public static Snapshot read(Collection<EntityVillager.ITradeList> generators) {
        List<Offer> offers = new ArrayList<>();
        Set<EntityVillager.ITradeList> seen = Collections.newSetFromMap(new IdentityHashMap<>());
        for (EntityVillager.ITradeList generator : generators) {
            if (generator == null || !seen.add(generator)) continue;
            // Never run structure searches or create map data just to discover an ineligible offer.
            if (generator.getClass().getName().equals(TREASURE_MAP)
                    || generator.getClass() == EntityVillager.ListEnchantedBookForEmeralds.class) continue;
            try {
                if (!readStandard(generator, offers)) sample(generator, offers);
            } catch (RuntimeException | LinkageError failure) {
                CapnsBeeAddon.LOGGER.warn("Cannot display villager trade generator {}: {}",
                        generator.getClass().getName(), failure.toString());
            }
        }
        return new Snapshot(entries(offers, Mode.BUSINESS), entries(offers, Mode.JUNK_SELLER));
    }

    private static boolean readStandard(EntityVillager.ITradeList generator, List<Offer> offers) {
        // Exact classes: a modded subclass may override generation entirely.
        Class<?> type = generator.getClass();
        if (type == EntityVillager.EmeraldForItems.class) {
            EntityVillager.EmeraldForItems trade = (EntityVillager.EmeraldForItems) generator;
            offers.add(new Offer(new ItemStack(trade.buyingItem), Range.of(trade.price),
                    ItemStack.EMPTY, Range.ONE, new ItemStack(Items.EMERALD), Range.ONE, false, false));
        } else if (type == EntityVillager.ListItemForEmeralds.class) {
            EntityVillager.ListItemForEmeralds trade = (EntityVillager.ListItemForEmeralds) generator;
            Range price = Range.of(trade.priceInfo);
            // Negative prices buy multiple goods for one emerald. Zero creates an empty payment.
            ItemStack goods = new ItemStack(trade.itemToBuy.getItem(), 1, trade.itemToBuy.getMetadata());
            if (price.min < 0) offers.add(new Offer(new ItemStack(Items.EMERALD), Range.ONE,
                    ItemStack.EMPTY, Range.ONE, goods,
                    new Range(-Math.min(-1, price.max), -price.min), false, false));
            if (price.max > 0) offers.add(new Offer(new ItemStack(Items.EMERALD),
                    new Range(Math.max(1, price.min), price.max), ItemStack.EMPTY, Range.ONE,
                    goods, Range.ONE, false, false));
        } else if (type == EntityVillager.ListEnchantedItemForEmeralds.class) {
            EntityVillager.ListEnchantedItemForEmeralds trade = (EntityVillager.ListEnchantedItemForEmeralds) generator;
            // Enchanting a book creates StoredEnchantments NBT, which Junk Seller does not accept.
            if (trade.enchantedItemStack.getItem() == Items.BOOK) return true;
            offers.add(new Offer(new ItemStack(Items.EMERALD), Range.of(trade.priceInfo),
                    ItemStack.EMPTY, Range.ONE, new ItemStack(trade.enchantedItemStack.getItem(), 1,
                    trade.enchantedItemStack.getMetadata()), Range.ONE, true, false));
        } else if (type == EntityVillager.ItemAndEmeraldToItem.class) {
            EntityVillager.ItemAndEmeraldToItem trade = (EntityVillager.ItemAndEmeraldToItem) generator;
            offers.add(new Offer(new ItemStack(trade.buyingItemStack.getItem(), 1, trade.buyingItemStack.getMetadata()),
                    Range.of(trade.buyingPriceInfo), new ItemStack(Items.EMERALD), Range.ONE,
                    new ItemStack(trade.sellingItemstack.getItem(), 1, trade.sellingItemstack.getMetadata()),
                    Range.of(trade.sellingPriceInfo), false, false));
        } else if (type == VillagerTradeLists.GiveEmeraldForItems.class) {
            VillagerTradeLists.GiveEmeraldForItems trade = (VillagerTradeLists.GiveEmeraldForItems) generator;
            offers.add(new Offer(trade.buyingItem, Range.of(trade.price), ItemStack.EMPTY, Range.ONE,
                    new ItemStack(Items.EMERALD), Range.ONE, false, false));
        } else if (type == VillagerTradeLists.GiveItemForEmeralds.class) {
            VillagerTradeLists.GiveItemForEmeralds trade = (VillagerTradeLists.GiveItemForEmeralds) generator;
            offers.add(new Offer(new ItemStack(Items.EMERALD), Range.of(trade.emeraldPriceInfo),
                    ItemStack.EMPTY, Range.ONE, trade.itemToSell, Range.of(trade.sellInfo), false, false));
        } else if (type == VillagerTradeLists.GiveItemForItemAndEmerald.class) {
            VillagerTradeLists.GiveItemForItemAndEmerald trade = (VillagerTradeLists.GiveItemForItemAndEmerald) generator;
            offers.add(new Offer(trade.buyingItemStack, Range.of(trade.buyingPriceInfo),
                    new ItemStack(Items.EMERALD), Range.of(trade.emeraldPriceInfo),
                    trade.sellingItemstack, Range.of(trade.sellingPriceInfo), false, false));
        } else if (type == VillagerTradeLists.GiveItemForTwoItems.class) {
            VillagerTradeLists.GiveItemForTwoItems trade = (VillagerTradeLists.GiveItemForTwoItems) generator;
            offers.add(new Offer(trade.buyingItemStack, Range.of(trade.buyingPriceInfo),
                    trade.buyingItemStackTwo, Range.of(trade.buyingPriceItemTwoInfo),
                    trade.sellingItemstack, Range.of(trade.sellingPriceInfo), false, false));
        } else return false;
        return true;
    }

    private static void sample(EntityVillager.ITradeList generator, List<Offer> offers) {
        List<Offer> sampled = new ArrayList<>();
        Random random = new Random(0xCA971BEE);
        IMerchant merchant = new PreviewMerchant();
        try {
            for (int i = 0; i < SAMPLES; i++) {
                MerchantRecipeList generated = new MerchantRecipeList();
                generator.addMerchantRecipe(merchant, generated, random);
                for (MerchantRecipe recipe : generated) {
                    if (recipe == null || recipe.isRecipeDisabled()) continue;
                    ItemStack output = recipe.getItemToSell();
                    if (output.isEmpty()) continue;
                    boolean enchanted = output.isItemEnchanted();
                    Offer offer = new Offer(recipe.getItemToBuy(), Range.count(recipe.getItemToBuy()),
                            recipe.getSecondItemToBuy(), Range.count(recipe.getSecondItemToBuy()),
                            withoutEnchantments(output), Range.count(output), enchanted, true);
                    // Preserve distinct products/payments and inspect every generated offer, not only the first.
                    Offer matching = null;
                    for (Offer previous : sampled) if (previous.sameItems(offer)) { matching = previous; break; }
                    if (matching == null) sampled.add(offer);
                    else matching.merge(offer);
                }
            }
        } finally {
            offers.addAll(sampled);
        }
    }

    private static List<Entry> entries(List<Offer> offers, Mode mode) {
        Map<StackKey, Entry> grouped = new LinkedHashMap<>();
        for (Offer original : offers) {
            // An empty primary payment cannot become valid by combining it with a second payment.
            if (original.firstCount.max < 1) continue;
            Offer offer = original.normalized();
            if (offer.first.isEmpty() || offer.output.isEmpty() || offer.firstCount.min < 1
                    || offer.outputCount.min < 1 || (!offer.second.isEmpty() && offer.secondCount.min < 1)) continue;
            if (!mode.effect().isValidItemToSell(offer.output)
                    || !mode.effect().isValidItemToBuy(offer.first, offer.second.isEmpty() ? null : offer.second)) continue;
            if (!mode.isBusiness() && offer.output.isItemStackDamageable() && offer.output.getMaxDamage() == 1) continue;
            if (mode.isBusiness()) {
                // Forestry's blank princess requirement cannot match normal princess genetic NBT.
                // Rendering that placeholder gives it a default Forest genome and a misleading name.
                if (isBlankPrincess(offer.first) || isBlankPrincess(offer.second)) continue;
                // Normal signed books carry NBT and cannot match a blank written-book requirement.
                if (isBlankWrittenBook(offer.first) || isBlankWrittenBook(offer.second)) continue;
                addEntry(grouped, offer.first, offer);
                if (!offer.second.isEmpty()) addEntry(grouped, offer.second, offer);
            } else {
                ItemStack goods = withoutEnchantments(offer.output);
                if (goods.isItemStackDamageable()) goods.setItemDamage(0);
                addEntry(grouped, goods, offer);
            }
        }
        List<Entry> result = new ArrayList<>(grouped.values());
        for (Entry entry : result) entry.cacheAmountVariability(mode);
        result.sort(Comparator.comparing((Entry entry) -> String.valueOf(entry.stack.getItem().getRegistryName()))
                .thenComparingInt(entry -> entry.stack.getMetadata())
                .thenComparing(entry -> String.valueOf(entry.stack.getTagCompound())));
        return Collections.unmodifiableList(result);
    }

    private static void addEntry(Map<StackKey, Entry> grouped, ItemStack stack, Offer offer) {
        Entry entry = grouped.computeIfAbsent(new StackKey(stack), key -> new Entry(stack));
        for (Offer existing : entry.offers) if (existing.sameOffer(offer)) return;
        entry.offers.add(offer);
    }

    private static boolean onlyEnchantments(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.getKeySet().size() == 1 && tag.hasKey("ench", 9);
    }

    private static ItemStack withoutEnchantments(ItemStack stack) {
        ItemStack copy = stack.copy();
        if (onlyEnchantments(copy)) copy.setTagCompound(null);
        return copy;
    }

    private static ItemStack unit(ItemStack stack) {
        ItemStack copy = stack.copy();
        if (!copy.isEmpty()) copy.setCount(1);
        return copy;
    }

    private static boolean isBlankPrincess(ItemStack stack) {
        return !stack.hasTagCompound() && stack.getItem() instanceof ItemBeeGE
                && ((ItemBeeGE) stack.getItem()).getType() == EnumBeeType.PRINCESS;
    }

    private static boolean isBlankWrittenBook(ItemStack stack) {
        return !stack.hasTagCompound() && stack.getItem() == Items.WRITTEN_BOOK;
    }

    public static final class Snapshot {
        private final List<Entry> business;
        private final List<Entry> junk;

        private Snapshot(List<Entry> business, List<Entry> junk) {
            this.business = business;
            this.junk = junk;
        }

        public List<Entry> get(Mode mode) { return mode.isBusiness() ? business : junk; }
    }

    public static final class Entry {
        private final ItemStack stack;
        private final List<Offer> offers = new ArrayList<>();
        private boolean amountsMayVary;

        private Entry(ItemStack stack) { this.stack = unit(stack); }

        public ItemStack getStack(Mode mode) {
            ItemStack display = mode.isBusiness() ? stack.copy() : mode.effect().getStack(new Random(0), stack.copy());
            display.setCount(getTypicalAmount(mode));
            return display;
        }

        /** Representative batch size across this item's offers, rather than a production-rate estimate. */
        public int getTypicalAmount(Mode mode) {
            double total = 0;
            for (Offer offer : offers) {
                Range amount = amountOf(offer, mode);
                total += ((double) amount.min + amount.max) / 2;
            }
            return offers.isEmpty() ? 1 : (int) Math.max(1, Math.round(total / offers.size()));
        }

        public boolean amountsMayVary() { return amountsMayVary; }

        private void cacheAmountVariability(Mode mode) {
            int fixedAmount = -1;
            for (Offer offer : offers) {
                Range amount = amountOf(offer, mode);
                // Sampling cannot establish that an unfamiliar generator always returns a fixed quantity.
                if (offer.sampled || amount.min != amount.max || (fixedAmount >= 0 && fixedAmount != amount.min)) {
                    amountsMayVary = true;
                    return;
                }
                fixedAmount = amount.min;
            }
        }

        private Range amountOf(Offer offer, Mode mode) {
            return !mode.isBusiness() ? offer.outputCount
                    : ItemHandlerHelper.canItemStacksStack(stack, offer.first) ? offer.firstCount : offer.secondCount;
        }

        public List<Offer> getOffers() { return Collections.unmodifiableList(offers); }

        public boolean matches(ItemStack candidate, Mode mode) {
            if (candidate.isEmpty() || candidate.getItem() != stack.getItem()) return false;
            if (mode.isBusiness()) return ItemHandlerHelper.canItemStacksStack(stack, candidate);
            if (!mode.effect().isValidItemToSell(candidate)) return false;
            // Junk Seller chooses durability after selecting the offer; enchanted equipment is indexed by type.
            ItemStack normalized = withoutEnchantments(candidate);
            if (stack.isItemStackDamageable()) normalized.setItemDamage(stack.getItemDamage());
            return ItemHandlerHelper.canItemStacksStack(stack, normalized);
        }
    }

    public static final class Offer {
        public final ItemStack first;
        public final ItemStack second;
        public final ItemStack output;
        public Range firstCount;
        public Range secondCount;
        public Range outputCount;
        public final boolean enchanted;
        public final boolean sampled;

        private Offer(ItemStack first, Range firstCount, ItemStack second, Range secondCount,
                      ItemStack output, Range outputCount, boolean enchanted, boolean sampled) {
            this.first = unit(first);
            this.second = unit(second);
            this.output = unit(output);
            // Counts are already decoded here; negative villager prices are handled by the readers.
            this.firstCount = firstCount.positive();
            this.secondCount = secondCount.positive();
            this.outputCount = outputCount.positive();
            this.enchanted = enchanted;
            this.sampled = sampled;
        }

        private Offer normalized() {
            if (!second.isEmpty() && ItemHandlerHelper.canItemStacksStack(first, second)) {
                return new Offer(first, new Range(firstCount.min + secondCount.min, firstCount.max + secondCount.max),
                        ItemStack.EMPTY, Range.ONE, output, outputCount, enchanted, sampled);
            }
            return this;
        }

        private boolean sameItems(Offer other) {
            return new StackKey(first).equals(new StackKey(other.first))
                    && new StackKey(second).equals(new StackKey(other.second))
                    && new StackKey(output).equals(new StackKey(other.output)) && enchanted == other.enchanted;
        }

        private boolean sameOffer(Offer other) {
            return sameItems(other) && firstCount.min == other.firstCount.min && firstCount.max == other.firstCount.max
                    && secondCount.min == other.secondCount.min && secondCount.max == other.secondCount.max
                    && outputCount.min == other.outputCount.min && outputCount.max == other.outputCount.max
                    && sampled == other.sampled;
        }

        private void merge(Offer other) {
            firstCount = firstCount.union(other.firstCount);
            secondCount = secondCount.union(other.secondCount);
            outputCount = outputCount.union(other.outputCount);
        }

        public Range payout() {
            return new Range(outputCount.min - outputCount.min / 2,
                    outputCount.max - (outputCount.max > 1 ? 1 : 0));
        }
    }

    public static final class Range {
        private static final Range ONE = new Range(1, 1);
        public final int min;
        public final int max;

        private Range(int min, int max) { this.min = min; this.max = max; }

        private static Range of(@Nullable EntityVillager.PriceInfo price) {
            return price == null ? ONE : new Range(price.getFirst(), Math.max(price.getFirst(), price.getSecond()));
        }

        private static Range count(ItemStack stack) { return new Range(stack.getCount(), stack.getCount()); }

        // Keep wholly nonpositive ranges invalid instead of inventing a one-item trade.
        private Range positive() { return min < 1 && max >= 1 ? new Range(1, max) : this; }

        private Range union(Range other) { return new Range(Math.min(min, other.min), Math.max(max, other.max)); }

        @Override
        public String toString() { return min == max ? String.valueOf(min) : min + "-" + max; }
    }

    private static final class StackKey {
        private final ItemStack stack;

        private StackKey(ItemStack stack) { this.stack = unit(stack); }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof StackKey)) return false;
            ItemStack candidate = ((StackKey) other).stack;
            return stack.isEmpty() ? candidate.isEmpty() : !candidate.isEmpty()
                    && ItemHandlerHelper.canItemStacksStack(stack, candidate);
        }

        @Override
        public int hashCode() {
            return stack.isEmpty() ? 0 : 31 * System.identityHashCode(stack.getItem()) + stack.getMetadata();
        }
    }

    /** No actual world is supplied to arbitrary generators during JEI registration. */
    private static final class PreviewMerchant implements IMerchant {
        @Override @Nullable public EntityPlayer getCustomer() { return null; }
        @Override public void setCustomer(@Nullable EntityPlayer player) { }
        @Override @Nullable public MerchantRecipeList getRecipes(EntityPlayer player) { return null; }
        @Override public void setRecipes(@Nullable MerchantRecipeList recipes) { }
        @Override public void useRecipe(MerchantRecipe recipe) { }
        @Override public void verifySellingItem(ItemStack stack) { }
        @Override public ITextComponent getDisplayName() { return new TextComponentString("Bee trade preview"); }
        @Override @Nullable public World getWorld() { return null; }
        @Override public BlockPos getPos() { return BlockPos.ORIGIN; }
    }
}
