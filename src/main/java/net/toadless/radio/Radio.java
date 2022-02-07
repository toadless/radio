package net.toadless.radio;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.SelfUser;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import net.toadless.radio.objects.bot.ConfigOption;
import net.toadless.radio.objects.bot.Configuration;
import net.toadless.radio.objects.bot.EventWaiter;
import net.toadless.radio.objects.info.BotInfo;
import net.toadless.radio.objects.module.Modules;
import net.toadless.radio.util.DatabaseUtils;
import net.toadless.radio.util.StringUtils;
import okhttp3.OkHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class Radio extends ListenerAdapter
{
    private final Logger logger;
    private final LocalDateTime startTimestamp;
    private final Configuration configuration;
    private final OkHttpClient okHttpClient;
    private final Modules modules;
    private final EventWaiter eventWaiter;
    private ShardManager shardManager;

    public Radio()
    {
        this.logger = LoggerFactory.getLogger(Radio.class);
        this.configuration = new Configuration(this);
        this.okHttpClient = new OkHttpClient();
        this.startTimestamp = LocalDateTime.now();
        this.modules = new Modules(this);
        this.eventWaiter = new EventWaiter();
    }

    public EventWaiter getEventWaiter()
    {
        return eventWaiter;
    }

    public OkHttpClient getOkHttpClient()
    {
        return okHttpClient;
    }

    public Modules getModules()
    {
        return modules;
    }

    public void build() throws LoginException
    {

        this.shardManager = DefaultShardManagerBuilder
                .create(getConfiguration().getString(ConfigOption.TOKEN),
                        GatewayIntent.GUILD_MEMBERS,
                        GatewayIntent.GUILD_PRESENCES,

                        GatewayIntent.DIRECT_MESSAGES,
                        GatewayIntent.DIRECT_MESSAGE_REACTIONS,

                        GatewayIntent.GUILD_MESSAGES,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.GUILD_VOICE_STATES)

                .disableCache(
                        CacheFlag.ACTIVITY,
                        CacheFlag.EMOTE,
                        CacheFlag.CLIENT_STATUS,
                        CacheFlag.ROLE_TAGS,
                        CacheFlag.ONLINE_STATUS,
                        CacheFlag.MEMBER_OVERRIDES)

                .setHttpClient(okHttpClient)

                .setMemberCachePolicy(MemberCachePolicy.NONE)
                .setShardsTotal(-1)

                .addEventListeners(
                        this,
                        eventWaiter
                )
                .addEventListeners(
                        modules.getModules()
                )

                .setActivity(Activity.playing(" loading."))
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .build();
    }

    @Override
    public void onReady(ReadyEvent event)
    {
        registerGuilds(event.getJDA().getShardManager());
        switchStatus(event.getJDA());

        getLogger().info("  ___      _     ___ _            _          _ _ ");
        getLogger().info(" | _ ) ___| |_  / __| |_ __ _ _ _| |_ ___ __| | |");
        getLogger().info(" | _ \\/ _ \\  _| \\__ \\  _/ _` | '_|  _/ -_) _` |_|");
        getLogger().info(" |___/\\___/\\__| |___/\\__\\__,_|_|  \\__\\___\\__,_(_)");
        getLogger().info("");
        getLogger().info("Account:         " + event.getJDA().getSelfUser().getAsTag() + " / " + event.getJDA().getSelfUser().getId());
        getLogger().info("Total Shards:    " + BotInfo.getTotalShards(event.getJDA().getShardManager()));
        getLogger().info("Total Guilds:    " + BotInfo.getGuildCount(event.getJDA().getShardManager()));
        getLogger().info("JDA Version:     " + JDAInfo.VERSION);
        getLogger().info("Radio Version:   " + Constants.VERSION);
        getLogger().info("JVM Version:     " + BotInfo.getJavaVersion());

        modules.addRepeatingTask(() -> switchStatus(event.getJDA()), TimeUnit.MINUTES, 2);
    }

    public SelfUser getSelfUser()
    {
        if (getJDA() == null)
        {
            throw new UnsupportedOperationException("No JDA present.");
        }
        return getJDA().getSelfUser();
    }

    public JDA getJDA()
    {
        return shardManager.getShardCache().stream().filter(Objects::nonNull).findFirst().orElse(null);
    }

    public void registerGuilds(ShardManager shardManager)
    {
        if (shardManager == null)
        {
            throw new UnsupportedOperationException("Cannot register guilds without a shard manager.");
        }
        for (Guild guild : shardManager.getGuilds())
        {
            DatabaseUtils.registerGuild(guild, this);
        }
    }

    private void switchStatus(JDA jda)
    {
        ShardManager manager = jda.getShardManager();

        if (manager == null)
        {
            return;
        }

        List<Activity> status = List.of(
                Activity.watching(BotInfo.getGuildCount(manager) + StringUtils.plurify(" server", (int) BotInfo.getGuildCount(manager))),
                Activity.watching(BotInfo.getUserCount(manager) + StringUtils.plurify("  user", (int) BotInfo.getUserCount(manager))),
                Activity.watching("the kids"),
                Activity.listening("your commands"),
                Activity.listening(Constants.DEFAULT_BOT_PREFIX + "help"),
                Activity.playing("forknife!!!!"),
                Activity.playing("with your feelings"),
                Activity.competing("the race to 100 servers")
        );

        jda.getPresence().setPresence(OnlineStatus.ONLINE, status.get(new Random().nextInt(status.size())));
    }

    public LocalDateTime getStartTimestamp()
    {
        return this.startTimestamp;
    }

    public ShardManager getShardManager()
    {
        if (this.shardManager == null)
        {
            throw new UnsupportedOperationException("Shardmanager is not built.");
        }
        return this.shardManager;
    }


    public Configuration getConfiguration()
    {
        return this.configuration;
    }

    public Logger getLogger()
    {
        return this.logger;
    }
}
