package pl.kitek.buk.common.validation

import android.util.Patterns
import android.webkit.URLUtil

class LocalValidator {

    //region Url

    /**
     * Validates string using simple build-in methods.
     * Not a bulletproof solution but sufficient for a small project like this.
     */
    fun validateUrl(value: String): Boolean =
        URLUtil.isValidUrl(value) &&
                Patterns.WEB_URL.matcher(value).matches()

    //endregion
}
