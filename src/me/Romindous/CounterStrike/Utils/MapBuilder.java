package me.Romindous.CounterStrike.Utils;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;

import me.Romindous.CounterStrike.Main;
import me.Romindous.CounterStrike.Enums.GameType;
import me.Romindous.CounterStrike.Enums.TileSet;
import me.Romindous.CounterStrike.Enums.TileType;
import me.Romindous.CounterStrike.Objects.PasteSet;
import net.minecraft.core.BaseBlockPosition;
import ru.komiss77.Ostrov;
import ru.komiss77.modules.world.Schematic;
import ru.komiss77.modules.world.Schematic.Rotate;

public class MapBuilder {
	
	public final String nm;
	private final boolean genCeiling;
	private GameType genType;
	private Material ceilMat;
	private BaseBlockPosition origin, Asite, Bsite, mapDims, cellDims;
	private final BaseBlockPosition[] Tspawn, CTspawn;
	private final int[] stairs;
	public final LinkedList<PasteSet> sets = new LinkedList<>();

	public static final Material dftCeilMat = Material.SMOOTH_SANDSTONE;
	public static final BaseBlockPosition dftMapDims = new BaseBlockPosition(16, 2, 20);
	public static final BaseBlockPosition dftCellDims = new BaseBlockPosition(5, 8, 5);
	public static final int maxCheckDist = TileType.getNoiseDiff();
	public static final int encodeBits = 6;
	
	public MapBuilder(final String nm) {
		this.nm = nm;
		this.mapDims = dftMapDims;
		this.cellDims = dftCellDims;
		this.genCeiling = false;
		this.ceilMat = dftCeilMat;
		this.genType = GameType.DEFUSAL;
		this.Asite = null;
		this.Bsite = null;
		this.stairs = new int[4];
		this.Tspawn = new BaseBlockPosition[8];
		this.CTspawn = new BaseBlockPosition[8];
	}
	
	public MapBuilder(final String nm, final BaseBlockPosition mapDims, final BaseBlockPosition cellDims, final Material ceil, final GameType genType) {
		this.nm = nm;
		this.mapDims = mapDims;
		this.cellDims = cellDims;
		this.genCeiling = ceil != null;
		this.ceilMat = genCeiling ? ceil : dftCeilMat;
		this.genType = genType;
		this.Asite = null;
		this.Bsite = null;
		this.stairs = new int[4];
		this.Tspawn = new BaseBlockPosition[8];
		this.CTspawn = new BaseBlockPosition[8];
	}
	
	public void setType(final GameType genType) {
		this.genType = genType;
	}
	
	public BaseBlockPosition getASite() {
		return this.Asite;
	}
	
	public BaseBlockPosition getBSite() {
		return this.Bsite;
	}
	
	public BaseBlockPosition[] getTSpawns() {
		return this.Tspawn;
	}
	
	public BaseBlockPosition[] getCTSpawns() {
		return this.CTspawn;
	}
	
	public void setCeiling(final Material ceil) {
		this.ceilMat = ceil;
	}
	
	public void setMapDims(final int x, final int y, final int z) {
		mapDims = new BaseBlockPosition(x, y, z);
	}
	
	public void setCellDims(final int x, final int y, final int z) {
		cellDims = new BaseBlockPosition(x, y, z);
	}
	
	public BaseBlockPosition getMapDims() {
		return mapDims;
	}
	
	public BaseBlockPosition getCellDims() {
		return cellDims;
	}
	
	public BaseBlockPosition getOrigin() {
		return origin;
	}
	
