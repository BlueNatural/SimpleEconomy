package com.simpleeconomy.bluenatural;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class Main extends JavaPlugin implements Listener {
	public static Plugin plugin;
	public static Server server;
	public static String pluginName;
	public static String pluginVersion;
	private static Economy econ = null;
	public static EconomyResponse r;
	String cslprefix = "[SimpleEconomy] ";
	
	@Override
    public void onLoad()
    {
	    plugin = this;
        server = plugin.getServer();
        NLog.setPluginLogger(plugin.getLogger());
        NLog.setServerLogger(plugin.getLogger());
        pluginName = plugin.getDescription().getName();
        pluginVersion = plugin.getDescription().getVersion();
        this.saveDefaultConfig();
    }
	public void loadingConfiguration(){
		String prefix = "prefix";
		plugin.getConfig().addDefault(prefix, "&7&l[&eSimple&cEconomy&7&l] ");
		
		String successfulsound = "successful-sound";
		plugin.getConfig().addDefault(successfulsound, "ENTITY_PLAYER_LEVELUP");
		String failingsound = "failing-sound";
		plugin.getConfig().addDefault(failingsound, "ITEM_SHIELD_BREAK");
		String nopermsound = "no-perm-sound";
		plugin.getConfig().addDefault(nopermsound, "ENTITY_ENDERDRAGON_GROWL");
		
		String depositcomplete = "deposit-complete";
		plugin.getConfig().addDefault(depositcomplete, "&aLấy tiền thành công");
		String depositfailing = "deposit-failing";
		plugin.getConfig().addDefault(depositfailing, "&cLấy tiền thất bại");
		
		String withdrawcomplete = "withdraw-complete";
		plugin.getConfig().addDefault(withdrawcomplete, "&aRút tiền thành công");
		
		String successfultitle = "successful-title";
		plugin.getConfig().addDefault(successfultitle, "&aTHÀNH CÔNG !");
		
		String moneywhenfirst = "money-when-first";
		plugin.getConfig().addDefault(moneywhenfirst, Double.valueOf(100));
		String takemoneydeath = "take-money-death";
		plugin.getConfig().addDefault(takemoneydeath, Double.valueOf(10));
		String activetakemoneydeath = "active-take-moneydeath";
		plugin.getConfig().addDefault(activetakemoneydeath, Boolean.valueOf(true));
		
		String reload = "reload";
		plugin.getConfig().addDefault(reload, "&aReload Successfully !");
		
		String noperm = "no-perm";
		plugin.getConfig().addDefault(noperm, "&cYou do not have permission to do that ! Do not do this again !");
		
		String stayingtime = "staying-time";
		plugin.getConfig().addDefault(stayingtime, Integer.valueOf(4));
		//20 ticks * 4 = 80 ticks
	    //Show how much title appear and disappear
		
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
		
	}
	PluginDescriptionFile pdf = getDescription();
	@Override 
	public void onEnable(){
		ConsoleCommandSender console = getServer().getConsoleSender();
		if(setupEconomy()){
			plugin = this;
			getServer().getPluginManager().enablePlugin(this);
			console.sendMessage(this.cslprefix + ChatColor.GREEN + "The plugin will start in a few seconds");
			getServer().getPluginManager().registerEvents(this, this);
			loadingConfiguration();
			console.sendMessage(this.cslprefix + ChatColor.YELLOW + "The plugin started");
			console.sendMessage(this.cslprefix + ChatColor.AQUA + pdf.getName() + pdf.getVersion());
		}else if(!setupEconomy()){
			console.sendMessage(this.cslprefix + ChatColor.RED + "Not found it,sure you have been installed Vault before starting this plugin");
			getServer().getPluginManager().disablePlugin(this);
		}
	}
	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
			}
			RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
			if (rsp == null) {
				return false;
			}
			econ = (Economy) rsp.getProvider();
			return econ != null;
		
	}
	@EventHandler
	public void onJoin(PlayerJoinEvent e){
		Player pl = e.getPlayer();
		Location l = pl.getLocation();
		double money = plugin.getConfig().getDouble("money-when-first");
		if(!pl.hasPlayedBefore()){
        econ.depositPlayer(pl, money);
        if(r.transactionSuccess()){
        	pl.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("prefix"))+
        			ChatColor.translateAlternateColorCodes('&', "&aĐã gửi số tiền" + money + "&avào" + pl));
        	
        }else{
        pl.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("prefix"))+
        		ChatColor.RED + "Đã xảy ra lỗi,vui lòng báo cáo quản trị viên");
        pl.playSound(l, Sound.valueOf(Main.this.getConfig().getString("no-perm-sound")), 4.0F, 1.0F);
        }
			
		}else{
			
		}
							}
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onDeath(PlayerDeathEvent e){
		Player player = e.getEntity();
		Location lo = player.getLocation();
		Double yourmoney = econ.getBalance(player.getName());
		if(getConfig().getBoolean("active-take-moneydeath")){
			if(yourmoney >= getConfig().getDouble("take-money-death")){
			econ.withdrawPlayer(player, getConfig().getDouble("take-money-death"));
			if(r.transactionSuccess()){
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("prefix"))+
						ChatColor.translateAlternateColorCodes('&', "&cBạn đã mất" + getConfig().getDouble("take-money-death")));
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("prefix"))+
						ChatColor.translateAlternateColorCodes('&', "&aSố tiền hiện tại là:" + econ.getBalance(player)));
				player.playSound(lo, Sound.valueOf(Main.this.getConfig().getString("no-perm-sound")), 4F, 1F);
			}else{
				 
			        
			}
			}else if(yourmoney < getConfig().getDouble("take-money-death")){
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("prefix"))+
						ChatColor.translateAlternateColorCodes('&', "&4Tịch thu số tiền hiện có:" + econ.getBalance(player)));
				econ.withdrawPlayer(player, yourmoney);
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("prefix"))+
						ChatColor.translateAlternateColorCodes('&', "&aSố tiền hiện có là" + r.balance));
				
			}
				
			
		}else{
			
		}
	}

	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
		ConsoleCommandSender console = Bukkit.getConsoleSender();
		if (!(sender instanceof Player)) {
			console.sendMessage(this.cslprefix + ChatColor.RED + "This command only use in game");				
			}else{
				Player p = (Player) sender;
				Location loc = p.getLocation();
				if(cmd.getName().equalsIgnoreCase("simpleeconomy")){
					if(args.length < 1){
						if(p.hasPermission("se.help")){
							p.playSound(loc, Sound.valueOf(Main.this.getConfig().getString("successful-sound")), 4.0F, 1.0F);
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&m------------------------------------------------"));
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b/simpleeconomy add <player> <số tiền>  &7chuyển tiền vào người chơi"));
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b/simpleeconomy remove <player> <số tiền> &7xóa số tiền khỏi người chơi"));
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b/simpleeconomy show &7xem tài khoản bản thân"));
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b/simpleeconomy reset <player> &7reset lại tiền của một ai đó"));
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&b/simpleeconomy reload &7reload plugin"));
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&ePlugin made by &bBlueNatural"));
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&m------------------------------------------------"));
							return true;
						}else{
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("prefix"))+ 
									ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("no-perm")));
							p.playSound(loc, Sound.valueOf(Main.this.getConfig().getString("no-perm-sound")), 4F, 1F);
						
							return true;
						}
						}else if(args.length == 3 && args[0].equalsIgnoreCase("add")){
						   if(p.hasPermission("so.deposit")){
							Player target = Bukkit.getPlayer(args[1]);
							double depositAmount = Double.valueOf(args[2]);
							econ.depositPlayer(target, depositAmount);
							if(r.transactionSuccess()){
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("prefix"))+ 
									ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("deposit-complete"))+ r.amount);
							Titles.sendTitle(p, ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("successful-title")), "", 20, Main.this.getConfig().getInt("staying-time") *20, 20);
							p.playSound(loc, Sound.valueOf(Main.this.getConfig().getString("successful-sound")), 4.0F, 1.0F);
							return true;
						}else{
							p.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("prefix"))+ 
									ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("deposit-failing"))+ r.amount);
							return true;			
						}
						   
				}else{
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("prefix"))+ 
							ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("no-perm")));
					p.playSound(loc, Sound.valueOf(Main.this.getConfig().getString("no-perm-sound")), 4.0F, 1F);
				}
					}else if(args.length == 1 && args[0].equalsIgnoreCase("show")){
					if(p.hasPermission("se.show")){
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', "&aSố tiền của" + p.getDisplayName() + "&alà" + econ.getBalance(p)));
					return true;
				}else{
					p.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("prefix"))+ 
							ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("no-perm")));
					p.playSound(loc, Sound.valueOf(Main.this.getConfig().getString("no-perm-sound")), 4F, 1F);
					return true;
					
				}					
				}else if(args.length == 3 && args[0].equalsIgnoreCase("remove")){
					if(p.hasPermission("se.withdraw")){
						Player target = Bukkit.getPlayer(args[1]);
						Double withdraw = Double.valueOf(args[2]);
					    		r = econ.withdrawPlayer(target, withdraw);
					    	if(r.transactionSuccess()){
					    		Titles.sendTitle(p, ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("successful-title")), "", 20, Main.this.getConfig().getInt("staying-time"), 20);
					    		p.playSound(loc, Sound.valueOf(Main.this.getConfig().getString("successful-sound")), 4.0F, 1.0F);
					    		p.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("withdraw-complete")));
					    		return true;				    
					    	}else{
					    		 p.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("prefix"))+
							        		ChatColor.RED + "Đã xảy ra lỗi,vui lòng báo cáo quản trị viên");
							        p.playSound(loc, Sound.valueOf(Main.this.getConfig().getString("no-perm-sound")), 4.0F, 1.0F);	
							      return true;
					    	}
					    	}else{
					    		p.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("prefix"))+ 
										ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("no-perm")));
								p.playSound(loc, Sound.valueOf(Main.this.getConfig().getString("no-perm-sound")), 4.0F, 1F);	
								
								return true;
					    	}
					    }else if(args.length == 2 && args[0].equalsIgnoreCase("reset")){
					    	if(p.hasPermission("se.reset")){
					    		Player target = Bukkit.getPlayer(args[1]);
					            r = econ.withdrawPlayer(target, econ.getBalance(target));
					             if(r.transactionSuccess()){
					            	 Titles.sendTitle(p, ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("successful-title")), "", 20, Main.this.getConfig().getInt("staying-time"), 20);
					            	 p.playSound(loc, Sound.valueOf(Main.this.getConfig().getString("successful-sound")), 4.0F, 1.0F);
					            	 p.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("prefix"))+ 
					            			 ChatColor.translateAlternateColorCodes('&', "&aĐã reset số tiền của người chơi") + r.balance);
					            	 return true;
					            	 
					             }else{
					            	p.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("prefix"))+ 
					            			ChatColor.translateAlternateColorCodes('&', "&cFailing to reset money !!"));
					            	return true;
					             }
					             
					    	}else{
					    		p.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("prefix"))+ 
										ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("no-perm")));
								p.playSound(loc, Sound.valueOf(Main.this.getConfig().getString("no-perm-sound")), 4.0F, 1F);	
								return true;	
					    	}
					    }else if(args.length == 1 && args[0].equalsIgnoreCase("reload")){
					    	if(p.hasPermission("se.reload")){
					    		ReloadPlugin();
					    		p.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("prefix"))+ 
					    				ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("reload")));
					    		return true;
					    	
					    	}else{
					    		p.sendMessage(ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("prefix"))+ 
										ChatColor.translateAlternateColorCodes('&', Main.this.getConfig().getString("no-perm")));
								p.playSound(loc, Sound.valueOf(Main.this.getConfig().getString("no-perm-sound")), 4.0F, 1F);	
								return true;	
					    	}
					  
						}
					}
			}
		return true;
					
						}
	private void ReloadPlugin() {
		plugin.reloadConfig();
		plugin.saveConfig();
		
		
	}
				}
