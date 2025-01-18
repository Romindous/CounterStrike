package me.Romindous.CounterStrike.Objects;

import javax.annotation.Nullable;
import me.Romindous.CounterStrike.Game.Arena;
import net.kyori.adventure.key.Key;
import org.bukkit.World;
import org.bukkit.block.Block;
import ru.komiss77.modules.world.WXYZ;
import ru.komiss77.modules.world.XYZ;

public abstract class Defusable extends WXYZ {

    public static final String OFF_CLR = "off", PLANT = "plant", KIT_MDL = PLANT + "/defuse",
        PLIERS_MDL = PLANT + "/pliers", BOMB_MDL = PLANT + "/bomb", WIRE = "wire", DISP = "disp";
    public static final String[] COLORS = {"blue", "green", "red", "yellow"};

    public static Key disp(final String clr) {
        return Key.key(PLANT + "/" + DISP + "/" + clr);
    }

    public static Key wire(final String clr) {
        return Key.key(PLANT + "/" + WIRE + "/" + clr);
    }

    public Defusable(final Block b) {
        super(b.getWorld(), b.getX(), b.getY(), b.getZ());
    }
    public Defusable(final World w, final XYZ loc) {
        super(w, loc);
    }

    public abstract Arena arena();

    public abstract @Nullable Shooter defusing();

    public abstract void defusing(final @Nullable Shooter sh);
}
