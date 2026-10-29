package com.example.mobile_skadi;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SquadsSearchUnitTest {

    @Test
    public void matchesMemberSearch_emptyQuery_returnsTrue() {
        assertTrue(SquadsActivity.matchesMemberSearch("diego valadares operador", ""));
        assertTrue(SquadsActivity.matchesMemberSearch("diego valadares operador", "   "));
        assertTrue(SquadsActivity.matchesMemberSearch("diego valadares operador", null));
    }

    @Test
    public void matchesMemberSearch_byNameOrRole_returnsTrue() {
        assertTrue(SquadsActivity.matchesMemberSearch("diego valadares operador", "Diego"));
        assertTrue(SquadsActivity.matchesMemberSearch("marise peres gestora gestor", "GESTOR"));
        assertTrue(SquadsActivity.matchesMemberSearch("sérgio silva sergio silva operador", "sergio"));
    }

    @Test
    public void matchesMemberSearch_nonMatchingQuery_returnsFalse() {
        assertFalse(SquadsActivity.matchesMemberSearch("roberta furtado operador", "caio"));
        assertFalse(SquadsActivity.matchesMemberSearch(null, "diego"));
    }

    @Test
    public void teamMemberDetailsExtras_constantsAreDefinedCorrectly() {
        assertEquals("extra_member_name", TeamMemberDetailsActivity.EXTRA_MEMBER_NAME);
        assertEquals("extra_member_role", TeamMemberDetailsActivity.EXTRA_MEMBER_ROLE);
        assertEquals("extra_member_initials", TeamMemberDetailsActivity.EXTRA_MEMBER_INITIALS);
        assertEquals("extra_member_username", TeamMemberDetailsActivity.EXTRA_MEMBER_USERNAME);
        assertEquals("extra_open_alerts", TeamMemberDetailsActivity.EXTRA_OPEN_ALERTS);
        assertEquals("extra_resolved_alerts", TeamMemberDetailsActivity.EXTRA_RESOLVED_ALERTS);
    }
}
