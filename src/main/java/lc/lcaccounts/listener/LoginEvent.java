package lc.lcaccounts.listener;

import lc.lcaccounts.LCAccounts;
import lc.lcaccounts.entidades.Database;
import lc.lcaccounts.entidades.LCProfile;
import lc.lcaccounts.utilidades.Util;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

@SuppressWarnings("deprecation")
public class LoginEvent implements Listener {

    @EventHandler
    public void onPreLogin(PreLoginEvent event){
        String name = event.getConnection().getName();
        event.registerIntent(LCAccounts.get());
        LCProfile profile = LCProfile.getProfile(name);
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
                jug.setLastIP(e.getPlayer().getPendingConnection().getAddress().getHostString());
                Database.saveProfile(jug);
                LCAccounts.premiums.add(e.getPlayer().getName());
                return;
            }
            if(e.getPlayer().getPendingConnection().getAddress().getHostString().equals(jug.getContrasenia())){
                jug.setAuthLogged(true);
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
}
