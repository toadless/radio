package net.toadless.radio.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import net.toadless.radio.modules.DatabaseModule;
import net.dv8tion.jda.api.entities.Icon;

public class IOUtils
{
    private IOUtils()
    {
        //Overrides the default, public, constructor
    }

    public static InputStream getResourceFile(String fileName)
    {
        InputStream file;
        try
        {
            file = DatabaseModule.class.getClassLoader().getResourceAsStream(fileName);
        }
        catch (Exception exception)
        {
            return null;
        }
        return file;
    }

    public static Icon getIcon(String url)
    {
        Icon icon;
        try
        {
            icon = Icon.from(new URL(url).openStream());

        }
        catch (Exception exception)
        {
            return null;
        }
        return icon;
    }

    public static InputStream getFromURL(String url)
    {
        try
        {
            return new URL(url).openStream();
        }
        catch (Exception exception)
        {
            return null;
        }
    }

    public static String convertToString(InputStream inputStream)
    {
        InputStreamReader isReader = new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(isReader);

        StringBuilder stringBuilder = new StringBuilder();
        String str;
        try
        {
            while ((str = reader.readLine()) != null)
            {
                stringBuilder.append(str);
            }
        }
        catch (Exception exception)
        {
            return "";
        }

        return stringBuilder.toString();
    }

    public static boolean isURL(String url)
    {
        try
        {
            URL obj = new URL(url);
            obj.toURI();
            return true;
        }
        catch (Exception exception)
        {
            return false;
        }
    }
}