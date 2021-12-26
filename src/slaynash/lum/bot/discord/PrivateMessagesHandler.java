package slaynash.lum.bot.discord;

import java.util.List;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.Message.Attachment;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;

public class PrivateMessagesHandler {
    public static final String LOG_IDENTIFIER = "PrivateMessagesHandler";

    public static void handle(MessageReceivedEvent event) {

        if (event.getAuthor().getIdLong() != JDAManager.getJDA().getSelfUser().getIdLong()) {
            System.out.println(String.format("[DM] %s%s%s: %s",
                    event.getAuthor().getAsTag(),
                    event.getMessage().isEdited() ? " *edited*" : "",
                    event.getMessage().getType().isSystem() ? " *system*" : "",
                    event.getMessage().getContentRaw().replace("\n", "\n\t\t")));
            List<Attachment> attachments = event.getMessage().getAttachments();
            if (attachments.size() > 0) {
                System.out.println(attachments.size() + " Files");
                for (Attachment a : attachments)
                    System.out.println(" - " + a.getUrl());
            }
            if (ScamShield.checkForFishingPrivate(event)) {
                System.out.println("I was DM'd a Scam");
            }
            
            Guild mainguild = JDAManager.getJDA().getGuildById(145556654241349632L);
            TextChannel guildchannel = mainguild.getTextChannelsByName("dm-" + event.getAuthor().getIdLong(), true).stream().findFirst().orElse(null);
            if (guildchannel == null)
                guildchannel = mainguild.createTextChannel("dm-" + event.getAuthor().getIdLong()).complete(); // This may stuck the main thread if it takes time
            if (guildchannel != null)
                guildchannel.sendMessage(event.getMessage()).queue();
        }
        // CommandManager.runAsClient(event);
    }

    public static void handle(MessageUpdateEvent event) {
        //handle(new MessageReceivedEvent(event.getJDA(), event.getResponseNumber(), event.getMessage()));
    }
}
