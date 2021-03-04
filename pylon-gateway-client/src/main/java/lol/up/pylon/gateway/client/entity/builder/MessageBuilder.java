package lol.up.pylon.gateway.client.entity.builder;

import bot.pylon.proto.discord.v1.model.MessageData;
import bot.pylon.proto.discord.v1.rest.CreateMessageRequest;
import com.google.protobuf.ByteString;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class MessageBuilder {

    private Optional<String> content;
    private Optional<MessageData.MessageEmbedData> embedData;
    private Optional<CreateMessageRequest.Attachment> attachment;
    private Optional<CreateMessageRequest.AllowedMentions> allowedMentions;

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
    }

    public void apply(final CreateMessageRequest.Builder request) {
        content.ifPresent(request::setContent);
        embedData.ifPresent(request::setEmbed);
        attachment.ifPresent(request::setAttachment);
        allowedMentions.ifPresent(request::setAllowedMentions);
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

    public void setAttachment(final String name, final byte[] data) {
        this.attachment = Optional.of(CreateMessageRequest.Attachment.newBuilder()
                .setName(name)
                .setContent(ByteString.copyFrom(data))
                .build());
    }

    public void setAllowedMentions(@Nullable final CreateMessageRequest.AllowedMentions allowedMentions) {
        this.allowedMentions = Optional.ofNullable(allowedMentions);
    }
}
