package net.toadless.radio.objects.bot;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

public class BotTask
{
    private final ScheduledFuture<?> task;
    private final String name;
    private final long expiresAt;
    private final TimeUnit unit;

    public BotTask(@NotNull ScheduledFuture<?> task, @NotNull String name, long expiresAt, @NotNull TimeUnit timeUnit)
    {
        this.task = task;
        this.name = name;
        this.expiresAt = expiresAt;
        this.unit = timeUnit;
    }

    public @NotNull TimeUnit getUnit()
    {
        return unit;
    }

    public @NotNull ScheduledFuture<?> getTask()
    {
        return task;
    }

    public @NotNull String getName()
    {
        return name;
    }

    public long getExpiresAt()
    {
        return expiresAt;
    }

    public void cancel(boolean shouldInterrupt)
    {
        task.cancel(shouldInterrupt);
    }
}