package net.toadless.radio;

import javax.security.auth.login.LoginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args)
    {
        LOGGER.info(getVanity());

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

    private static String getVanity()
    {
        return """
                                                   
                                                   dddddddd                         \s
                                                   d::::::d  iiii                   \s
                                                   d::::::d i::::i                  \s
                                                   d::::::d  iiii                   \s
                                                   d:::::d                          \s
    rrrrr   rrrrrrrrr   aaaaaaaaaaaaa      ddddddddd:::::d iiiiiii    ooooooooooo   \s
    r::::rrr:::::::::r  a::::::::::::a   dd::::::::::::::d i:::::i  oo:::::::::::oo \s
    r:::::::::::::::::r aaaaaaaaa:::::a d::::::::::::::::d  i::::i o:::::::::::::::o
    rr::::::rrrrr::::::r         a::::ad:::::::ddddd:::::d  i::::i o:::::ooooo:::::o
     r:::::r     r:::::r  aaaaaaa:::::ad::::::d    d:::::d  i::::i o::::o     o::::o
     r:::::r     rrrrrrraa::::::::::::ad:::::d     d:::::d  i::::i o::::o     o::::o
     r:::::r           a::::aaaa::::::ad:::::d     d:::::d  i::::i o::::o     o::::o
     r:::::r          a::::a    a:::::ad:::::d     d:::::d  i::::i o::::o     o::::o
     r:::::r          a::::a    a:::::ad::::::ddddd::::::ddi::::::io:::::ooooo:::::o
     r:::::r          a:::::aaaa::::::a d:::::::::::::::::di::::::io:::::::::::::::o
     r:::::r           a::::::::::aa:::a d:::::::::ddd::::di::::::i oo:::::::::::oo \s
     rrrrrrr            aaaaaaaaaa  aaaa  ddddddddd   dddddiiiiiiii   ooooooooooo   \s
   =================================================================================\s""";
    }
}