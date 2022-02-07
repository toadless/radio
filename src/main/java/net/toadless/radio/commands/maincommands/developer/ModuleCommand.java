package net.toadless.radio.commands.maincommands.developer;

import java.util.List;
import java.util.function.Consumer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.toadless.radio.modules.CommandModule;
import net.toadless.radio.objects.command.Command;
import net.toadless.radio.objects.command.CommandEvent;
import net.toadless.radio.objects.command.CommandFlag;
import net.toadless.radio.objects.exception.CommandException;
import net.toadless.radio.objects.exception.CommandResultException;
import net.toadless.radio.objects.exception.CommandSyntaxException;
import net.toadless.radio.util.CommandChecks;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings ("unused")
public class ModuleCommand extends Command
{
    public ModuleCommand()
    {
        super("Module", "Disables / Enables the specified module.", "[enable/disable] [module-name]");
        addFlags(CommandFlag.DEVELOPER_ONLY);
        addAliases("module", "command");
        addChildren(
                new ModuleEnableCommand(this),
                new ModuleDisableCommand(this),
                new ModuleReloadCommand(this)
        );
    }

    @Override
    public void run(@NotNull List<String> args, @NotNull CommandEvent event, @NotNull Consumer<CommandException> failure)
    {
        failure.accept(new CommandSyntaxException(event));
    }

    public static class ModuleEnableCommand extends Command
    {
        public ModuleEnableCommand(Command parent)
        {
            super(parent, "enable", "Enables a module", "[module-name]");
            addFlags(CommandFlag.DEVELOPER_ONLY);
        }

        @Override
        public void run(@NotNull List<String> args, @NotNull CommandEvent event, @NotNull Consumer<CommandException> failure)
        {
            if (CommandChecks.argsEmpty(event, failure)) return;
            String moduleName = args.get(0);
            Command command = event.getRadio().getModules().get(CommandModule.class).getCommandMap().get(moduleName);
            if (command == null)
            {
                failure.accept(new CommandResultException("Module " + moduleName + " was not found"));
                return;
            }

            if (!command.isDisabled())
            {
                failure.accept(new CommandResultException("Module " + command.getName() + " was already enabled."));
                return;
            }
            command.setDisabled(false);
            event.replySuccess("Enabled module: `" + command.getName() + "`.");
            event.getRadio().getLogger().warn("Module " + command.getName() + " was enabled.");
        }
    }

    public static class ModuleDisableCommand extends Command
    {
        public ModuleDisableCommand(Command parent)
        {
            super(parent, "disable", "Disables a module", "[module-name]");
            addFlags(CommandFlag.DEVELOPER_ONLY);
        }

        @Override
        public void run(@NotNull List<String> args, @NotNull CommandEvent event, @NotNull Consumer<CommandException> failure)
        {
            if (CommandChecks.argsEmpty(event, failure)) return;
            String moduleName = args.get(0);
            Command command = event.getRadio().getModules().get(CommandModule.class).getCommandMap().get(moduleName);
            if (command == null)
            {
                failure.accept(new CommandResultException("Module " + moduleName + " was not found"));
                return;
            }
            if (command.isDisabled())
            {
                failure.accept(new CommandResultException("Module " + command.getName() + " was already disabled."));
                return;
            }

            command.setDisabled(true);
            event.replySuccess("Disabled module: `" + command.getName() + "`.");
            event.getRadio().getLogger().warn("Module " + command.getName() + " was disabled.");
        }
    }

    public static class ModuleReloadCommand extends Command
    {
        public ModuleReloadCommand(Command parent)
        {
            super(parent, "reload", "Refreshes the command map.", "[none]");
            addFlags(CommandFlag.DEVELOPER_ONLY);
        }

        @Override
        public void run(@NotNull List<String> args, @NotNull CommandEvent event, @NotNull Consumer<CommandException> failure)
        {
            event.sendMessage(new EmbedBuilder().setDescription("Attempting to reload all commands, hope you know what you're doing!"));
            event.getRadio().getModules().get(CommandModule.class).getCommandMap().clear();
            event.getRadio().getModules().get(CommandModule.class).getCommandMap().putAll(event.getRadio().getModules().get(CommandModule.class).loadCommands());
            event.replySuccess("Reload complete!");
        }
    }
}