package lc.lcaccounts.listener;

import lc.lcaccounts.LCAccounts;
import lc.lcaccounts.entidades.Database;
import lc.lcaccounts.entidades.LCProfile;
import lc.lcaccounts.utilidades.Util;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import javax.xml.crypto.Data;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings("deprecation")
public class LoginEvent implements Listener {

    @EventHandler
    public void onPreLogin(PreLoginEvent event){
        String name = event.getConnection().getName();
        event.registerIntent(LCAccounts.get());
        LCProfile profile = LCProfile.getProfile(name);
        if(Database.isPlayerInDB(profile.getNombre())) Database.loadProfile(profile);

        profile.setUuid(event.getConnection().getUniqueId());

        LCAccounts.get().getProxy().getScheduler().runAsync(LCAccounts.get(), () -> {
            try {
                profile.setPremium(Database.isUsernamePremium(name));
                Database.loadProfile(profile);
                if(profile.isPremium()){
                    event.getConnection().setOnlineMode(true);
                }
                if(profile.getLastIP() != null && profile.getLastIP().equals(event.getConnection().getAddress().getHostString())){
                    profile.setAuthLogged(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                event.setCancelled(true);
            }finally {
                event.completeIntent(LCAccounts.get());
            }
        });
    }

    @EventHandler
    public void onPostLogin(final PostLoginEvent e) {
        LCAccounts.get().getProxy().getScheduler().runAsync(LCAccounts.get(), () -> {
            LCProfile jug = LCProfile.getProfile(e.getPlayer().getName());
            jug.setPlayer(e.getPlayer());
            jug.setUuid(e.getPlayer().getUniqueId());
            if (e.getPlayer().getPendingConnection().isOnlineMode()){
                jug.setAuthLogged(true);
                jug.setCaptcha(true);
                jug.setLastIP(e.getPlayer().getPendingConnection().getAddress().getHostString());
                Database.saveProfile(jug);
                LCAccounts.premiums.add(e.getPlayer().getName());
                return;
            }
            if(e.getPlayer().getPendingConnection().getAddress().getHostString().equals(jug.getLastIP())){
                jug.setAuthLogged(true);
                jug.setCaptcha(true);
                jug.setLastIP(e.getPlayer().getPendingConnection().getAddress().getHostString());
                Database.saveProfile(jug);
            }
        });

    }
    @EventHandler
    public void onConnect(ServerConnectEvent e) {
        LCProfile jug = LCProfile.getProfile(e.getPlayer().getName());
        if(!e.getTarget().getName().equalsIgnoreCase("auth")){
            if(!Database.isPlayerInDB(jug.getNombre()) || jug.getContrasenia() == null){
                e.setTarget(ProxyServer.getInstance().getServerInfo("auth"));
            }else if(!jug.isAuthLogged()){
                e.setTarget(ProxyServer.getInstance().getServerInfo("auth"));
            }
            return;
        }
        if(!Database.isPlayerInDB(jug.getNombre()) || jug.getContrasenia() == null){
            Util.sendMessage(e.getPlayer(), "&aRegistrate con /register <clave> <clave>");
            return;
        }
        if(jug.isAuthLogged() || jug.isPremium()){
            e.setTarget(ProxyServer.getInstance().getServerInfo("lobby"));
            if (e.getPlayer().getPendingConnection().isOnlineMode()) {
                jug.setAuthLogged(true);
                Util.sendMessage(e.getPlayer(), "&a¡Ingreso Premium realizado éxitosamente!");
                return;
            }
            Util.sendMessage(e.getPlayer(), "&aHas iniciado sesión correctamente.");
        }else
            Util.sendMessage(e.getPlayer(), "&aRegistrate con /login <clave>");

    }

    @SuppressWarnings("SuspiciousMethodCalls")
    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent e){
        LCProfile profile = LCProfile.getProfile(e.getPlayer().getName());
        if(profile.isRegistered()){
            if(!Database.isPlayerInDB(profile.getNombre())) Database.createProfile(profile);
            Date date = new Date();
            DateFormat hourdateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
            profile.setLastQuit(hourdateFormat.format(date));
            Database.saveProfile(profile);
        }
        if(profile.getContrasenia() == null){
            Database.deleteProfile(profile);
        }
        LCProfile.profiles.remove(LCProfile.getProfile(e.getPlayer().getName()));
    }
}
