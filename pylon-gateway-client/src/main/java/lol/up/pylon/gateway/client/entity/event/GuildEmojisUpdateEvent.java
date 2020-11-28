package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.entity.Emoji;
import lol.up.pylon.gateway.client.service.CacheService;

import java.util.List;
import java.util.stream.Collectors;

public interface GuildEmojisUpdateEvent extends Event<GuildEmojisUpdateEvent> {

    default List<Emoji> getEmojis() {
        if (!(this instanceof bot.pylon.proto.discord.v1.event.GuildEmojisUpdateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "bot.pylon.proto.discord.v1.event." + getClass().getSimpleName());
        }
        final bot.pylon.proto.discord.v1.event.GuildEmojisUpdateEvent event =
                (bot.pylon.proto.discord.v1.event.GuildEmojisUpdateEvent) this;
        return event.getPayload().getEmojisList().stream()
                .map(emojiData -> new Emoji(CacheService.getSingleton(), getBotId(), emojiData))
                .collect(Collectors.toList());
    }

}
