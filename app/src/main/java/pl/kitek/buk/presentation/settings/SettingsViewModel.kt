package pl.kitek.buk.presentation.settings

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import pl.kitek.buk.common.addTo
import pl.kitek.buk.common.validation.LocalValidator
import pl.kitek.buk.common.viewModel.BaseViewModel
import pl.kitek.buk.data.repository.SettingsRepository
import pl.kitek.buk.data.service.player.PlayerController
import timber.log.Timber

class SettingsViewModel(
    private val playerController: PlayerController,
    private val validator: LocalValidator,
    settingsRepository: SettingsRepository
) : BaseViewModel() {

    init {
        observeSettings(settingsRepository)
    }

    private fun observeSettings(settingsRepository: SettingsRepository) {

        settingsRepository.observeServerSettings()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                //                playerController.stop()
                Timber.tag("kitek").d("PLAYER.STOP ")
            }, {

            }).addTo(disposable)
    }

    //region Validation

    fun validateUrl(value: String): Boolean =
        validator.validateUrl(value)

    //endregion
}
