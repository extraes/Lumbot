package slaynash.lum.bot.discord.logscanner;

public class ModVersion {
    public VersionUtils.VersionData version;
    public String hash;
    
    public ModVersion(String version, String hash) {
        this.version = VersionUtils.GetVersion(version);
        this.hash = hash;
    }
}
