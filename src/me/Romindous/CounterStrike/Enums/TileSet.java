package me.Romindous.CounterStrike.Enums;

public enum TileSet {
	
	FULL_WALL(	TileType.WALL, 		mkArry(new int[] {0,0,0,0}, new TileType[][] {new TileType[] {TileType.WALL}}), 												true, false, 0),
	MOST_WALL(	TileType.WALL, 		mkArry(new int[] {0,0,0,1}, new TileType[] {TileType.WALL}, new TileType[] {TileType.OPEN, TileType.UPSTS}), 					false, false, 0),
	HALF_WALL(	TileType.WALL, 		mkArry(new int[] {0,0,1,1}, new TileType[] {TileType.WALL}, new TileType[] {TileType.OPEN, TileType.UPSTS}), 					false, false, 0),
	CRSS_WALL(	TileType.WALL, 		mkArry(new int[] {0,1,0,1}, new TileType[] {TileType.WALL}, new TileType[] {TileType.OPEN, TileType.UPSTS}), 					false, false, 0),
	SOME_WALL(	TileType.WALL, 		mkArry(new int[] {0,1,1,1}, new TileType[] {TileType.WALL}, new TileType[] {TileType.OPEN, TileType.UPSTS}), 					false, false, 0),
	PILLAR(		TileType.WALL, 		mkArry(new int[] {0,0,0,0}, new TileType[][] {new TileType[] {TileType.OPEN, TileType.UPSTS}}), 								true, false, 0),
	
	OPEN_OPEN(	TileType.OPEN, 		mkArry(new int[] {0,0,0,0}, new TileType[][] {new TileType[] {TileType.BOX, TileType.WALL, TileType.OPEN}}), 					true, false, 0),
	SOME_OPEN(	TileType.OPEN, 		mkArry(new int[] {1,1,1,0}, 
		new TileType[] {TileType.DWNSTS, TileType.UPSTS, TileType.HGSTS}, new TileType[] {TileType.BOX, TileType.WALL, TileType.OPEN}), 							false, false, 0),
	HALF_OPEN(	TileType.OPEN, 		mkArry(new int[] {1,1,0,0}, 
		new TileType[] {TileType.DWNSTS, TileType.UPSTS, TileType.HGSTS}, new TileType[] {TileType.BOX, TileType.WALL, TileType.OPEN}), 							false, false, 0),
	CRSS_OPEN(	TileType.OPEN, 		mkArry(new int[] {0,1,0,1}, 
		new TileType[] {TileType.DWNSTS, TileType.UPSTS, TileType.HGSTS}, new TileType[] {TileType.BOX, TileType.WALL, TileType.OPEN}), 							false, false, 0),
	MOST_OPEN(	TileType.OPEN, 		mkArry(new int[] {0,0,0,1}, 
		new TileType[] {TileType.DWNSTS, TileType.UPSTS, TileType.HGSTS}, new TileType[] {TileType.BOX, TileType.WALL, TileType.OPEN}), 							false, false, 0),
	FULL_OPEN(	TileType.OPEN, 		mkArry(new int[] {0,0,0,0}, new TileType[][] {new TileType[] {TileType.DWNSTS, TileType.UPSTS, TileType.HGSTS}}), 				true, false, 0),
	
	OPEN_BOX(	TileType.BOX, 		mkArry(new int[] {0,0,0,0}, new TileType[][] {new TileType[] {TileType.OPEN, TileType.DWNSTS, TileType.UPSTS}}), 				true, false, 0),
	SOME_BOX(	TileType.BOX, 		mkArry(new int[] {0,1,1,1}, new TileType[] {TileType.BOX}, new TileType[] {TileType.OPEN, TileType.UPSTS, TileType.DWNSTS}), 	false, false, 0),
	HALF_BOX(	TileType.BOX, 		mkArry(new int[] {0,0,1,1}, new TileType[] {TileType.BOX}, new TileType[] {TileType.OPEN, TileType.UPSTS, TileType.DWNSTS}), 	false, false, 0),
	CRSS_BOX(	TileType.BOX, 		mkArry(new int[] {0,1,0,1}, new TileType[] {TileType.BOX}, new TileType[] {TileType.OPEN, TileType.UPSTS, TileType.DWNSTS}), 	false, false, 0),
	MOST_BOX(	TileType.BOX, 		mkArry(new int[] {0,0,0,1}, new TileType[] {TileType.BOX}, new TileType[] {TileType.OPEN, TileType.UPSTS, TileType.DWNSTS}), 	false, false, 0),
	FULL_BOX(	TileType.BOX, 		mkArry(new int[] {0,0,0,0}, new TileType[][] {new TileType[] {TileType.BOX}}), 													true, false, 0),
	
	NONE_UP(	TileType.UPSTS, 	mkArry(new int[] {0,0,0,0}, new TileType[][] {new TileType[] {TileType.OPEN, TileType.DWNSTS}}), 								true, true, 0),
	SOME_UP(	TileType.UPSTS, 	mkArry(new int[] {0,0,0,1}, new TileType[] {TileType.BOX, TileType.UPSTS}, new TileType[] {TileType.OPEN, TileType.DWNSTS}), 	false, true, 0),
	HALF_UP(	TileType.UPSTS, 	mkArry(new int[] {0,0,1,1}, new TileType[] {TileType.BOX, TileType.UPSTS}, new TileType[] {TileType.OPEN, TileType.DWNSTS}), 	false, true, 0),
	CRSS_UP(	TileType.UPSTS, 	mkArry(new int[] {0,1,0,1}, new TileType[] {TileType.BOX, TileType.UPSTS}, new TileType[] {TileType.OPEN, TileType.DWNSTS}), 	false, true, 0),
	MOST_UP(	TileType.UPSTS, 	mkArry(new int[] {0,1,1,1}, new TileType[] {TileType.BOX, TileType.UPSTS}, new TileType[] {TileType.OPEN, TileType.DWNSTS}), 	false, true, 0),
	FULL_UP(	TileType.UPSTS, 	mkArry(new int[] {0,0,0,0}, new TileType[][] {new TileType[] {TileType.BOX, TileType.UPSTS}}), 									true, true, 0),
	
