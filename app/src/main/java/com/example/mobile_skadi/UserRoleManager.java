package com.example.mobile_skadi;

import android.app.Activity;
import android.view.View;

public final class UserRoleManager {

    public enum Role {
        GESTOR,
        FUNCIONARIO
    }

    private static Role currentRole = Role.GESTOR;

    private UserRoleManager() {
    }

    public static Role getCurrentRole() {
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
        return currentRole == Role.FUNCIONARIO;
    }

    static boolean isEmployeeAccount(String username) {
        if (username == null) return false;
        String lower = username.trim().toLowerCase();
        return lower.contains("diego")
                || lower.contains("operador")
                || lower.contains("funcionario")
                || lower.contains("funcionário");
    }

    public static String getFirstNameForGreeting() {
        return isEmployee() ? "Diego" : "Maria Eduarda";
    }

    public static String getFullName() {
        return isEmployee() ? "Diego Valadares" : "Maria Eduarda Rosa";
    }

    public static String getRoleLabel() {
        return isEmployee() ? "Operador(a)" : "Gestor(a)";
    }

    public static String getUsernameHandle() {
        return isEmployee() ? "diego.valadares" : "maria.rosa0912";
    }

    public static void applyBottomNavForRole(Activity activity) {
        if (activity == null) return;
        View navRelatorios = activity.findViewById(R.id.navRelatorios);
        if (navRelatorios != null) {
            navRelatorios.setVisibility(isEmployee() ? View.GONE : View.VISIBLE);
        }
    }
}
