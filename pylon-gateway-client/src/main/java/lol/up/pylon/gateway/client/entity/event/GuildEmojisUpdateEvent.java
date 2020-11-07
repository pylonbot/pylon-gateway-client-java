package lol.up.pylon.gateway.client.entity.event;

import lol.up.pylon.gateway.client.entity.Emoji;
import lol.up.pylon.gateway.client.service.GatewayCacheService;

import java.util.List;
import java.util.stream.Collectors;

public interface GuildEmojisUpdateEvent extends Event<GuildEmojisUpdateEvent> {

    default List<Emoji> getEmojis() {
        if (!(this instanceof pylon.rpc.discord.v1.event.GuildEmojisUpdateEvent)) {
            throw new IllegalStateException(getClass().getSimpleName() + " interface might only be implemented by " +
                    "pylon.rpc.discord.v1.event." + getClass().getSimpleName());
        }
        final pylon.rpc.discord.v1.event.GuildEmojisUpdateEvent event =
                (pylon.rpc.discord.v1.event.GuildEmojisUpdateEvent) this;
        return event.getPayload().getEmojisList().stream()
                .map(emojiData -> new Emoji(GatewayCacheService.getSingleton(), getBotId(), emojiData))
                .collect(Collectors.toList());
    }

}
