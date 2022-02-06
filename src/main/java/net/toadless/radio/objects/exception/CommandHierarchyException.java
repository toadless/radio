package net.toadless.radio.objects.exception;

import net.toadless.radio.objects.command.Command;

public class CommandHierarchyException extends CommandException
{
    public CommandHierarchyException(Command command)
    {
        super(command);
    }
}
