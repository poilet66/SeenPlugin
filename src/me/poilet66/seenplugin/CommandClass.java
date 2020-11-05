package me.poilet66.seenplugin;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;
import com.palmergames.bukkit.towny.object.Resident;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandClass implements CommandExecutor {

    //init variables
    public ChatColor allyColor, enemyColour, friendlyColour, neutralColour;
    private static Economy econ = null;
    final String[] measures = new String[]{" years"," months"," weeks,"," days"," hours"," minutes"," seconds"}; //formatting for last/first played
    final int[] modulus = new int[]{1, 12, 4, 365, 24, 60, 60}; //number of x(years months weeks etc) that go into the next highest measurement

    public CommandClass(SeenMain main) {
        this.econ = main.getEcon();
        try {
            allyColor = ChatColor.valueOf(main.getConfig().getString("relationcolours.ally").toUpperCase());
            enemyColour = ChatColor.valueOf(main.getConfig().getString("relationcolours.enemy").toUpperCase());
            friendlyColour = ChatColor.valueOf(main.getConfig().getString("relationcolours.friendly").toUpperCase());
            neutralColour = ChatColor.valueOf(main.getConfig().getString("relationcolours.neutral").toUpperCase());
        }
        catch (NullPointerException e) {
            main.getLogger().severe("Section missing from SeenPlugin config.yml, using default colours");
            allyColor = ChatColor.GOLD;
            enemyColour = ChatColor.RED;
            friendlyColour = ChatColor.GREEN;
            neutralColour = ChatColor.YELLOW;
        }
        catch (IllegalArgumentException e) {
            main.getLogger().severe("Section in SeenPlugin config.yml formatted incorrectly (Search valid chatcolour aliases), using default colours");
            allyColor = ChatColor.LIGHT_PURPLE;
            enemyColour = ChatColor.RED;
            friendlyColour = ChatColor.GREEN;
            neutralColour = ChatColor.YELLOW;
        }

    } //get economy from main

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(command.getName().equalsIgnoreCase("seen")) { // /seen command
            if(sender instanceof Player) { //if player
                String name;
                if(args.length > 0) { //if any arguments are given
                    name = args[0]; //name = first argument
                }
                else { //if no arguments given
                    name = sender.getName(); //just use sender's name
                }
                OfflinePlayer player = Bukkit.getOfflinePlayer(name); //get offline player by name
                if (player.hasPlayedBefore() || player.isOnline()) {

                    sender.sendMessage(ChatColor.GOLD + "-----------[" + ChatColor.DARK_GREEN + "Player " + player.getName() + ChatColor.GOLD + "]-----------"); //header
                    if(sender.hasPermission("seenplugin.uuid")) {
                        sender.sendMessage(ChatColor.AQUA + "UUID: " + ChatColor.LIGHT_PURPLE + player.getUniqueId()); //UUID
                    }
                    //sender.sendMessage(ChatColor.AQUA + "Display Name: " + player.getPlayer().getDisplayName()); //Display Name, doesnt work while players offline
                    sender.sendMessage(ChatColor.AQUA + "Name: " + ChatColor.LIGHT_PURPLE + player.getName()); //Name
                    //sender.sendMessage(ChatColor.AQUA + "Display Name: " + ChatColor.LIGHT_PURPLE + player.getPlayer().getDisplayName()); //Display name, only works when online
                    sender.sendMessage(getLastPlayed(name)); //last played
                    sender.sendMessage(getFirstPlayed(name)); //first played
                    sender.sendMessage(ChatColor.AQUA + "Balance: " + ChatColor.LIGHT_PURPLE + econ.format(econ.getBalance(player))); //balance
                    sender.sendMessage(getTown(name, (Player) sender)); //town name
                    sender.sendMessage(getNation(name, (Player) sender)); //nation name
                    if(sender.hasPermission("seenplugin.world")) {
                        if(player.isOnline()) {
                            sender.sendMessage(ChatColor.AQUA + "World: " + ChatColor.LIGHT_PURPLE + player.getPlayer().getWorld().getName());
                            sender.sendMessage(ChatColor.AQUA + "Coordinates: " + ChatColor.LIGHT_PURPLE + "X: " + player.getPlayer().getLocation().getBlockX() + " Y: " + player.getPlayer().getLocation().getBlockY() + " Z: " + player.getPlayer().getLocation().getBlockZ());
                        }
                        else {
                            sender.sendMessage(ChatColor.AQUA + "World: " + ChatColor.RED + "Not online");
                            sender.sendMessage(ChatColor.AQUA + "Coordinates: " + ChatColor.RED + "Not online");
                        }
                    }

                }
                else {
                    sender.sendMessage(ChatColor.RED + "No player matches " +  "\"" + ChatColor.LIGHT_PURPLE + name + ChatColor.RED + "\""); //if player hasn't played before
                }

            }
            else {
                sender.sendMessage(ChatColor.RED + "Only players can run this command.");
            }

            return true;
        }

        return false;
    }

    private String getLastPlayed(String name) {

        int[] lastTimes = new int[7];
        long current = System.currentTimeMillis(); //millis since 1st jan 1970
        OfflinePlayer player = Bukkit.getOfflinePlayer(name); //get offline player by name

        lastTimes[6] = (int) ((current - player.getLastPlayed()) / 1000); //seconds
        lastTimes[5] = (lastTimes[6] / 60); //minutes
        lastTimes[4] = (lastTimes[5] / 60); //hours
        lastTimes[3] = (lastTimes[4] / 24); //days
        lastTimes[2] = (lastTimes[3] / 7); //weeks
        lastTimes[1] = (lastTimes[2] / 12); //months
        lastTimes[0] = lastTimes[1] / 365; //years

        if(Bukkit.getPlayer(name) == null) { //if not online
            int count = 0;
            for(int item : lastTimes) {
                if(item > 0 && count != lastTimes.length - 1) { //if theyve played for more than 0 (unit) and not on seconds
                    return ChatColor.AQUA + "Last played: " + ChatColor.LIGHT_PURPLE + lastTimes[count] % modulus[count] + measures[count] + " and " + lastTimes[count+1] % modulus[count+1] + measures[count+1] + " ago.";
                }
                else if(count == lastTimes.length - 1) { //if only on seconds
                    return ChatColor.AQUA + "Last played: " + ChatColor.LIGHT_PURPLE + lastTimes[count] % modulus[count] + measures[count] + " ago.";
                }
                count++;
            }
        }
        else { //if online
            return ChatColor.AQUA + "Last Played: " + ChatColor.GREEN + "Online right now.";
        }

        return ChatColor.RED + "Error";

    }

    private String getFirstPlayed(String name) {
        int[] firstTimes = new int[7];
        long current = System.currentTimeMillis(); //millis since 1st jan 1970
        OfflinePlayer player = Bukkit.getOfflinePlayer(name); //get offline player by name

        firstTimes[6] = (int) ((current - player.getFirstPlayed()) / 1000); //seconds
        firstTimes[5] = (firstTimes[6] / 60); //minutes
        firstTimes[4] = (firstTimes[5] / 60); //hours
        firstTimes[3] = (firstTimes[4] / 24); //days
        firstTimes[2] = (firstTimes[3] / 7); //weeks
        firstTimes[1] = (firstTimes[2] / 12); //months
        firstTimes[0] = firstTimes[1] / 365; //years

        int count = 0;
        for(int item : firstTimes) {
            if (item > 0 && count != firstTimes.length - 1) { //if theyve played for more than 0 (unit) and not not seconds
                return ChatColor.AQUA + "First played: " + ChatColor.LIGHT_PURPLE + firstTimes[count] % modulus[count] + measures[count] + " and " + firstTimes[count + 1] % modulus[count + 1] + measures[count + 1] + " ago.";
            } else if (count == firstTimes.length - 1) { //if only on seconds
                return ChatColor.AQUA + "First played: " + ChatColor.LIGHT_PURPLE + firstTimes[count] % modulus[count] + measures[count] + " ago.";
            }
            count++;
        }
        return ChatColor.RED + "Error";
    }

    private String getTown(String name, Player sender) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        try {
            Resident res = TownyAPI.getInstance().getDataSource().getResident(player.getName()); //try get player from towny database
            try {
                return (ChatColor.AQUA + "Town: " + getRelationColour(name, sender) + res.getTown().getName());
            }
            catch (NotRegisteredException e) {
                return (ChatColor.AQUA + "Town: " + ChatColor.GRAY + "No Town.");
            }

        } catch (NotRegisteredException e) { //if not in towny database, will print 'error', this will happen if the seen plugin is loaded up before towny is even installed on the server, wont be a problem if both uploaded at the same time or towny first.
            e.printStackTrace();
            return (ChatColor.AQUA + "Town: " + ChatColor.RED + "Error");
        }
    }

    private String getNation(String name, Player sender) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(name);
        try {
            Resident res = TownyAPI.getInstance().getDataSource().getResident(player.getName()); //try get player from towny database
            try {
                return (ChatColor.AQUA + "Nation: " + getRelationColour(name, sender) + res.getTown().getNation().getName());
            }
            catch (NotRegisteredException e) {
                return (ChatColor.AQUA + "Nation: " + ChatColor.GRAY + "No Nation.");
            }

        } catch (NotRegisteredException e) { //likewise to line 140
            e.printStackTrace();
            return (ChatColor.AQUA + "Town: " + ChatColor.RED + "Error");
        }
    }

    private ChatColor getRelationColour(String targetName, Player sender) { //only called if they have a town
        OfflinePlayer player = Bukkit.getOfflinePlayer(targetName);
        try {
            Resident targetRes = TownyAPI.getInstance().getDataSource().getResident(player.getName()); //try get player from towny database
            Resident senderRes = TownyAPI.getInstance().getDataSource().getResident(sender.getName()); //try get player from towny database

            if(targetRes.hasNation() && senderRes.hasNation()) { //if both players in a nation
                if(targetRes.getTown().getNation().getEnemies().contains(senderRes.getTown().getNation())) { //if enemies
                    return enemyColour;
                }
                else if(targetRes.getTown().getNation().getAllies().contains(senderRes.getTown().getNation())) { //if allies
                    return allyColor;
                }
                else if(targetRes.getTown().getNation() == senderRes.getTown().getNation()) { //if same nation
                    return friendlyColour;
                }
                else { //if not allies, enemies or same nation
                    return neutralColour;
                }
            }
            else { //if either or both players arent in a nation
                return neutralColour;
            }

        } catch (NotRegisteredException e) {
            e.printStackTrace();
        }
        return null; //error
    }

}
