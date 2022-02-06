package net.toadless.radio.objects.module;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import net.toadless.radio.Radio;
import net.toadless.radio.objects.exception.ModuleNotFoundException;

import java.lang.reflect.Constructor;
import java.util.LinkedHashMap;
import java.util.Map;

public class Modules
{
    public static final String MODULE_PACKAGE = "net.toadless.radio.modules";

    private final ClassGraph classGraph = new ClassGraph().acceptPackages(MODULE_PACKAGE);
    private final Map<Class<?>, Module> modules;
    private final Radio radio;

    public Modules(Radio radio)
    {
        this.radio = radio;
        modules = loadModules();
    }

    public Map<Class<?>, Module> loadModules()
    {
        Map<Class<?>, Module> modules = new LinkedHashMap<>();
        try (ScanResult result = classGraph.scan())
        {
            for (ClassInfo cls : result.getAllClasses())
            {
                Constructor<?>[] constructors = cls.loadClass().getDeclaredConstructors();
                if (constructors.length == 0)
                {
                    radio.getLogger().warn("No valid constructors found for Module class (" + cls.getSimpleName() + ")!");
                    continue;
                }
                if (constructors[0].getParameterCount() > 2)
                {
                    continue;
                }
                Object instance = constructors[0].newInstance(radio, this);
                if (!(instance instanceof Module))
                {
                    radio.getLogger().warn("Non Module class (" + cls.getSimpleName() + ") found in commands package!");
                    continue;
                }
                modules.put(instance.getClass(), ((Module) instance));
            }
        }
        catch (Exception exception)
        {
            radio.getLogger().error("A module exception occurred", exception);
            System.exit(1);
        }

        radio.getLogger().info("Finished loading {} modules", modules.size());
        return modules;
    }

    public Object[] getModules()
    {
        return this.modules.values().toArray();
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T get(Class<T> clazz)
    {
        var module = this.modules.get(clazz);
        if(module == null)
        {
            throw new ModuleNotFoundException(clazz);
        }
        return (T) module;
    }
}