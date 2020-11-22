package pl.kitek.buk.common

import android.os.Build
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import pl.kitek.buk.common.validation.LocalValidator

@RunWith(RobolectricTestRunner::class)
@Config(
    sdk = [Build.VERSION_CODES.O_MR1],
    manifest = Config.NONE
)
class LocalValidatorTest {

    //region Validator

    private lateinit var validator: LocalValidator

    //endregion

    //region Setup

    @Before
    fun setUp() {

        // Build validator.
        validator = LocalValidator()
    }

    //endregion

    //region Url

    /**
     * Verify that invalid URLs are rejected.
     */
    @Test
    fun rejectInvalidURLs() {

        // Given.
        val url1 = ""
        val url2 = "test.com"
        val url3 = "www.test.com"
        val url4 = "random text"

        // When.
        val result1 = validator.validateUrl(url1)
        val result2 = validator.validateUrl(url2)
        val result3 = validator.validateUrl(url3)
        val result4 = validator.validateUrl(url4)

        // Then.
        Assert.assertFalse(result1)
        Assert.assertFalse(result2)
        Assert.assertFalse(result3)
        Assert.assertFalse(result4)
    }

    /**
     * Verify that valid URLs are accepted.
     */
    @Test
    fun acceptValidURLs() {

        // Given.
        val url1 = "http://test.com"
        val url2 = "https://test.com"
        val url3 = "https://www.test.com"

        // When.
        val result1 = validator.validateUrl(url1)
        val result2 = validator.validateUrl(url2)
        val result3 = validator.validateUrl(url3)

        // Then.
        Assert.assertTrue(result1)
        Assert.assertTrue(result2)
        Assert.assertTrue(result3)
    }

    //endregion
}
