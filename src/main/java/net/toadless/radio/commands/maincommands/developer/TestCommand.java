package net.toadless.radio.commands.maincommands.developer;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.toadless.radio.modules.CooldownModule;
import net.toadless.radio.objects.command.Command;
import net.toadless.radio.objects.command.CommandEvent;
import net.toadless.radio.objects.command.CommandFlag;
import net.toadless.radio.objects.exception.CommandCooldownException;
import net.toadless.radio.objects.exception.CommandException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@SuppressWarnings ("unused")
public class TestCommand extends Command
{
    public TestCommand()
    {
        super("Test", "Tests the bots basic functionality.", "[none]");
        addAliases("test");
        addFlags(CommandFlag.DEVELOPER_ONLY);
        setCooldown(5000L);
    }

    @Override
    public void run(@NotNull List<String> args, @NotNull CommandEvent event, @NotNull Consumer<CommandException> failure)
    {
        if (event.getRadio().getModules().get(CooldownModule.class).isOnCooldown(event.getMember(), this))
        {
            failure.accept(new CommandCooldownException(this));
            return;
        }

        event.replySuccess("Success");
        event.replyError("Error");
        failure.accept(new CommandException("Exception"));
        event.sendDeletingMessage(new EmbedBuilder().setTitle("Test embed, now testing event waiting."));
        event.getRadio().getModules().get(CooldownModule.class).addCooldown(event.getMember(), this);
        event.getRadio().getEventWaiter().waitForEvent(
                GuildMessageReceivedEvent.class,
                msg -> msg.getAuthor().equals(event.getAuthor()),
                msg -> event.replySuccess(msg.getMessage().getContentRaw()),
                5,
                TimeUnit.SECONDS,
                () -> event.replyError("Timeout"));
    }
}
