package net.toadless.radio.main;

import javax.security.auth.login.LoginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args)
    {
        Radio bot = new Radio();
        LOGGER.debug("Beginning startup sequence.");
        try
        {
            bot.build();
            LOGGER.debug("Bot built successfully.");
        }
        catch (LoginException exception)
        {
            bot.getLogger().error("The provided token was invalid, please ensure you put a valid token in bot.cfg");
            System.exit(1);
        }
        catch (IllegalArgumentException exception)
        {
            bot.getLogger().error("A provided value was invalid, please double check the values in bot.cfg");
            System.exit(1);
        }
        catch (Exception exception)
        {
            bot.getLogger().error("An unexpected exception occurred", exception);
            System.exit(1);
        }
    }
}