package net.toadless.radio.web.shards;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.utils.data.DataArray;
import net.dv8tion.jda.api.utils.data.DataObject;
import net.toadless.radio.modules.WebModule;
import org.jetbrains.annotations.NotNull;

public class ShardsRoute implements Handler
{
    private final WebModule webModule;

    public ShardsRoute(WebModule webModule)
    {
        this.webModule = webModule;
    }

    @Override
    public void handle(@NotNull Context ctx)
    {
        webModule.ok(ctx, DataObject.empty()
                .put("shards", DataArray.fromCollection(
                        webModule.getRadio().getShardManager().getShardCache().stream().map(
                                shard -> DataObject.empty()
                                        .put("id", shard.getShardInfo().getShardId())
                                        .put("guilds", shard.getGuildCache().size())
                                        .put("status", shard.getStatus().name())
                                        .put("ping", shard.getGatewayPing())

                        ).collect(Collectors.toList())))
        );
    }
}