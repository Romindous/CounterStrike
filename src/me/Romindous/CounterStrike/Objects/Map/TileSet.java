package me.Romindous.CounterStrike.Objects.Map;

public enum TileSet {
	
	FULL_WALL(	TileType.WALL, 		mkArry(new int[] {0,0,0,0}, new TileType[][] {new TileType[] {TileType.WALL}}), 												true, true, 0, "fullwall"),
	MOST_WALL(	TileType.WALL, 		mkArry(new int[] {0,0,0,1}, new TileType[] {TileType.WALL}, new TileType[] {TileType.OPEN, TileType.UPSTS}), 					false, true, 0, "mostwall"),
	HALF_WALL(	TileType.WALL, 		mkArry(new int[] {0,0,1,1}, new TileType[] {TileType.WALL}, new TileType[] {TileType.OPEN, TileType.UPSTS}), 					false, true, 0, "halfwall"),
	CRSS_WALL(	TileType.WALL, 		mkArry(new int[] {0,1,0,1}, new TileType[] {TileType.WALL}, new TileType[] {TileType.OPEN, TileType.UPSTS}), 					false, true, 0, "crsswall"),
	SOME_WALL(	TileType.WALL, 		mkArry(new int[] {0,1,1,1}, new TileType[] {TileType.WALL}, new TileType[] {TileType.OPEN, TileType.UPSTS}), 					false, true, 0, "somewall"),
	PILLAR(		TileType.WALL, 		mkArry(new int[] {0,0,0,0}, new TileType[][] {new TileType[] {TileType.OPEN, TileType.UPSTS}}), 								true, true, 0, "nonewall"),
	
	OPEN_OPEN(	TileType.OPEN, 		mkArry(new int[] {0,0,0,0}, new TileType[][] {new TileType[] {TileType.BOX, TileType.WALL, TileType.OPEN}}), 					true, true, 0, "noneopen"),
	SOME_OPEN(	TileType.OPEN, 		mkArry(new int[] {1,1,1,0}, 
		new TileType[] {TileType.DWNSTS, TileType.UPSTS, TileType.HGSTS}, new TileType[] {TileType.BOX, TileType.WALL, TileType.OPEN}), 							false, true, 0, "someopen"),
	HALF_OPEN(	TileType.OPEN, 		mkArry(new int[] {1,1,0,0}, 
		new TileType[] {TileType.DWNSTS, TileType.UPSTS, TileType.HGSTS}, new TileType[] {TileType.BOX, TileType.WALL, TileType.OPEN}), 							false, true, 0, "halfopen"),
	CRSS_OPEN(	TileType.OPEN, 		mkArry(new int[] {0,1,0,1}, 
		new TileType[] {TileType.DWNSTS, TileType.UPSTS, TileType.HGSTS}, new TileType[] {TileType.BOX, TileType.WALL, TileType.OPEN}), 							false, true, 0, "crssopen"),
	MOST_OPEN(	TileType.OPEN, 		mkArry(new int[] {0,0,0,1}, 
		new TileType[] {TileType.DWNSTS, TileType.UPSTS, TileType.HGSTS}, new TileType[] {TileType.BOX, TileType.WALL, TileType.OPEN}), 							false, true, 0, "mostopen"),
	FULL_OPEN(	TileType.OPEN, 		mkArry(new int[] {0,0,0,0}, new TileType[][] {new TileType[] {TileType.DWNSTS, TileType.UPSTS, TileType.HGSTS}}), 				true, true, 0, "fullopen"),
	
