package com.example.pokedex_kmp.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

private var androidDatabase: PokedexDatabase? = null

fun initializePokedexDatabase(context: Context) {
    if (androidDatabase == null) {
        androidDatabase = AndroidPokedexDatabase(context.applicationContext)
    }
}

actual fun getPlatformPokedexDatabase(): PokedexDatabase {
    return androidDatabase
        ?: error("Banco da Pokédex não inicializado. Chame initializePokedexDatabase(context) na MainActivity.")
}

private class AndroidPokedexDatabase(context: Context) : PokedexDatabase {
    private val helper = PokedexOpenHelper(context)

    override suspend fun isPokemonCacheEmpty(): Boolean {
        helper.readableDatabase.rawQuery("SELECT COUNT(*) FROM pokemon_cache", emptyArray()).use { cursor ->
            return cursor.moveToFirst() && cursor.getInt(0) == 0
        }
    }

    override suspend fun upsertPokemonCache(items: List<PokemonListItem>) {
        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            items.forEach { item ->
                val values = ContentValues().apply {
                    put("id", item.id)
                    put("name", item.name)
                    put("image_url", item.imageUrl)
                    put("types", item.types.joinToString("|"))
                }
                db.insertWithOnConflict("pokemon_cache", null, values, SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    override suspend fun getPokemonPage(query: String, type: String?, limit: Int, offset: Int): List<PokemonListItem> {
        val where = mutableListOf<String>()
        val args = mutableListOf<String>()

        if (query.isNotBlank()) {
            where += "LOWER(name) LIKE ?"
            args += "%${query.lowercase()}%"
        }

        if (!type.isNullOrBlank()) {
            where += "LOWER(types) LIKE ?"
            args += "%${type.lowercase()}%"
        }

        val whereSql = if (where.isEmpty()) "" else "WHERE ${where.joinToString(" AND ")}"
        val sql = """
            SELECT id, name, image_url, types
            FROM pokemon_cache
            $whereSql
            ORDER BY id
            LIMIT ? OFFSET ?
        """.trimIndent()

        args += limit.toString()
        args += offset.toString()

        helper.readableDatabase.rawQuery(sql, args.toTypedArray()).use { cursor ->
            val result = mutableListOf<PokemonListItem>()
            while (cursor.moveToNext()) {
                result += cursor.toPokemonListItem()
            }
            return result
        }
    }

    override suspend fun getPokemonByIdFromCache(id: Int): PokemonListItem? {
        helper.readableDatabase.rawQuery(
            "SELECT id, name, image_url, types FROM pokemon_cache WHERE id = ?",
            arrayOf(id.toString())
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.toPokemonListItem() else null
        }
    }

    override suspend fun getFavoriteIds(): Set<Int> {
        helper.readableDatabase.rawQuery("SELECT id FROM favorite_pokemon", emptyArray()).use { cursor ->
            val ids = mutableSetOf<Int>()
            while (cursor.moveToNext()) {
                ids += cursor.getInt(0)
            }
            return ids
        }
    }

    override suspend fun getFavorites(): List<FavoritePokemon> {
        helper.readableDatabase.rawQuery(
            "SELECT id, name, image_url, types, captured_at, saved_at FROM favorite_pokemon ORDER BY saved_at DESC",
            emptyArray()
        ).use { cursor ->
            val result = mutableListOf<FavoritePokemon>()
            while (cursor.moveToNext()) {
                result += cursor.toFavoritePokemon()
            }
            return result
        }
    }

    override suspend fun getFavoriteById(id: Int): FavoritePokemon? {
        helper.readableDatabase.rawQuery(
            "SELECT id, name, image_url, types, captured_at, saved_at FROM favorite_pokemon WHERE id = ?",
            arrayOf(id.toString())
        ).use { cursor ->
            return if (cursor.moveToFirst()) cursor.toFavoritePokemon() else null
        }
    }

    override suspend fun saveFavorite(pokemon: Pokemon, capturedAt: String) {
        val values = ContentValues().apply {
            put("id", pokemon.id)
            put("name", pokemon.name)
            put("image_url", pokemon.imageUrl)
            put("types", pokemon.types.joinToString("|"))
            put("captured_at", capturedAt)
            put("saved_at", System.currentTimeMillis())
        }
        helper.writableDatabase.insertWithOnConflict("favorite_pokemon", null, values, SQLiteDatabase.CONFLICT_REPLACE)
    }

    override suspend fun deleteFavorite(id: Int) {
        helper.writableDatabase.delete("favorite_pokemon", "id = ?", arrayOf(id.toString()))
    }
}

private class PokedexOpenHelper(context: Context) : SQLiteOpenHelper(context, "pokedex_m2.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS pokemon_cache (
                id INTEGER PRIMARY KEY,
                name TEXT NOT NULL,
                image_url TEXT NOT NULL,
                types TEXT NOT NULL
            )
            """.trimIndent()
        )

        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS favorite_pokemon (
                id INTEGER PRIMARY KEY,
                name TEXT NOT NULL,
                image_url TEXT NOT NULL,
                types TEXT NOT NULL,
                captured_at TEXT NOT NULL,
                saved_at INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS pokemon_cache")
        db.execSQL("DROP TABLE IF EXISTS favorite_pokemon")
        onCreate(db)
    }
}

private fun android.database.Cursor.toPokemonListItem(): PokemonListItem {
    return PokemonListItem(
        id = getInt(getColumnIndexOrThrow("id")),
        name = getString(getColumnIndexOrThrow("name")),
        imageUrl = getString(getColumnIndexOrThrow("image_url")),
        types = getString(getColumnIndexOrThrow("types")).split("|").filter { it.isNotBlank() },
    )
}

private fun android.database.Cursor.toFavoritePokemon(): FavoritePokemon {
    return FavoritePokemon(
        id = getInt(getColumnIndexOrThrow("id")),
        name = getString(getColumnIndexOrThrow("name")),
        imageUrl = getString(getColumnIndexOrThrow("image_url")),
        types = getString(getColumnIndexOrThrow("types")).split("|").filter { it.isNotBlank() },
        capturedAt = getString(getColumnIndexOrThrow("captured_at")),
        savedAtMillis = getLong(getColumnIndexOrThrow("saved_at")),
    )
}
