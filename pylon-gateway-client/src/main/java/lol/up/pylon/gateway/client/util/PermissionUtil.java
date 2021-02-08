/*
 * Copyright 2015-2020 Austin Keener, Michael Ritter, Florian Spieß, and the JDA contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package lol.up.pylon.gateway.client.util;


import bot.pylon.proto.discord.v1.model.ChannelData;
import lol.up.pylon.gateway.client.entity.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Bad class, uses complete on all grpc requests :stonky:
 */
public class PermissionUtil {

    /**
     * Checks if one given Member can interact with a 2nd given Member - in a permission sense (kick/ban/modify perms).
     * This only checks the Role-Position and does not check the actual permission (kick/ban/manage_role/...)
     *
     * @param issuer The member that tries to interact with 2nd member
     * @param target The member that is the target of the interaction
     * @return True, if issuer can interact with target in guild
     * @throws IllegalArgumentException if any of the provided parameters is {@code null}
     * or the provided entities are not from the same guild
     */
    public static boolean canInteract(Member issuer, Member target) {
        Guild guild = issuer.getGuild().complete();
        if (!guild.equals(target.getGuild().complete())) {
            throw new IllegalArgumentException("Provided members must both be Member objects of the same Guild!");
        }
        if (issuer.isOwner(guild)) {
            return true;
        }
        if (target.isOwner(guild)) {
            return false;
        }
        List<Role> issuerRoles = issuer.getRoles().complete();
        List<Role> targetRoles = target.getRoles().complete();
        return !issuerRoles.isEmpty() && (targetRoles.isEmpty() || canInteract(issuerRoles.get(0), targetRoles.get(0)));
    }

    /**
     * Checks if a given Member can interact with a given Role - in a permission sense (kick/ban/modify perms).
     * This only checks the Role-Position and does not check the actual permission (kick/ban/manage_role/...)
     *
     * @param issuer The member that tries to interact with the role
     * @param target The role that is the target of the interaction
     * @return True, if issuer can interact with target
     * @throws IllegalArgumentException if any of the provided parameters is {@code null}
     * or the provided entities are not from the same guild
     */
    public static boolean canInteract(Member issuer, Role target) {
        Guild guild = issuer.getGuild().complete();
        if (!guild.equals(target.getGuild().complete())) {
            throw new IllegalArgumentException("Provided Member issuer and Role target must be from the same Guild!");
        }
        if (issuer.isOwner(guild)) {
            return true;
        }
        List<Role> issuerRoles = issuer.getRoles().complete();
        return !issuerRoles.isEmpty() && canInteract(issuerRoles.get(0), target);
    }

    /**
     * Checks if one given Role can interact with a 2nd given Role - in a permission sense (kick/ban/modify perms).
     * This only checks the Role-Position and does not check the actual permission (kick/ban/manage_role/...)
     *
     * @param issuer The role that tries to interact with 2nd role
     * @param target The role that is the target of the interaction
     * @return True, if issuer can interact with target
     * @throws IllegalArgumentException if any of the provided parameters is {@code null}
     * or the provided entities are not from the same guild
     */
    public static boolean canInteract(Role issuer, Role target) {
        if (!issuer.getGuild().equals(target.getGuild())) {
            throw new IllegalArgumentException("The 2 Roles are not from same Guild!");
        }
        return target.getPosition() < issuer.getPosition();
    }

    /**
     * Check whether the provided {@link Member Member} can use the specified {@link Emoji Emote}.
     *
     * <p>If the specified Member is not in the emote's guild or the emote provided is from a message this will
     * return false.
     * Otherwise it will check if the emote is restricted to any roles and if that is the case if the Member has one
     * of these.
     *
     * <br><b>Note</b>: This is not checking if the issuer owns the Guild or not.
     *
     * @param issuer The member that tries to interact with the Emote
     * @param emote The emote that is the target interaction
     * @return True, if the issuer can interact with the emote
     * @throws IllegalArgumentException if any of the provided parameters is {@code null}
     * or the provided entities are not from the same guild
     */
    public static boolean canInteract(Member issuer, Emoji emote) {
        if (!issuer.getGuild().equals(emote.getGuild())) {
            throw new IllegalArgumentException("The issuer and target are not in the same Guild");
        }

        if (emote.isManaged() && !emote.getRoleIds().isEmpty()) {
            return emote.getRoleIds().stream() // does the user have at least one of the emote roles
                    .mapToLong(Long::longValue)
                    .anyMatch(emoteRoleId -> issuer.getRoleIds().stream()
                            .mapToLong(Long::longValue)
                            .anyMatch(userRoleId -> emoteRoleId == userRoleId));
        }

        return true;
    }

