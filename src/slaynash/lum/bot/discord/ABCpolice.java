package slaynash.lum.bot.discord;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class ABCpolice {
    public static boolean abcPolice(MessageReceivedEvent event) {
        if (event.getChannel().getIdLong() != 815364277123940423L) //modders and chill
            return false;
        List<Message> history = new ArrayList<>(event.getTextChannel().getHistoryBefore(event.getMessage(), 5).complete().getRetrievedHistory());
        //TODO check if Lum already yelled at them
        history.removeIf(m -> m.getAuthor().isBot());
        if (history.size() == 0) //new channel or wipe or bot spam
            return true;
        char currentLetter = event.getMessage().getContentDisplay().toLowerCase().charAt(0);
        char previousLetter = history.get(0).getContentDisplay().toLowerCase().charAt(0);

        if (previousLetter == 'z')
            previousLetter = 'a' - 1;

        if ((int) currentLetter != (int) (previousLetter) + 1) {
            System.out.println("abc does not match");
            event.getChannel().sendMessage(event.getMember().getNickname() + " just broke the chain <:Neko_sad:865328470652485633> Start back from `A`")/*.delay(Duration.ofSeconds(30)).flatMap(Message::delete)*/.queue();
            return true;
        }
        else if (event.getMessage().getContentDisplay().length() == 1) {
            System.out.println("abc hey that is cheating ");
            event.getMessage().addReaction(":baka:828070018935685130").queue();
            event.getChannel().sendMessage("Hey that is cheating <:Neko_pout:865328471102324778>")/*.delay(Duration.ofSeconds(30)).flatMap(Message::delete)*/.queue();
            return true;
        }
        else if (history.size() > 1 && history.get(0).getAuthor().equals(event.getAuthor()) && !history.get(1).getAuthor().equals(event.getAuthor())) {
            System.out.println("abc spacing not meet");
            event.getChannel().sendMessage("Spacing was not meet <:Neko_sad:865328470652485633> Start back from `A`")/*.delay(Duration.ofSeconds(30)).flatMap(Message::delete)*/.queue();
            return true;
        }

        //valid ABC
        return true;
    }
}
