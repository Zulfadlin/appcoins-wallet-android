package com.asfoundation.wallet.wallet_validation.generic

import android.animation.Animator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.asf.wallet.R
import com.asfoundation.wallet.ui.BaseActivity
import com.asfoundation.wallet.ui.TransactionsActivity
import com.asfoundation.wallet.wallet_validation.ValidationInfo
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_wallet_validation.*
import kotlinx.android.synthetic.main.layout_referral_status.*

class WalletValidationActivity : BaseActivity(),
    WalletValidationView {

  private lateinit var presenter: WalletValidationPresenter

  private var minFrame = 0
  private var maxFrame = 30
  private var loopAnimation = -1

  private val hasBeenInvitedFlow: Boolean by lazy {
    intent.getBooleanExtra(HAS_BEEN_INVITED_FLOW, false)
  }

  private val navigateToTransactions: Boolean by lazy {
    intent.getBooleanExtra(NAVIGATE_TO_TRANSACTIONS, true)
  }

  companion object {
    const val FRAME_RATE = 30
    const val HAS_BEEN_INVITED_FLOW = "has_been_invited_flow"
    const val NAVIGATE_TO_TRANSACTIONS = "navigate_to_transactions"

    @JvmStatic
    fun newIntent(context: Context, hasBeenInvitedFlow: Boolean,
                  navigateToTransactions: Boolean): Intent {
      val intent = Intent(context, WalletValidationActivity::class.java)
      intent.putExtra(HAS_BEEN_INVITED_FLOW, hasBeenInvitedFlow)
      intent.putExtra(NAVIGATE_TO_TRANSACTIONS, navigateToTransactions)
      return intent
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    AndroidInjection.inject(this)
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_wallet_validation)
    presenter = WalletValidationPresenter(this)

    setupUI()

    presenter.present()
  }

  private fun setupUI() {
    validation_progress_animation.setMinAndMaxFrame(minFrame, maxFrame)
    validation_progress_animation.repeatCount = loopAnimation
    validation_progress_animation.playAnimation()
    setupToolbar()
  }

  private fun setupToolbar() {
    if (!navigateToTransactions || !hasBeenInvitedFlow) {
      toolbar()
      setTitle(getString(R.string.verification_settings_unverified_title))
      wallet_validation_toolbar.visibility = View.VISIBLE
    }
  }

  override fun showProgressAnimation() {
    validation_progress_animation.visibility = View.VISIBLE
  }

  override fun hideProgressAnimation() {
    validation_progress_animation.visibility = View.INVISIBLE
  }

  override fun showPhoneValidationView(countryCode: String?, phoneNumber: String?,
                                       errorMessage: Int?) {
    if (countryCode != null && phoneNumber != null) {
      reverseAnimation(30, 60, 0)
    }
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            PhoneValidationFragment.newInstance(
                countryCode, phoneNumber, errorMessage, hasBeenInvitedFlow))
        .commit()

    Handler().postDelayed({
      if (countryCode != null && phoneNumber != null) {
        reverseAnimation(0, 30, -1)
      }
    }, 1000)

  }

  override fun showCodeValidationView(countryCode: String, phoneNumber: String) {
    increaseAnimationFrames()
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            CodeValidationFragment.newInstance(countryCode, phoneNumber, hasBeenInvitedFlow))
        .commit()
  }

  override fun showCodeValidationView(validationInfo: ValidationInfo, errorMessage: Int) {
    updateAnimation()
    supportFragmentManager.beginTransaction()
        .replace(R.id.fragment_container,
            CodeValidationFragment.newInstance(validationInfo, errorMessage, hasBeenInvitedFlow))
        .commit()
  }

  override fun showLastStepAnimation() {
    increaseAnimationFrames()
  }

  override fun finishActivity() {
    if (navigateToTransactions) {
      startActivity(TransactionsActivity.newIntent(this))
    }
    finish()
  }

  private fun updateAnimation() {
    validation_progress_animation.removeAllAnimatorListeners()
    validation_progress_animation.setMinAndMaxFrame(minFrame, maxFrame)
    validation_progress_animation.repeatCount = loopAnimation
    validation_progress_animation.addAnimatorListener(object : Animator.AnimatorListener {
      override fun onAnimationRepeat(animation: Animator?) {
      }

      override fun onAnimationEnd(animation: Animator?) {
        minFrame += FRAME_RATE
        maxFrame += FRAME_RATE
        loopAnimation = -1
        validation_progress_animation.setMinAndMaxFrame(minFrame, maxFrame)
        validation_progress_animation.repeatCount = loopAnimation
        validation_progress_animation.playAnimation()
      }

      override fun onAnimationCancel(animation: Animator?) {
      }

      override fun onAnimationStart(animation: Animator?) {
      }
    })
    validation_progress_animation.playAnimation()
  }

  private fun increaseAnimationFrames() {
    minFrame += FRAME_RATE
    maxFrame += FRAME_RATE
    loopAnimation = 0
    updateAnimation()
  }

  override fun onDestroy() {
    validation_progress_animation?.removeAllUpdateListeners()
    validation_progress_animation?.removeAllLottieOnCompositionLoadedListener()

    referral_status_animation?.removeAllUpdateListeners()
    referral_status_animation?.removeAllLottieOnCompositionLoadedListener()

    super.onDestroy()
  }

  private fun reverseAnimation(minFrame: Int, maxFrame: Int, loop: Int) {
    this.minFrame = minFrame
    this.maxFrame = maxFrame
    loopAnimation = loop

    validation_progress_animation.removeAllAnimatorListeners()
    validation_progress_animation.setMinAndMaxFrame(this.minFrame, this.maxFrame)
    validation_progress_animation.repeatCount = loopAnimation
    validation_progress_animation.reverseAnimationSpeed()
    validation_progress_animation.playAnimation()
  }

}