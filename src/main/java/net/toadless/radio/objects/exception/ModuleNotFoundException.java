package net.toadless.radio.objects.exception;

import net.toadless.radio.objects.module.Module;

public class ModuleNotFoundException extends RuntimeException
{
    public <T extends Module> ModuleNotFoundException(Class<T> clazz)
    {
        super("Module '" + clazz.getName() + "' not found");
    }
}