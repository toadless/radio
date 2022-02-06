package net.toadless.radio.objects.exception;

import net.toadless.radio.objects.command.Command;

public class CommandCooldownException extends CommandException
{
    public CommandCooldownException(Command command)
    {
        super(command);
    }
}