package me.Romindous.CounterStrike.Objects.Bots;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import org.bukkit.entity.EntityType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.mojang.datafixers.util.Pair;

import ru.komiss77.Ostrov;

public enum BotType {
	
	REGULAR(EntityType.HUSK, "soja", "Corwin4_", "ProKirill", "Colorsss", "scbro", "fnf_", 
		"UEI", "jakubak", "Scail", "shyrezz", "EpexLord", "Stijnaaa", "Tatyan", "Zadan", 
			"Dark_Chara", "devim", "CrashGod", "skarti", "arthuga", "boysi");
	
	public final Pair<String, String>[] txs;
	public final EntityType from;
	
	@SuppressWarnings("unchecked")
	private BotType(final EntityType from, final String... nms) {
		txs = (Pair<String, String>[]) new Pair<?, ?>[nms.length];
		this.from = from;
		Ostrov.async(() -> {
			for (int i = 0; i < txs.length; i++) {
				txs[i] = getSkin(nms[i]);
			}
		});
	}
	
    private Pair<String, String> getSkin(final String nm) {
    	try {
    		final InputStreamReader irn = new InputStreamReader(new URL("https://api.mojang.com/users/profiles/minecraft/" + nm).openStream());
    		final String id = (String) ((JSONObject) new JSONParser().parse(irn)).get("id");
    		
    		final InputStreamReader tsr = new InputStreamReader(new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + id + "?unsigned=false").openStream());
    		final JSONObject ppt = ((JSONObject) ((JSONArray) ((JSONObject) new JSONParser().parse(tsr)).get("properties")).get(0));
    		
    		//p.getBukkitEntity().sendMessage(ppt.toString());
    		return new Pair<String, String>((String) ppt.get("value"), (String) ppt.get("signature"));
		} catch (NullPointerException | IOException | ParseException e) {
			//final Property pr = (Property) ds.bf().t().get(0).fq().getProperties().get("textures").toArray()[0];
			return new Pair<String, String>("", "");
		}
	}
}
