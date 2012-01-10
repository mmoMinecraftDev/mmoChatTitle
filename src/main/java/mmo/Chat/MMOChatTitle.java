/*
 * This file is part of mmoMinecraft (https://github.com/mmoMinecraftDev).
 *
 * mmoMinecraft is free software: you can redistribute it and/or modify
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
import mmo.Core.MMO;
import mmo.Core.MMOListener;
import mmo.Core.MMOPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.Event.Type;
import org.bukkit.util.config.Configuration;

public class MMOChatTitle extends MMOPlugin {

	static public boolean config_always_show = false;
	static public int config_max_titles = 1;
	static public String config_stop_prefix = "!!!";
	static public List<String> config_default = new ArrayList<String>();
	static public Map<String, String> default_perms = new LinkedHashMap<String, String>();

	@Override
	public void loadConfiguration(Configuration cfg) {
		config_always_show = cfg.getBoolean("always_show", config_always_show);
		config_max_titles = cfg.getInt("max_titles", config_max_titles);
		config_stop_prefix = cfg.getString("stop_prefix", config_stop_prefix);
		config_default = cfg.getStringList("default", config_default);
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
		pm.registerEvent(Type.CUSTOM_EVENT,
				new MMOListener() {

					@Override
					public void onMMOChat(MMOChatEvent event) {
						if (config_always_show || event.hasFilter("Title")) {
							LinkedList<String> titles = new LinkedList<String>();
							Player from = event.getPlayer();
							Map<String, String> perms = new LinkedHashMap<String, String>(default_perms);
							for (String arg : event.getArgs("Title")) {
								String[] perm = arg.split("=");
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
				}, Priority.High, this);
	}
}
