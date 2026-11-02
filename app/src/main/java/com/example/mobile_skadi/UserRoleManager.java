package com.example.mobile_skadi;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.example.mobile_skadi.network.SessionManager;

public final class UserRoleManager {

    public enum Role {
        GESTOR,
        FUNCIONARIO
    }

    private static Role currentRole = null;

    private UserRoleManager() {
    }

    public static Role getCurrentRole() {
        if (currentRole == null) {
            return Role.GESTOR;
        }
        return currentRole;
    }

    public static Role getCurrentRole(Context context) {
        if (currentRole != null) return currentRole;
        if (context != null) {
            String roleStr = SessionManager.getInstance(context).getRole();
            if (roleStr != null && !roleStr.trim().isEmpty()) {
                if (roleStr.equalsIgnoreCase("ROLE_USER")
                        || roleStr.equalsIgnoreCase("FUNCIONARIO")
                        || roleStr.equalsIgnoreCase("OPERADOR")) {
                    currentRole = Role.FUNCIONARIO;
                    return currentRole;
                }
            }
            String username = SessionManager.getInstance(context).getUsername();
            if (isEmployeeAccount(username)) {
                currentRole = Role.FUNCIONARIO;
                return currentRole;
            }
        }
        currentRole = Role.GESTOR;
        return currentRole;
    }

    public static void setCurrentRole(Role role) {
        currentRole = (role != null) ? role : Role.GESTOR;
    }

    public static Role setRoleFromUsername(String username) {
        if (isEmployeeAccount(username)) {
            currentRole = Role.FUNCIONARIO;
        } else {
            currentRole = Role.GESTOR;
        }
        return currentRole;
    }

    public static boolean isEmployee() {
        return getCurrentRole() == Role.FUNCIONARIO;
    }

    public static boolean isEmployee(Context context) {
        return getCurrentRole(context) == Role.FUNCIONARIO;
    }

    static boolean isEmployeeAccount(String username) {
        if (username == null) return false;
        String lower = username.trim().toLowerCase();
        return lower.contains("diego")
                || lower.contains("operador")
                || lower.contains("funcionario")
                || lower.contains("funcionário");
    }

    public static String getFirstNameForGreeting(Context context) {
        if (context != null) {
            String name = SessionManager.getInstance(context).getName();
            if (name != null && !name.trim().isEmpty()) {
                String[] parts = name.trim().split("\\s+");
                if (parts.length > 0 && !parts[0].isEmpty()) {
                    return parts[0];
                }
            }
            String username = SessionManager.getInstance(context).getUsername();
            if (username != null && !username.trim().isEmpty()) {
                return username;
            }
        }
        return isEmployee() ? "Diego" : "Maria Eduarda";
    }

    public static String getFullName(Context context) {
        if (context != null) {
            String name = SessionManager.getInstance(context).getName();
            if (name != null && !name.trim().isEmpty()) {
                return name;
            }
        }
        return isEmployee() ? "Diego Valadares" : "Maria Eduarda Rosa";
    }

    public static String getFirstNameForGreeting() {
        return getFirstNameForGreeting(null);
    }

    public static String getFullName() {
        return getFullName(null);
    }

    public static String getRoleLabel() {
        return isEmployee() ? "Operador(a)" : "Gestor(a)";
    }

    public static String getUsernameHandle(Context context) {
        if (context != null) {
            String user = SessionManager.getInstance(context).getUsername();
            if (user != null && !user.trim().isEmpty()) {
                return user;
            }
        }
        return isEmployee() ? "diego.valadares" : "maria.rosa0912";
    }

    public static String getUsernameHandle() {
        return getUsernameHandle(null);
    }

    public static void applyBottomNavForRole(Activity activity) {
        if (activity == null) return;
        View navRelatorios = activity.findViewById(R.id.navRelatorios);
        if (navRelatorios != null) {
            navRelatorios.setVisibility(isEmployee(activity) ? View.GONE : View.VISIBLE);
        }
    }
}
