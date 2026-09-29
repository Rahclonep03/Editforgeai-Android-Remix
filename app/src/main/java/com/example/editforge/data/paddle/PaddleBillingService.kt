package com.example.editforge.data.paddle

import com.example.editforge.data.model.SubscriptionTier
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SubscriptionStatus(val label: String) {
    ACTIVE("Active"),
    TRIALING("Trialing"),
    PAUSED("Paused"),
    CANCELLED("Cancelled")
}

enum class BillingCycle(val label: String, val discountPercent: Int) {
    MONTHLY("Monthly", 0),
    ANNUAL("Annual (Save 20%)", 20)
}

data class PaddleInvoice(
    val invoiceId: String,
    val date: String,
    val planName: String,
    val amountFormatted: String,
    val status: String = "Paid",
    val pdfReceiptUrl: String = "https://paddle.com/receipt"
)

data class PaddleSubscriptionInfo(
    val subscriptionId: String = "sub_pdl_01h8x4k9audio2026",
    val customerEmail: String = "producer@editforge.ai",
    val tier: SubscriptionTier = SubscriptionTier.PRO,
    val status: SubscriptionStatus = SubscriptionStatus.ACTIVE,
    val cycle: BillingCycle = BillingCycle.MONTHLY,
    val nextBillingDate: String = "October 25, 2026",
    val paymentMethodSummary: String = "Visa ending in 4242",
    val paddleCustomerId: String = "ctm_01h8x4k9editforge",
    val lastChargeAmount: String = "$21.99",
    val invoices: List<PaddleInvoice> = listOf(
        PaddleInvoice("INV-PDL-2026-0925", "Sep 25, 2026", "Pro Studio Plan (Monthly)", "$21.99"),
        PaddleInvoice("INV-PDL-2026-0825", "Aug 25, 2026", "Pro Studio Plan (Monthly)", "$21.99"),
        PaddleInvoice("INV-PDL-2026-0725", "Jul 25, 2026", "Pro Studio Plan (Monthly)", "$21.99")
    )
)

class PaddleBillingService {

    private val _subscriptionInfo = MutableStateFlow(PaddleSubscriptionInfo())
    val subscriptionInfo: StateFlow<PaddleSubscriptionInfo> = _subscriptionInfo.asStateFlow()

    private val _isProcessingCheckout = MutableStateFlow(false)
    val isProcessingCheckout: StateFlow<Boolean> = _isProcessingCheckout.asStateFlow()

    fun updateCustomerEmail(email: String) {
        _subscriptionInfo.value = _subscriptionInfo.value.copy(customerEmail = email)
    }

    fun updateBillingCycle(cycle: BillingCycle) {
        _subscriptionInfo.value = _subscriptionInfo.value.copy(cycle = cycle)
    }

    fun getWebCheckoutUrl(tier: SubscriptionTier, cycle: BillingCycle): String {
        val tierParam = tier.name.lowercase()
        val cycleParam = if (cycle == BillingCycle.ANNUAL) "annual" else "monthly"
        return "https://editforge.ai/checkout?plan=$tierParam&interval=$cycleParam"
    }

    fun getCreditPackCheckoutUrl(): String {
        return "https://editforge.ai/checkout?product=credits_100&amount=9.99"
    }

    suspend fun upgradeSubscription(
        newTier: SubscriptionTier,
        cycle: BillingCycle,
        cardNumberLast4: String = "4242"
    ): Boolean {
        _isProcessingCheckout.value = true
        kotlinx.coroutines.delay(1200) // Handshake with Paddle MoR Gateway

        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
        val todayStr = dateFormat.format(Date())
        val price = if (cycle == BillingCycle.ANNUAL) {
            newTier.annualPriceFormatted
        } else {
            newTier.price
        }

        val newInvoice = PaddleInvoice(
            invoiceId = "INV-PDL-" + System.currentTimeMillis().toString().takeLast(6),
            date = todayStr,
            planName = "${newTier.label} Plan (${cycle.label})",
            amountFormatted = price
        )

        val updatedInvoices = listOf(newInvoice) + _subscriptionInfo.value.invoices

        _subscriptionInfo.value = _subscriptionInfo.value.copy(
            tier = newTier,
            cycle = cycle,
            status = SubscriptionStatus.ACTIVE,
            paymentMethodSummary = "Card ending in $cardNumberLast4",
            lastChargeAmount = price,
            invoices = updatedInvoices
        )

        _isProcessingCheckout.value = false
        return true
    }

    fun cancelSubscription() {
        _subscriptionInfo.value = _subscriptionInfo.value.copy(
            status = SubscriptionStatus.CANCELLED
        )
    }

    fun resumeSubscription() {
        _subscriptionInfo.value = _subscriptionInfo.value.copy(
            status = SubscriptionStatus.ACTIVE
        )
    }
}
