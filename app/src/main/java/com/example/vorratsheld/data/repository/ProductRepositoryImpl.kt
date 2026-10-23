package com.example.vorratsheld.data.repository

import com.example.vorratsheld.data.model.Product
import com.example.vorratsheld.data.remote.CollectionNames
import com.example.vorratsheld.data.remote.model.ProductDto
import com.example.vorratsheld.data.remote.model.toDto
import com.example.vorratsheld.data.remote.model.toProduct
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Produkte in der Firestore-Collection "products"
class ProductRepositoryImpl(
    firestore: FirebaseFirestore
) : ProductRepository {

    private val productsCollection = firestore.collection(CollectionNames.PRODUCTS)

    override suspend fun getProducts(userId: String): Result<List<Product>> = runCatching {
        productsCollection
            .whereEqualTo("userId", userId)
            .get()
            .await()
            .toObjects(ProductDto::class.java)
            .map { it.toProduct() }
    }

    override suspend fun getProductById(productId: String): Result<Product?> = runCatching {
        productsCollection.document(productId)
            .get()
            .await()
            .toObject(ProductDto::class.java)
            ?.toProduct()
    }

    // .document() ohne ID vergibt eine neue, die speichern wir gleich mit
    override suspend fun addProduct(product: Product): Result<Unit> = runCatching {
        val docRef = productsCollection.document()
        docRef.set(product.copy(id = docRef.id).toDto()).await()
    }

    override suspend fun updateProduct(product: Product): Result<Unit> = runCatching {
        productsCollection.document(product.id).set(product.toDto()).await()
    }

    override suspend fun deleteProduct(productId: String): Result<Unit> = runCatching {
        productsCollection.document(productId).delete().await()
    }

    override suspend fun deleteAllProducts(userId: String): Result<Unit> = runCatching {
        getProducts(userId).getOrThrow().forEach { productsCollection.document(it.id).delete().await() }
    }
}
