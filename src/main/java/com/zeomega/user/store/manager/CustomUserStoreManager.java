package com.zeomega.user.store.manager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.user.api.Properties;
import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.core.UserRealm;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.user.core.claim.ClaimManager;
import org.wso2.carbon.user.core.jdbc.JDBCRealmConstants;
import org.wso2.carbon.user.core.jdbc.JDBCUserStoreManager;
import org.wso2.carbon.user.core.profile.ProfileConfigurationManager;
import org.wso2.carbon.user.core.util.DatabaseUtil;

import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Map;


/**
 * Custom User Store Manager Class
 *
 * JDBCUserStoreManager can not be used for a user table with contains two columns. Therefore these
 * override method just ensure that reading is done according to the custom schema.
 * Therefore most of the override methods are same as the methods in JDBCUserStoreManager class.
 * 
 * Some functionality has been limited this user table such as tenant aware, salted password
 * value ,creating time of user and etc.
 * 
 */
public class CustomUserStoreManager extends JDBCUserStoreManager {


    private static Log log = LogFactory.getLog(CustomUserStoreManager.class);

    public CustomUserStoreManager() {
    }

    public CustomUserStoreManager(org.wso2.carbon.user.api.RealmConfiguration realmConfig,
                                  Map<String, Object> properties,
                                  ClaimManager claimManager,
                                  ProfileConfigurationManager profileManager,
                                  UserRealm realm, Integer tenantId)
            throws UserStoreException {
        super(realmConfig, properties, claimManager, profileManager, realm, tenantId, false);
    }

    public static String getMD5(String input) {
        String md5 = null;
         
        if(null == input) return null;
         
        try {
             
        //Create MessageDigest object for MD5
        MessageDigest digest = MessageDigest.getInstance("MD5");
         
        //Update input string in message digest
        digest.update(input.getBytes(), 0, input.length());
 
        //Converts message digest value in base 16 (hex) 
        md5 = new BigInteger(1, digest.digest()).toString(16);
 
        } catch (NoSuchAlgorithmException e) {
 
            e.printStackTrace();
        }
        return md5;
    }
    
    @Override
    public boolean doAuthenticate(String userName, Object credential) throws UserStoreException {

        if (CarbonConstants.REGISTRY_ANONNYMOUS_USERNAME.equals(userName)) {
            log.error("Anonymous user trying to login");
            return false;
        }

        Connection dbConnection = null;
        ResultSet rs = null;
        PreparedStatement prepStmt = null;
        String sqlstmt = null;
        String password = (String) credential;
        String salt = password.substring(0, 4);
        String pwd = getMD5(password+salt);
        boolean isAuthed = false;

        try {
            dbConnection = getDBConnection();
            dbConnection.setAutoCommit(false);
            sqlstmt = realmConfig.getUserStoreProperty(JDBCRealmConstants.SELECT_USER);

            if (log.isDebugEnabled()) {
                log.debug(sqlstmt);
            }

            prepStmt = dbConnection.prepareStatement(sqlstmt);
            prepStmt.setString(1, userName);

            rs = prepStmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString(6);
                log.info(storedPassword+"and"+pwd);
                if ((storedPassword != null) && (storedPassword.trim().equals(pwd))) {
                    isAuthed = true;
                }

            }
        } catch (SQLException e) {
            throw new UserStoreException("Authentication Failure. Using sql :" + sqlstmt);
        } finally {
            DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
        }

        if (log.isDebugEnabled()) {
            log.debug("User " + userName + " login attempt. Login success :: " + isAuthed);
        }

