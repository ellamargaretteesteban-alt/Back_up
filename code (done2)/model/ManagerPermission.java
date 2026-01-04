package model;

/**
 * ManagerPermission data class representing manager function permissions.
 */
public class ManagerPermission {
    public String managerUsername;
    public boolean canRequestMedicine; // Can request add/edit/remove medicine
    public boolean canRequestUser; // Can request add/edit/remove users
    public boolean canRequestHealthTip; // Can request add/edit/remove health tips

    public ManagerPermission(String managerUsername, boolean canRequestMedicine,
                            boolean canRequestUser, boolean canRequestHealthTip) {
        this.managerUsername = managerUsername;
        this.canRequestMedicine = canRequestMedicine;
        this.canRequestUser = canRequestUser;
        this.canRequestHealthTip = canRequestHealthTip;
    }

    public ManagerPermission(String managerUsername) {
        this(managerUsername, true, true, true); // Default: all enabled
    }
}
