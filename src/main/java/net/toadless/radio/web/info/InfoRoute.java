package net.toadless.radio.web.info;


import io.javalin.http.Context;
import io.javalin.http.Handler;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.toadless.radio.modules.CommandModule;
import net.toadless.radio.modules.WebModule;
import org.jetbrains.annotations.NotNull;

public class InfoRoute implements Handler
{
    private final WebModule webModule;

    public InfoRoute(WebModule webModule)
    {
        this.webModule = webModule;
    }

    @Override
    public void handle(@NotNull Context ctx)
    {
        ShardManager shardManager = webModule.getRadio().getShardManager();
        webModule.ok(ctx, DataObject.empty()
                .put("shards", shardManager.getShardCache().size())
                .put("guilds", shardManager.getGuildCache().size())
                .put("users", shardManager.getGuildCache().applyStream(guildStream -> guildStream.mapToInt(Guild::getMemberCount).sum()))
                .put("jda_version", JDAInfo.VERSION)
                .put("commands", webModule.getRadio().getModules().get(CommandModule.class).getCommandMap().values().stream().distinct().count())
        );
    }
}