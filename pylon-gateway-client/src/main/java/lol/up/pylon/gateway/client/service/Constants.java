package lol.up.pylon.gateway.client.service;

import io.grpc.Context;
import io.grpc.Metadata;

class Constants {

    static Context.Key<Long> CTX_BOT_ID = Context.key("BID");
    static Context.Key<Long> CTX_GUILD_ID = Context.key("GID");

    static Metadata.Key<String> METADATA_BOT_ID = Metadata.Key.of("x-pylon-bot-id", Metadata.ASCII_STRING_MARSHALLER);
    static Metadata.Key<String> METADATA_GUILD_ID = Metadata.Key.of("x-pylon-guild-id", Metadata.ASCII_STRING_MARSHALLER);

}
