package net.citizensnpcs.npc.entity;

import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.MobEntityController;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.NMS;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_8_R1.Block;
import net.minecraft.server.v1_8_R1.BlockPosition;
import net.minecraft.server.v1_8_R1.EntityHuman;
import net.minecraft.server.v1_8_R1.EntityVillager;
import net.minecraft.server.v1_8_R1.NBTTagCompound;
import net.minecraft.server.v1_8_R1.World;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R1.CraftServer;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftVillager;
import org.bukkit.entity.Villager;
import org.bukkit.util.Vector;

public class VillagerController extends MobEntityController {
    public VillagerController() {
        super(EntityVillagerNPC.class);
    }

    @Override
    public Villager getBukkitEntity() {
        return (Villager) super.getBukkitEntity();
    }

    public static class EntityVillagerNPC extends EntityVillager implements NPCHolder {
        private boolean blockTrades = true;
        private final CitizensNPC npc;

        public EntityVillagerNPC(World world) {
            this(world, null);
        }

        public EntityVillagerNPC(World world, NPC npc) {
            super(world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                NMS.clearGoals(goalSelector, targetSelector);
            }
        }

        @Override
        public void a(boolean flag) {
            float oldw = width;
            float oldl = length;
            super.a(flag);
            if (oldw != width || oldl != length) {
                this.setPosition(locX - 0.01, locY, locZ - 0.01);
                this.setPosition(locX + 0.01, locY, locZ + 0.01);
            }
        }

        @Override
        protected void a(double d0, boolean flag, Block block, BlockPosition blockposition) {
            if (npc == null || !npc.isFlyable()) {
                super.a(d0, flag, block, blockposition);
            }
        }

        @Override
        public boolean a(EntityHuman entityhuman) {
            return npc == null || !blockTrades ? super.a(entityhuman) : false; // block
            // trades
        }

        @Override
        protected String bn() {
            return npc == null ? super.bn() : npc.data().get(NPC.HURT_SOUND_METADATA, super.bn());
        }

        @Override
        protected String bo() {
            return npc == null ? super.bo() : npc.data().get(NPC.DEATH_SOUND_METADATA, super.bo());
        }

        @Override
        public boolean cb() {
            if (npc == null)
                return super.cb();
            boolean protectedDefault = npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true);
            if (!protectedDefault || !npc.data().get(NPC.LEASH_PROTECTED_METADATA, protectedDefault))
                return super.cb();
            if (super.cb()) {
                unleash(true, false); // clearLeash with client update
            }
            return false; // shouldLeash
        }

        @Override
        public void collide(net.minecraft.server.v1_8_R1.Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.collide(entity);
            if (npc != null) {
                Util.callCollisionEvent(npc, entity.getBukkitEntity());
            }
        }

        @Override
        public boolean d(NBTTagCompound save) {
            return npc == null ? super.d(save) : false;
        }

        @Override
        protected void D() {
            if (npc == null) {
                super.D();
            }
        }

        @Override
        public void doTick() {
            super.doTick();
            if (npc != null) {
                npc.update();
            }
        }

        @Override
        public void e(float f, float f1) {
            if (npc == null || !npc.isFlyable()) {
                super.e(f, f1);
            }
        }

        @Override
        public void g(double x, double y, double z) {
            if (npc == null) {
                super.g(x, y, z);
                return;
            }
            if (NPCPushEvent.getHandlerList().getRegisteredListeners().length == 0) {
                if (!npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true))
                    super.g(x, y, z);
                return;
            }
            Vector vector = new Vector(x, y, z);
            NPCPushEvent event = Util.callPushEvent(npc, vector);
            if (!event.isCancelled()) {
                vector = event.getCollisionVector();
                super.g(vector.getX(), vector.getY(), vector.getZ());
            }
            // when another entity collides, this method is called to push the
            // NPC so we prevent it from doing anything if the event is
            // cancelled.
        }

        @Override
        public void g(float f, float f1) {
            if (npc == null || !npc.isFlyable()) {
                super.g(f, f1);
            } else {
                NMS.flyingMoveLogic(this, f, f1);
            }
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (bukkitEntity == null && npc != null)
                bukkitEntity = new VillagerNPC(this);
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        public boolean isBlockingTrades() {
            return blockTrades;
        }

        @Override
        public boolean j_() {
            if (npc == null || !npc.isFlyable()) {
                return super.j_();
            } else {
                return false;
            }
        }

        public void setBlockTrades(boolean blocked) {
            this.blockTrades = blocked;
        }

        @Override
        protected String z() {
            return npc == null || !npc.data().has(NPC.AMBIENT_SOUND_METADATA) ? super.z() : npc.data().get(
                    NPC.AMBIENT_SOUND_METADATA, super.z());
        }
    }

    public static class VillagerNPC extends CraftVillager implements NPCHolder {
        private final CitizensNPC npc;

        public VillagerNPC(EntityVillagerNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }
}