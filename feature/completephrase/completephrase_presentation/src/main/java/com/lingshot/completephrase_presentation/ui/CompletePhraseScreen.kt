package com.lingshot.completephrase_presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lingshot.common.helper.onLoading
import com.lingshot.common.helper.onSuccess
import com.lingshot.completephrase_presentation.CompletePhraseEvent
import com.lingshot.completephrase_presentation.CompletePhraseUiState
import com.lingshot.completephrase_presentation.CompletePhraseViewModel
import com.lingshot.completephrase_presentation.ui.component.CompletePhraseAnswerSheet
import com.lingshot.completephrase_presentation.ui.component.CompletePhraseIndicatorPage
import com.lingshot.completephrase_presentation.ui.component.CompletePhraseTextFieldCard
import com.lingshot.completephrase_presentation.ui.component.CompletePhraseTranslateCard
import com.lingshot.designsystem.component.LingshotLayout
import com.lingshot.designsystem.component.LingshotLoading
import com.lingshot.designsystem.theme.LingshotTheme
import com.lingshot.domain.helper.FormatPhraseHelper.extractWordsInDoubleParentheses
import com.lingshot.domain.helper.FormatPhraseHelper.processPhraseWithDoubleParentheses
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@Composable
internal fun CompletePhraseScreenRoute(
    languageId: String?,
    onBackClick: () -> Unit,
    viewModel: CompletePhraseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.fetchPhrasesByLanguageCollections(languageId)
    }

    CompletePhraseScreen(
        uiState = uiState,
        handleEvent = viewModel::handleEvent,
        onBackClick = onBackClick
    )
}

@Composable
private fun CompletePhraseScreen(
    uiState: CompletePhraseUiState,
    handleEvent: (CompletePhraseEvent) -> Unit,
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    var currentPageIndex by remember { mutableStateOf(0) }
    val currentPage = (currentPageIndex + 1)

    LingshotLayout(
        title = "Complete phrase",
        onClickNavigation = onBackClick
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            uiState.phrasesByLanguageCollectionsStatus.onSuccess { listPhraseDomain ->
                val phraseDomain = listPhraseDomain[currentPageIndex]
                val listWords = processPhraseWithDoubleParentheses(
                    phraseDomain.original
                ).toImmutableList()

                val wordWithoutParentheses =
                    extractWordsInDoubleParentheses(phraseDomain.original)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    CompletePhraseIndicatorPage(currentPage, listPhraseDomain.size)
                    key(currentPageIndex) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CompletePhraseTextFieldCard(
                                listWords = listWords,
                                wordWithoutParentheses = wordWithoutParentheses,
                                wordToFill = uiState.wordToFill,
                                onFillWord = { word ->
                                    handleEvent(CompletePhraseEvent.FillWord(word))
                                },
                                isSpeechActive = uiState.isSpeechActive,
                                onSpeakText = {
                                    handleEvent(
                                        CompletePhraseEvent.FetchTextToSpeech(
                                            phraseDomain.original
                                        )
                                    )
                                }
                            )
                            CompletePhraseTranslateCard(
                                translateText = phraseDomain.translate,
                                isTranslatedTextVisible = uiState.isTranslatedTextVisible,
                                onToggleTranslatedTextVisibility = {
                                    handleEvent(
                                        CompletePhraseEvent.ToggleTranslatedTextVisibility
                                    )
                                }
                            )
                        }
                    }
                }

                ExtendedFloatingActionButton(
                    modifier = Modifier
                        .padding(16.dp)
                        .align(Alignment.BottomEnd),
                    text = {
                        Text(text = "Verify")
                    },
                    icon = {
                        Icon(imageVector = Icons.Default.SkipNext, contentDescription = null)
                    },
                    onClick = {
                        val isAnswerCorrect = uiState.wordToFill.equals(
                            wordWithoutParentheses,
                            ignoreCase = true
                        )
                        handleEvent(CompletePhraseEvent.ShowAnswerSheet(isAnswerCorrect))
                    }
                )

                if (uiState.isAnswerSheetVisible) {
                    CompletePhraseAnswerSheet(
                        answerState = uiState.answerState,
                        onContinue = {
                            if (currentPage <= (listPhraseDomain.size - 1)) {
                                scope.launch {
                                    handleEvent(CompletePhraseEvent.ClearState)
                                }.invokeOnCompletion {
                                    currentPageIndex = currentPage
                                }
                            }
                        },
                        onDismiss = {
                            handleEvent(CompletePhraseEvent.HideAnswerSheet)
                        }
                    )
                }
            }.onLoading {
                LingshotLoading(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    LaunchedEffect(currentPageIndex, uiState.isAnswerSheetVisible) {
        if (currentPageIndex != 0 && scrollState.value != 0) {
            scrollState.animateScrollTo(0)
        }
        if (uiState.isAnswerSheetVisible) {
            handleEvent(CompletePhraseEvent.FetchAnswerSound)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CompletePhraseScreenPreview() {
    LingshotTheme(isDarkTheme = true) {
        CompletePhraseScreen(
            uiState = CompletePhraseUiState(),
            handleEvent = {},
            onBackClick = {}
        )
    }
}
