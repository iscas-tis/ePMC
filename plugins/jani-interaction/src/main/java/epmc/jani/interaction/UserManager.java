/****************************************************************************

    ePMC - an extensible probabilistic model checker
    Copyright (C) 2017

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

 *****************************************************************************/

package epmc.jani.interaction;

import static epmc.error.UtilError.*;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.MessageFormat;
import java.util.Base64;
import java.util.Random;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import epmc.jani.interaction.database.Database;
import epmc.jani.interaction.error.ProblemsJANIInteractionJDBC;
import epmc.jani.interaction.options.OptionsJANIInteractionJDBC;
import epmc.options.Options;

public final class UserManager {
    /** Statement to create the login information table. */
    private final static String SQL_CREATE_LOGIN_TABLE = "CREATE TABLE IF NOT EXISTS login (id {0}, username VARCHAR UNIQUE, password VARCHAR, salt VARCHAR);";
    private final static String SQL_CREATE_USER = "INSERT INTO login (username,password,salt) VALUES (?,?,?);";
    private final static String SQL_FIND_USER = "SELECT id, password, salt FROM login WHERE username = ?;";
    private final static String SQL_CHANGE_PASSWORD_BY_ID = "UPDATE login SET password=?, salt=? WHERE id=?";
    private final static String SQL_CHANGE_PASSWORD_BY_USERNAME = "UPDATE login SET password=?, salt=? WHERE username=?";
    private final static String SQL_DELETE_USER_BY_USERNAME = "DELETE FROM login WHERE username=?";
    private final static String SQL_CHANGE_USERNAME_BY_USERNAME = "UPDATE login SET username=? WHERE username=?";

    /** Type of algorithm to use for secret factory. */
    private final static String SECRET_FACTORY = "PBKDF2WithHmacSHA1";
    /** TODO ?? */
    private final static int ITERATION_COUNT = 65536;
    /** Length of key to generate from password. */
    private final static int KEY_LENGTH = 128;

    /** Random object to use to create new salts. */
    private final Random random = new Random();

    private final Connection connection;
    private final PreparedStatement createUser;
    private final PreparedStatement findUser;
    private final PreparedStatement changePasswordByID;
    private final PreparedStatement changePasswordByUsername;
    private final PreparedStatement deleteUserByUsername;
    private final PreparedStatement changeUsernameByUsername;

