package com.miinfc.di

import android.content.Context
import com.miinfc.data.local.*
import com.miinfc.domain.amiibo.*
import com.miinfc.domain.repository.*
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import java.util.ServiceLoader

@Module
@InstallIn(SingletonComponent::class)
object AmiiboCryptoModule {
    @Provides @Singleton
    fun provideAmiiboCryptoEngine(): AmiiboCryptoEngine = RealAmiiboCryptoEngine()

    @Provides @Singleton
    fun provideAmiiboCryptoEngineProvider(engine: AmiiboCryptoEngine): AmiiboCryptoEngineProvider =
        AmiiboCryptoEngineProvider {
            ServiceLoader.load(AmiiboCryptoEngine::class.java, AmiiboCryptoEngine::class.java.classLoader)
                .firstOrNull { it.cryptoPreparationAvailable }
                ?: engine
        }

    @Provides @Singleton
    fun provideKeyStore(@ApplicationContext context: Context): AmiiboKeyStore = LocalAmiiboKeyStore(context)

    @Provides @Singleton
    fun provideKeyStatusRepository(): KeyStatusRepository = InMemoryKeyStatusRepository()

    @Provides @Singleton
    fun provideRawWriteStatusRepository(): RawWriteStatusRepository = InMemoryRawWriteStatusRepository()

    @Provides @Singleton
    fun provideLibrary(@ApplicationContext context: Context): LocalAmiiboLibraryRepository = LocalAmiiboLibraryRepository(context)

    @Provides fun provideAmiiboLibrary(repository: LocalAmiiboLibraryRepository): AmiiboLibraryRepository = repository
    @Provides fun provideSelectedAmiibo(repository: LocalAmiiboLibraryRepository): SelectedAmiiboRepository = repository
}