    /**
     * Checks whether the specified {@link Emoji Emote} can be used by the provided
     * {@link User User} in the {@link Channel MessageChannel}.
     *
     * @param issuer The user that tries to interact with the Emote
     * @param emote The emote that is the target interaction
     * @param channel The MessageChannel this emote should be interacted within
     * @param botOverride Whether bots can use non-managed emotes in other guilds
     * @return True, if the issuer can interact with the emote within the specified MessageChannel
     * @throws IllegalArgumentException if any of the provided parameters is {@code null}
     * or the provided entities are not from the same guild
     */
    public static boolean canInteract(User issuer, Emoji emote, Channel channel, boolean botOverride) {
        final Guild guild = emote.getGuild().complete();
        if (guild == null || !guild.isMember(issuer)) {
            return false; // cannot use an emote if you're not in its guild
        }
        Member member = guild.getMemberById(issuer.getData().getId()).complete();
        if (!canInteract(member, emote)) {
            return false;
        }
        // external means it is available outside of its own guild - works for bots or if its managed
        // currently we cannot check whether other users have nitro, we assume no here
        final boolean external = emote.isManaged() || (issuer.isBot() && botOverride);
        switch (channel.getData().getType()) {
            case GUILD_TEXT:
                member = guild.getMemberById(issuer.getData().getId()).complete();
                return emote.getGuild().equals(channel.getGuild()) // within the same guild
                        || (external && member != null && member.hasPermission(channel,
                        Permission.USE_EXTERNAL_EMOJIS)); // in different guild
            default:
                return external; // In Group or Private it only needs to be external
        }
    }

    /**
     * Checks whether the specified {@link Emoji Emote} can be used by the provided
     * {@link User User} in the {@link Channel MessageChannel}.
     *
     * @param issuer The user that tries to interact with the Emote
     * @param emote The emote that is the target interaction
     * @param channel The MessageChannel this emote should be interacted within
     * @return True, if the issuer can interact with the emote within the specified MessageChannel
     * @throws IllegalArgumentException if any of the provided parameters is {@code null}
     * or the provided entities are not from the same guild
     */
    public static boolean canInteract(User issuer, Emoji emote, Channel channel) {
        return canInteract(issuer, emote, channel, true);
    }

    /**
     * Checks to see if the {@link Member Member} has the specified {@link Permission Permissions}
     * in the specified {@link Guild Guild}. This method properly deals with Owner status.
     *
     * <p><b>Note:</b> this is based on effective permissions, not literal permissions. If a member has permissions
     * that would
     * enable them to do something without the literal permission to do it, this will still return true.
     * <br>Example: If a member has the {@link Permission#ADMINISTRATOR} permission, they will be able to
     * {@link Permission#MANAGE_GUILD} as well, even without the literal permissions.
     *
     * @param member The {@link Member Member} whose permissions are being checked.
     * @param permissions The {@link Permission Permissions} being checked for.
     * @return True -
     * if the {@link Member Member} effectively has the specified {@link Permission Permissions}.
     * @throws IllegalArgumentException if any of the provided parameters is null
     */
    public static boolean checkPermission(Member member, Permission... permissions) {
        long effectivePerms = getEffectivePermission(member);
        return isApplied(effectivePerms, Permission.ADMINISTRATOR.getValue())
                || isApplied(effectivePerms, PermissionSet.of(permissions).getValue());
    }

