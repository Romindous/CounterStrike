package me.Romindous.CounterStrike.Objects.Loc;

import java.util.Set;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.VoxelShape;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.version.Nms;

public interface Info {

    Set<BlockType> BREAKABLE = Set.of(BlockType.OAK_LEAVES, BlockType.ACACIA_LEAVES, BlockType.BIRCH_LEAVES, BlockType.JUNGLE_LEAVES, BlockType.CHERRY_LEAVES,
        BlockType.SPRUCE_LEAVES, BlockType.DARK_OAK_LEAVES, BlockType.MANGROVE_LEAVES, BlockType.AZALEA_LEAVES, BlockType.FLOWERING_AZALEA_LEAVES,
        BlockType.GLASS, BlockType.WHITE_STAINED_GLASS, BlockType.GLASS_PANE, BlockType.WHITE_STAINED_GLASS_PANE, BlockType.FLOWER_POT, BlockType.DECORATED_POT,
        BlockType.DIAMOND_ORE, BlockType.COAL_ORE, BlockType.IRON_ORE, BlockType.EMERALD_ORE);
    Set<BlockType> PASSABLE = Set.of(BlockType.AIR, BlockType.CAVE_AIR, BlockType.VOID_AIR, BlockType.POWDER_SNOW,
        BlockType.SEAGRASS, BlockType.TALL_SEAGRASS, BlockType.WEEPING_VINES, BlockType.SOUL_FIRE, BlockType.FIRE, BlockType.TWISTING_VINES,

        BlockType.BLACK_CARPET, BlockType.BLUE_CARPET, BlockType.BROWN_CARPET, BlockType.CYAN_CARPET, BlockType.GRAY_CARPET,
        BlockType.GREEN_CARPET, BlockType.LIGHT_BLUE_CARPET, BlockType.LIGHT_GRAY_CARPET, BlockType.LIME_CARPET,
        BlockType.MAGENTA_CARPET, BlockType.MOSS_CARPET, BlockType.ORANGE_CARPET, BlockType.PINK_CARPET,
        BlockType.PURPLE_CARPET, BlockType.RED_CARPET, BlockType.WHITE_CARPET, BlockType.YELLOW_CARPET,

        BlockType.WATER, BlockType.IRON_BARS, BlockType.CHAIN, BlockType.STRUCTURE_VOID, BlockType.COBWEB, BlockType.SNOW,
        BlockType.BARRIER, BlockType.TRIPWIRE, BlockType.LADDER, BlockType.RAIL, BlockType.POWERED_RAIL, BlockType.LAVA,
        BlockType.DETECTOR_RAIL, BlockType.ACTIVATOR_RAIL, BlockType.CAMPFIRE, BlockType.SOUL_CAMPFIRE);
    Set<BlockType> BANGABLE = Set.of(BlockType.ACACIA_SLAB, BlockType.BIRCH_SLAB, BlockType.CRIMSON_SLAB, BlockType.SPRUCE_SLAB, BlockType.WARPED_SLAB, BlockType.CHERRY_SLAB, BlockType.BAMBOO_SLAB,
        BlockType.DARK_OAK_SLAB, BlockType.OAK_SLAB, BlockType.JUNGLE_SLAB, BlockType.PETRIFIED_OAK_SLAB, BlockType.MANGROVE_SLAB, BlockType.BAMBOO_MOSAIC_SLAB, BlockType.PALE_OAK_SLAB,

        BlockType.ACACIA_STAIRS, BlockType.BIRCH_STAIRS, BlockType.CRIMSON_STAIRS, BlockType.SPRUCE_STAIRS, BlockType.CHERRY_STAIRS, BlockType.BAMBOO_STAIRS, BlockType.PALE_OAK_STAIRS,
        BlockType.WARPED_STAIRS, BlockType.DARK_OAK_STAIRS, BlockType.OAK_STAIRS, BlockType.JUNGLE_STAIRS, BlockType.MANGROVE_STAIRS, BlockType.BAMBOO_MOSAIC_STAIRS,

        BlockType.ACACIA_PLANKS, BlockType.BIRCH_PLANKS, BlockType.CRIMSON_PLANKS, BlockType.SPRUCE_PLANKS, BlockType.CHERRY_PLANKS, BlockType.BAMBOO_PLANKS, BlockType.PALE_OAK_PLANKS,
        BlockType.WARPED_PLANKS, BlockType.DARK_OAK_PLANKS, BlockType.OAK_PLANKS, BlockType.JUNGLE_PLANKS, BlockType.MANGROVE_PLANKS, BlockType.BAMBOO_MOSAIC,

        BlockType.ACACIA_TRAPDOOR, BlockType.BIRCH_TRAPDOOR, BlockType.CRIMSON_TRAPDOOR, BlockType.DARK_OAK_TRAPDOOR, BlockType.CHERRY_TRAPDOOR, BlockType.BAMBOO_TRAPDOOR,
        BlockType.JUNGLE_TRAPDOOR, BlockType.MANGROVE_TRAPDOOR, BlockType.OAK_TRAPDOOR, BlockType.SPRUCE_TRAPDOOR, BlockType.WARPED_TRAPDOOR, BlockType.PALE_OAK_TRAPDOOR,

        BlockType.ACACIA_WOOD, BlockType.BIRCH_WOOD, BlockType.CRIMSON_HYPHAE, BlockType.SPRUCE_WOOD, BlockType.CHERRY_WOOD, BlockType.BAMBOO_BLOCK,
        BlockType.WARPED_HYPHAE, BlockType.DARK_OAK_WOOD, BlockType.OAK_WOOD, BlockType.JUNGLE_WOOD, BlockType.MANGROVE_WOOD, BlockType.PALE_OAK_WOOD,

        BlockType.ACACIA_LOG, BlockType.BIRCH_LOG, BlockType.CRIMSON_STEM, BlockType.SPRUCE_LOG, BlockType.CHERRY_LOG, BlockType.BAMBOO,
        BlockType.WARPED_STEM, BlockType.DARK_OAK_LOG, BlockType.OAK_LOG, BlockType.JUNGLE_LOG, BlockType.MANGROVE_LOG, BlockType.PALE_OAK_LOG,

        BlockType.ACACIA_SIGN, BlockType.ACACIA_WALL_SIGN, BlockType.BIRCH_SIGN, BlockType.BIRCH_WALL_SIGN, BlockType.CRIMSON_SIGN, BlockType.BAMBOO_SIGN,
        BlockType.CRIMSON_WALL_SIGN, BlockType.SPRUCE_SIGN, BlockType.SPRUCE_WALL_SIGN, BlockType.WARPED_SIGN, BlockType.PALE_OAK_SIGN, BlockType.WARPED_WALL_SIGN,
        BlockType.DARK_OAK_SIGN, BlockType.DARK_OAK_WALL_SIGN, BlockType.OAK_SIGN, BlockType.CHERRY_SIGN, BlockType.CHERRY_WALL_SIGN, BlockType.BAMBOO_WALL_SIGN,
        BlockType.OAK_WALL_SIGN, BlockType.JUNGLE_SIGN, BlockType.JUNGLE_WALL_SIGN, BlockType.MANGROVE_SIGN, BlockType.MANGROVE_WALL_SIGN, BlockType.PALE_OAK_WALL_SIGN,

        BlockType.STRIPPED_ACACIA_WOOD, BlockType.STRIPPED_BIRCH_WOOD, BlockType.STRIPPED_CRIMSON_HYPHAE, BlockType.STRIPPED_SPRUCE_WOOD,
        BlockType.STRIPPED_WARPED_HYPHAE, BlockType.STRIPPED_DARK_OAK_WOOD, BlockType.STRIPPED_OAK_WOOD, BlockType.STRIPPED_JUNGLE_WOOD,
        BlockType.STRIPPED_MANGROVE_WOOD, BlockType.STRIPPED_CHERRY_WOOD, BlockType.STRIPPED_BAMBOO_BLOCK, BlockType.STRIPPED_PALE_OAK_WOOD,

        BlockType.STRIPPED_ACACIA_LOG, BlockType.STRIPPED_BIRCH_LOG, BlockType.STRIPPED_CRIMSON_STEM, BlockType.STRIPPED_SPRUCE_LOG,
        BlockType.STRIPPED_WARPED_STEM, BlockType.STRIPPED_DARK_OAK_LOG, BlockType.STRIPPED_OAK_LOG, BlockType.STRIPPED_JUNGLE_LOG,
        BlockType.STRIPPED_MANGROVE_LOG, BlockType.STRIPPED_CHERRY_LOG, BlockType.STRIPPED_PALE_OAK_LOG,

        BlockType.ACACIA_FENCE, BlockType.BIRCH_FENCE, BlockType.CRIMSON_FENCE, BlockType.SPRUCE_FENCE, BlockType.WARPED_FENCE, BlockType.DARK_OAK_FENCE,
        BlockType.OAK_FENCE, BlockType.JUNGLE_FENCE, BlockType.MANGROVE_FENCE, BlockType.CHERRY_FENCE, BlockType.BAMBOO_FENCE, BlockType.PALE_OAK_FENCE,

        BlockType.ACACIA_FENCE_GATE, BlockType.BIRCH_FENCE_GATE, BlockType.CRIMSON_FENCE_GATE, BlockType.CHERRY_FENCE_GATE, BlockType.BAMBOO_FENCE_GATE, BlockType.PALE_OAK_FENCE_GATE,
        BlockType.SPRUCE_FENCE_GATE, BlockType.WARPED_FENCE_GATE, BlockType.DARK_OAK_FENCE_GATE, BlockType.OAK_FENCE_GATE, BlockType.JUNGLE_FENCE_GATE, BlockType.MANGROVE_FENCE_GATE,

        BlockType.OAK_DOOR, BlockType.ACACIA_DOOR, BlockType.BIRCH_DOOR, BlockType.CRIMSON_DOOR, BlockType.DARK_OAK_DOOR, BlockType.CHERRY_DOOR,
        BlockType.JUNGLE_DOOR, BlockType.MANGROVE_DOOR, BlockType.WARPED_DOOR, BlockType.SPRUCE_DOOR, BlockType.BAMBOO_DOOR, BlockType.PALE_OAK_DOOR,

        BlockType.OAK_HANGING_SIGN, BlockType.ACACIA_HANGING_SIGN, BlockType.BIRCH_HANGING_SIGN, BlockType.CRIMSON_HANGING_SIGN, BlockType.DARK_OAK_HANGING_SIGN, BlockType.CHERRY_HANGING_SIGN,
        BlockType.JUNGLE_HANGING_SIGN, BlockType.MANGROVE_HANGING_SIGN, BlockType.WARPED_HANGING_SIGN, BlockType.SPRUCE_HANGING_SIGN, BlockType.BAMBOO_HANGING_SIGN, BlockType.PALE_OAK_HANGING_SIGN,

        BlockType.OAK_WALL_HANGING_SIGN, BlockType.ACACIA_WALL_HANGING_SIGN, BlockType.BIRCH_WALL_HANGING_SIGN, BlockType.CRIMSON_WALL_HANGING_SIGN,
        BlockType.DARK_OAK_WALL_HANGING_SIGN, BlockType.CHERRY_WALL_HANGING_SIGN, BlockType.JUNGLE_WALL_HANGING_SIGN, BlockType.MANGROVE_WALL_HANGING_SIGN,
        BlockType.WARPED_WALL_HANGING_SIGN, BlockType.SPRUCE_WALL_HANGING_SIGN, BlockType.BAMBOO_WALL_HANGING_SIGN, BlockType.PALE_OAK_WALL_HANGING_SIGN,

        BlockType.BARREL, BlockType.BEEHIVE, BlockType.BEE_NEST, BlockType.NOTE_BLOCK, BlockType.JUKEBOX, BlockType.CRAFTING_TABLE);