    public UserManager(Database storage) {
        assert storage != null;
        connection = storage.getConnection();
        Options options = Options.get();
        try {
            String createLoginTableString = MessageFormat.format(SQL_CREATE_LOGIN_TABLE, 
                    new Object[]{options.getString(OptionsJANIInteractionJDBC.JANI_INTERACTION_JDBC_DBTYPE_PRIMARY_KEY_AUTOINCREMENT)});
            PreparedStatement createLoginTable = connection.prepareStatement(createLoginTableString);
            createLoginTable.execute();
            connection.commit();
            createUser = connection.prepareStatement(SQL_CREATE_USER, Statement.RETURN_GENERATED_KEYS);
            findUser = connection.prepareStatement(SQL_FIND_USER);
            changePasswordByID = connection.prepareStatement(SQL_CHANGE_PASSWORD_BY_ID);
            changePasswordByUsername = connection.prepareStatement(SQL_CHANGE_PASSWORD_BY_USERNAME);
            deleteUserByUsername = connection.prepareStatement(SQL_DELETE_USER_BY_USERNAME);
            changeUsernameByUsername = connection.prepareStatement(SQL_CHANGE_USERNAME_BY_USERNAME);
        } catch (SQLException e) {
            fail(ProblemsJANIInteractionJDBC.JANI_INTERACTION_JDBC_SQL_ERROR, e, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Create a new user.
     * The user name and password shall be clear text.
     * None of the parameters may be {@code null}.
     * 
     * @param username username of user to create
     * @param password password of user to create
     * @return -1 in case of failure, otherwise unique identifier
     */
    public int createUser(String username, String password) {
        assert username != null;
        assert password != null;
        byte[] saltBytes = newSalt();
        String encodedPassword = encodePassword(password, saltBytes);
        String saltString = Base64.getEncoder().encodeToString(saltBytes);
        try {
            createUser.setString(1, username);
            createUser.setString(2, encodedPassword);
            createUser.setString(3, saltString);
        } catch (SQLException e) {
            fail(ProblemsJANIInteractionJDBC.JANI_INTERACTION_JDBC_SQL_ERROR, e, e.getMessage());
            throw new RuntimeException(e);
        }
        try {
            createUser.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            return -1;
        }
        ResultSet rs;
        try {
            rs = createUser.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            fail(ProblemsJANIInteractionJDBC.JANI_INTERACTION_JDBC_SQL_ERROR, e, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Check the credentials of a given user.
     * The login parameter must not be null;
     * The user name and password will be checked against the credentials known
     * by the user manager. The function will return {@code true} if and only if
     * a user with the given user name exists and the password is correct, function will return .
     * 
     * @param username name of user to try to log in
     * @param password password of user to try to log in
     * @return identifier for user or -1 in case of failure
     */
    public int checkLogin(String username, String password) {
        assert username != null;
        assert password != null;
        try {
            findUser.setString(1, username);
            ResultSet resultSet = findUser.executeQuery();
            if (!resultSet.next()) {
                return -1;
            }
            int id = resultSet.getInt(1);
            String storedPassword = resultSet.getString(2);
            String salt = resultSet.getString(3);
            if (checkPassword(password, storedPassword, salt)) {
                return id;
            }
        } catch (SQLException e) {
            fail(ProblemsJANIInteractionJDBC.JANI_INTERACTION_JDBC_SQL_ERROR, e, e.getMessage());
            throw new RuntimeException(e);
        }
        return -1;
    }

    public void changePassword(int userID, String password) {
        byte[] saltBytes = newSalt();
        String encodedPassword = encodePassword(password, saltBytes);
        String saltString = Base64.getEncoder().encodeToString(saltBytes);
        try {
            changePasswordByID.setString(1, encodedPassword);
            changePasswordByID.setString(2, saltString);
            changePasswordByID.setInt(3, userID);
            changePasswordByID.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            fail(ProblemsJANIInteractionJDBC.JANI_INTERACTION_JDBC_SQL_ERROR, e, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void changePassword(String username, String password) {
        byte[] saltBytes = newSalt();
        String encodedPassword = encodePassword(password, saltBytes);
        String saltString = Base64.getEncoder().encodeToString(saltBytes);
        try {
            changePasswordByUsername.setString(1, encodedPassword);
            changePasswordByUsername.setString(2, saltString);
            changePasswordByUsername.setString(3, username);
            changePasswordByUsername.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            fail(ProblemsJANIInteractionJDBC.JANI_INTERACTION_JDBC_SQL_ERROR, e, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private boolean checkPassword(String check, String stored, String salt) {
        assert check != null;
        assert stored != null;
        assert salt != null;
        String encoded = encodePassword(check, salt);
        return encoded.equals(stored);
    }

    /**
     * Generate a new salt for a new user.
     * 
     * @return new salt for new user
     */
    private byte[] newSalt() {
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return salt;
    }

    private String encodePassword(String password, String salt) {
        assert password != null;
        assert salt != null;
        Base64.Decoder decoder = Base64.getDecoder();
        return encodePassword(password, decoder.decode(salt));
    }

    private String encodePassword(String password, byte[] salt) {
        assert password != null;
        assert salt != null;
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKeyFactory f;
        try {
            f = SecretKeyFactory.getInstance(SECRET_FACTORY);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        byte[] hash;
        try {
            hash = f.generateSecret(spec).getEncoded();
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
        Base64.Encoder enc = Base64.getEncoder();
        return enc.encodeToString(hash);
    }

    public void delete(String username) {
        assert username != null;
        try {
            deleteUserByUsername.setString(1, username);
            deleteUserByUsername.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            fail(ProblemsJANIInteractionJDBC.JANI_INTERACTION_JDBC_SQL_ERROR, e, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void changeUsername(String usernameOld, String usernameNew) {
        assert usernameOld != null;
        assert usernameNew != null;
        try {
            changeUsernameByUsername.setString(1, usernameNew);
            changeUsernameByUsername.setString(2, usernameOld);
            changeUsernameByUsername.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            fail(ProblemsJANIInteractionJDBC.JANI_INTERACTION_JDBC_SQL_ERROR, e, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