    /**
     * Checks to see if the {@link Member Member} has the specified {@link Permission Permissions}
     * in the specified {@link Channel GuildChannel}. This method properly deals with
     * PermissionOverrides and Owner status.
     *
     * <p><b>Note:</b> this is based on effective permissions, not literal permissions. If a member has permissions
     * that would
     * enable them to do something without the literal permission to do it, this will still return true.
     * <br>Example: If a member has the {@link Permission#ADMINISTRATOR} permission, they will be able to
     * {@link Permission#SEND_MESSAGES} in every channel.
     *
     * @param member The {@link Member Member} whose permissions are being checked.
     * @param channel The {@link Channel GuildChannel} being checked.
     * @param permissions The {@link Permission Permissions} being checked for.
     * @return True -
     * if the {@link Member Member} effectively has the specified {@link Permission Permissions}.
     * @throws IllegalArgumentException if any of the provided parameters is {@code null}
     * or the provided entities are not from the same guild
     */
    public static boolean checkPermission(Channel channel, Member member, Permission... permissions) {
        Guild guild = channel.getGuild().complete();
        checkGuild(guild, member.getGuild().complete(), "Member");

        long effectivePerms = getEffectivePermission(channel, member);
        return isApplied(effectivePerms, PermissionSet.of(permissions).getValue());
    }

    /**
     * Gets the {@code long} representation of the effective permissions allowed for this {@link Member Member}
     * in this {@link Guild Guild}. This can be used in conjunction with
     * {@link PermissionSet#of(long) PermissionSet.of(long)} to easily get a list of all
     * {@link Permission Permissions} that this member has in this {@link Guild Guild}.
     *
     * <p><b>This only returns the Guild-level permissions!</b>
     *
     * @param member The {@link Member Member} whose permissions are being checked.
     * @return The {@code long} representation of the literal permissions that
     * this {@link Member Member} has in this {@link Guild Guild}.
     * @throws IllegalArgumentException if any of the provided parameters is {@code null}
     * or the provided entities are not from the same guild
     */
    public static long getEffectivePermission(Member member) {
        if (member.isOwner().complete()) {
            return PermissionSet.all().getValue();
        }
        //Default to binary OR of all global permissions in this guild
        long permission = member.getGuild().complete().getPublicRole().complete().getPermissions();
        for (Role role : member.getRoles().complete()) {
            permission |= role.getPermissions();
            if (isApplied(permission, Permission.ADMINISTRATOR.getValue())) {
                return PermissionSet.all().getValue();
            }
        }

        return permission;
    }

    /**
     * Gets the {@code long} representation of the effective permissions allowed for this {@link Member Member}
     * in this {@link Channel GuildChannel}. This can be used in conjunction with
     * {@link PermissionSet#of(long) PermissionSet.of(long)} to easily get a list of all
     * {@link Permission Permissions} that this member can use in this {@link Channel GuildChannel}.
     * <br>This functions very similarly to how {@link Role#getPermissions()} () Role.getPermissionsRaw()}.
     *
     * @param channel The {@link Channel GuildChannel} being checked.
     * @param member The {@link Member Member} whose permissions are being checked.
     * @return The {@code long} representation of the effective permissions that this {@link Member Member}
     * has in this {@link Channel GuildChannel}.
     * @throws IllegalArgumentException if any of the provided parameters is {@code null}
     * or the provided entities are not from the same guild
     */
    public static long getEffectivePermission(Channel channel, Member member) {
        if (member.isOwner().complete()) {
            // Owner effectively has all permissions
            return PermissionSet.all().getValue();
        }

        long permission = getEffectivePermission(member);
        final long admin = Permission.ADMINISTRATOR.getValue();
        if (isApplied(permission, admin)) {
            return PermissionSet.all().getValue();
        }
        // MANAGE_CHANNEL allows to delete channels within a category (this is undocumented behavior)
        if (channel.getParentId() > 0 && checkPermission(channel.getParent().complete(), member, Permission.MANAGE_CHANNELS)) {
            permission |= Permission.MANAGE_CHANNELS.getValue();
        }


        AtomicLong allow = new AtomicLong(0);
        AtomicLong deny = new AtomicLong(0);
        getExplicitOverrides(channel, member, allow, deny);
        permission = apply(permission, allow.get(), deny.get());
        final long viewChannel = Permission.VIEW_CHANNEL.getValue();
        final long connectChannel = Permission.CONNECT.getValue();

        //When the permission to view the channel or to connect to the channel is not applied it is not granted
        // This means that we have no access to this channel at all
        final boolean hasConnect =
                channel.getData().getType() != ChannelData.ChannelType.GUILD_VOICE || isApplied(permission,
                        connectChannel);
        final boolean hasView = isApplied(permission, viewChannel);
        return hasView && hasConnect ? permission : 0;
    }

