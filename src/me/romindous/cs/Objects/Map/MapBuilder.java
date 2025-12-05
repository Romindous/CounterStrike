package me.romindous.cs.Objects.Map;

import java.io.File;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;
import me.romindous.cs.Enums.GameType;
import me.romindous.cs.Enums.TileSet;
import me.romindous.cs.Enums.TileType;
import me.romindous.cs.Game.Arena;
import me.romindous.cs.Main;
import me.romindous.cs.Objects.Loc.PasteSet;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.world.BVec;
import ru.komiss77.modules.world.Schematic;
import ru.komiss77.modules.world.Schematic.Rotate;
import ru.komiss77.utils.BlockUtil;
import ru.komiss77.utils.ClassUtil;
import ru.komiss77.utils.NumUtil;
import ru.komiss77.version.Nms;

public class MapBuilder {
	
	public final Setup stp;
	private final int[] stairs;
	private final GameType type;
	private final BlockType ceilType;
	private final boolean genCeiling;
	private final BVec mapDims, cellDims;
	private final BVec[] Tspawn;
	private final BVec[] CTspawn;
	private BVec[] spots;
	public final LinkedList<PasteSet> sets = new LinkedList<>();
	
	//	private Area mar;
	private BVec origin;
    private BVec Asite;
    private BVec Bsite;

	public static final BlockType dftCeilMat = BlockType.STONE;
	public static final BVec dftMapDims = BVec.of(16, 1, 20);
	public static final BVec dftCellDims = BVec.of(5, 8, 5);
	public static final int maxCheckDist = TileType.getNoiseDiff();
	public static final TileType[] etl = new TileType[0];
	public static final int encodeBits = 6;
	
	public MapBuilder(final Setup stp, GameType type) {
		this.stp = stp;
		this.type = type;
		this.genCeiling = false;
		this.cellDims = dftCellDims;
		this.mapDims = stp.dims == null ? dftMapDims : stp.dims;
		this.ceilType = stp.ceil == null ? dftCeilMat : stp.ceil;
		this.Asite = null;
		this.Bsite = null;
		this.stairs = new int[4];
		this.Tspawn = new BVec[8];
		this.CTspawn = new BVec[8];
		this.spots = new BVec[0];
	}
	
	public BVec getASite() {
		return this.Asite;
	}
	
	public BVec getBSite() {
		return this.Bsite;
	}
	
	public BVec[] getTSpawns() {
		return this.Tspawn;
	}
	
	public BVec[] getCTSpawns() {
		return this.CTspawn;
	}

	public BVec[] getSpots() {
		return this.spots;
	}
	
