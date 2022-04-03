package net.toadless.radio.util;

import java.sql.Connection;
import net.dv8tion.jda.api.entities.Guild;
import net.toadless.radio.main.Radio;
import net.toadless.radio.jooq.Tables;
import net.toadless.radio.jooq.tables.Guilds;
import net.toadless.radio.modules.DatabaseModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseUtils
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseUtils.class);

    private DatabaseUtils()
    {
        //Overrides the default, public, constructor
    }

    public static void removeGuild(Guild guild, Radio radio)
    {
        LOGGER.debug("Removed guild " + guild.getId());
        try (Connection connection = radio.getModules().get(DatabaseModule.class).getConnection())
        {
            var context = radio.getModules().get(DatabaseModule.class).getContext(connection)
                    .deleteFrom(Tables.GUILDS)
                    .where(Guilds.GUILDS.GUILD_ID.eq(guild.getIdLong()));
            context.execute();
        }
        catch (Exception exception)
        {
            radio.getLogger().error("An SQL error occurred", exception);
        }
    }

    public static void removeGuild(long guildId, Radio radio)
    {
        LOGGER.debug("Removed guild " + guildId);
        try (Connection connection = radio.getModules().get(DatabaseModule.class).getConnection())
        {
            var context = radio.getModules().get(DatabaseModule.class).getContext(connection)
                    .deleteFrom(Tables.GUILDS)
                    .where(Guilds.GUILDS.GUILD_ID.eq(guildId));
            context.execute();
        }
        catch (Exception exception)
        {
            radio.getLogger().error("An SQL error occurred", exception);
        }
    }

    public static void registerGuild(Guild guild, Radio radio)
    {
        LOGGER.debug("Registered guild " + guild.getId());
        try (Connection connection = radio.getModules().get(DatabaseModule.class).getConnection())
        {
            var context = radio.getModules().get(DatabaseModule.class).getContext(connection)
                    .insertInto(Tables.GUILDS)
                    .columns(Guilds.GUILDS.GUILD_ID)
                    .values(guild.getIdLong())
                    .onDuplicateKeyIgnore();
            context.execute();
        }
        catch (Exception exception)
        {
            radio.getLogger().error("An SQL error occurred", exception);
        }
    }

    public static void registerGuild(long guildId, Radio radio)
    {
        LOGGER.debug("Removed guild " + guildId);
        try (Connection connection = radio.getModules().get(DatabaseModule.class).getConnection())
        {
            var context = radio.getModules().get(DatabaseModule.class).getContext(connection)
                    .insertInto(Tables.GUILDS)
                    .columns(Guilds.GUILDS.GUILD_ID)
                    .values(guildId)
                    .onDuplicateKeyIgnore();
            context.execute();
        }
        catch (Exception exception)
        {
            radio.getLogger().error("An SQL error occurred", exception);
        }
    }
}