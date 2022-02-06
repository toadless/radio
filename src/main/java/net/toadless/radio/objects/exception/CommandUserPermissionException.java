package net.toadless.radio.objects.exception;

import net.toadless.radio.objects.command.Command;

public class CommandUserPermissionException extends CommandException
{
    public CommandUserPermissionException(Command command)
    {
        super(command);
    }
}