    /**
     * Gets the {@code long} representation of the effective permissions allowed for this {@link Role Role}
     * in this {@link Channel GuildChannel}. This can be used in conjunction with
     * {@link PermissionSet#of(long) PermissionSet.of(long)} to easily get a list of all
     * {@link Permission Permissions} that this role can use in this {@link Channel GuildChannel}.
     *
     * @param channel The {@link Channel GuildChannel} in which permissions are being checked.
     * @param role The {@link Role Role} whose permissions are being checked.
     * @return The {@code long} representation of the effective permissions that this {@link Role Role}
     * has in this {@link Channel GuildChannel}
     * @throws IllegalArgumentException if any of the provided parameters is {@code null}
     * or the provided entities are not from the same guild
     */
    public static long getEffectivePermission(Channel channel, Role role) {
        Guild guild = channel.getGuild().complete();
        if (!guild.equals(role.getGuild())) {
            throw new IllegalArgumentException("Provided channel and role are not of the same guild!");
        }

        long permissions = getExplicitPermission(channel, role);
        if (isApplied(permissions, Permission.ADMINISTRATOR.getValue())) {
            return PermissionSet.of(Arrays.stream(Permission.values())
                    .filter(permission -> permission.getTypes().length > 0)
                    .toArray(Permission[]::new))
                    .getValue();
        } else if (!isApplied(permissions, Permission.VIEW_CHANNEL.getValue())) {
            return 0;
        }
        return permissions;
    }

    /**
     * Retrieves the explicit permissions of the specified {@link Member Member}
     * in its hosting {@link Guild Guild}.
     * <br>This method does not calculate the owner in.
     *
     * <p>All permissions returned are explicitly granted to this Member via its
     * {@link Role Roles}.
     * <br>Permissions like {@link Permission#ADMINISTRATOR Permission.ADMINISTRATOR} do not
     * grant other permissions in this value.
     *
     * @param member The non-null {@link Member Member} for which to get implicit
     * permissions
     * @return Primitive (unsigned) long value with the implicit permissions of the specified member
     * @throws IllegalArgumentException If the specified member is {@code null}
     * @since 3.1
     */
    public static long getExplicitPermission(Member member) {
        final Guild guild = member.getGuild().complete();
        long permission = guild.getPublicRole().complete().getPermissions();

        for (Role role : member.getRoles().complete())
            permission |= role.getPermissions();

        return permission;
    }

    /**
     * Retrieves the explicit permissions of the specified {@link Member Member}
     * in its hosting {@link Guild Guild} and specific
     * {@link Channel GuildChannel}.
     * <br>This method does not calculate the owner in.
     * <b>Allowed permissions override denied permissions of PermissionOverrides!</b>
     *
     * <p>All permissions returned are explicitly granted to this Member via its
     * {@link Role Roles}.
     * <br>Permissions like {@link Permission#ADMINISTRATOR Permission.ADMINISTRATOR} do not
     * grant other permissions in this value.
     * <p>This factor in all PermissionOverrides that affect
     * this member
     * and only grants the ones that are explicitly given.
     *
     * @param channel The target channel of which to check PermissionOverrides
     * @param member The non-null {@link Member Member} for which to get implicit
     * permissions
     * @return Primitive (unsigned) long value with the implicit permissions of the specified member in the specified
     * channel
     * @throws IllegalArgumentException If any of the arguments is {@code null}
     * or the specified entities are not from the same {@link Guild Guild}
     * @since 3.1
     */
    public static long getExplicitPermission(Channel channel, Member member) {
        final Guild guild = member.getGuild().complete();
        checkGuild(channel.getGuild().complete(), guild, "Member");

        long permission = getExplicitPermission(member);

        AtomicLong allow = new AtomicLong(0);
        AtomicLong deny = new AtomicLong(0);

        // populates allow/deny
        getExplicitOverrides(channel, member, allow, deny);

        return apply(permission, allow.get(), deny.get());
    }

