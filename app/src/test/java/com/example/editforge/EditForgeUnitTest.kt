package com.example.editforge

import com.example.editforge.data.model.AudioDemosData
import com.example.editforge.data.model.DeliverableType
import com.example.editforge.data.model.ProjectStatus
import com.example.editforge.data.model.SubscriptionTier
import com.example.editforge.data.paddle.BillingCycle
import com.example.editforge.data.paddle.PaddleBillingService
import com.example.editforge.data.paddle.SubscriptionStatus
import com.example.editforge.ui.components.DASHBOARD_AUDIO_PRESETS
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EditForgeUnitTest {

    @Test
    fun testCyberneticGrooveDemoTrack() {
        val master = AudioDemosData.MASTER_TRACK
        assertNotNull(master)
        assertTrue(master.title.contains("Cybernetic Groove.wav"))
        assertEquals(150.0f, master.duration, 0.01f)
        assertEquals("2:30", master.durationLabel)
        assertTrue(master.peaks.isNotEmpty())

        val demos = AudioDemosData.DEMOS
        assertEquals(5, demos.size)
        assertTrue(demos.any { it.id == "edit-60" && it.title.contains("Cybernetic Groove") })
        assertTrue(demos.any { it.id == "edit-30" && it.title.contains("Cybernetic Groove") })
        assertTrue(demos.any { it.id == "edit-15" && it.title.contains("Cybernetic Groove") })
        assertTrue(demos.any { it.id == "edit-sting" })
        assertTrue(demos.any { it.id == "edit-stems" })

        val defaultPreset = DASHBOARD_AUDIO_PRESETS.first()
        assertEquals("Cybernetic Groove.wav", defaultPreset.fileName)
        assertEquals(150.0f, defaultPreset.durationSeconds, 0.01f)
    }

    @Test
    fun testDeliverablePricing() {
        assertEquals(1, DeliverableType.FULL_MIX.creditCost)
        assertEquals(2, DeliverableType.EDIT_60.creditCost)
        assertEquals(2, DeliverableType.EDIT_30.creditCost)
        assertEquals(2, DeliverableType.EDIT_15.creditCost)
        assertEquals(1, DeliverableType.STING.creditCost)
        assertEquals(3, DeliverableType.STEM_VOCALS.creditCost)
    }

    @Test
    fun testSubscriptionTiers() {
        assertEquals(5, SubscriptionTier.FREE.monthlyCredits)
        assertEquals(50, SubscriptionTier.PLUS.monthlyCredits)
        assertEquals(150, SubscriptionTier.PRO.monthlyCredits)
        assertEquals(500, SubscriptionTier.PREMIER.monthlyCredits)
    }

    @Test
    fun testProjectStatuses() {
        assertNotNull(ProjectStatus.UPLOADED)
        assertNotNull(ProjectStatus.ANALYZING)
        assertNotNull(ProjectStatus.ANALYZED)
        assertNotNull(ProjectStatus.COMPLETE)
        assertTrue(ProjectStatus.entries.size >= 5)
    }

    @Test
    fun testPaddleBillingService() = runBlocking {
        val service = PaddleBillingService()
        assertEquals(SubscriptionTier.PRO, service.subscriptionInfo.value.tier)
        assertEquals(SubscriptionStatus.ACTIVE, service.subscriptionInfo.value.status)

        val upgraded = service.upgradeSubscription(SubscriptionTier.PREMIER, BillingCycle.ANNUAL, "9912")
        assertTrue(upgraded)
        assertEquals(SubscriptionTier.PREMIER, service.subscriptionInfo.value.tier)
        assertEquals(BillingCycle.ANNUAL, service.subscriptionInfo.value.cycle)
        assertTrue(service.subscriptionInfo.value.paymentMethodSummary.contains("9912"))
        assertTrue(service.subscriptionInfo.value.invoices.isNotEmpty())

        service.cancelSubscription()
        assertEquals(SubscriptionStatus.CANCELLED, service.subscriptionInfo.value.status)

        service.resumeSubscription()
        assertEquals(SubscriptionStatus.ACTIVE, service.subscriptionInfo.value.status)
    }
}
