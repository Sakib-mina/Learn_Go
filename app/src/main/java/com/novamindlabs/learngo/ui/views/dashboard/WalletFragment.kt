package com.novamindlabs.learngo.ui.views.dashboard

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.*
import com.novamindlabs.learngo.databinding.FragmentWalletBinding
import com.novamindlabs.learngo.ui.viewModel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class WalletFragment : Fragment() {
    private var _binding: FragmentWalletBinding? = null
    private val binding get() = _binding!!
    private val authViewModel: AuthViewModel by activityViewModels()
    private lateinit var billingClient: BillingClient
    private var isProcessingPurchase = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentWalletBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBillingClient()
        observeData()
        setupClickListeners()
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(requireContext())
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (purchase in purchases) {
                        handlePurchase(purchase)
                    }
                } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                    showToast("Purchase Cancelled")
                }
                isProcessingPurchase = false
            }
            .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
            .build()
        startBillingConnection()
    }

    private fun startBillingConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryPurchases()
                }
            }
            override fun onBillingServiceDisconnected() {
                lifecycleScope.launch { startBillingConnection() }
            }
        })
    }

    private fun setupClickListeners() {
        binding.apply {
            btnBuy50.setOnClickListener { initiatePurchase("coins_50") }
            btnBuy100.setOnClickListener { initiatePurchase("coins_100") }
            btnBuy200.setOnClickListener { initiatePurchase("coins_200") }
            btnBuy300.setOnClickListener { initiatePurchase("coins_300") }
            btnBuy400.setOnClickListener { initiatePurchase("coins_400") }
            btnBuy500.setOnClickListener { initiatePurchase("coins_500") }
            btnBuy600.setOnClickListener { initiatePurchase("coins_600") }
            btnBuy1000.setOnClickListener { initiatePurchase("coins_1000") }
            btnBuy99.setOnClickListener { initiatePurchase("coins_99") }
            btnBuy199.setOnClickListener { initiatePurchase("coins_199") }
        }
    }

    private fun initiatePurchase(productId: String) {
        if (isProcessingPurchase) return
        if (!billingClient.isReady) {
            startBillingConnection()
            return
        }

        isProcessingPurchase = true

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsResult ->
            val productDetailsList = productDetailsResult.productDetailsList
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                val productDetails = productDetailsList[0]
                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
                    ))
                    .build()
                billingClient.launchBillingFlow(requireActivity(), billingFlowParams)
            } else {
                isProcessingPurchase = false
                showToast("Product not available!")
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            val consumeParams = ConsumeParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()

            billingClient.consumeAsync(consumeParams) { billingResult, _ ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val coinsToAdd = getCoinAmountFromId(purchase.products[0])
                        authViewModel.updateCoins(coinsToAdd)

                        withContext(Dispatchers.Main) {
                            showToast("✨ Success! $coinsToAdd Coins added.")
                        }
                    }
                }
            }
        }
    }

    private fun queryPurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchases) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        handlePurchase(purchase)
                    }
                }
            }
        }
    }

    private fun getCoinAmountFromId(id: String): Int {
        return when (id) {
            "coins_50" -> 50
            "coins_100" -> 100
            "coins_200" -> 200
            "coins_300" -> 300
            "coins_400" -> 400
            "coins_500" -> 500
            "coins_600" -> 600
            "coins_1000" -> 1000
            "coins_99" -> 90
            "coins_199" -> 250
            else -> 0
        }
    }

    @SuppressLint("DefaultLocale")
    private fun observeData() {
        viewLifecycleOwner.lifecycleScope.launch {
            authViewModel.userCoins.collectLatest { coins ->
                binding.tvWalletBalance.text = String.format("%,d", coins)
            }
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::billingClient.isInitialized) billingClient.endConnection()
        _binding = null
    }
}