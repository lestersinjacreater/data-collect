package ke.linda.donate.redactor

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvTest {
    @Test fun quotesCommasQuotesAndNewlines() {
        assertEquals("plain", Csv.cell("plain"))
        assertEquals("\"a,b\"", Csv.cell("a,b"))
        assertEquals("\"say \"\"hi\"\"\"", Csv.cell("say \"hi\""))
        assertEquals("\"l1\nl2\"", Csv.cell("l1\nl2"))
    }

    @Test fun formulaStartsAreNeutralised() {
        assertEquals("'=SUM(A1)", Csv.cell("=SUM(A1)"))
        assertEquals("'+254", Csv.cell("+254"))
    }

    @Test fun renderHasBomHeaderAndEmptyLabel() {
        val out = Csv.render(listOf(DonationRow("d1", "MPESA", "company", "hello, world", "2026-10-20T14:02:11Z")))
        assertTrue(out.startsWith("﻿donor_id,sender_id,sender_type,text,received_at,label\r\n"))
        assertTrue(out.endsWith("d1,MPESA,company,\"hello, world\",2026-10-20T14:02:11Z,\r\n"))
    }
}
