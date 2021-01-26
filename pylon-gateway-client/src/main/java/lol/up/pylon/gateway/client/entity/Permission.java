package lol.up.pylon.gateway.client.entity;

public enum Permission {

    // @formatter:off
    CREATE_INSTANT_INVITE   (0x00000001, Type.TEXT_CHANNEL, Type.VOICE_CHANNEL),
    KICK_MEMBERS            (0x00000002),
    BAN_MEMBERS             (0x00000004),
    ADMINISTRATOR           (0x00000008),
    MANAGE_CHANNELS         (0x00000010, Type.TEXT_CHANNEL, Type.VOICE_CHANNEL),
    MANAGE_GUILD            (0x00000020),
    ADD_REACTIONS           (0x00000040, Type.TEXT_CHANNEL),
    VIEW_AUDIT_LOG          (0x00000080),
    PRIORITY_SPEAKER        (0x00000100, Type.VOICE_CHANNEL),
    STREAM                  (0x00000200, Type.VOICE_CHANNEL),
    VIEW_CHANNEL            (0x00000400, Type.TEXT_CHANNEL, Type.VOICE_CHANNEL),
    SEND_MESSAGES           (0x00000800, Type.TEXT_CHANNEL),
    SEND_TTS_MESSAGES       (0x00001000, Type.TEXT_CHANNEL),
    MANAGE_MESSAGES         (0x00002000, Type.TEXT_CHANNEL),
    EMBED_LINKS             (0x00004000, Type.TEXT_CHANNEL),
    ATTACH_FILES            (0x00008000, Type.TEXT_CHANNEL),
    READ_MESSAGE_HISTORY    (0x00010000, Type.TEXT_CHANNEL),
    MENTION_EVERYONE        (0x00020000, Type.TEXT_CHANNEL),
    USE_EXTERNAL_EMOJIS     (0x00040000, Type.TEXT_CHANNEL),
    VIEW_GUILD_INSIGHTS     (0x00080000),
    CONNECT                 (0x00100000, Type.VOICE_CHANNEL),
    SPEAK                   (0x00200000, Type.VOICE_CHANNEL),
    MUTE_MEMBERS            (0x00400000, Type.VOICE_CHANNEL),
    DEAFEN_MEMBERS          (0x00800000, Type.VOICE_CHANNEL),
    MOVE_MEMBERS            (0x01000000, Type.VOICE_CHANNEL),
    USE_VAD                 (0x02000000, Type.VOICE_CHANNEL),
    CHANGE_NICKNAME         (0x04000000),
    MANAGE_NICKNAMES        (0x08000000),
    MANAGE_ROLES            (0x10000000, Type.TEXT_CHANNEL, Type.VOICE_CHANNEL),
    MANAGE_WEBHOOKS         (0x20000000, Type.TEXT_CHANNEL, Type.VOICE_CHANNEL),
    MANAGE_EMOJIS           (0x40000000);
    // @formatter:on

    private final long value;
    private final Type[] types;

    Permission(final long value, final Type... types) {
        this.value = value;
        this.types = types;
    }

    Permission(final long value) {
        this(value, Type.SERVER);
    }

    public long getValue() {
        return value;
    }

    public Type[] getTypes() {
        return types;
    }

    public enum Type {
        SERVER, TEXT_CHANNEL, VOICE_CHANNEL
    }
}
