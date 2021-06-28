package slaynash.lum.bot.discord;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdatePendingEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege;
import net.dv8tion.jda.api.interactions.commands.privileges.CommandPrivilege.Type;
import slaynash.lum.bot.ConfigManager;
import slaynash.lum.bot.DBConnectionManagerShortUrls;
import slaynash.lum.bot.Localization;
import slaynash.lum.bot.discord.commands.Slash;
import slaynash.lum.bot.discord.melonscanner.MelonScanner;
import slaynash.lum.bot.steam.Steam;


public class Main extends ListenerAdapter {
    public static JDA jda;
    public static boolean isShuttingDown = false;

    public static void main(String[] args) throws LoginException, IllegalArgumentException, InterruptedException, RateLimitedException {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            isShuttingDown = true;
            MelonScanner.shutdown();
            
            JDA jda = JDAManager.getJDA();
            if (jda != null && jda.getSelfUser().getIdLong() == 275759980752273418L) // Lum (blue)
            jda
                .getGuildById(633588473433030666L)
                .getTextChannelById(808076226064941086L)
                .sendMessageEmbeds(JDAManager.wrapMessageInEmbed("Lum is shutting down", Color.orange))
                .complete();
        }));

        ConfigManager.init();
        Localization.init();

        DBConnectionManagerShortUrls.init();

        new Steam().start();
        
        loadLogchannelList();
        loadVerifychannelList();
        loadReactionsList();
        loadScreeningRolesList();
        loadMelonLoaderVersions();
        loadMLHashes();
        loadMLVRCHash();
        loadMLReportChannels();
        //loadBrokenVRCMods();
        loadVRCBuild();
        loadGuildConfigs();

        MelonScanner.init();

        CommandManager.init();
        JDAManager.init(ConfigManager.discordToken);
        
        JDAManager.getJDA().getPresence().setActivity(Activity.watching("melons getting loaded"));

        if (JDAManager.getJDA().getSelfUser().getIdLong() == 275759980752273418L) // Lum (blue)
            JDAManager.getJDA()
                .getGuildById(633588473433030666L)
                .getTextChannelById(808076226064941086L)
                .sendMessageEmbeds(JDAManager.wrapMessageInEmbed("Lum restarted successfully !", Color.green))
                .queue();

        VRCApiVersionScanner.init();

        registerCommands();

        System.out.println("LUM Started!");
    }

    private static void loadReactionsList() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("rolereactions.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ", 4);
                if(parts.length == 3 && parts[0].matches("^\\d+$") && parts[2].matches("^\\d+$")) {
                    CommandManager.reactionListeners.add(new ReactionListener(parts[0], parts[1], parts[2]));
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadScreeningRolesList() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("rolescreening.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ", 4);
                if(parts.length == 2 && parts[0].matches("^\\d+$") && parts[1].matches("^\\d+$")) {
                    CommandManager.autoScreeningRoles.put(Long.parseLong(parts[0]), Long.parseLong(parts[1]));
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void loadMLHashes() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("mlhashes.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.equals("")) {
                    String[] parts = line.split(" ", 3);
                    if (parts[0].equals("r"))
                        CommandManager.melonLoaderHashes.add(new MLHashPair(parts[1], parts[2]));
                    else
                        CommandManager.melonLoaderAlphaHashes.add(new MLHashPair(parts[1], parts[2]));

                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadMLVRCHash() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("mlvrchash.txt"));
            CommandManager.melonLoaderVRCHash = reader.readLine();
            CommandManager.melonLoaderVRCMinDate = reader.readLine();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*
    private static void loadBrokenVRCMods() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("brokenvrcmods.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().equals(""))
                    CommandManager.brokenVrchatMods.add(line.trim());
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    */
    private static void loadVRCBuild() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("vrchatbuild.txt"));
            String line = reader.readLine();
            if (line != null)
                CommandManager.vrchatBuild = line.trim();
            else
                CommandManager.vrchatBuild = "1";
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void loadMLReportChannels() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("mlreportchannels.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ", 4);
                if(parts.length == 2 && parts[0].matches("^\\d+$") && parts[1].matches("^\\d+$")) {
                    CommandManager.mlReportChannels.put(Long.parseLong(parts[0]), parts[1]);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void loadLogchannelList() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("logchannels.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ", 4);
                if(parts.length == 2 && parts[0].matches("^\\d+$") && parts[1].matches("^\\d+$")) {
                    CommandManager.logChannels.put(Long.parseLong(parts[0]), parts[1]);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void loadVerifychannelList() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("verifychannels.txt"));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ", 5);
                if(parts.length == 3 && parts[0].matches("^\\d+$") && parts[1].matches("^\\d+$") && parts[2].matches("^\\d+$")) {
                    CommandManager.verifyChannels.put(Long.parseLong(parts[0]), new VerifyPair(parts[1], parts[2]));
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private static void loadMelonLoaderVersions() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("melonloaderversions.txt"));
            MelonScanner.latestMLVersionRelease = reader.readLine().trim();
            MelonScanner.latestMLVersionBeta = reader.readLine().trim();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadGuildConfigs() {
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader("guildconfigurations.txt"));
            String line;
            HashMap<Long, Boolean[]> tempMap = new HashMap<>();
            while ((line = reader.readLine()) != null) {
                String[] broke = line.split(" ");
                Boolean[] tempBooleans = new Boolean[GuildConfigurations.ConfigurationMap.values().length];
                for (int i = 1; i < broke.length; i++)
                    tempBooleans[i - 1] = Boolean.parseBoolean(broke[i]);
                tempMap.put(Long.parseLong(broke[0]), tempBooleans);
            }
            reader.close();
            GuildConfigurations.configurations = tempMap;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.isFromType(ChannelType.PRIVATE)) {
            PrivateMessagesHandler.handle(event);
        } else {
            ServerMessagesHandler.handle(event);
        }
    }

    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {
        //System.out.println("[" + event.getGuild().getName() + "] [#" + event.getChannel().getName() + "] " + event.getUser().getName() + " reacted with " + event.getReactionEmote().getName() + " (isEmote: " + event.getReactionEmote().isEmote() + ", is Emoji: " + EmojiUtils.containsEmoji(event.getReactionEmote().getName()) + ")");
        for (ReactionListener rl : CommandManager.reactionListeners) {
            if(event.getMessageId().equals(rl.messageId) && (event.getReactionEmote().isEmote() ? event.getReactionEmote().getEmote().getId().equals(rl.emoteId) : event.getReactionEmote().getName().equals(rl.emoteId))) {
                Role role = event.getGuild().getRoleById(rl.roleId);
                if(role != null) {
                    event.getGuild().addRoleToMember(event.getMember(), role).queue();
                    WriteLogMessage(event.getGuild(), "Added role `" + role.getName() + "` to <@" + event.getUser().getId() + ">");
                }
                return;
            }
        }
    }
    
    private void WriteLogMessage(Guild guild, String message) {
        String channelId = null;
        if((channelId = CommandManager.logChannels.get(guild.getIdLong())) != null) {
            for(TextChannel c : guild.getTextChannels()) {
                if(c.getId().equals(channelId)) {
                    ((TextChannel)c).sendMessageEmbeds(JDAManager.wrapMessageInEmbed(message, Color.gray)).queue();
                    break;
                }
            }
        }
    }

    @Override
    public void onGuildMessageReactionRemove(GuildMessageReactionRemoveEvent event) {
        for(ReactionListener rl : CommandManager.reactionListeners) {
            if(event.getMessageId().equals(rl.messageId) && (event.getReactionEmote().isEmote() ? event.getReactionEmote().getEmote().getId().equals(rl.emoteId) : event.getReactionEmote().getName().equals(rl.emoteId))) {
                Role role = event.getGuild().getRoleById(rl.roleId);
                if(role != null) {
                    event.getGuild().removeRoleFromMember(event.getUserId(), role).queue();
                    WriteLogMessage(event.getGuild(), "Removed role `" + role.getName() + "` from <@" + event.getUserId() + ">");
                }
                return;
            }
        }
    }

    @Override
    public void onGuildMemberUpdatePending(GuildMemberUpdatePendingEvent event) {
        long targetRoleId = CommandManager.autoScreeningRoles.get(event.getGuild().getIdLong());
        if (targetRoleId > 0) {
            Role role = event.getGuild().getRoleById(targetRoleId);
            if (role != null)
                event.getGuild().addRoleToMember(event.getMember(), role).queue();
        }
    }

    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event){
        Moderation.voiceJoin(event);
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event){
        Moderation.voiceLeave(event);
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event){
        event.getGuild().getOwner().getUser().openPrivateChannel().flatMap(channel -> channel.sendMessage(
            "Thank you for using Lum!\nLum has a few features that can be enabled like the Scam Shield.\n" +
            "If you would like any of these enabled use the command `/config` or contact us in Slaynash's server <https://discord.gg/akFkAG2>")).queue();
        event.getGuild().upsertCommand("config", "send server config buttons for this guild").queue(); // register Guild command for newly joined server
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        Slash.slashRun(event);
    }

    @Override
    public void onButtonClick(ButtonClickEvent event) {
        Slash.buttonUpdate(event);
    }

    private static void registerCommands(){
        JDAManager.getJDA().updateCommands().addCommands(new CommandData("configs", "send server config buttons")
            .addOption(OptionType.STRING, "guild", "Enter Guild ID", true)).queue(); // Global/DM command
        for(Guild guild : JDAManager.getJDA().getGuilds()){
            var cmd = guild.upsertCommand("config", "send server config buttons for this guild").setDefaultEnabled(false).complete(); // Guild command
            guild.updateCommandPrivilegesById(cmd.getIdLong(), Arrays.asList(new CommandPrivilege(Type.USER, true, guild.getOwnerIdLong()), new CommandPrivilege(Type.USER, true, 145556654241349632L/*Slay*/),new CommandPrivilege(Type.USER, true, 240701606977470464L/*rakosi2*/))).queue();
        }
    }
}
