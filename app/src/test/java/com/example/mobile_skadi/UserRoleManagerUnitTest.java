package com.example.mobile_skadi;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UserRoleManagerUnitTest {

    @After
    public void tearDown() {
        UserRoleManager.setCurrentRole(UserRoleManager.Role.GESTOR);
        UserRoleManager.setCurrentPlan(UserRoleManager.Plan.EMPRESARIAL);
    }

    @Test
    public void setRoleFromUsername_withEmployeeKeywords_setsFuncionarioRole() {
        assertEquals(
                UserRoleManager.Role.FUNCIONARIO,
                UserRoleManager.setRoleFromUsername("diego.valadares")
        );
        assertTrue(UserRoleManager.isEmployee());
        assertEquals("Diego", UserRoleManager.getFirstNameForGreeting());
        assertEquals("Diego Valadares", UserRoleManager.getFullName());
        assertEquals("Operador(a)", UserRoleManager.getRoleLabel());
        assertEquals("diego.valadares", UserRoleManager.getUsernameHandle());
    }

    @Test
    public void setRoleFromUsername_withManagerOrDefault_setsGestorRole() {
        assertEquals(
                UserRoleManager.Role.GESTOR,
                UserRoleManager.setRoleFromUsername("maria.rosa0912")
        );
        assertFalse(UserRoleManager.isEmployee());
        assertEquals("Maria Eduarda", UserRoleManager.getFirstNameForGreeting());
        assertEquals("Maria Eduarda Rosa", UserRoleManager.getFullName());
        assertEquals("Gestor(a)", UserRoleManager.getRoleLabel());
        assertEquals("maria.rosa0912", UserRoleManager.getUsernameHandle());
    }

    @Test
    public void setRoleFromUsername_withBasicoPlan_setsBasicoPlan() {
        UserRoleManager.setRoleFromUsername("maria.basico");
        assertTrue(UserRoleManager.isBasicoPlan());

        UserRoleManager.setRoleFromUsername("maria.rosa0912");
        assertFalse(UserRoleManager.isBasicoPlan());
    }
}
