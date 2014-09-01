package com.zeomega.user.store.manager;


import org.wso2.carbon.user.api.Property;
import org.wso2.carbon.user.core.UserStoreConfigConstants;
import org.wso2.carbon.user.core.jdbc.JDBCRealmConstants;

import java.util.ArrayList;

public class CustomUserStoreConstants {


    //Properties for Read Active Directory User Store Manager
    public static final ArrayList<Property> CUSTOM_UM_MANDATORY_PROPERTIES = new ArrayList<Property>();
    public static final ArrayList<Property> CUSTOM_UM_OPTIONAL_PROPERTIES = new ArrayList<Property>();
    public static final ArrayList<Property> CUSTOM_UM_ADVANCED_PROPERTIES = new ArrayList<Property>();


    static {

        setMandatoryProperty(JDBCRealmConstants.DRIVER_NAME, "Driver Name", "", "Full qualified driver name");
        setMandatoryProperty(JDBCRealmConstants.URL,"Connection URL", "", "URL of the user store database");
        setMandatoryProperty(JDBCRealmConstants.USER_NAME, "User Name","", "Username for the database");
        setMandatoryProperty(JDBCRealmConstants.PASSWORD, "Password","", "Password for the database");

        setProperty(UserStoreConfigConstants.disabled,"Disabled", "false", UserStoreConfigConstants.disabledDescription);

        setProperty("ReadOnly","Read Only", "true", "Indicates whether the user store of this realm operates in the user read only mode or not");
        setProperty(UserStoreConfigConstants.SCIMEnabled,"SCIM Enabled", "false", UserStoreConfigConstants.SCIMEnabledDescription);


        //Advanced Properties (No descriptions added for each property)
        setAdvancedProperty("SelectUserSQL","Select User SQL", "select * from sys_user where sys_user_id =?", "");
        setAdvancedProperty("UserFilterSQL","User Filter SQL", "select sys_user_id FROM sys_user where sys_user_id LIKE ?  ORDER BY user_idn", "");

    }


    private static void setProperty(String name, String displayName, String value, String description) {
        Property property = new Property(name, value, description, null);
        CUSTOM_UM_OPTIONAL_PROPERTIES.add(property);

    }

    private static void setMandatoryProperty(String name, String displayName, String value, String description) {
        Property property = new Property(name, value, description, null);
        CUSTOM_UM_MANDATORY_PROPERTIES.add(property);

    }

    private static void setAdvancedProperty(String name, String displayName, String value, String description) {
        Property property = new Property(name, value, description, null);
        CUSTOM_UM_ADVANCED_PROPERTIES.add(property);

    }

}

