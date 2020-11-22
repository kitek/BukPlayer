package pl.kitek.buk.common.di

import org.koin.dsl.module
import pl.kitek.buk.common.validation.LocalValidator

val commonModule = module {

    single { LocalValidator() }

}
