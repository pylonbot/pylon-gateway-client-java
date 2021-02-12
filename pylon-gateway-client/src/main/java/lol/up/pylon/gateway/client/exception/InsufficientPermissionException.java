package lol.up.pylon.gateway.client.exception;

import lol.up.pylon.gateway.client.entity.Permission;

import java.util.Arrays;
import java.util.stream.Collectors;

public class InsufficientPermissionException extends ValidationException {

    private final Permission[] permissions;

    public InsufficientPermissionException(Permission... permissions) {
        super("Missing permissions: " + Arrays.stream(permissions)
                .map(permission -> "`" + permission.name() + "`")
                .collect(Collectors.joining(", ")));
        this.permissions = permissions;
    }

    public Permission[] getPermissions() {
        return permissions;
    }
}
