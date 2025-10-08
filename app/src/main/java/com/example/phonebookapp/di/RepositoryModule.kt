package com.example.phonebookapp.di

import com.example.phonebookapp.data.remote.api.ContactsApi
import com.example.phonebookapp.data.repository.ContactsRepositoryImpl
import com.example.phonebookapp.domain.repository.ContactsRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideContactsRepository(api: ContactsApi): ContactsRepository {
        return ContactsRepositoryImpl(api)
    }
}