	DOWN(		TileType.DWNSTS, 	mkArry(new int[] {0,0,0,0}, new TileType[][] {new TileType[] {TileType.OPEN, TileType.DWNSTS, TileType.UPSTS, TileType.BOX}}), 	true, true, 0),
	
	NONE_HGS(	TileType.HGSTS, 	mkArry(new int[] {0,0,0,0}, new TileType[][] {new TileType[] {TileType.BOX, TileType.OPEN}}), 									true, false, 0),
	SOME_HGS(	TileType.HGSTS, 	mkArry(new int[] {0,0,1,0}, new TileType[] {TileType.OPEN}, new TileType[] {TileType.HGBOX, TileType.HGSTS}), 					false, false, 0),
	HALF_HGS(	TileType.HGSTS, 	mkArry(new int[] {0,0,1,1}, new TileType[] {TileType.OPEN}, new TileType[] {TileType.HGBOX, TileType.HGSTS}), 					false, false, 0),
	CRSS_HGS(	TileType.HGSTS, 	mkArry(new int[] {0,1,0,1}, new TileType[] {TileType.OPEN}, new TileType[] {TileType.HGBOX, TileType.HGSTS}), 					false, false, 0),
	MOST_HGS(	TileType.HGSTS, 	mkArry(new int[] {0,1,1,1}, new TileType[] {TileType.OPEN}, new TileType[] {TileType.HGBOX, TileType.HGSTS}), 					false, false, 0),
	FULL_HGS(	TileType.HGSTS, 	mkArry(new int[] {0,0,0,0}, new TileType[][] {new TileType[] {TileType.HGBOX, TileType.HGSTS}}), 								true, false, 0),
	
	NONE_HGBOX( TileType.HGBOX, 	mkArry(new int[] {0,0,0,0}, new TileType[][] {new TileType[] {TileType.HGSTS, TileType.HGWALL}}), 								true, false, 3),
	SOME_HGBOX(	TileType.HGBOX, 	mkArry(new int[] {1,0,0,0}, new TileType[] {TileType.HGSTS, TileType.HGWALL}, new TileType[] {TileType.HGBOX}), 				false, false, 3),
	HALF_HGBOX(	TileType.HGBOX, 	mkArry(new int[] {1,1,0,0}, new TileType[] {TileType.HGSTS, TileType.HGWALL}, new TileType[] {TileType.HGBOX}), 				false, false, 3),
	CRSS_HGBOX(	TileType.HGBOX, 	mkArry(new int[] {1,0,1,0}, new TileType[] {TileType.HGSTS, TileType.HGWALL}, new TileType[] {TileType.HGBOX}), 				false, false, 3),
	MOST_HGBOX(	TileType.HGBOX, 	mkArry(new int[] {1,1,1,0}, new TileType[] {TileType.HGSTS, TileType.HGWALL}, new TileType[] {TileType.HGBOX}), 				false, false, 3),
	FULL_HGBOX(	TileType.HGBOX, 	mkArry(new int[] {0,0,0,0}, new TileType[][] {new TileType[] {TileType.HGBOX}}), 												true, false, 3),
	
	NONE_HGWALL(TileType.HGWALL, 	mkArry(new int[] {0,0,0,0}, new TileType[][] {new TileType[] {TileType.HGSTS, TileType.HGBOX}}), 								true, false, 3),
	SOME_HGWALL(TileType.HGWALL, 	mkArry(new int[] {1,0,0,0}, new TileType[] {TileType.HGSTS, TileType.HGBOX}, new TileType[] {TileType.HGWALL}), 				false, false, 3),
	HALF_HGWALL(TileType.HGWALL, 	mkArry(new int[] {1,1,0,0}, new TileType[] {TileType.HGSTS, TileType.HGBOX}, new TileType[] {TileType.HGWALL}), 				false, false, 3),
	CRSS_HGWALL(TileType.HGWALL, 	mkArry(new int[] {1,0,1,0}, new TileType[] {TileType.HGSTS, TileType.HGBOX}, new TileType[] {TileType.HGWALL}), 				false, false, 3),
	MOST_HGWALL(TileType.HGWALL, 	mkArry(new int[] {1,1,1,0}, new TileType[] {TileType.HGSTS, TileType.HGBOX}, new TileType[] {TileType.HGWALL}), 				false, false, 3),
	FULL_HGWALL(TileType.HGWALL, 	mkArry(new int[] {0,0,0,0}, new TileType[][] {new TileType[] {TileType.HGWALL}}), 												true, false, 3);
	
	public final TileType[][] form;	//being the 4 surrounding tiles
	public final boolean rotateRnd;	//if not "connected" to anything
	public final boolean pstAir;	//ignore air blocks in schem
	public final TileType original;
	public final int height;		//height of the tile placement
	
	TileSet(final TileType org, final TileType[][] frm, final boolean rndmRtt, final boolean pstAir, final int dY) {
		this.rotateRnd = rndmRtt;
		this.pstAir = pstAir;
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