	public void build(final Location cntr) {
		final int dX = mapDims.u(), dY = mapDims.v(), dZ = mapDims.w();
		final int cX = cellDims.u(), cZ = cellDims.w();
		final Location loc = cntr.clone().subtract(dX * cX >> 1, 0d, dZ * cZ >> 1);
		origin = new BaseBlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
//		final World w = loc.getWorld();
//		final Chunk ch = loc.getChunk();
//		for (int x = (Math.abs(loc.getBlockX() - cntr.getBlockX()) >> 4) * 2; x >= 0; x --) {
//			for (int z = (Math.abs(loc.getBlockX() - cntr.getBlockX()) >> 4) * 2; x >= 0; x --) {
//				final CompletableFuture<Chunk> cf = w.getChunkAtAsyncUrgently(ch.getX() + x, ch.getZ() + z);
//				cf.thenRun(() -> {
//					try {
//						cf.get().load(false);
//					} catch (InterruptedException | ExecutionException e) {
//						e.printStackTrace();
//					}
//				});
//			}
//		}

		final int rx = Main.srnd.nextInt((dX >> 3) + 1) + 2,
		rz = Main.srnd.nextInt((dZ >> 3) + 1) + 2;
		
		Asite = origin.c((rx + 2) * cX + 2, 3, (dZ - rz - 2) * cZ + 2);
		Bukkit.getConsoleSender().sendMessage("ast-(" + (rx + 3) + ", " + (dZ - rz - 3) + ")");
		Bsite = origin.c((dX - rx - 3) * cX + 2, 3, (rz + 1) * cZ + 2);
		Bukkit.getConsoleSender().sendMessage("bst-(" + (dX - rx - 3) + ", " + (rz + 3) + ")");
		
		final BaseBlockPosition Tsp;
		final BaseBlockPosition CTsp;
		switch (genType) {
		case DEFUSAL:
		case INVASION:
		case GUNGAME:
			Tsp = origin.c((rx) * cX + 2, dY * cellDims.v() - 4, (rz) * cZ + 2);
			Bukkit.getConsoleSender().sendMessage("tsp-(" + (rx) + ", " + (rz) + ")");
			for (int i = Tspawn.length - 1; i >= 0; i--) {
				Tspawn[i] = Tsp;
			}
			CTsp = origin.c((dX - rx) * cX + 2, dY * cellDims.v() - 4, (dZ - rz) * cZ + 2);
			Bukkit.getConsoleSender().sendMessage("ctsp-(" + (dX - rx) + ", " + (dZ - rz) + ")");
			for (int i = CTspawn.length - 1; i >= 0; i--) {
				CTspawn[i] = CTsp;
			}
			break;
		}
		
		Ostrov.async(() -> {
			for (int f = 0; f < mapDims.v(); f++) {
				Bukkit.getConsoleSender().sendMessage("Generating floor " + f);
				final Floor flr;
				if (f == mapDims.v() - 1) {
					flr = Floor.TOP;
				} else if (f == 0) {
					flr = Floor.BOT;
				} else {
					flr = Floor.MID;
				}
				buildFloor(dX, dZ, origin.c(0, cellDims.v() * f, 0), flr, loc.getWorld());
			}
		}, 0);
	}
	
	private void buildFloor(final int lenX, final int widZ, final BaseBlockPosition org, final Floor flr, final World w) {
		final int cX = cellDims.u(), cZ = cellDims.w();
		final HashMap<Integer, TileType> tiles = new HashMap<Integer, TileType>();
		for (final int c : stairs) {
			tiles.put(c, TileType.DWNSTS);
		}
		final LinkedList<Integer> cells = new LinkedList<>();
		for (int x = 0; x < lenX; x++) {
			for (int z = 0; z < widZ; z++) {
				cells.add(encd(x, z));
				//presets
				if (x == 0 || x == lenX - 1) {
					tiles.put(encd(x, z), TileType.WALL);
				} else if (z == 0 || z == widZ - 1) {
					tiles.put(encd(x, z), TileType.WALL);
				}
			}
		}
		
		Collections.shuffle(cells);
		
		//upstairs stairs and sites
		final int halfX = lenX >> 1, halfZ = widZ >> 1;
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
			tiles.put(encd((Asite.u() - org.u()) / cX, (Asite.w() - org.w()) / cZ), TileType.HGBOX);
			tiles.put(encd((Bsite.u() - org.u()) / cX, (Bsite.w() - org.w()) / cZ), TileType.HGBOX);
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
			tiles.put(encd((Tspawn[0].u() - org.u()) / cX, (Tspawn[0].w() - org.w()) / cZ), TileType.OPEN);
			tiles.put(encd((CTspawn[0].u() - org.u()) / cX, (CTspawn[0].w() - org.w()) / cZ), TileType.OPEN);
			break;
		}
		
		for (final int c : stairs) {
			if (c != 0) {
				tiles.put(c, TileType.UPSTS);
			}
		}
		