	OPEN_BOX(	TileType.BOX, 		mkArry(new int[] {0,0,0,0}, new TileType[][] {new TileType[] {TileType.OPEN, TileType.DWNSTS, TileType.UPSTS}}), 				true, true, 0, "nonebox"),
	SOME_BOX(	TileType.BOX, 		mkArry(new int[] {0,1,1,1}, new TileType[] {TileType.BOX}, new TileType[] {TileType.OPEN, TileType.UPSTS, TileType.DWNSTS}), 	false, true, 0, "somebox"),
	HALF_BOX(	TileType.BOX, 		mkArry(new int[] {0,0,1,1}, new TileType[] {TileType.BOX}, new TileType[] {TileType.OPEN, TileType.UPSTS, TileType.DWNSTS}), 	false, true, 0, "halfbox"),
	CRSS_BOX(	TileType.BOX, 		mkArry(new int[] {0,1,0,1}, new TileType[] {TileType.BOX}, new TileType[] {TileType.OPEN, TileType.UPSTS, TileType.DWNSTS}), 	false, true, 0, "crssbox"),
	MOST_BOX(	TileType.BOX, 		mkArry(new int[] {0,0,0,1}, new TileType[] {TileType.BOX}, new TileType[] {TileType.OPEN, TileType.UPSTS, TileType.DWNSTS}), 	false, true, 0, "mostbox"),
	FULL_BOX(	TileType.BOX, 		mkArry(new int[] {0,0,0,0}, new TileType[][] {new TileType[] {TileType.BOX}}), 													true, true, 0, "fullbox"),
	
	NONE_UP(	TileType.UPSTS, 	mkArry(new int[] {0,0,0,0}, new TileType[][] {new TileType[] {TileType.OPEN, TileType.DWNSTS}}), 								true, false, 0, "noneup"),
	SOME_UP(	TileType.UPSTS, 	mkArry(new int[] {0,0,0,1}, new TileType[] {TileType.BOX, TileType.UPSTS}, new TileType[] {TileType.OPEN, TileType.DWNSTS}), 	false, false, 0, "someup"),
	HALF_UP(	TileType.UPSTS, 	mkArry(new int[] {0,0,1,1}, new TileType[] {TileType.BOX, TileType.UPSTS}, new TileType[] {TileType.OPEN, TileType.DWNSTS}), 	false, false, 0, "halfup"),
	CRSS_UP(	TileType.UPSTS, 	mkArry(new int[] {0,1,0,1}, new TileType[] {TileType.BOX, TileType.UPSTS}, new TileType[] {TileType.OPEN, TileType.DWNSTS}), 	false, false, 0, "crssup"),
	MOST_UP(	TileType.UPSTS, 	mkArry(new int[] {0,1,1,1}, new TileType[] {TileType.BOX, TileType.UPSTS}, new TileType[] {TileType.OPEN, TileType.DWNSTS}), 	false, false, 0, "mostup"),
	FULL_UP(	TileType.UPSTS, 	mkArry(new int[] {0,0,0,0}, new TileType[][] {new TileType[] {TileType.BOX, TileType.UPSTS}}), 									true, false, 0, "fullup"),
	
	DOWN(		TileType.DWNSTS, 	mkArry(new int[] {0,0,0,0}, new TileType[][] {new TileType[] {TileType.OPEN, TileType.DWNSTS, TileType.UPSTS, TileType.BOX}}), 	true, true, 0, "down"),
	
	NONE_HGS(	TileType.HGSTS, 	mkArry(new int[] {0,0,0,0}, new TileType[][] {new TileType[] {TileType.BOX, TileType.OPEN}}), 									true, true, 0, "nonehgs"),
	SOME_HGS(	TileType.HGSTS, 	mkArry(new int[] {0,0,1,0}, new TileType[] {TileType.OPEN}, new TileType[] {TileType.HGBOX, TileType.HGSTS}), 					false, true, 0, "strthgs"),
	HALF_HGS(	TileType.HGSTS, 	mkArry(new int[] {0,0,1,1}, new TileType[] {TileType.OPEN}, new TileType[] {TileType.HGBOX, TileType.HGSTS}), 					false, true, 0, "halfhgs"),
	CRSS_HGS(	TileType.HGSTS, 	mkArry(new int[] {0,1,0,1}, new TileType[] {TileType.OPEN}, new TileType[] {TileType.HGBOX, TileType.HGSTS}), 					false, true, 0, "crsshgs"),
	MOST_HGS(	TileType.HGSTS, 	mkArry(new int[] {0,1,1,1}, new TileType[] {TileType.OPEN}, new TileType[] {TileType.HGBOX, TileType.HGSTS}), 					false, true, 0, "strthgs"),
	FULL_HGS(	TileType.HGSTS, 	mkArry(new int[] {0,0,0,0}, new TileType[][] {new TileType[] {TileType.HGBOX, TileType.HGSTS}}), 								true, true, 0, "fullhgs"),
	
