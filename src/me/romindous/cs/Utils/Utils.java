package me.romindous.cs.Utils;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Set;
import io.papermc.paper.datacomponent.DataComponentTypes;
import me.romindous.cs.Enums.GunType;
import me.romindous.cs.Main;
import me.romindous.cs.Objects.Loc.Info;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.items.ItemBuilder;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.utils.EntityUtil;
import ru.komiss77.utils.ItemUtil;
import ru.komiss77.utils.LocUtil;
import ru.komiss77.utils.ScreenUtil;
import ru.komiss77.version.Nms;

public class Utils {

    private static int id = 0;

    public static void sendTtlSbTtl(final Player p, final String ttl, final String sbttl, final int tm) {
        ScreenUtil.sendTitleDirect(p, ttl, sbttl, 4, tm, 20);
    }

    public static void sendTtl(final Player p, final String ttl, final int tm) {
        ScreenUtil.sendTitleDirect(p, ttl, "", 4, tm, 20);
    }

    public static void sendSbTtl(final Player p, final String sbttl, final int tm) {
        ScreenUtil.sendTitleDirect(p, "", sbttl, 4, tm, 20);
    }

    public static void sendAcBr(final Player p, final String msg) {
        ScreenUtil.sendActionBarDirect(p, msg);
    }

    public static void crackBlock(final World w, final BVec bl) {
        final Location loc = bl.center(w);
        final float stg = getNxtStg(bl.w(w));
        w.getPlayers().forEach(p -> p.sendBlockDamage(loc, stg,
            (id == 1000 ? id = 0 : id++) + 10000));
    }

    private static final int MAX_CRACK = 7;
    private static final float CRACK_DEL = 1f / MAX_CRACK;
    public static float getNxtStg(final BVec bl) {
        try {
            for (final BVec s : Main.cracks) {
                if (s.distAbs(bl) == 0) {
                    final byte[] vals = s.vals();
                    if (vals.length == 0) continue;
                    if (vals[0] != MAX_CRACK) vals[0]++;
                    return vals[0] * CRACK_DEL;
                }
            }
        } catch (final ConcurrentModificationException e) {
            return 0.4f;
        }
        Main.cracks.add(bl.wals(bl.wname(), new byte[]{1}));
        return 0.1f;
    }

    public static boolean isSeen(final Location from, final LivingEntity le) {
        return LocUtil.trace(from, EntityUtil.center(le).subtract(from).toVector(),
            (bp, bd) -> !seeThru(bd.getMaterial().asBlockType())).endDst();
    }

    private static final Set<BlockType> THRU = getThru();
    private static Set<BlockType> getThru() {
        final Set<BlockType> all = new HashSet<>(Info.PASSABLE);
        all.addAll(Info.BREAKABLE); all.addAll(Info.BANGABLE);
        final Set<BlockType> thru = new HashSet<>();
        for (final BlockType bt : Ostrov.registries.BLOCKS) {
            if ((!bt.isOccluding() && all.contains(bt))
                || !bt.hasCollision()) thru.add(bt);
        }
        return thru;
    }

    private static boolean seeThru(final BlockType bt) {
        return THRU.contains(bt);
    }

    public static void spy(final Player pl, final boolean snp, final boolean snk) {
        final PlayerInventory pi = pl.getInventory();
        if (!snk) {
            Nms.zoom(pl, 0f);
            final ItemStack it = pi.getItemInMainHand();
            if (!it.isEmpty()) it.setData(DataComponentTypes
                .CHARGED_PROJECTILES, GunType.CHARGE);
            pl.updateInventory();
            pi.setItemInOffHand(snp ? Main.spy : ItemUtil.air);
            return;
        }
        if (!snp) {
            Nms.zoom(pl, 0f);
            pi.setItemInOffHand(ItemUtil.air);
            final ItemStack it = pi.getItemInMainHand();
            if (!it.isEmpty()) it.setData(DataComponentTypes
                .CHARGED_PROJECTILES, GunType.CHARGE);
            pl.updateInventory();
            return;
        }
        pi.setItemInOffHand(Main.spy);
        final ItemStack it = pi.getItemInMainHand();
        if (!it.isEmpty()) Nms.fakeItem(pl, new ItemBuilder(it)
            .reset(DataComponentTypes.CHARGED_PROJECTILES).build(),
            pi.getHeldItemSlot());
        Nms.zoom(pl, 10f);
    }
}