		//filling in the rest
		for (final int coords : cells) {
			if (tiles.get(coords) != null) continue;
			final int Z = coords >> encodeBits, X = coords - (Z << encodeBits);
			final EnumSet<TileType> possible = EnumSet.copyOf(TileType.gns);
			for (final Entry<Integer, TileType> en : tiles.entrySet()) {
				final int z = en.getKey() >> encodeBits, x = en.getKey() - (z << encodeBits);
				final int d = Math.abs(X - x) + Math.abs(Z - z);
				if (d < maxCheckDist) {
					final TileType tileAtXZ = en.getValue();
					final Iterator<TileType> it = possible.iterator();
					while (it.hasNext()) {
						final TileType possibility = it.next();
						if (!tileAtXZ.canPlaceNear(possibility, d)) {
							//Bukkit.getConsoleSender().sendMessage("excluding-" + ttt.toString() + " d-" + d);
							it.remove();
						}
					}
				}
			}
			
			if (possible.isEmpty()) {
				Bukkit.getConsoleSender().sendMessage("No possible for floor " + mapDims.v());
				return;//fallback
			}
			//Bukkit.getConsoleSender().sendMessage("final-" + tt.toString());
			tiles.put(coords, (TileType) Main.rndElmt(possible.toArray()));
		}
		for (final Integer in : tiles.keySet()) {
			final int z = in >> encodeBits, x = in - (z << encodeBits);
			//Bukkit.getConsoleSender().sendMessage("x-" + x + "z-" + z);
			pasteSet(x, z, tiles, org);
		}
		Bukkit.getConsoleSender().sendMessage("Done async generating a floor");
	}

	public void pasteSet(final int X, final int Z, final HashMap<Integer, TileType> tiles, final BaseBlockPosition org) {
		final TileType[] around = new TileType[4];
		TileType tile = tiles.get(encd(X + 1, Z));
		around[0] = tile == null ? TileType.OPEN : tile;
		tile = tiles.get(encd(X, Z + 1));
		around[1] = tile == null ? TileType.OPEN : tile;
		tile = tiles.get(encd(X - 1, Z));
		around[2] = tile == null ? TileType.OPEN : tile;
		tile = tiles.get(encd(X, Z - 1));
		around[3] = tile == null ? TileType.OPEN : tile;
		
		placeRotateSet(tiles.get(encd(X, Z)), around, new BaseBlockPosition(X * cellDims.w() + org.u(), org.v(), Z * cellDims.w() + org.w()));
	}

	private void placeRotateSet(final TileType tile, final TileType[] around, final BaseBlockPosition loc) {
		//Bukkit.getConsoleSender().sendMessage("loc-" + loc.toString() + ",\nType-" + tile.toString());
		for (final TileSet set : TileSet.values()) {//for every set
			if (set.original == tile) {
				final int ln = around.length;//usually 4
				sts : for (int rot = 0; rot < ln; rot++) {//for every possible rotation
					for (int j = 0; j < ln; j++) {//check if array matches set
						if (!arrayContains(set.form[j], around[j])) {//rotate by 1 if not
							final TileType first_Last = around[0];
							for (int i = 1; i < ln; i++) {//rotating
								around[i - 1] = around[i];
							}
							around[ln - 1] = first_Last;
							continue sts;
						}
					}
					//getServer().getConsoleSender().sendMessage("\nloc-" + loc.toString() + ",\nType-" + tt.toString() + ", Set-" + ts.toString() + ", Rot-" + i);
					//Bukkit.getConsoleSender().sendMessage("added set " + set.toString());
					sets.add(new PasteSet(set, (byte) (set.rotateRnd ? Main.srnd.nextInt(4) : rot), new BaseBlockPosition(loc.u(), loc.v(), loc.w())));
					return;
				}
			}
		}
		//placeWESet(TileSet.OPEN, 0, loc, ess);
	}

	public void remove(final World w, final int i) {
		final BaseBlockPosition lm = new BaseBlockPosition(mapDims.u() * cellDims.u(), mapDims.v() * cellDims.v(), mapDims.w() * cellDims.w());
		for (int x = lm.u(); x >= 0; x--) {
			for (int y = lm.v(); y >= 0; y--) {
				for (int z = lm.w(); z >= 0; z--) {
					w.getBlockAt(origin.u() + x, origin.v() + y - 1, origin.w() + z).setType(Material.AIR, false);
				}
			}
		}
		Ostrov.sync(() -> {
			if (w.getHighestBlockYAt(0, 0) > 0 && i != 0) {
				Bukkit.getConsoleSender().sendMessage("couldnt remove arena form " + origin.toString() + " trying again");
				remove(w, i - 1);
			}
		}, 10);
	}
	
	public void placeSets(final World w, final int trs) {
		final HashMap<String, Schematic> clips = new HashMap<>();
		final CommandSender cmd = Bukkit.getConsoleSender();
		final int cX = cellDims.u(), cZ = cellDims.w();
		
		/*//load chunkssssss
		for (final BlockVector2 ch : new CuboidRegion(origin, origin.add(mapDims.multiply(cellDims))).getChunks()) {
			w.getChunkAt(ch.getX(), ch.getZ()).load();
		}*/
		
		//ceiling
		for (int y = genCeiling ? mapDims.v() : mapDims.v() - 1; y > 0; y--) {
			final int cY = cellDims.v() * y - 2;
			for (int x = (cX * mapDims.u()) - 1; x >= 0; x--) {
				for (int z = (cZ * mapDims.w()) - 1; z >= 0; z--) {
					w.getBlockAt(origin.u() + x, origin.v() + cY, origin.w() + z).setType(ceilMat, false);
					if ((x & 3) == 0 && (z & 3) == 0) {
						final Block b = w.getBlockAt(origin.u() + x, origin.v() + cY - 1, origin.w() + z);
						if (b.getType().isAir()) {
							b.setType(Material.LIGHT, false);
						}
					}
				}
			}
		}
		
		for (final PasteSet set : sets) {
			//floor
			final Block b = w.getBlockAt(set.loc.u(), set.loc.v(), set.loc.w());
			for (int y = set.ts.height - 1; y >= 0; y--) {
				for (int x = 0; x < cX; x++) {
					for (int z = 0; z < cZ; z++) {
						b.getRelative(x,y,z).setType(set.ts.original.floorMat, false);
					}
				}
			}
			
			final String sch = Main.rndElmt(set.ts.schems);
			final Schematic clip;
			if (clips.containsKey(sch)) {
				clip = clips.get(sch);
			} else {
				final File fl = new File(Main.plug.getDataFolder() + "/schems/" + sch + ".schem");
				if (fl.exists()) {
					clip = new Schematic(cmd, fl, false);
					clips.put(sch, clip);
				} else {
					Bukkit.getConsoleSender().sendMessage("Schem-" + fl.getName() + " doesn't exist!");
					continue;
				}
			}
			
			switch (set.ts) {
			case CRSS_UP:
			case FULL_UP:
			case HALF_UP:
			case MOST_UP:
			case NONE_UP:
			case SOME_UP:
				final int y = cellDims.v() - 2;
				for (int x = cellDims.u() - 1; x >= 0; x--) {
					for (int z = cellDims.w() - 1; z >= 0; z--) {
						final BaseBlockPosition rf = set.loc.c(x, y, z);
						w.getBlockAt(rf.u(), rf.v(), rf.w()).setType(Material.AIR, false);
					}
				}
				break;
			default:
				break;
			}
			
			//cl.getRegion().contract(null);
			final BaseBlockPosition bvc;
			switch (set.rtt) {
			case 3:
				bvc = set.loc.c(0, set.ts.height, cZ - 1);
				clip.paste(cmd, new Location(w, bvc.u(), bvc.v(), bvc.w()), Rotate.r270, false);
				break;
			case 2:
				bvc = set.loc.c(cX - 1, set.ts.height, cZ - 1);
				clip.paste(cmd, new Location(w, bvc.u(), bvc.v(), bvc.w()), Rotate.r180, false);
				break;
			case 1:
				bvc = set.loc.c(cX - 1, set.ts.height, 0);
				clip.paste(cmd, new Location(w, bvc.u(), bvc.v(), bvc.w()), Rotate.r90, false);
				break;
			default:
				bvc = set.loc.c(0, set.ts.height, 0);
				clip.paste(cmd, new Location(w, bvc.u(), bvc.v(), bvc.w()), Rotate.r0, false);
				break;
			}
		}
		
		Ostrov.sync(() -> {
			if (w.getHighestBlockYAt(0, 0) > 0) {
				sets.clear();
				clips.clear();
			} else if (trs != 0) {
				Bukkit.getConsoleSender().sendMessage("not placed, sets n-" + sets.toString());
				/*for (final Entry<String, Clipboard> s : clips.entrySet()) {
					Bukkit.getConsoleSender().sendMessage("clip-" + s.getKey() + " for " + s.getValue().getRegion().getBoundingBox().toString());
				}*/
				placeSets(w, trs - 1);
			}
		}, 10);
	}

	private <G> boolean arrayContains(final G[] array, final G elem) {
		for (final G g : array) {
			if (g.equals(elem)) return true; 
		}
		return false;
	}
	
	public static int encd(final int x, final int z) {
		return x + (z << encodeBits);
	}
	
	public enum Floor {
		TOP, MID, BOT;
	}
}
