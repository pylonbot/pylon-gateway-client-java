package lol.up.pylon.gateway.client.util;

import lol.up.pylon.gateway.client.entity.Permission;

import java.util.*;
import java.util.function.Predicate;

/**
 * Idea for PermissionSet taken from Discord4J open source repository
 */
public class PermissionSet extends AbstractSet<Permission> {

    private static final long ALL_RAW = Arrays.stream(Permission.values())
            .mapToLong(Permission::getValue)
            .reduce(0, (left, right) -> left | right);
    private static final long NONE_RAW = 0L;

    public static PermissionSet all() {
        return PermissionSet.of(ALL_RAW);
    }

    public static PermissionSet none() {
        return PermissionSet.of(NONE_RAW);
    }

    public static PermissionSet of(final Permission... permissions) {
        return PermissionSet.of(Arrays.stream(permissions)
                .mapToLong(Permission::getValue)
                .reduce(0, (left, right) -> left | right));
    }

    public static PermissionSet of(final long raw) {
        return new PermissionSet(raw);
    }

    private final long value;

    private PermissionSet(final long value) {
        this.value = value;
    }

    @Override
    public boolean add(Permission permission) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends Permission> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeIf(Predicate<? super Permission> filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public EnumSet<Permission> enumSet() {
        final EnumSet<Permission> enumSet = EnumSet.allOf(Permission.class);
        enumSet.removeIf(permission -> !contains(permission));
        return enumSet;
    }

    @Override
    public Iterator<Permission> iterator() {
        return Collections.unmodifiableSet(enumSet()).iterator();
    }

    @Override
    public int size() {
        return Long.bitCount(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        PermissionSet that = (PermissionSet) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }

    @Override
    public String toString() {
        return "PermissionSet{" +
                "value=" + value +
                '}';
    }
}
