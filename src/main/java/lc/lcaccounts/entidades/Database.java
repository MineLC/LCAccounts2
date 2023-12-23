package lc.lcaccounts.entidades;

import lc.lcaccounts.LCAccounts;
import lc.lcaccounts.seguridad.EncriptadorAES;
import lc.lcaccounts.utilidades.Util;
import net.md_5.bungee.config.Configuration;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Database {

    private static String ip;
    private static String puerto;
    private static String dbNombre;
    private static String usuario;
    private static String contrasenia;

    private static final EncriptadorAES encriptador = new EncriptadorAES();

    private static Connection connection = null;

    public static void loadDatabaseConfig(Configuration c) {
        ip = c.getString("MySQL.Address");
        puerto = c.getString("MySQL.Puerto");
        dbNombre = c.getString("MySQL.Database");
        usuario = c.getString("MySQL.Usuario");
        contrasenia = c.getString("MySQL.Password");
    }

    public static void conectar() throws SQLException {
        Util.console("&a[LCAccounts] Conectando con la base de datos...");
        Util.console("&a[LCAccounts] Host: &e" + ip + "&a:&e" + puerto);
        try {
            InetAddress address = InetAddress.getByName(ip);
            ip = address.getHostAddress();
        } catch (UnknownHostException e) {
            Util.console("&c[LCAccounts] Host desconocido.");
        }
        connection = DriverManager.getConnection("jdbc:mysql://" + ip + ":" + puerto + "/" + dbNombre + "?autoReconnect=true", usuario, contrasenia);
        crearTabla();
    }

    private static void crearTabla() throws SQLException {
        String update_accounts = "CREATE TABLE IF NOT EXISTS Cuentas (`ID` INTEGER AUTO_INCREMENT UNIQUE, `Player` VARCHAR(24) UNIQUE, `Password` VARCHAR(100), `ultimaSalida` VARCHAR(24), `Premium` BOOLEAN, `UltimaIP` VARCHAR(16), PRIMARY KEY (`ID`), KEY (`Player`))";

        Statement update = connection.createStatement();
        update.execute(update_accounts);
        update.close();
    }
    public static void reconnect() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://" + ip + ":" + puerto + "/" + dbNombre + "?autoReconnect=true", usuario, contrasenia);
        } catch (Exception e) {
            Util.console("&c[LCAccounts] No se ha podido re-conectar con la base de datos, reintentando...");

        }
    }
    public static void checkearConexion() {
        try {
            if (connection == null || connection.isClosed()) {
                reconnect();
            }
        } catch (Exception e) {
            reconnect();
        }
    }
    public static Boolean isPlayerInDB(String name) {
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            statement = connection.prepareStatement("SELECT * FROM `Cuentas` WHERE `Player` = ?;");
            statement.setString(1, name);
            resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        close(
                resultSet
        );
        close(statement);
        return false;
    }
    public static void loadProfile(LCProfile profile) {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            String queryBuilder = "SELECT * FROM `Cuentas` WHERE `Player` = ?;";
            preparedStatement = connection.prepareStatement(queryBuilder);
            preparedStatement.setString(1, profile.getNombre());
            resultSet = preparedStatement.executeQuery();

            if (resultSet != null && resultSet.next()) {
                profile.setLastIP(encriptador.desencriptar(resultSet.getString(
                        "LastIP"
                ), LCAccounts.clave));
                if(resultSet.getString("Password") == null ||
                        resultSet.getString("Password").equalsIgnoreCase("null") ||
                        resultSet.getString("Password").isEmpty()){
                    profile.setContrasenia(null);
                }else
                    profile.setContrasenia(encriptador.desencriptar(resultSet.getString("Password"), LCAccounts.clave));
                if(resultSet.getTimestamp("ultimaSalida") == null) profile.setLastQuit(null);
                else profile.setLastQuit(resultSet.getString("ultimaSalida"));
            }
        } catch (SQLException | UnsupportedEncodingException | NoSuchAlgorithmException | InvalidKeyException |
                 NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException Exception) {
            Util.console("&c[LCAccounts] Excepcion cargando Cuenta de "+profile.getNombre()+". (Talvez cuenta Premium invalida)");
        } finally {
            close(resultSet);
            close(preparedStatement);
        }



    }
    public static void createProfile(LCProfile profile) {
        PreparedStatement statement = null;
        try {

            String queryBuilder = "INSERT INTO `Cuentas` (`Player`, `Password`, `ultimaSalida`, `Premium`, `UltimaIP`) VALUES (?, ?, ?, ?, ?);";
            statement = connection.prepareStatement(queryBuilder);
            statement.setString(1, profile.getNombre());
            statement.setString(2, null);
            statement.setTimestamp(3, null);
            statement.setBoolean(4, profile.isPremium());
            statement.setString(5, profile.getLastIP());
            statement.executeUpdate();
        } catch (SQLException e) {
            Util.console("&c[LCAccounts] Excepcion creando Cuenta de "+profile.getNombre()+".");
        }finally {
            close(statement);
        }
    }

    public static boolean isUsernamePremium(String username) {
        URL url = null;
        try {
            url = new URL("https://api.mojang.com/users/profiles/minecraft/"+username);
            BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
            String line;
            StringBuilder result = new StringBuilder();
            while ((line = in.readLine())!=null){
                result.append(line);
            }
            return !result.toString().isEmpty();
        } catch (IOException e) {
            return false;
        }

    }


    private static void close(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (final Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void close(PreparedStatement preparedStatement) {
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (Exception e) {
                checkearConexion();
            }
        } else {
            checkearConexion();
        }
    }

    public static void saveProfile(LCProfile jug) {
        LCAccounts.get().getProxy().getScheduler().runAsync(LCAccounts.get(), () -> {
            PreparedStatement preparedStatement = null;
            try {
                StringBuilder queryBuilder = new StringBuilder();
                queryBuilder.append("INSERT INTO `Cuentas` ");
                queryBuilder.append("(`Player`, `UltimaIP`, `Password`, `ultimaSalida`, `Premium`) VALUES ");
                queryBuilder.append("(?, ?, ?, ?, ?) ");
                queryBuilder.append("ON DUPLICATE KEY UPDATE ");
                queryBuilder.append("`UltimaIP` = ?, `Password` = ?, `ultimaSalida` = ?, `Premium` = ?;");
                preparedStatement = connection.prepareStatement(queryBuilder.toString());
                preparedStatement.setString(1, jug.getNombre());

                preparedStatement.setString(2, encriptador.encriptar(jug.getLastIP(), LCAccounts.clave));

                preparedStatement.setString(3, encriptador.encriptar(jug.getContrasenia(), LCAccounts.clave));
                preparedStatement.setString(4, jug.getLastQuit());
                if(jug.isPremium()){
                    preparedStatement.setInt(5, 1);
                }else{
                    preparedStatement.setInt(5, 0);
                }
                //UPDATE
                preparedStatement.setString(6,encriptador.encriptar( jug.getLastIP(), LCAccounts.clave));

                preparedStatement.setString(3, encriptador.encriptar(jug.getContrasenia(), LCAccounts.clave));
                preparedStatement.setString(8, jug.getLastQuit());
                if(jug.isPremium()){
                    preparedStatement.setInt(9, 1);
                }else{
                    preparedStatement.setInt(9, 0);
                }




                preparedStatement.executeUpdate();
            } catch (Exception sqlException) {
                Util.console("&c[LCAccounts] Excepcion guardando la Cuenta de "+jug.getNombre()+".");
            } finally {
                close(preparedStatement);
            }
        });
    }

    public static void deleteProfile(LCProfile profile) {
        PreparedStatement statement = null;
        try{
            String delete = "DELETE FROM `Cuentas` WHERE  `Player`=?;";
            statement = connection.prepareStatement(delete);
            statement.setString(1, profile.getNombre());
            int eliminateds = statement.executeUpdate();

            if(eliminateds != 0){
                Util.console("&a[LCAccounts] Se ha eliminado la cuenta &e"+profile.getNombre()+" &aéxitosamente");
            }else{
                Util.console("&c[LCAccounts] No se ha podido eliminar la cuenta &4"+profile.getNombre()+"&c.");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }finally {
            close(statement);
        }
    }

    public static ArrayList<String> getPlayerbyIP(LCProfile ap) {
        ArrayList<String> list = new ArrayList<>();
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        try {
            String queryBuilder = "SELECT * " +
                    "FROM `Cuentas` " +
                    "WHERE `IP` = ? ;";
            preparedStatement = connection.prepareStatement(queryBuilder);
            preparedStatement.setString(1, ap.getLastIP());
            resultSet = preparedStatement.executeQuery();
            while (resultSet != null && resultSet.next()) {
                String player = resultSet.getString("Player");
                list.add(player);
            }
        } catch (Exception sqlException) {
            throw new RuntimeException(sqlException);
        } finally {
            close(resultSet);
            close(preparedStatement);
        }
        return list;
    }

    public static ArrayList<String> getPremiums() {
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;
        ArrayList<String> premiums = new ArrayList<>();
        try {
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("SELECT * ");
            queryBuilder.append("FROM `Cuentas` ");
            queryBuilder.append("WHERE `Premium` = 1;");
            preparedStatement = connection.prepareStatement(queryBuilder.toString());
            resultSet = preparedStatement.executeQuery();
            if (resultSet != null)
                while (resultSet.next()) {
                    premiums.add(resultSet.getString("Player"));
                }
        } catch (Exception sqlException) {
            sqlException.printStackTrace();
        } finally {
            close(preparedStatement);
            close(resultSet);
        }
        return premiums;
    }
}
