package net.toadless.radio.web.invite;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import net.toadless.radio.modules.WebModule;
import org.jetbrains.annotations.NotNull;

public class InviteBotRoute implements Handler
{
    private final WebModule webModule;

    public InviteBotRoute(WebModule webModule)
    {
        this.webModule = webModule;
    }

    @Override
    public void handle(@NotNull Context ctx)
    {
        ctx.redirect("https://discord.com/oauth2/authorize?client_id=" + webModule.getRadio().getJDA().getSelfUser().getId() + "&permissions=8&scope=bot");
    }
}