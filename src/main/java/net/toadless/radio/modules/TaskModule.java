package net.toadless.radio.modules;

import net.toadless.radio.Radio;
import net.toadless.radio.objects.bot.BotTask;
import net.toadless.radio.objects.module.Module;
import net.toadless.radio.objects.module.Modules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TaskModule extends Module
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskModule.class);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);
    private final List<BotTask> tasks = new ArrayList<>();
    private final List<UUID> currentUUIDs = new ArrayList<>();

    public TaskModule(Radio radio, Modules modules)
    {
        super(radio, modules);
        LOGGER.debug("Started TaskHandler.");
    }

    public BotTask addTask(Runnable task, TimeUnit unit, long time)
    {
        String taskName = getTaskName();
        BotTask botTask = new BotTask(scheduler.schedule(task, time, unit), taskName, time, unit);
        tasks.add(botTask);
        scheduleDeletion(botTask);
        LOGGER.debug("Added new task with name " + taskName + " expires in " + botTask.getExpiresAt() + " " + botTask.getUnit());
        return botTask;
    }

    public BotTask addTask(Runnable task, String taskName, TimeUnit unit, long time)
    {
        BotTask botTask = new BotTask(scheduler.schedule(task, time, unit), taskName, time, unit);
        tasks.add(botTask);
        scheduleDeletion(botTask);
        LOGGER.debug("Added new task with name " + taskName + " expires in " + botTask.getExpiresAt() + " " + botTask.getUnit());
        return botTask;
    }

    public BotTask addTask(Callable<?> task, String taskName, TimeUnit unit, long time)
    {
        BotTask botTask = new BotTask(scheduler.schedule(task, time, unit), taskName, time, unit);
        tasks.add(botTask);
        scheduleDeletion(botTask);
        LOGGER.debug("Added new task with name " + taskName + " expires in " + botTask.getExpiresAt() + " " + botTask.getUnit());
        return botTask;
    }

    public BotTask addRepeatingTask(Runnable task, String taskName, long initialDelay, TimeUnit unit, long period)
    {
        BotTask botTask = new BotTask(scheduler.scheduleAtFixedRate(task, initialDelay, period, unit), taskName, period + initialDelay, unit);
        tasks.add(botTask);
        return botTask;
    }

    public BotTask addRepeatingTask(Runnable task, String taskName, TimeUnit unit, long time)
    {
        return addRepeatingTask(task, taskName, 0, unit, time);
    }

    public BotTask addRepeatingTask(Runnable task, TimeUnit unit, long time)
    {
        return addRepeatingTask(task, "" + System.currentTimeMillis(), 0, unit, time);
    }

    public BotTask getTask(String taskName)
    {
        for (BotTask task : tasks)
        {
            if (task.getName().equalsIgnoreCase(taskName))
            {
                return task;
            }
        }
        return null;
    }

    public void cancelTask(String taskName, boolean shouldInterrupt)
    {
        LOGGER.debug("Cancelling task " + taskName);
        for (BotTask task : tasks)
        {
            if (task.getName().equalsIgnoreCase(taskName))
            {
                LOGGER.debug("Cancelled task " + taskName);
                task.getTask().cancel(shouldInterrupt);
                return;
            }
        }
        LOGGER.debug("Task " + taskName + " could not be found");
    }

    public void close()
    {
        LOGGER.debug("Closing TaskHandler");
        for (BotTask task : tasks)
        {
            task.cancel(false);
        }
        LOGGER.debug("TaskHandler closed");
    }

    public String getTaskName()
    {
        UUID uuid = UUID.randomUUID();
        if (!currentUUIDs.contains(uuid))
        {
            currentUUIDs.add(uuid);
            return uuid.toString();
        }
        else
        {
            return getTaskName();
        }
    }

    public List<BotTask> getTasks()
    {
        return tasks;
    }

    private void scheduleDeletion(BotTask task)
    {
        LOGGER.debug("Task " + task.getName() + " scheduled for deletion in " + task.getExpiresAt() + " " + task.getUnit());
        scheduler.schedule(() -> tasks.remove(task), task.getExpiresAt(), task.getUnit());
    }
}
