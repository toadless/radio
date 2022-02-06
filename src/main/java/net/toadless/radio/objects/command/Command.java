package net.toadless.radio.objects.command;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.dv8tion.jda.api.Permission;
import net.toadless.radio.objects.exception.*;
import net.toadless.radio.util.EmbedUtils;
import org.jetbrains.annotations.NotNull;

public abstract class Command
{
    private final Command parent;
    private final String name;
    private final String description;
    private final String syntax;
    private final List<Command> children;
    private final List<String> aliases;

    private final List<Permission> memberRequiredPermissions;

    private final List<Permission> selfRequiredPermissions;
    private final List<CommandFlag> flags;
    private boolean isDisabled;
    private long cooldown;

    protected Command(@Nullable Command parent, @NotNull String name, @NotNull String description, @NotNull String syntax)
    {
        this.parent = parent;
        this.name = name;
        this.description = description;
        this.syntax = syntax;
        this.children = new ArrayList<>();
        this.aliases = new ArrayList<>();
        this.isDisabled = false;
        this.memberRequiredPermissions = new ArrayList<>();
        this.selfRequiredPermissions = new ArrayList<>();
        this.cooldown = 1500;
        this.flags = new ArrayList<>();
    }

    protected Command(@NotNull String name, @NotNull String description, @NotNull String syntax)
    {
        this.parent = null;
        this.name = name;
        this.description = description;
        this.syntax = syntax;
        this.children = new ArrayList<>();
        this.aliases = new ArrayList<>();
        this.isDisabled = false;
        this.memberRequiredPermissions = new ArrayList<>();
        this.selfRequiredPermissions = new ArrayList<>();
        this.cooldown = 0;
        this.flags = new ArrayList<>();
    }

    public void process(@NotNull CommandEvent event)
    {
        if (hasFlag(CommandFlag.GUILD_ONLY) && !event.isFromGuild())
        {
            event.replyError("This command must be executed in a server.");
        }
        else if (event.isDeveloper())
        {
            execute(event);
        }
        else if (isDisabled() || hasFlag(CommandFlag.DISABLED))
        {
            EmbedUtils.sendDisabledError(event);
        }
        else if (!getMemberRequiredPermissions().isEmpty() && !event.memberPermissionCheck(getMemberRequiredPermissions()))
        {
            EmbedUtils.sendMemberPermissionError(event);
        }
        else if (!getSelfRequiredPermissions().isEmpty() && !event.selfPermissionCheck(getSelfRequiredPermissions()))
        {
            EmbedUtils.sendSelfPermissionError(event);
        }
        else if (hasFlag(CommandFlag.DEVELOPER_ONLY) && !event.isDeveloper())
        {
            event.replyError("This command is for developers only.");
        }
        else
        {
            execute(event);
        }
    }

    private void execute(@NotNull CommandEvent event)
    {
        if (hasFlag(CommandFlag.AUTO_DELETE_MESSAGE) && event.selfPermissionCheck(Permission.MESSAGE_MANAGE))
        {
            event.getMessage().delete().queue();
        }

        run(event.getArgs(), event, exception ->
        {
            if (exception instanceof CommandCooldownException)
            {
                event.replyError(event.getAuthor().getAsMention() + " is on cooldown from command `" + getName() + "`");
            }
            if (exception instanceof CommandResultException)
            {
                event.replyError("Something went wrong. " + exception.getText());
            }
            else if (exception instanceof CommandInputException)
            {
                event.replyError("Your input was invalid. " + exception.getText());
            }
            else if (exception instanceof CommandSyntaxException)
            {
                EmbedUtils.sendSyntaxError(event);
            }
            else if (exception instanceof CommandHierarchyException)
            {
                event.replyError("A hierarchy error occurred when trying to run command `" + getName() + "`");
            }
            else if (exception instanceof CommandUserPermissionException)
            {
                EmbedUtils.sendMemberPermissionError(event);
            }
            else if (exception instanceof MissingConfigurationException)
            {
                event.replyError("`" + exception.getText() + "` is not setup.");
            }
            else
            {
                event.replyError(exception.getText());
            }
        });
    }

    public boolean hasChildren()
    {
        return !getChildren().isEmpty();
    }

    public boolean hasParent()
    {
        return parent != null;
    }

    public abstract void run(@NotNull List<String> args, @NotNull CommandEvent event, @NotNull Consumer<CommandException> failure);

    public long getCooldown()
    {
        return cooldown;
    }

    public void setCooldown(long millis)
    {
        this.cooldown = millis;
    }

    public boolean isDisabled()
    {
        return isDisabled;
    }

    public void setDisabled(boolean newState)
    {
        isDisabled = newState;
    }

    public @Nullable Command getParent()
    {
        return parent;
    }

    public @NotNull String getName()
    {
        return name;
    }

    public @NotNull String getDescription()
    {
        return description;
    }

    public @NotNull String getSyntax()
    {
        return syntax;
    }

    public @NotNull List<Command> getChildren()
    {
        return children;
    }

    public @NotNull List<String> getAliases()
    {
        return aliases;
    }

    public void addChildren(@NotNull Command... children)
    {
        this.children.addAll(List.of(children));
    }

    public void addAliases(@NotNull String... aliases)
    {
        this.aliases.addAll(List.of(aliases));
    }

    public @NotNull List<Permission> getMemberRequiredPermissions()
    {
        return memberRequiredPermissions;
    }

    public void addMemberPermissions(@NotNull Permission... permissions)
    {
        this.memberRequiredPermissions.addAll(List.of(permissions));
    }

    public void addSelfPermissions(@NotNull Permission... permissions)
    {
        this.selfRequiredPermissions.addAll(List.of(permissions));
    }

    public @NotNull List<Permission> getSelfRequiredPermissions()
    {
        return selfRequiredPermissions;
    }


    public @NotNull List<CommandFlag> getFlags()
    {
        return flags;
    }

    public void addFlags(@NotNull CommandFlag... flags)
    {
        this.flags.addAll(List.of(flags));
    }

    public boolean hasFlag(@NotNull CommandFlag flag)
    {
        return this.flags.contains(flag);
    }
}