package lc.lcaccounts;

import lc.lcaccounts.configuration.LCConfig;
import lc.lcaccounts.entidades.Database;
import lc.lcaccounts.listener.LoginEvent;
import lc.lcaccounts.utilidades.Util;
import net.md_5.bungee.api.plugin.Plugin;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public final class LCAccounts extends Plugin {

    private static LCAccounts instance;
    public static String clave;
    public static LCConfig databaseConfig;
    public boolean selfCrashing = false;
    public static final Set<String> premiums = new HashSet<>();

    @Override
    public void onEnable() {
        instance = this;
        databaseConfig = new LCConfig("database", this);
        Database.loadDatabaseConfig(databaseConfig.getConfig());
        clave = databaseConfig.getConfig().getString("Encriptador.Clave");
        try {
            Database.conectar();
        } catch (SQLException e) {
            Util.console("&c[LCAccounts] No se ha podido conectar con la base de datos");
            Util.console("&c[LCAccounts] &4Adios!");
            selfCrashing = true;
            getProxy().stop();
            return;
        }
        Util.console("&a[LCAccounts] ¡Conexión realizada con la base de datos!");
        checkearConexion();
        getProxy().getPluginManager().registerListener(this, new LoginEvent());
    }

    @Override
    public void onDisable() {
        if(!selfCrashing){
            Util.console("&c[LCAccounts] &4Adios!");
        }
    }
    private void checkearConexion() {
        getProxy().getScheduler().schedule(this, Database::checkearConexion,  1800L, 1800L, TimeUnit.SECONDS);
    }

    public static LCAccounts get() {
        return instance;
    }
}
