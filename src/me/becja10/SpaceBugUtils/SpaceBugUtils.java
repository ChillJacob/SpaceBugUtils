package me.becja10.SpaceBugUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import me.becja10.SpaceBugUtils.FileManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class SpaceBugUtils extends JavaPlugin implements Listener
{
	public final Logger logger = Logger.getLogger("Minecraft");
	private static SpaceBugUtils plugin;
	private Player lastAttacker;
	private Entity dragon;

	private Map<String, Long> joined = new HashMap<String, Long>();
	private List<String> list = new LinkedList<String>();

	public void onEnable()
	{
		PluginDescriptionFile pdfFile = getDescription();
		this.logger.info(pdfFile.getName() + " Version " + pdfFile.getVersion() + " Has Been Enabled!");
		getServer().getPluginManager().registerEvents(this, this);
		plugin = this;
	    FileManager.saveDefaultPlayers();

	}

	public void onDisable()
	{
		PluginDescriptionFile pdfFile = getDescription();
		this.logger.info(pdfFile.getName() + " Has Been Disabled!");
	}
	
	@EventHandler
	public void onDispense(BlockDispenseEvent event)
	{
		Block block = event.getBlock();
		if(block.getState() instanceof Dispenser)
		{
			Dispenser dis = (Dispenser)block.getState();
			Inventory inv = dis.getInventory();
			for(int i = 0; i < inv.getSize(); i++)
			{
				ItemStack stack = inv.getItem(i);
				if(stack != null && stack.getAmount() < 0)
				{
					inv.setItem(i, new ItemStack(Material.AIR));
					event.setCancelled(true);
				}
			}
		}
	}
	
	@EventHandler
	public void onOpenInv(InventoryOpenEvent event)
	{
		for(int i = 0; i < event.getInventory().getSize(); i++)
		{
			ItemStack stack = event.getInventory().getItem(i);
			if(stack != null && stack.getAmount() <= 0)
			{
				event.getInventory().setItem(i, new ItemStack(Material.AIR));
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onClickInv(InventoryClickEvent event)
	{
		for(int i = 0; i < event.getInventory().getSize(); i++)
		{
			ItemStack stack = event.getInventory().getItem(i);
			if(stack != null && stack.getAmount() <= 0)
			{
				event.getInventory().setItem(i, new ItemStack(Material.AIR));
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent event)
	{
		joined.put(event.getPlayer().getName(), System.currentTimeMillis());
		list.add(event.getPlayer().getName());
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onQuite(PlayerQuitEvent event)
	{
		joined.remove(event.getPlayer().getName());
		list.remove(event.getPlayer().getName());
	}
	
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		//onlinelongest
		if(cmd.getName().equalsIgnoreCase("onlinelongest"))
		{
			if(sender instanceof Player && !sender.hasPermission("spacebugutils.onlinelongest"))
				sender.sendMessage("You ain't got the perms foo!");
			else
			{
				long curTime = System.currentTimeMillis();
				for(String p : list)
				{
					//if this player is no longer online, remove from list
					if(Bukkit.getPlayer(p) == null)
					{
						list.remove(p);
						continue;
					}
					Integer t = (int) (curTime - joined.get(p))/1000;
					
					String time = "";
					if (t >= 3600)
					{
						Integer hours = t / 3600; //how many hours
						Integer min = (t - (hours * 3600))/60;// left over minutes
						time = hours.toString() + " hour(s) " + min.toString() + " minutes";
					}
					else if (t >= 60)
					{
						Integer min = t/60;
						time = min.toString() + " minutes";
					}
					else
						time = t.toString() + " seconds";
					sender.sendMessage(ChatColor.GREEN+p+ " " + time);
				}
			}
		}
		
		//silentTP
		else if(cmd.getName().equalsIgnoreCase("silenttp"))
		{
			if(!(sender instanceof Player))
				sender.sendMessage("This command can only be run by a player.");
			else
			{
				Player p = (Player) sender;
				if(!p.hasPermission("spacebugutils.silenttp")) return true;
				switch (args.length)
				{
				case 1: //proper amount of players sent
					//make sure the target is online/real
					Player tar = Bukkit.getPlayer(args[0]);
					if(tar == null)
						p.sendMessage(ChatColor.RED+"Player not found.");
					else
					{
						tar.teleport(p.getLocation());
						p.sendMessage("Very sneaky");
					}
					break;
				default: //they screwed up.
					return false;
				}
			}
		}
		
		//sbureload
		else if(cmd.getName().equalsIgnoreCase("sbureload"))
		{
			//if is player, and doesn't have permission
			if((sender instanceof Player) && !(sender.hasPermission("spacebugutils.reload")))
					sender.sendMessage(ChatColor.DARK_RED+"No permission.");
			else
			{
				FileManager.reloadPlayers();
				//loadConfig();
			}
		}
		return true;
	}

	@EventHandler(priority=EventPriority.HIGHEST)
	public void onClick(PlayerInteractEvent event)
	{
		Player p = event.getPlayer();
		if(p.hasPermission("spacebugutils.changespawner")) return;
		
		Material inHand = p.getItemInHand().getType();
		//prevent players changing mob spanwers
		if(inHand == Material.MONSTER_EGG || inHand == Material.MONSTER_EGGS)
			if(event.getAction() == Action.RIGHT_CLICK_BLOCK)
				if(event.getClickedBlock().getType() == Material.MOB_SPAWNER)
					event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onEntityDeath(EntityDeathEvent event)
	{
		//only care about dragons
		if(event.getEntityType() != EntityType.ENDER_DRAGON) return;
		//make sure this is the same dragon that just got attacked. Should never not be the case, but whatevs
		if(!dragon.equals(event.getEntity())) return;
		
		//get the last attacker of the dragon (the one that caused the death)
		Player slayer = lastAttacker;
		
		String id = slayer.getUniqueId().toString();
		DateFormat dateformat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		
		FileManager.getPlayers().set(id+".name", slayer.getName());
		FileManager.getPlayers().set(id+".on", dateformat.format(date));
		FileManager.savePlayers();
		
		slayer.sendMessage(ChatColor.GOLD+"Congratulations on slaying the dragon!");
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onHarm(EntityDamageByEntityEvent event)
	{
		//only care about dragons
		if(event.getEntityType() != EntityType.ENDER_DRAGON) return;
		dragon = event.getEntity();
		Player slayer = null;
		
		//check if the player is damaging directly
		if(event.getDamager() instanceof Player)
			slayer = (Player)event.getDamager();
		
		//check if it was an arrow fired by a player
		else if (event.getDamager() instanceof Arrow)
		{
			Arrow arrow = (Arrow)event.getDamager();
			if(arrow.getShooter() instanceof Player)
				slayer = (Player)arrow.getShooter();
		}
		
		//wasn't a player who damaged the dragon
		if(slayer == null) return;
		
		//see if they've killed the dragon before
		String id = slayer.getUniqueId().toString();
		lastAttacker = slayer;
		if(FileManager.getPlayers().contains(id))
		{
			slayer.sendMessage(ChatColor.GOLD+"Stay your weapon, slayer. Allow someone else to kill the beast!");
			event.setDamage(0);; //hopefully this will prevent mcMMO from interferring.
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onPlayerMove(PlayerMoveEvent event)
	{
		Player p = event.getPlayer();
		if (p.hasPermission("spacebugutils.nether")) return;
		Location to = event.getTo();
		if (to.getWorld().getEnvironment().equals(World.Environment.NETHER))
		{
			if (to.getBlockY() > 127)
			{
				p.performCommand("spawn");
				p.sendMessage(ChatColor.RED + "Going above the Nether ceiling is not allowed");
				this.logger.info("[SpaceBugUtils] "+ p.getName() + " was prevented from going above nether.");
				event.setTo(event.getFrom());
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	public void onTP(PlayerTeleportEvent event)
	{
		Player p = event.getPlayer();
		if (p.hasPermission("spacebugutils.nether")) return;
		Location to = event.getTo();
		if (to.getWorld().getEnvironment().equals(World.Environment.NETHER))
		{
			if (to.getBlockY() > 127)
			{
				p.sendMessage(ChatColor.RED + "Going above the Nether ceiling is not allowed");
				this.logger.info("[SpaceBugUtils] "+ p.getName() + " was prevented from going above nether.");
				event.setTo(event.getFrom());
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(priority=EventPriority.LOWEST)
	public void onChat(AsyncPlayerChatEvent event)
	{
		//don't lower mods/admins
		if(event.getPlayer().hasPermission("spacebugutils.chat")) return;
		String msg = event.getMessage();
		//loop over msg and count caps
		int caps = 0;
		int low = 0;
		for(Character c : msg.toCharArray())
		{
			if(Character.isUpperCase(c)) 
				caps++;
			else
				low++;
		}
		//if more than half the characters are caps, make them lower case
		if ((double) caps/low > 1 && msg.length() > 5)
			event.setMessage(msg.toLowerCase());
	}
	
	@SuppressWarnings("unused")
	private void print(String p){System.out.println(p);}
	public static JavaPlugin getInstance() {return plugin;}
}