    Info SNOW = new Info() {
        public boolean smoke() {return true;}
        public boolean broke() {return false;}
        public boolean pass() {return true;}
        public boolean bang() {return false;}
        public boolean collides() {return true;}
        public BlockType type() {return null;}
        public BlockData data() {return null;}
        public VoxelShape shape() {return null;}
    };

    Info PASS_AIR = new Info() {
        public boolean smoke() {return false;}
        public boolean broke() {return false;}
        public boolean pass() {return true;}
        public boolean bang() {return true;}
        public boolean collides() {return false;}
        public BlockType type() {return null;}
        public BlockData data() {return null;}
        public VoxelShape shape() {return null;}
    };

    Info PASS_PHYS = new Info() {
        public boolean smoke() {return false;}
        public boolean broke() {return false;}
        public boolean pass() {return true;}
        public boolean bang() {return true;}
        public boolean collides() {return true;}
        public BlockType type() {return null;}
        public BlockData data() {return null;}
        public VoxelShape shape() {return null;}
    };

    boolean smoke();
    boolean broke();
    boolean pass();
    boolean bang();
    boolean collides();
    BlockType type();
    BlockData data();
    VoxelShape shape();

    static Info of(final World w, final BVec bv) {
        final BlockType bt = Nms.fastType(w, bv);
        if (bt == BlockType.POWDER_SNOW) return SNOW;
        if (PASSABLE.contains(bt))
            return bt.hasCollision() ? PASS_PHYS : PASS_AIR;
        if (BREAKABLE.contains(bt))
            return new DataInfo(bt, Nms.fastData(w, bv));
        return new BlockInfo(bt, w.getBlockAt(bv.x, bv.y, bv.z));
    }

    class DataInfo implements Info {
        private final BlockType type;
        private final BlockData data;
        private DataInfo(final BlockType bt, final BlockData bd) {
            this.type = bt; this.data = bd;
        }
        public boolean smoke() {return false;}
        public boolean broke() {return true;}
        public boolean pass() {return true;}
        public boolean bang() {return true;}
        public boolean collides() {return true;}
        public BlockType type() {return type;}
        public BlockData data() {return data;}
        public VoxelShape shape() {return null;}
    }

    class BlockInfo implements Info {
        private final boolean bang;
        private final BlockType type;
        private final BlockData data;
        private final VoxelShape shape;
        private BlockInfo(final BlockType bt, final Block bl) {
            this.bang = BANGABLE.contains(bt);
            this.type = bt; this.data = bl.getBlockData();
            this.shape = bl.getCollisionShape();
        }
        public boolean smoke() {return false;}
        public boolean broke() {return false;}
        public boolean pass() {return false;}
        public boolean bang() {return bang;}
        public boolean collides() {return true;}
        public BlockType type() {return type;}
        public BlockData data() {return data;}
        public VoxelShape shape() {return shape;}
    }

}
