package com.lingshot.domain.repository

import com.lingshot.domain.model.LanguageCodeFromAndToDomain
import com.lingshot.domain.model.PhraseDomain
import com.lingshot.domain.model.Status

interface PhraseCollectionRepository {

    suspend fun savePhraseInLanguageCollections(
        languageCodeFromAndToDomain: LanguageCodeFromAndToDomain,
        phraseDomain: PhraseDomain
    ): Status<Unit>

    suspend fun getLanguageCollections(): Status<List<LanguageCodeFromAndToDomain>>

    suspend fun getPhrasesByLanguageCollections(
        languageCodeFromAndToDomain: LanguageCodeFromAndToDomain
    ): Status<List<PhraseDomain>>

    suspend fun isPhraseSaved(languageId: String, phraseId: String): Boolean

    suspend fun deletePhraseSaved(languageId: String, phraseId: String)
}