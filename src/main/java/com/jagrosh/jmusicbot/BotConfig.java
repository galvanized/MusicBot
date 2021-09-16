/*
 * Copyright 2018 John Grosh (jagrosh)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jagrosh.jmusicbot;

import com.jagrosh.jmusicbot.entities.Prompt;
import com.jagrosh.jmusicbot.utils.FormatUtil;
import com.jagrosh.jmusicbot.utils.OtherUtil;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.typesafe..*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;

/**
 * 
 * 
 * @author John Grosh (jagrosh)
 */
public class Bot
{
    private final Prompt prompt;
    private final static String CONTEXT = "";
    private final static String START_TOKEN = "/// START OF JMUSICBOT  ///";
    private final static String END_TOKEN = "/// END OF JMUSICBOT  ///";
    
    private Path path = null;
    private String token, prefix, altprefix, helpWord, playlistsFolder,
            successEmoji, warningEmoji, errorEmoji, loadingEmoji, searchingEmoji;
    private boolean stayInChannel, songInGame, npImages, updatealerts, useEval, dbots;
    private long owner, maxSeconds, aloneTimeUntilStop;
    private OnlineStatus status;
    private Activity game;
    private  aliases, transforms;

    private boolean valid = false;
    
    public Bot(Prompt prompt)
    {
        this.prompt = prompt;
    }
    
    public void load()
    {
        valid = false;
        
        // read  from file
        try 
        {
            // get the path to the , default .txt
            path = OtherUtil.getPath(System.getProperty(".file", System.getProperty("", ".txt")));
            if(path.toFile().exists())
            {
                if(System.getProperty(".file") == null)
                    System.setProperty(".file", System.getProperty("", ".txt"));
                Factory.invalidateCaches();
            }
            
            // load in the  file, plus the default values
            //  = Factory.parseFile(path.toFile()).withFallback(Factory.load());
              = Factory.load();
            
            // set values
            token = .getString("token");
            prefix = .getString("prefix");
            altprefix = .getString("altprefix");
            helpWord = .getString("help");
            owner = .getLong("owner");
            successEmoji = .getString("success");
            warningEmoji = .getString("warning");
            errorEmoji = .getString("error");
            loadingEmoji = .getString("loading");
            searchingEmoji = .getString("searching");
            game = OtherUtil.parseGame(.getString("game"));
            status = OtherUtil.parseStatus(.getString("status"));
            stayInChannel = .getBoolean("stayinchannel");
            songInGame = .getBoolean("songinstatus");
            npImages = .getBoolean("npimages");
            updatealerts = .getBoolean("updatealerts");
            useEval = .getBoolean("eval");
            maxSeconds = .getLong("maxtime");
            aloneTimeUntilStop = .getLong("alonetimeuntilstop");
            playlistsFolder = .getString("playlistsfolder");
            aliases = .get("aliases");
            transforms = .get("transforms");
            dbots = owner == 113156185389092864L;
            
            // we may need to write a new  file
            boolean write = false;

            // validate bot token
            if(token==null || token.isEmpty() || token.equalsIgnoreCase("BOT_TOKEN_HERE"))
            {
                token = prompt.prompt("Please provide a bot token."
                        + "\nInstructions for obtaining a token can be found here:"
                        + "\nhttps://github.com/jagrosh/MusicBot/wiki/Getting-a-Bot-Token."
                        + "\nBot Token: ");
                if(token==null)
                {
                    prompt.alert(Prompt.Level.WARNING, CONTEXT, "No token provided! Exiting.\n\n Location: " + path.toAbsolutePath().toString());
                    return;
                }
                else
                {
                    write = true;
                }
            }
            
            // validate bot owner
            if(owner<=0)
            {
                try
                {
                    owner = Long.parseLong(prompt.prompt("Owner ID was missing, or the provided owner ID is not valid."
                        + "\nPlease provide the User ID of the bot's owner."
                        + "\nInstructions for obtaining your User ID can be found here:"
                        + "\nhttps://github.com/jagrosh/MusicBot/wiki/Finding-Your-User-ID"
                        + "\nOwner User ID: "));
                }
                catch(NumberFormatException | NullPointerException ex)
                {
                    owner = 0;
                }
                if(owner<=0)
                {
                    prompt.alert(Prompt.Level.ERROR, CONTEXT, "Invalid User ID! Exiting.\n\n Location: " + path.toAbsolutePath().toString());
                    return;
                }
                else
                {
                    write = true;
                }
            }
            
            if(write)
                writeToFile();
            
            // if we get through the whole , it's good to go
            valid = true;
        }
        catch (Exception ex)
        {
            prompt.alert(Prompt.Level.ERROR, CONTEXT, ex + ": " + ex.getMessage() + "\n\n Location: " + path.toAbsolutePath().toString());
        }
    }
    
