package lc.lcaccounts.entidades;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LCProfile {

    public static final Map<String, LCProfile> profiles = new HashMap<>();
    private final String nombre;
    private String lastQuit;
    private ProxiedPlayer player;
    private UUID uuid;
    private String contrasenia;

    private boolean authLogged = false;
    private boolean registered = false;

    private String lastIP;
    private boolean captcha = false;
    private boolean premium;

    public LCProfile(String nombre) {
        this.nombre = nombre;
    }

    public boolean isAuthLogged() {
        return authLogged;
    }

    public void setAuthLogged(boolean authLogged) {
        this.authLogged = authLogged;
    }

    public static LCProfile getProfile(String name){
        if(profiles.containsKey(name)) return profiles.get(name);
        LCProfile newProfile = new LCProfile(name);
        profiles.put(name, newProfile);
        return newProfile;
    }

    public boolean isRegistered() {
        return registered;
    }

    public void setRegistered(boolean registered) {
        this.registered = registered;
    }

    public void setContrasenia(String contrasenia) {
        this.contrasenia = contrasenia;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public boolean isPremium() {
        return premium;
    }

    public ProxiedPlayer getPlayer() {
        return player;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }

    public void setPlayer(ProxiedPlayer player) {
        this.player = player;
    }

    public String getLastQuit() {
        return lastQuit;
    }

    public void setLastQuit(String lastQuit) {
        this.lastQuit = lastQuit;
    }

    public String getNombre() {
        return nombre;
    }

    public String getContrasenia() {
        return contrasenia;
    }

    public String getLastIP() {
        return lastIP;
    }

    public void setLastIP(String lastIP) {
        this.lastIP = lastIP;
    }

    public void getUUID(UUID id) {
        this.uuid = id;
    }

    public UUID getUUID() {
        return uuid;
    }

    public boolean isCaptcha() {
        return captcha;
    }

    public void setCaptcha(boolean b) {
        this.captcha = b;
    }
}