	NONE_HGBOX( TileType.HGBOX, 	mkArry(new int[] {0,0,0,0}, new TileType[][] {new TileType[] {TileType.HGSTS, TileType.HGWALL}}), 								true, true, 3, "nonehgb"),
	SOME_HGBOX(	TileType.HGBOX, 	mkArry(new int[] {1,0,0,0}, new TileType[] {TileType.HGSTS, TileType.HGWALL}, new TileType[] {TileType.HGBOX}), 				false, true, 3, "somehgb"),
	HALF_HGBOX(	TileType.HGBOX, 	mkArry(new int[] {1,1,0,0}, new TileType[] {TileType.HGSTS, TileType.HGWALL}, new TileType[] {TileType.HGBOX}), 				false, true, 3, "halfhgb"),
	CRSS_HGBOX(	TileType.HGBOX, 	mkArry(new int[] {1,0,1,0}, new TileType[] {TileType.HGSTS, TileType.HGWALL}, new TileType[] {TileType.HGBOX}), 				false, true, 3, "crsshgb"),
	MOST_HGBOX(	TileType.HGBOX, 	mkArry(new int[] {1,1,1,0}, new TileType[] {TileType.HGSTS, TileType.HGWALL}, new TileType[] {TileType.HGBOX}), 				false, true, 3, "mosthgb"),
	FULL_HGBOX(	TileType.HGBOX, 	mkArry(new int[] {0,0,0,0}, new TileType[][] {new TileType[] {TileType.HGBOX}}), 												true, true, 3, "fullhgb"),
	
	NONE_HGWALL(TileType.HGWALL, 	mkArry(new int[] {0,0,0,0}, new TileType[][] {new TileType[] {TileType.HGSTS, TileType.HGBOX}}), 								true, true, 3, "nonehgw"),
	SOME_HGWALL(TileType.HGWALL, 	mkArry(new int[] {1,0,0,0}, new TileType[] {TileType.HGSTS, TileType.HGBOX}, new TileType[] {TileType.HGWALL}), 				false, true, 3, "somehgw"),
	HALF_HGWALL(TileType.HGWALL, 	mkArry(new int[] {1,1,0,0}, new TileType[] {TileType.HGSTS, TileType.HGBOX}, new TileType[] {TileType.HGWALL}), 				false, true, 3, "halfhgw"),
	CRSS_HGWALL(TileType.HGWALL, 	mkArry(new int[] {1,0,1,0}, new TileType[] {TileType.HGSTS, TileType.HGBOX}, new TileType[] {TileType.HGWALL}), 				false, true, 3, "crsshgw"),
	MOST_HGWALL(TileType.HGWALL, 	mkArry(new int[] {1,1,1,0}, new TileType[] {TileType.HGSTS, TileType.HGBOX}, new TileType[] {TileType.HGWALL}), 				false, true, 3, "mosthgw"),
	FULL_HGWALL(TileType.HGWALL, 	mkArry(new int[] {0,0,0,0}, new TileType[][] {new TileType[] {TileType.HGWALL}}), 												true, true, 3, "fullhgw");
	
	public final TileType[][] form;	//being the 4 surrounding tiles
	public final boolean rotateRnd;	//if not "connected" to anything
	public final boolean ignrAir;	//ignore air blocks in schem
	public final TileType original;
	public final String[] schems;
	public final int height;		//height of the tile placement
	
	private TileSet(final TileType org, final TileType[][] frm, final boolean rndmRtt, final boolean ignrAir, final int dY, final String... schms) {
		this.rotateRnd = rndmRtt;
		this.ignrAir = ignrAir;
		this.schems = schms;
		this.form = frm;
		this.original = org;
		this.height = dY;
	}
	
	private static TileType[][] mkArry(final int[] which, final TileType[]... tilesArr) {
		final TileType[][] finalForm = new TileType[which.length][];
		for (int j = 0; j < finalForm.length; j++) {
			finalForm[j] = tilesArr[which[j]];
		}
		
		return finalForm;
	}
}