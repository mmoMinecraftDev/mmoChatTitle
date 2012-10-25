/*
 * This file is part of mmoChatTitle <http://github.com/mmoMinecraftDev/mmoChatTitle>.
 *
 * mmoChatTitle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package mmo.Chat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import mmo.Core.ChatAPI.MMOChatEvent;
import mmo.Core.MMOPlugin.Support;
import mmo.Core.util.EnumBitSet;
import mmo.Core.MMO;
import mmo.Core.MMOPlugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.getspout.spoutapi.player.SpoutPlayer;

public final class MMOChatTitle extends MMOPlugin implements Listener {
	protected static final HandlerList handlers = new HandlerList();
	public static boolean config_always_show = false;
	public static int config_max_titles = 1;
	public static String config_stop_prefix = "!!!";
	public static List<String> config_default = new ArrayList<String>();
	public static Map<String, String> default_perms = new LinkedHashMap<String, String>();

	@Override
	public EnumBitSet mmoSupport(EnumBitSet support) {
		support.set(Support.MMO_AUTO_EXTRACT);
		return support;
	}
	
	@Override
	public void loadConfiguration(final FileConfiguration cfg) {
		 
		config_always_show = cfg.getBoolean("always_show", config_always_show);
		config_max_titles = cfg.getInt("max_titles", config_max_titles);
		config_stop_prefix = cfg.getString("stop_prefix", config_stop_prefix);
		if (cfg.isList("default")) {
			config_default = cfg.getStringList("default");
		}
		default_perms.clear();
		for (String arg : config_default) {
			String[] perm = arg.split("=");
			if (perm.length == 2) {
				default_perms.put(perm[0], perm[1]);
			}
		}
	}

	@Override
	public void onEnable() {
		super.onEnable();
		pm.registerEvents(this, this);		
	}

	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void onMMOChat(final MMOChatEvent event) {		
		if (config_always_show || event.hasFilter("Title")) {
			final List<String> titles = new LinkedList<String>();
			final Player from = event.getPlayer();
			final Map<String, String> perms = new LinkedHashMap<String, String>(default_perms);
			for (String arg : event.getArgs("Title")) {
				final String[] perm = arg.split("=");
				if (perm.length == 2) {
					perms.put(perm[0], perm[1]);
				}
			}
			for (String arg : perms.keySet()) {
				boolean end = false;
				if (from.hasPermission(arg)) {
					String title = perms.get(arg);
					if (!config_stop_prefix.isEmpty() && title.startsWith(config_stop_prefix)) {
						end = true;
						title = title.substring(config_stop_prefix.length()).trim();
					}
					if (!title.isEmpty()) {
						titles.add(title);
					}
					if (config_max_titles > 0 && titles.size() >= config_max_titles) {
						end = true;
					}
				}
				if (end) {
					break;
				}
			}
			if (!titles.isEmpty()) {
				for (Player to : event.getRecipients()) {
					event.setFormat(to, event.getFormat(to).replaceAll("%2\\$s", MMO.join(titles, " ") + " %2\\$s"));					
				}
			}
		}
	}
}
