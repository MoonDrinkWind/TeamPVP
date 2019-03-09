package china.moondrinkwind.minecraft.teampvp;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class TeamPVP extends JavaPlugin implements Listener {
    public HashMap<Player,List<Player>> teamList = new HashMap<>();
    public HashMap<Player,List<Player>> subordinateTeam = new HashMap<>();
    public Player targetPlayer;
    public Player senderPlayer;
    private Boolean isSender;
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(label.equalsIgnoreCase("team")){
            if(sender instanceof Player){
                Player player = (Player)sender;
                if(!(args.length == 0)){
                    if(args.length == 1 && args[0].equalsIgnoreCase("create")){
                        teamList.put(player,new ArrayList<Player>());
                        subordinateTeam.put(player,teamList.get(player));
                    }else if(args.length == 2 && args[0].equalsIgnoreCase("invite")){
                        if(Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(args[1]))){
                            targetPlayer = Bukkit.getPlayer(args[1]);
                            senderPlayer = player;
                            targetPlayer.sendMessage("§c玩家"+targetPlayer.getName()+"向你发送了组队PVP请求!");
                            targetPlayer.spigot().sendMessage(new ComponentBuilder("接受").color(ChatColor.GREEN).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/team accpet")).create());
                            targetPlayer.spigot().sendMessage(new ComponentBuilder("拒绝").color(ChatColor.RED).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/team refush")).create());
                            isSender = true;
                            player.sendMessage("§c消息发送完毕!");
                        }else{
                            player.sendMessage("§c此玩家不在线!");
                        }
                    }else if(args.length == 1 && args[0].equalsIgnoreCase("accept")){
                        if(isSender && player.equals(targetPlayer)){
                            subordinateTeam.put(targetPlayer,teamList.get(senderPlayer));
                            List<Player> list = subordinateTeam.get(senderPlayer);
                            list.add(player);
                            teamList.replace(targetPlayer,list);
                            player.sendMessage("§c接受成功!");
                            senderPlayer.sendMessage("§c对方接受了你的请求!");
                        }else{
                            player.sendMessage("§c邀请过期或没有邀请!");
                        }
                    }else if(args.length == 1 && args[0].equalsIgnoreCase("refush")){
                        if(isSender && player.equals(targetPlayer)){
                            senderPlayer.sendMessage("§c对方拒绝了你的请求!");
                        }else{
                            player.sendMessage("§c邀请过期或没有邀请!");
                        }
                    }else if(args.length == 1 && args[0].equalsIgnoreCase("list")){
                        List<Player> team = subordinateTeam.get(player);
                        player.sendMessage("队伍中的玩家");
                        for(Player p:team){
                            player.sendMessage(p.getName());
                        }
                    }else if(args.length == 2 && args[0].equalsIgnoreCase("PVP")){
                        if(Bukkit.getOnlinePlayers().contains(Bukkit.getPlayer(args[1]))){
                            Player targetPVPPlayer = Bukkit.getPlayer(args[1]);
                            if(teamList.containsValue(targetPVPPlayer)){
                                startPVP(player);
                            }else{
                                player.sendMessage("§c此玩家不是队长!");
                            }
                        }else{
                            player.sendMessage("§c玩家不在线!");
                        }
                    }else if(args.length == 1 && args[0].equalsIgnoreCase("exit")){
                        subordinateTeam.remove(player);
                        player.sendMessage("§cOK");
                    }else if(args.length == 1 && args[0].equalsIgnoreCase("dissolution")){
                        subordinateTeam.remove(player);
                        teamList.remove(player);
                        player.sendMessage("§cOK");
                    }
                }else{
                    return false;
                }
            }else{
                sender.sendMessage("你以为控制台能打群架?");
            }
        }
        return true;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        saveConfig();
        Bukkit.getPluginManager().registerEvents(this,this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public void startPVP(Player player){
        List<String> places = getConfig().getStringList("place");
        if(!(places.size() == 0)){
            int size = new Random().nextInt(places.size());
            Location location = new Location(Bukkit.getWorld(getConfig().getString(places.get(size)+".world")),
                    getConfig().getDouble(places.get(size)+"x"),
                    getConfig().getDouble(places.get(size)+".y"),
                    getConfig().getDouble(places.get(size)+".z"));
            List<Player> joinPlayer = new ArrayList<>();
            for (Player p:subordinateTeam.keySet()){
                p.teleport(location);
            }
        }else{
            player.sendMessage("§c服务器暂无PVP场地");
        }
    }
    @EventHandler
    public void onPVP(EntityDamageByEntityEvent event){
        if(event.getEntity() instanceof Player &&event.getDamager() instanceof Player){
            if(subordinateTeam.get(event.getEntity()).equals(subordinateTeam.get(event.getDamager()))){
                event.setCancelled(true);
            }
        }
    }
}
