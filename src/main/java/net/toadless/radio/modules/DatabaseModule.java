package net.toadless.radio.modules;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.toadless.radio.Radio;
import net.toadless.radio.objects.bot.ConfigOption;
import net.toadless.radio.objects.bot.Configuration;
import net.toadless.radio.objects.module.Module;
import net.toadless.radio.objects.module.Modules;
import net.toadless.radio.util.IOUtils;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.sql.Connection;

public class DatabaseModule extends Module
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseModule.class);
    private final HikariDataSource pool;

    public DatabaseModule(Radio radio, Modules modules)
    {
        super(radio, modules);

        LOGGER.debug("Starting local database pool.");
        this.pool = initHikari();
        initTables();
        System.getProperties().setProperty("org.jooq.no-logo", "true");
    }

    private void initTables()
    {
        LOGGER.debug("Initialise table guilds.");
        initTable("guilds");

        LOGGER.debug("Table setup complete.");
    }

    public HikariDataSource getPool()
    {
        return pool;
    }

    public Connection getConnection()
    {
        try
        {
            return pool.getConnection();
        }
        catch (Exception exception)
        {
            return getConnection();
        }
    }

    private void initTable(String table)
    {
        try
        {
            InputStream file = IOUtils.getResourceFile("sql/" + table + ".sql");
            if (file == null)
            {
                throw new NullPointerException("File for table '" + table + "' not found");
            }
            getConnection().createStatement().execute(IOUtils.convertToString(file));
        }
        catch (Exception exception)
        {
            radio.getLogger().error("Error initializing table: '" + table + "'", exception);
        }
    }

    private HikariDataSource initHikari()
    {
        LOGGER.debug("Starting local HikariCP setup.");
        HikariConfig hikariConfig = new HikariConfig();
        Configuration configuration = radio.getConfiguration();

        hikariConfig.setDriverClassName(configuration.getString(ConfigOption.DBDRIVER));
        hikariConfig.setJdbcUrl(configuration.getString(ConfigOption.DBURL));

        hikariConfig.setUsername(radio.getConfiguration().getString(ConfigOption.DBUSERNAME));
        hikariConfig.setPassword(radio.getConfiguration().getString(ConfigOption.DBPASSWORD));

        hikariConfig.setMaximumPoolSize(30);
        hikariConfig.setMinimumIdle(10);
        hikariConfig.setConnectionTimeout(10000);
        LOGGER.debug("Local HikariCP setup complete.");
        try
        {
            return new HikariDataSource(hikariConfig);
        }
        catch (Exception exception)
        {
            radio.getLogger().error("Local database offline, connection failure.");
            System.exit(1);
            return null;
        }
    }

    public DSLContext getContext()
    {
        return getContext(getConnection());
    }

    public DSLContext getContext(Connection connection)
    {
        return DSL.using(connection, SQLDialect.POSTGRES);
    }

    public void close()
    {
        LOGGER.debug("Closed local database.");
        pool.close();
    }
}
