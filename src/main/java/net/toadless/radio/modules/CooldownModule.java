package net.toadless.radio.modules;

import net.dv8tion.jda.api.entities.Member;
import net.toadless.radio.main.Radio;
import net.toadless.radio.objects.command.Command;
import net.toadless.radio.objects.module.Module;
import net.toadless.radio.objects.module.Modules;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownModule extends Module
{
    private final Map<CooledCommand, Long> COOLDOWN_MAP = new ConcurrentHashMap<>(); //K = userId, guildId, command V = timestamp

    public CooldownModule(Radio radio, Modules modules)
    {
        super(radio, modules);
    }

    public boolean isOnCooldown(Member member, Command command)
    {
        long userId = member.getIdLong();
        long guildId = member.getGuild().getIdLong();
        for (Map.Entry<CooledCommand, Long> entry : COOLDOWN_MAP.entrySet())
        {
            CooledCommand cooledCommand = entry.getKey();
            long expiry = entry.getValue();

            if (cooledCommand.getUserId() == userId && cooledCommand.getGuildId() == guildId && cooledCommand.getCommand().equals(command))
            {
                if (System.currentTimeMillis() <= expiry)
                {
                    return true;
                }
                COOLDOWN_MAP.remove(cooledCommand);
                return false;
            }
        }
        return false;
    }

    public void addCooldown(Member member, Command command)
    {
        COOLDOWN_MAP.put(new CooledCommand(member, command), System.currentTimeMillis() + command.getCooldown());
    }

    public class CooledCommand
    {
        private final long userId;
        private final long guildId;
        private final Command command;

        public CooledCommand(Member member, Command command)
        {
            this.userId = member.getIdLong();
            this.guildId = member.getGuild().getIdLong();
            this.command = command;
        }

        public long getUserId()
        {
            return userId;
        }

        public long getGuildId()
        {
            return guildId;
        }

        public Command getCommand()
        {
            return command;
        }
    }
}