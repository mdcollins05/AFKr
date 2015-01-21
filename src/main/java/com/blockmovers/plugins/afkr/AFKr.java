package com.blockmovers.plugins.afkr;

import java.util.*;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class AFKr extends JavaPlugin implements Listener {

    static final Logger log = Logger.getLogger("Minecraft"); //set up our logger
    public Configuration config = new Configuration(this);
    private Scheduler sched = new Scheduler(this);
    public final List<String> afkPlayers = new ArrayList();
    public final HashMap<String, Long> playerTime = new HashMap();
    public final HashMap<String, Location> playerLocation = new HashMap();

    public void onDisable() {
        PluginDescriptionFile pdffile = this.getDescription();

        log.info(pdffile.getName() + " version " + pdffile.getVersion() + " is disabled.");
    }

    public void onEnable() {
        PluginDescriptionFile pdffile = this.getDescription();

        getServer().getPluginManager().registerEvents(this, this);

        config.loadConfiguration();

        getServer().getScheduler().scheduleSyncRepeatingTask(this, this.sched, 600L, 600L);

        for (Player p : getServer().getOnlinePlayers()) {
            this.addPlayer(p);
            this.setName(p);
        }

        log.info(pdffile.getName() + " version " + pdffile.getVersion() + " is enabled.");
    }

    public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] args) {
        if (args.length == 0) {
            String s = null;
            StringBuilder sb = new StringBuilder();
            for (String player : this.afkPlayers) {
                sb.append(player + ", ");
            }
            if (sb.length() >= 2) {
                s = sb.substring(0, sb.length() - 2);
            } else {
                s = "No one";
            }
            cs.sendMessage(this.replaceText("&f[&1AFKr&f]&7 Current AFKr's: " + s, ""));
            return true;
        }
        if (args.length == 1) {
            Player player = null;
            if (getServer().getPlayer(args[0]) != null) {
                if (this.afkPlayers.contains(getServer().getPlayer(args[0]).getName())) {
                    player = getServer().getPlayer(args[0]);
                }
            }

            if (player != null) {
                Calendar cal = new GregorianCalendar();
                cal.setTimeInMillis(System.currentTimeMillis());

                Long curTime = cal.getTimeInMillis() / 1000L;
                Long time = curTime - this.playerTime.get(player.getName());

                cs.sendMessage(this.replaceText("&f[&1AFKr&f]&7 $p AFKr since: " + this.timeToString(time) + " ago.", player.getName()));
                return true;
            } else {
                cs.sendMessage(ChatColor.RED + "Player not AFK/not found.");
            }
        }
        return false;
    }

    private void setName(Player p, ChatColor color) {
        try {
        p.setPlayerListName(color + this.removeColors(p.getDisplayName()));
        }
        catch(IllegalArgumentException e) {
            try {
            p.setPlayerListName(color + p.getDisplayName());
            }
            catch(IllegalArgumentException x) {
                log.info(this.getDescription().getName() + " " + p.getName() + " has too long of a name.");
            }
        }
    }

    private void setName(Player p) {
        ChatColor color = ChatColor.WHITE;
        this.setName(p, color);
    }

    private void addPlayer(Player p) {
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(System.currentTimeMillis());

        this.playerTime.put(p.getName(), (cal.getTimeInMillis() / 1000L));
        this.playerLocation.put(p.getName(), p.getLocation());
    }

    private void removePlayer(Player p) {
        if (this.afkPlayers.contains(p.getName())) {
            this.afkPlayers.remove(p.getName());
        }
        this.playerLocation.remove(p.getName());
        this.playerTime.remove(p.getName());
    }
    
     public String removeColors(String s) {
        s = ChatColor.stripColor(s);
        s = s.replaceAll("&[0-9a-fk-or]", "");
        return s;
    }

    public void updatePlayer(Player p) {
        this.addPlayer(p);
    }

    public boolean isAFK(Player p) {
        if (this.afkPlayers.contains(p.getName())) {
            return true;
        }
        return false;
    }

    public void markAFK(Player p) {
        this.setName(p, ChatColor.DARK_RED);
        this.addPlayer(p);
        this.afkPlayers.add(p.getName());
        p.sendMessage(this.replaceText(this.config.AFKStartMsg, ""));
    }

    public void markBack(Player p) {
        this.setName(p);
        this.updatePlayer(p);
        this.afkPlayers.remove(p.getName());
        p.sendMessage(this.replaceText(this.config.AFKEndMsg, ""));
    }

    public void kickPlayer(Player p) {
        this.removePlayer(p);
        p.kickPlayer(this.config.AFKKickMsg);
        p.getServer().broadcastMessage(this.replaceText(this.config.AFKKickBroadcast, p.getName()));
    }

    public boolean checkDistance(Player p) {
        Location last = this.playerLocation.get(p.getName());
        Location current = p.getLocation();
        if (this.Distance3D(last, current) >= config.AFKBlocksMoved) {
            return true;
        }
        return false;
    }

    public String replaceText(String string, String playername) {
        string = string.replaceAll("\\$p", playername);
        string = string.replaceAll("&(?=[0-9a-f])", "\u00A7");
        return string;
    }

    private int Distance3D(Location one, Location two) {
        //Our end result
        int result = 0;
        //Take x2-x1, then square it
        double part1 = Math.pow((two.getX() - one.getX()), 2);
        //Take y2-y1, then sqaure it
        double part2 = Math.pow((two.getY() - one.getY()), 2);
        //Take z2-z1, then square it
        double part3 = Math.pow((two.getZ() - one.getZ()), 2);
        //Add both of the parts together
        double underRadical = part1 + part2 + part3;
        //Get the square root of the parts
        result = (int) Math.sqrt(underRadical);
        //Return our result
        return result;
    }

    public String timeToString(Long time) {
        StringBuilder sb = new StringBuilder();
        String s = null;
        Integer temp = 0;

        Integer year = 31536000;
        Integer week = 604800;
        Integer day = 86400;
        Integer hour = 3600;
        Integer minute = 60;

        if (time >= year) {
            temp = (int) Math.floor(time / year);
            sb.append(temp + " year");
            if (temp > 1) {
                sb.append("s");
            }
            sb.append(", ");
            time = (time % year);
        }
        if (time >= week) {
            temp = (int) Math.floor(time / week);
            sb.append(temp + " week");
            if (temp > 1) {
                sb.append("s");
            }
            sb.append(", ");
            time = (time % week);
        }
        if (time >= day) {
            temp = (int) Math.floor(time / day);
            sb.append(temp + " day");
            if (temp > 1) {
                sb.append("s");
            }
            sb.append(", ");
            time = (time % day);
        }
        if (time >= hour) {
            temp = (int) Math.floor(time / hour);
            sb.append(temp + " hour");
            if (temp > 1) {
                sb.append("s");
            }
            sb.append(", ");
            time = (time % hour);
        }
        if (time >= minute) {
            temp = (int) Math.floor(time / minute);
            sb.append(temp + " minute");
            if (temp > 1) {
                sb.append("s");
            }
            sb.append(", ");
            time = (time % minute);
        }
        if (time > 0) {
            sb.append(time + " second");
            if (time > 1) {
                sb.append("s");
            }
            s = sb.toString();
        }
        if (time == 0) {
            s = sb.substring(0, sb.length() - 2);
        }


        return s;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player p = event.getPlayer();
        this.setName(p);
        this.addPlayer(p);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        this.removePlayer(p);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getTo().getBlock().equals(event.getFrom().getBlock())) {
            return;
        }

        Player player = event.getPlayer();

        if (this.afkPlayers.contains(player.getName())) {
            if (this.checkDistance(player)) {
                this.markBack(player);
            }
        }
    }
}