	public void build(final Location cntr) {
		final int dX = mapDims.x, dY = mapDims.y, dZ = mapDims.z;
		final int cX = cellDims.x, cZ = cellDims.z;
		final Location loc = cntr.clone().subtract(dX * cX >> 1, 0d, dZ * cZ >> 1);
		origin = BVec.of(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

		final int rx = Main.srnd.nextInt((dX >> 3) + 1) + 2,
		rz = Main.srnd.nextInt((dZ >> 3) + 1) + 2;
		
		Asite = origin.add((rx + 2) * cX + 2, type == GameType.INVASION ? 1 : 3, (dZ - rz - 3) * cZ + 2);
		Bukkit.getConsoleSender().sendMessage("ast-(" + (rx + 2) + ", " + (dZ - rz - 3) + ")");
		Bsite = origin.add((dX - rx - 3) * cX + 2, type == GameType.INVASION ? 1 : 3, (rz + 2) * cZ + 2);
		Bukkit.getConsoleSender().sendMessage("bst-(" + (dX - rx - 3) + ", " + (rz + 2) + ")");

		final BVec Tsp = origin.add((rx) * cX + 2, dY * cellDims.y - 4, (rz) * cZ + 2);
		Bukkit.getConsoleSender().sendMessage("tsp-(" + (rx) + ", " + (rz) + ")");
		for (int i = Tspawn.length - 1; i >= 0; i--) {
			Tspawn[i] = Tsp;
		}
		final BVec CTsp = origin.add((dX - rx) * cX + 2, dY * cellDims.y - 4, (dZ - rz) * cZ + 2);
		Bukkit.getConsoleSender().sendMessage("ctsp-(" + (dX - rx) + ", " + (dZ - rz) + ")");
		for (int i = CTspawn.length - 1; i >= 0; i--) {
			CTspawn[i] = CTsp;
		}

		final LinkedList<BVec> spts = new LinkedList<>();
		for (int f = 0; f != mapDims.y; f++) {
			Bukkit.getConsoleSender().sendMessage("Generating floor " + f);
			final Floor flr;
			if (f == mapDims.y - 1) {
				flr = Floor.TOP;
			} else if (f == 0) {
				flr = Floor.BOT;
			} else {
				flr = Floor.MID;
			}
			buildFloor(dX, dZ, origin.clone().add(0, cellDims.y * f, 0), flr, spts);
		}
		spots = spts.toArray(spots);
//		Bukkit.createBossBar(OStrap.key(stp.nm), "Драконус", BarColor.PURPLE,
//			BarStyle.SEGMENTED_12, BarFlag.DARKEN_SKY, BarFlag.CREATE_FOG);
	}
	
	private void buildFloor(final int lenX, final int widZ, final BVec org, final Floor flr, final LinkedList<BVec> spts) {
		final int cX = cellDims.x, cZ = cellDims.z;
		final HashMap<Integer, TileType> tiles = new HashMap<>();
		for (final int c : stairs) {
			tiles.put(c, TileType.DWNSTS);
		}
		final int[] cells = new int[lenX * widZ];
		int cnt = 0;
		for (int x = 0; x != lenX; x++) {
			for (int z = 0; z != widZ; z++) {
				cells[cnt] = encd(x, z); cnt++;
				//presets
				if (x == 0 || x == lenX - 1) {
					tiles.put(encd(x, z), TileType.WALL);
				} else if (z == 0 || z == widZ - 1) {
					tiles.put(encd(x, z), TileType.WALL);
				}
			}
		}
		
		Main.shuffle(cells);
		
		//upstairs stairs and sites
		final int halfX = lenX >> 1, halfZ = widZ >> 1,
			totalSpots = (halfX + halfZ) >> 1;
		Arrays.fill(stairs, 0);
		switch (flr) {
		case BOT:
			final int rx = Main.srnd.nextInt(lenX >> 3) + 2,
			rz = Main.srnd.nextInt(widZ >> 3) + 2;
			if (halfX * halfZ > 100) {
				stairs[0] = encd(halfX, halfZ + rz);
				stairs[1] = encd(halfX, halfZ - rz);
				stairs[2] = encd(halfX + rx, halfZ);
				stairs[3] = encd(halfX - rx, halfZ);
			} else {
				stairs[0] = encd(halfX, halfZ + rz);
				stairs[1] = encd(halfX, halfZ - rz);
			}
			tiles.put(encd((Asite.x - org.x) / cX, (Asite.z - org.z) / cZ),
				type == GameType.INVASION ? TileType.OPEN : TileType.HGBOX);
			tiles.put(encd((Bsite.x - org.x) / cX, (Bsite.z - org.z) / cZ),
				type == GameType.INVASION ? TileType.OPEN : TileType.HGBOX);
			break;
		case MID:
			while (true) {
				final int ux = Main.srnd.nextInt(lenX >> 3) + 2,
				uz = Main.srnd.nextInt(widZ >> 3) + 2;
				if (tiles.get(encd(halfX, halfZ + uz)) != null) continue;
				if (halfX * halfZ > 100) {
					stairs[0] = encd(halfX, halfZ + uz);
					stairs[1] = encd(halfX, halfZ - uz);
					stairs[2] = encd(halfX + ux, halfZ);
					stairs[3] = encd(halfX - ux, halfZ);
				} else {
					stairs[0] = encd(halfX, halfZ + uz);
					stairs[1] = encd(halfX, halfZ - uz);
				}
				break;
			}
			break;
		case TOP:
			tiles.put(encd((Tspawn[0].x - org.x) / cX, (Tspawn[0].z - org.z) / cZ), TileType.OPEN);
			tiles.put(encd((CTspawn[0].x - org.x) / cX, (CTspawn[0].z - org.z) / cZ), TileType.OPEN);
			break;
		}
		
		for (final int c : stairs) {
			if (c != 0) {
				tiles.put(c, TileType.UPSTS);
			}
		}

		int sps = 0;
		//filling in the rest
		for (final int coords : cells) {
			if (tiles.get(coords) != null) continue;
			final int Z = coords >> encodeBits, X = coords - (Z << encodeBits);
			final EnumSet<TileType> possible = EnumSet.copyOf(TileType.gns);
			for (final Entry<Integer, TileType> en : tiles.entrySet()) {
				final int enc = en.getKey(), z = enc >> encodeBits, x = enc - (z << encodeBits),
					d = NumUtil.abs(X - x) + NumUtil.abs(Z - z);
				if (d < maxCheckDist) {
					final TileType tileAtXZ = en.getValue();
                    possible.removeIf(pos -> !tileAtXZ.canPlaceNear(pos, d));
				}
			}
			
			if (possible.isEmpty()) {
				Bukkit.getConsoleSender().sendMessage("No possible for floor " + mapDims.y);
				return;//fallback
			}
			final TileType ftp = ClassUtil.rndElmt(possible.toArray(etl));
			if (ftp == TileType.OPEN && sps != totalSpots) {
				Bukkit.getConsoleSender().sendMessage("Add spot at " + X + ", " + Z);
				spts.add(org.clone().add((((X << 1) + 1) * cellDims.x) >> 1,
					type == GameType.INVASION ? 1 : 3, (((Z << 1) + 1) * cellDims.z) >> 1)); sps++;
			}
			tiles.put(coords, ftp);
		}

		for (final Entry<Integer, TileType> en : tiles.entrySet()) {//paste set
			final int enc = en.getKey(), z = enc >> encodeBits, x = enc - (z << encodeBits);
			final TileType[] around = new TileType[4];
			TileType tile = tiles.get(encd(x + 1, z));
			around[0] = tile == null ? TileType.OPEN : tile;
			tile = tiles.get(encd(x, z + 1));
			around[1] = tile == null ? TileType.OPEN : tile;
			tile = tiles.get(encd(x - 1, z));
			around[2] = tile == null ? TileType.OPEN : tile;
			tile = tiles.get(encd(x, z - 1));
			around[3] = tile == null ? TileType.OPEN : tile;

			placeRotateSet(en.getValue(), around, org.add(x * cX, 0, z * cZ));
		}
		Bukkit.getConsoleSender().sendMessage("Done async generating a floor");
	}

	private void placeRotateSet(final TileType tile, final TileType[] around, final BVec loc) {
		for (final TileSet set : TileSet.values()) {//for every set
			if (set.original == tile) {
				final int ln = around.length;//always 4
				sts : for (int rot = 0; rot != ln; rot++) {//for every possible rotation
					for (int j = 0; j != ln; j++) {//check if array matches set
						if (!arrayContains(set.form[j], around[j])) {//rotate by 1 if not
							final TileType first = around[0];
							for (int i = 1; i != ln; i++) {//rotating
								around[i - 1] = around[i];
							}
							around[ln - 1] = first;
							continue sts;
						}
					}
					sets.add(new PasteSet(set, (byte) (set.rotateRnd ? Main.srnd.nextInt(4) : rot), loc));
					return;
				}
			}
		}
	}

	private <G> boolean arrayContains(final G[] array, final G elem) {
		for (final G g : array) {
			if (g.equals(elem)) return true;
		}
		return false;
	}

	public void remove(final World w, final int tries) {
		final BVec lm = BVec.of(mapDims.x * cellDims.x, mapDims.y * cellDims.y, mapDims.z * cellDims.z);
		for (int x = lm.x; x >= 0; x--) {
			for (int y = lm.y; y >= 0; y--) {
				for (int z = lm.z; z >= 0; z--) {
					w.getBlockAt(origin.x + x, origin.y + y - 1, origin.z + z).setBlockData(BlockUtil.air, false);
				}
			}
		}
		
		Ostrov.sync(() -> {
			if (w.getHighestBlockYAt(origin.x, origin.z) > 0 && tries != 0) {
				Bukkit.getConsoleSender().sendMessage("couldnt remove arena form " + origin.toString() + " trying again");
				remove(w, tries - 1);
			}
		}, 10);
	}
	
	public void placeSets(final Arena ar, final int trs) {
		final HashMap<String, Schematic> clips = new HashMap<>();
		final CommandSender cmd = Bukkit.getConsoleSender();
		final int cX = cellDims.x, cZ = cellDims.z;
		
		//ceiling
		final BlockData ceilData = ceilType.createBlockData();
		final BlockData lightData = BlockType.LIGHT.createBlockData();
		for (int y = genCeiling ? mapDims.y : mapDims.y - 1; y > 0; y--) {
			final int cY = cellDims.y * y - 1;
			for (int x = (cX * mapDims.x) - 1; x >= 0; x--) {
				for (int z = (cZ * mapDims.z) - 1; z >= 0; z--) {
					ar.w.getBlockAt(origin.x + x, origin.y + cY, origin.z + z).setBlockData(ceilData, false);
					if ((x & 3) == 0 && (z & 3) == 0) {
						final Block b = ar.w.getBlockAt(origin.x + x, origin.y + cY - 1, origin.z + z);
						if (b.getType().isAir()) b.setBlockData(lightData, false);
					}
				}
			}
		}

		final String cm = String.valueOf(Character.toUpperCase(ceilType.key().asMinimalString().charAt(0)));
		for (final PasteSet set : sets) {
			//floor
			final Block b = ar.w.getBlockAt(set.loc().x, set.loc().y, set.loc().z);
			for (int y = set.set().height - 1; y >= 0; y--) {
				for (int x = 0; x < cX; x++) {
					for (int z = 0; z < cZ; z++) {
						BlockUtil.set(b.getRelative(x,y,z), set.set().original.floorMat, false);
					}
				}
			}
			
			final String sch = cm + Setup.sep + set.set().name();
			final Schematic clip;
			if (clips.containsKey(sch)) {
				clip = clips.get(sch);
			} else {
				final File fl = new File(Bukkit.getPluginsFolder().getAbsolutePath() + "/Ostrov/schematics/" + sch + ".schem");
				if (fl.exists()) {
					clip = new Schematic(cmd, fl, false);
					clips.put(sch, clip);
				} else {
					Bukkit.getConsoleSender().sendMessage("Schem " + fl.getName() + " doesn't exist!");
					continue;
				}
			}
			
			switch (set.set()) {
			case CRSS_UP:
			case FULL_UP:
			case HALF_UP:
			case MOST_UP:
			case NONE_UP:
			case SOME_UP:
				final int y = cellDims.y - 2;
				for (int x = cellDims.x - 1; x >= 0; x--) {
					for (int z = cellDims.z - 1; z >= 0; z--) {
						set.loc().clone().add(x, y, z).center(ar.w).getBlock().setBlockData(BlockUtil.air, false);
					}
				}
				break;
			default:
				break;
			}
			
			//cl.getRegion().contract(null);
			final BVec bv;
			switch (set.rtt()) {
			case 3:
				bv = set.loc().add(0, set.set().height, cZ - 1);
				clip.paste(cmd, BVec.of(ar.w, bv.x, bv.y, bv.z), Rotate.r270, set.set().pstAir);
				break;
			case 2:
				bv = set.loc().add(cX - 1, set.set().height, cZ - 1);
				clip.paste(cmd, BVec.of(ar.w, bv.x, bv.y, bv.z), Rotate.r180, set.set().pstAir);
				break;
			case 1:
				bv = set.loc().w(ar.w).add(cX - 1, set.set().height, 0);
				clip.paste(cmd, BVec.of(ar.w, bv.x, bv.y, bv.z), Rotate.r90, set.set().pstAir);
				break;
			default:
				bv = set.loc().w(ar.w).add(0, set.set().height, 0);
				clip.paste(cmd, BVec.of(ar.w, bv.x, bv.y, bv.z), Rotate.r0, set.set().pstAir);
				break;
			}
		}
		
		Ostrov.sync(() -> {
			if (ar.w.getHighestBlockYAt(0, 0) > 0) {
				sets.clear();
				clips.clear();
				final Location cntr = new Location(ar.w, 0.5d, 50.5d, 0.5d);
				ar.w.spawn(cntr, EnderCrystal.class).setShowingBottom(false);
				final EnderDragon ed = ar.w.spawn(cntr.add(0d, 2d, 0d), EnderDragon.class);
				if (ed.getDragonBattle() != null) ed.getDragonBattle().setPreviouslyKilled(true);
				ed.setPhase(EnderDragon.Phase.LEAVE_PORTAL);
				Nms.colorGlow(ed, NamedTextColor.DARK_PURPLE, false);
//				Ostrov.async(() -> mar.loadPos());
			} else if (trs != 0) {
				Bukkit.getConsoleSender().sendMessage("not placed, sets n-" + sets.toString());
				placeSets(ar, trs - 1);
			}
		}, 10);
	}
	
	public static int encd(final int x, final int z) {
		return x ^ (z << encodeBits);
	}
	
	public enum Floor {
		TOP, MID, BOT,
	}
}
