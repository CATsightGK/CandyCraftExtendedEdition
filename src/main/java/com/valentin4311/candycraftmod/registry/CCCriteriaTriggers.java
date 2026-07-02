package com.valentin4311.candycraftmod.registry;

import com.google.gson.JsonObject;
import com.valentin4311.candycraftmod.CandyCraft;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class CCCriteriaTriggers {
    public static final CandyTrigger EAT_BLOCK = register("eat_block");
    public static final CandyTrigger HEAL_CANDY_WOLF = register("heal_candy_wolf");
    public static final CandyTrigger STALL_CANDY_CREEPER = register("stall_candy_creeper");
    public static final CandyTrigger TAME_CANDY_WOLF = register("tame_candy_wolf");

    private CCCriteriaTriggers() {
    }

    public static void register() {
        // Loads the static trigger fields.
    }

    private static CandyTrigger register(String name) {
        return CriteriaTriggers.register(new CandyTrigger(new ResourceLocation(CandyCraft.MODID, name)));
    }

    public static class CandyTrigger extends SimpleCriterionTrigger<CandyTrigger.Instance> {
        private final ResourceLocation id;

        CandyTrigger(ResourceLocation id) {
            this.id = id;
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        protected Instance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext context) {
            return new Instance(id, player);
        }

        public void trigger(ServerPlayer player) {
            trigger(player, instance -> true);
        }

        public static class Instance extends AbstractCriterionTriggerInstance {
            Instance(ResourceLocation id, ContextAwarePredicate player) {
                super(id, player);
            }
        }
    }
}