    private void writeToFile()
    {
        String original = OtherUtil.loadResource(this, "/reference.conf");
        byte[] bytes;
        if(original==null)
        {
            bytes = ("token = "+token+"\r\nowner = "+owner).getBytes();
        }
        else
        {
            bytes = original.substring(original.indexOf(START_TOKEN)+START_TOKEN.length(), original.indexOf(END_TOKEN))
                .replace("BOT_TOKEN_HERE", token)
                .replace("0 // OWNER ID", Long.toString(owner))
                .trim().getBytes();
        }
        try 
        {
            Files.write(path, bytes);
        }
        catch(IOException ex) 
        {
            prompt.alert(Prompt.Level.WARNING, CONTEXT, "Failed to write new  options to .txt: "+ex
                + "\nPlease make sure that the files are not on your desktop or some other restricted area.\n\n Location: " 
                + path.toAbsolutePath().toString());
        }
    }
    
    public boolean isValid()
    {
        return valid;
    }
    
    public String getLocation()
    {
        return path.toFile().getAbsolutePath();
    }
    
    public String getPrefix()
    {
        return prefix;
    }
    
    public String getAltPrefix()
    {
        return "NONE".equalsIgnoreCase(altprefix) ? null : altprefix;
    }
    
    public String getToken()
    {
        return token;
    }
    
    public long getOwnerId()
    {
        return owner;
    }
    
    public String getSuccess()
    {
        return successEmoji;
    }
    
    public String getWarning()
    {
        return warningEmoji;
    }
    
    public String getError()
    {
        return errorEmoji;
    }
    
    public String getLoading()
    {
        return loadingEmoji;
    }
    
    public String getSearching()
    {
        return searchingEmoji;
    }
    
    public Activity getGame()
    {
        return game;
    }
    
    public OnlineStatus getStatus()
    {
        return status;
    }
    
    public String getHelp()
    {
        return helpWord;
    }
    
    public boolean getStay()
    {
        return stayInChannel;
    }
    
    public boolean getSongInStatus()
    {
        return songInGame;
    }
    
    public String getPlaylistsFolder()
    {
        return playlistsFolder;
    }
    
    public boolean getDBots()
    {
        return dbots;
    }
    
    public boolean useUpdateAlerts()
    {
        return updatealerts;
    }
    
    public boolean useEval()
    {
        return useEval;
    }
    
    public boolean useNPImages()
    {
        return npImages;
    }
    
    public long getMaxSeconds()
    {
        return maxSeconds;
    }
    
    public String getMaxTime()
    {
        return FormatUtil.formatTime(maxSeconds * 1000);
    }

    public long getAloneTimeUntilStop()
    {
        return aloneTimeUntilStop;
    }
    
    public boolean isTooLong(AudioTrack track)
    {
        if(maxSeconds<=0)
            return false;
        return Math.round(track.getDuration()/1000.0) > maxSeconds;
    }

    public String[] getAliases(String command)
    {
        try
        {
            return aliases.getStringList(command).toArray(new String[0]);
        }
        catch(NullPointerException | Exception.Missing e)
        {
            return new String[0];
        }
    }
    
    public  getTransforms()
    {
        return transforms;
    }
}