        return isAuthed;

    }

    @Override
    public Date getPasswordExpirationTime(String userName) throws UserStoreException {
        return null;
    }

    protected boolean isValueExisting(String sqlStmt, Connection dbConnection, Object... params)
            throws UserStoreException {
        PreparedStatement prepStmt = null;
        ResultSet rs = null;
        boolean isExisting = false;
        boolean doClose = false;
        try {
            if (dbConnection == null) {
                dbConnection = getDBConnection();
                doClose = true; 
            }
            if (DatabaseUtil.getStringValuesFromDatabase(dbConnection, sqlStmt, params).length > 0) {
                isExisting = true;
            }
            return isExisting;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            log.error("Using sql : " + sqlStmt);
            throw new UserStoreException(e.getMessage(), e);
        } finally {
            if (doClose) {
                DatabaseUtil.closeAllConnections(dbConnection, rs, prepStmt);
            }
        }
    }

    public String[] getUserListFromProperties(String property, String value, String profileName)
            throws UserStoreException {
        return new String[0];
    }


    @Override
    public boolean isReadOnly() throws UserStoreException {
        return true;
    }

    @Override
    public void doAddUser(String userName, Object credential, String[] roleList,
                          Map<String, String> claims, String profileName,
                          boolean requirePasswordChange) throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    public void doAddRole(String roleName, String[] userList, org.wso2.carbon.user.api.Permission[] permissions)
            throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    @Override
    public void doDeleteRole(String roleName) throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    @Override
    public void doDeleteUser(String userName) throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    @Override
    public boolean isBulkImportSupported() {
        return false;
    }

    @Override
    public void doUpdateRoleName(String roleName, String newRoleName) throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    @Override
    public void doUpdateUserListOfRole(String roleName, String[] deletedUsers, String[] newUsers)
            throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    @Override
    public void doUpdateRoleListOfUser(String userName, String[] deletedRoles, String[] newRoles)
            throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    @Override
    public void doSetUserClaimValue(String userName, String claimURI, String claimValue,
                                    String profileName) throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    @Override
    public void doSetUserClaimValues(String userName, Map<String, String> claims,
                                     String profileName) throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    @Override
    public void doDeleteUserClaimValue(String userName, String claimURI, String profileName)
            throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    @Override
    public void doDeleteUserClaimValues(String userName, String[] claims, String profileName)
            throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    @Override
    public void doUpdateCredential(String userName, Object newCredential, Object oldCredential)
            throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    @Override
    public void doUpdateCredentialByAdmin(String userName, Object newCredential)
            throws UserStoreException {
        throw new UserStoreException(
                "User store is operating in read only mode. Cannot write into the user store.");
    }

    public String[] getExternalRoleListOfUser(String userName) throws UserStoreException {
        /*informix user store manager is supposed to be read only and users in the custom user store
          users in the custom user store are only assigned to internal roles. Therefore this method
          returns an empty string.
         */

        return new String[0];
    }

    @Override
    public String[] doGetRoleNames(String filter, int maxItemLimit) throws UserStoreException {
        return new String[0];
    }

    @Override
    public boolean doCheckExistingRole(String roleName) throws UserStoreException {

        return false;
    }

    @Override
    public boolean doCheckExistingUser(String userName) throws UserStoreException {

        return true;
    }

    @Override
    public org.wso2.carbon.user.api.Properties getDefaultUserStoreProperties(){
        Properties properties = new Properties();
        properties.setMandatoryProperties(CustomUserStoreConstants.CUSTOM_UM_MANDATORY_PROPERTIES.toArray
                (new Property[CustomUserStoreConstants.CUSTOM_UM_MANDATORY_PROPERTIES.size()]));
        properties.setOptionalProperties(CustomUserStoreConstants.CUSTOM_UM_OPTIONAL_PROPERTIES.toArray
                (new Property[CustomUserStoreConstants.CUSTOM_UM_OPTIONAL_PROPERTIES.size()]));
        properties.setAdvancedProperties(CustomUserStoreConstants.CUSTOM_UM_ADVANCED_PROPERTIES.toArray
                (new Property[CustomUserStoreConstants.CUSTOM_UM_ADVANCED_PROPERTIES.size()]));
        return properties;
    }
}

