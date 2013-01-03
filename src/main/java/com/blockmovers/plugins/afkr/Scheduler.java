/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blockmovers.plugins.afkr;

import java.util.*;
import org.bukkit.entity.Player;

/**
 *
 * @author MattC
 */
public class Scheduler implements Runnable {

    AFKr plugin = null;

    public Scheduler(AFKr plugin) {
        this.plugin = plugin;
    }

    public void run() {
        if (this.plugin.playerLocation.size() > 0) {

            HashMap pL = new HashMap(this.plugin.playerLocation);
            HashMap pT = new HashMap(this.plugin.playerTime);

            Iterator entries = pL.entrySet().iterator();

            Calendar cal = new GregorianCalendar();
            cal.setTimeInMillis(System.currentTimeMillis());

            Long curTime = cal.getTimeInMillis() / 1000L;

            while (entries.hasNext()) {
                Map.Entry entry = (Map.Entry) entries.next();

                if (plugin.getServer().getPlayerExact(String.valueOf(entry.getKey())) != null) {
                    Player p = plugin.getServer().getPlayerExact(String.valueOf(entry.getKey()));
                    if (!plugin.checkDistance(p)) {
                        Long lastMovement = Long.valueOf(pT.get(p.getName()).toString());
                        Long timeToKick = (plugin.config.minsToKick * 60L) + lastMovement;
                        Long timeToAFK = (plugin.config.minsToAFK * 60L) + lastMovement;

                        if (curTime >= timeToAFK) { // player is afk
                            if (!plugin.isAFK(p)) {
                                plugin.markAFK(p);
                            }
                        }
                        if (curTime >= timeToKick) { // player has been afk for longer than the kick time
                            if (!p.hasPermission("afkr.exempt")) {
                                plugin.kickPlayer(p);
                            }
                        }
                    } else {
                        plugin.updatePlayer(p);
                    }
                }
            }
        }
    }
}
