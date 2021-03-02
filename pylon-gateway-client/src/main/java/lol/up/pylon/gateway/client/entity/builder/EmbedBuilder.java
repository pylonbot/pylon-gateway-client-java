package lol.up.pylon.gateway.client.entity.builder;

import bot.pylon.proto.discord.v1.model.MessageData;

import java.awt.Color;

public class EmbedBuilder {

    private static final String ZERO_WIDTH_SPACE = "\u200E";

    private final MessageData.MessageEmbedData.Builder builder;

    public EmbedBuilder() {
        this.builder = MessageData.MessageEmbedData.newBuilder();
    }

    public EmbedBuilder setTitle(final String title) {
        this.builder.setTitle(title);
        return this;
    }

    public EmbedBuilder setDescription(final String description) {
        this.builder.setDescription(description);
        return this;
    }

    public EmbedBuilder setColor(final Color color) {
        this.builder.setColor(color.getRGB() & 0xFFFFFF);
        return this;
    }

    public EmbedBuilder setFooter(final String text, final String iconUrl) {
        this.builder.setFooter(MessageData.MessageEmbedData.MessageEmbedFooterData.newBuilder()
                .setIconUrl(iconUrl)
                .setText(text)
                .build());
        return this;
    }

    public EmbedBuilder setThumbnail(final String thumbnailUrl) {
        this.builder.setThumbnail(MessageData.MessageEmbedData.MessageEmbedThumbnailData.newBuilder()
                .setUrl(thumbnailUrl)
                .build());
        return this;
    }

    public EmbedBuilder setImage(final String imageUrl) {
        this.builder.setImage(MessageData.MessageEmbedData.MessageEmbedImageData.newBuilder()
                .setUrl(imageUrl)
                .build());
        return this;
    }

    public EmbedBuilder setVideo(final String videoUrl) {
        this.builder.setVideo(MessageData.MessageEmbedData.MessageEmbedVideoData.newBuilder()
                .setUrl(videoUrl)
                .build());
        return this;
    }

    public EmbedBuilder setAuthor(final String name, final String iconUrl, final String url) {
        this.builder.setAuthor(MessageData.MessageEmbedData.MessageEmbedAuthorData.newBuilder()
                .setName(name)
                .setIconUrl(iconUrl)
                .setUrl(url)
                .build());
        return this;
    }

    public EmbedBuilder addField(final String name, final String value, final boolean inline) {
        this.builder.addFields(MessageData.MessageEmbedData.MessageEmbedFieldData.newBuilder()
                .setName(name)
                .setValue(value)
                .setInline(true)
                .build());
        return this;
    }

    public EmbedBuilder addEmptyField(final boolean inline) {
        return addField(ZERO_WIDTH_SPACE, ZERO_WIDTH_SPACE, inline);
    }

    public MessageData.MessageEmbedData toEmbedData() {
        return builder.build();
    }
}
