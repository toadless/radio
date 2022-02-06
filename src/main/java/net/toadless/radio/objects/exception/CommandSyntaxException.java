package net.toadless.radio.objects.exception;

import net.toadless.radio.objects.command.Command;
import net.toadless.radio.objects.command.CommandEvent;

public class CommandSyntaxException extends CommandException
{
    public CommandSyntaxException(Command command)
    {
        super(command);
    }

    public CommandSyntaxException(CommandEvent ctx)
    {
        super(ctx.getCommand());
    }
}