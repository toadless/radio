package net.toadless.radio.objects.module;

import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.toadless.radio.Radio;

public class Module extends ListenerAdapter
{
    protected final Radio radio;
    protected final Modules modules;

    public Module(Radio radio, Modules modules)
    {
        this.radio = radio;
        this.modules = modules;
    }
}