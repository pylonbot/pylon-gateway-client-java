package lol.up.pylon.gateway.client.entity.builder;

import bot.pylon.proto.discord.v1.model.MessageData;
import bot.pylon.proto.discord.v1.rest.CreateMessageRequest;
import com.google.protobuf.ByteString;
import lol.up.pylon.gateway.client.entity.Message;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class MessageBuilder {

    private Optional<String> content;
    private Optional<MessageData.MessageEmbedData> embedData;
    private Optional<CreateMessageRequest.Attachment> attachment;
    private Optional<CreateMessageRequest.AllowedMentions> allowedMentions;
    private Optional<CreateMessageRequest.MessageReference> messageReference;
    private List<MessageData.MessageComponentData> components;

    public static MessageBuilder embed(final EmbedBuilder embedBuilder) {
        return new MessageBuilder()
                .setEmbed(embedBuilder);
    }

    public static MessageBuilder text(final String text) {
        return new MessageBuilder()
                .setContent(text);
    }

    public MessageBuilder() {
        this.content = Optional.empty();
        this.embedData = Optional.empty();
        this.attachment = Optional.empty();
        this.allowedMentions = Optional.empty();
        this.messageReference = Optional.empty();
        this.components = new ArrayList<>();
    }

    public void apply(final CreateMessageRequest.Builder request) {
        content.ifPresent(request::setContent);
        embedData.ifPresent(request::setEmbed);
        attachment.ifPresent(request::setAttachment);
        allowedMentions.ifPresent(request::setAllowedMentions);
        messageReference.ifPresent(request::setMessageReference);
        request.addAllComponents(components);
    }

    public MessageBuilder setContent(@Nullable final String content) {
        this.content = Optional.ofNullable(content);
        return this;
    }

    public MessageBuilder setEmbed(@Nullable final EmbedBuilder builder) {
        this.embedData = Optional.ofNullable(builder)
                .map(EmbedBuilder::toEmbedData);
        return this;
    }

    public MessageBuilder setEmbed(@Nullable final MessageData.MessageEmbedData messageEmbedData) {
        this.embedData = Optional.ofNullable(messageEmbedData);
        return this;
    }

    public MessageBuilder setEmbed(final Consumer<EmbedBuilder> builder) {
        final EmbedBuilder embedBuilder = new EmbedBuilder();
        builder.accept(embedBuilder);
        return this.setEmbed(embedBuilder);
    }

    public MessageBuilder setAttachment(final String name, final byte[] data) {
        this.attachment = Optional.of(CreateMessageRequest.Attachment.newBuilder()
                .setName(name)
                .setContent(ByteString.copyFrom(data))
                .build());
        return this;
    }

    public MessageBuilder setAllowedMentions(@Nullable final CreateMessageRequest.AllowedMentions allowedMentions) {
        this.allowedMentions = Optional.ofNullable(allowedMentions);
        return this;
    }

    public MessageBuilder setMessageReference(final Message message) {
        return setMessageReference(message, false);
    }

    public MessageBuilder setMessageReference(final Message message, final boolean failIfNotExists) {
        return setMessageReference(message.getGuildId(), message.getChannelId(), message.getId(), failIfNotExists);
    }

    public MessageBuilder setMessageReference(final long guildId, final long channelId, final long messageId) {
        return setMessageReference(guildId, channelId, messageId, false);
    }

    public MessageBuilder setMessageReference(final long guildId, final long channelId, final long messageId,
                                    final boolean failIfNotExists) {
        this.messageReference = Optional.of(CreateMessageRequest.MessageReference.newBuilder()
                .setGuildId(guildId)
                .setChannelId(channelId)
                .setMessageId(messageId)
                .setFailIfNotExists(failIfNotExists)
                .build());
        return this;
    }

    public MessageBuilder addButton(final MessageData.MessageComponentData button) {
        return addButton(0, button);
    }

    public MessageBuilder addButton(final int line, final MessageData.MessageComponentData button) {
        final int size = this.components.size();
        if (size >= line) {
            for (int i = size; i <= line; i++) {
                this.components.add(MessageData.MessageComponentData.newBuilder()
                        .setType(MessageData.MessageComponentData.MessageComponentType.ACTION_ROW)
                        .build());
            }
        }
        final MessageData.MessageComponentData.Builder replaceBuilder = this.components.remove(line).toBuilder();
        replaceBuilder.addComponents(button);
        this.components.add(line, replaceBuilder.build());
        return this;
    }
}
