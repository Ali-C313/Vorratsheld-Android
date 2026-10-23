package com.example.vorratsheld.di

import com.example.vorratsheld.data.remote.EmailJsApi
import com.example.vorratsheld.data.remote.OpenFoodFactsApi
import com.example.vorratsheld.data.repository.AuthRepository
import com.example.vorratsheld.data.repository.AuthRepositoryImpl
import com.example.vorratsheld.data.repository.OpenFoodFactsRepository
import com.example.vorratsheld.data.repository.OpenFoodFactsRepositoryImpl
import com.example.vorratsheld.data.repository.ProductRepository
import com.example.vorratsheld.data.repository.ProductRepositoryImpl
import com.example.vorratsheld.data.repository.ShoppingRepository
import com.example.vorratsheld.data.repository.ShoppingRepositoryImpl
import com.example.vorratsheld.data.repository.SupportRepository
import com.example.vorratsheld.data.repository.SupportRepositoryImpl
import com.example.vorratsheld.data.repository.UserRepository
import com.example.vorratsheld.data.repository.UserRepositoryImpl
import com.example.vorratsheld.ui.viewmodel.AuthViewModel
import com.example.vorratsheld.ui.viewmodel.ProductDetailViewModel
import com.example.vorratsheld.ui.viewmodel.ProductInfoViewModel
import com.example.vorratsheld.ui.viewmodel.ProductViewModel
import com.example.vorratsheld.ui.viewmodel.ProfileViewModel
import com.example.vorratsheld.ui.viewmodel.ShoppingListViewModel
import com.example.vorratsheld.ui.viewmodel.SupportViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

// Alle Abhängigkeiten der App
val appModule = module {
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }

    single { OpenFoodFactsApi.service }
    single { EmailJsApi.service }

    single<AuthRepository> { AuthRepositoryImpl(get()) }
    single<ProductRepository> { ProductRepositoryImpl(get()) }
    single<ShoppingRepository> { ShoppingRepositoryImpl(get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<SupportRepository> { SupportRepositoryImpl(get(), get()) }
    single<OpenFoodFactsRepository> { OpenFoodFactsRepositoryImpl(get()) }

    viewModelOf(::AuthViewModel)
    viewModelOf(::ProductViewModel)
    viewModelOf(::ProductDetailViewModel)
    viewModelOf(::ProductInfoViewModel)
    viewModelOf(::ShoppingListViewModel)
    viewModelOf(::ProfileViewModel)
    viewModelOf(::SupportViewModel)
}
