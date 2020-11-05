package lol.up.pylon.gateway.client.entity;

public interface WrappedEntity<E> {

    long getBotId();
    long getGuildId();
    E getData();

}
