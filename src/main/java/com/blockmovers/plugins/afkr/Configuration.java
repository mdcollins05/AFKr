/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.blockmovers.plugins.afkr;

/**
 *
 * @author MattC
 */
public class Configuration {

    AFKr plugin = null;
    public Integer minsToAFK = null;
    public Integer minsToKick = null;
    public String AFKStartMsg = null;
    public String AFKEndMsg = null;
    public String AFKKickMsg = null;
    public String AFKKickBroadcast = null;
    public Integer AFKBlocksMoved = null;

    public Configuration(AFKr plugin) {
        this.plugin = plugin;
    }

    public void loadConfiguration() {
        plugin.getConfig().addDefault("afkr.minsToAFK", 5);
        plugin.getConfig().addDefault("afkr.minsToKick", 20);
        plugin.getConfig().addDefault("afkr.startmsg", "&f[&1AFKr&f]&7 You have gone afk.");
        plugin.getConfig().addDefault("afkr.endmsg", "&f[&1AFKr&f]&7 You have returned.");
        plugin.getConfig().addDefault("afkr.kickmsg", "You were kicked for afking too long.");
        plugin.getConfig().addDefault("afkr.kickbroadcast", "&f[&1AFKr&f]&7 $p was kicked for afking too long.");
        plugin.getConfig().addDefault("afkr.blocks.moved.to.be.unafk", 3);
        plugin.getConfig().options().copyDefaults(true);
        //Save the config whenever you manipulate it
        plugin.saveConfig();

        this.setVars();
    }

    public void setVars() {
        minsToAFK = plugin.getConfig().getInt("afkr.minsToAFK");
        minsToKick = plugin.getConfig().getInt("afkr.minsToKick");
        AFKStartMsg = plugin.getConfig().getString("afkr.startmsg");
        AFKEndMsg = plugin.getConfig().getString("afkr.endmsg");
        AFKKickMsg = plugin.getConfig().getString("afkr.kickmsg");
        AFKKickBroadcast = plugin.getConfig().getString("afkr.kickbroadcast");
        AFKBlocksMoved = plugin.getConfig().getInt("afkr.blocks.moved.to.be.unafk");
    }
}