    /**
     * Retrieves the explicit permissions of the specified {@link Role Role}
     * in its hosting {@link Guild Guild} and specific
     * {@link Channel GuildChannel}.
     * <br><b>Allowed permissions override denied permissions of PermissionOverrides!</b>
     *
     * <p>All permissions returned are explicitly granted to this Role.
     * <br>Permissions like {@link Permission#ADMINISTRATOR Permission.ADMINISTRATOR} do not
     * grant other permissions in this value.
     * <p>This factor in existing PermissionOverrides if possible.
     *
     * @param channel The target channel of which to check PermissionOverrides
     * @param role The non-null {@link Role Role} for which to get implicit permissions
     * @return Primitive (unsigned) long value with the implicit permissions of the specified role in the specified
     * channel
     * @throws IllegalArgumentException If any of the arguments is {@code null}
     * or the specified entities are not from the same {@link Guild Guild}
     * @since 3.1
     */
    public static long getExplicitPermission(Channel channel, Role role) {
        final Guild guild = role.getGuild().complete();
        checkGuild(channel.getGuild().complete(), guild, "Role");

        long permission = role.getPermissions() | guild.getPublicRole().complete().getPermissions();
        ChannelData.ChannelPermissionOverwriteData override =
                channel.getRolePermissionOverwrite(guild.getPublicRoleId());
        if (override != null) {
            permission = apply(permission, override.getAllow(), override.getDeny());
        }
        if (role.isPublicRole()) {
            return permission;
        }

        override = channel.getPermissionOverwrite(role);

        return override == null
                ? permission
                : apply(permission, override.getAllow(), override.getDeny());
    }

    private static void getExplicitOverrides(Channel channel, Member member, AtomicLong allow, AtomicLong deny) {
        ChannelData.ChannelPermissionOverwriteData override =
                channel.getRolePermissionOverwrite(member.getGuild().complete().getPublicRoleId());
        long allowRaw = 0;
        long denyRaw = 0;
        if (override != null) {
            denyRaw = override.getDeny();
            allowRaw = override.getAllow();
        }

        long allowRole = 0;
        long denyRole = 0;
        // create temporary bit containers for role cascade
        for (Role role : member.getRoles().complete()) {
            override = channel.getPermissionOverwrite(role);
            if (override != null) {
                // important to update role cascade not others
                denyRole |= override.getDeny();
                allowRole |= override.getAllow();
            }
        }
        // Override the raw values of public role then apply role cascade
        allowRaw = (allowRaw & ~denyRole) | allowRole;
        denyRaw = (denyRaw & ~allowRole) | denyRole;

        override = channel.getPermissionOverwrite(member);
        if (override != null) {
            // finally override the role cascade with member overrides
            final long oDeny = override.getDeny();
            final long oAllow = override.getAllow();
            allowRaw = (allowRaw & ~oDeny) | oAllow;
            denyRaw = (denyRaw & ~oAllow) | oDeny;
            // this time we need to exclude new allowed bits from old denied ones and OR the new denied bits as final
            // overrides
        }
        // set as resulting values
        allow.set(allowRaw);
        deny.set(denyRaw);
    }

    /*
     * Check whether the specified permission is applied in the bits
     */
    private static boolean isApplied(long permissions, long perms) {
        return (permissions & perms) == perms;
    }

    private static long apply(long permission, long allow, long deny) {
        permission &= ~deny;  //Deny everything that the cascade of roles denied.
        permission |= allow;  //Allow all the things that the cascade of roles allowed
        // The allowed bits override the denied ones!
        return permission;
    }

    private static void checkGuild(Guild o1, Guild o2, String name) {
        if (o1.getGuildId() != o2.getGuildId()) {
            throw new IllegalArgumentException("Specified " + name + " is not in the same guild! " +
                    "(" + o1.getData().getName() + " / " + o2.getData().getName() + ")");
        }
    }
}
