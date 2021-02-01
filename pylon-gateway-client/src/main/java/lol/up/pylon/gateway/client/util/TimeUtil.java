package lol.up.pylon.gateway.client.util;

import com.google.protobuf.Timestamp;

public class TimeUtil {

    public static long timestampToLong(final Timestamp timestamp) {
        return (timestamp.getSeconds() * 1_000) + (timestamp.getNanos() * 1_000_000);
    }
}
