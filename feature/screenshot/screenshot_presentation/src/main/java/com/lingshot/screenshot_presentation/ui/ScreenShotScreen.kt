/*
 * Copyright 2023 Lingshot
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lingshot.screenshot_presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lingshot.common.helper.isLoadingStatus
import com.lingshot.common.helper.onEmpty
import com.lingshot.common.helper.onError
import com.lingshot.common.helper.onLoading
import com.lingshot.common.helper.onSuccess
import com.lingshot.screenshot_presentation.R
import com.lingshot.screenshot_presentation.ScreenShotEvent
import com.lingshot.screenshot_presentation.ScreenShotEvent.ClearStatus
import com.lingshot.screenshot_presentation.ScreenShotEvent.CroppedImage
import com.lingshot.screenshot_presentation.ScreenShotEvent.FetchCorrectedOriginalText
import com.lingshot.screenshot_presentation.ScreenShotEvent.FetchTextRecognizer
import com.lingshot.screenshot_presentation.ScreenShotEvent.SelectedOptionsButtonMenuItem
import com.lingshot.screenshot_presentation.ScreenShotEvent.ToggleDictionaryFullScreenDialog
import com.lingshot.screenshot_presentation.ScreenShotUiState
import com.lingshot.screenshot_presentation.ScreenShotViewModel
import com.lingshot.screenshot_presentation.ui.component.ButtonMenuItem.TRANSLATE
import com.lingshot.screenshot_presentation.ui.component.ScreenShotButtonMenuEnd
import com.lingshot.screenshot_presentation.ui.component.ScreenShotButtonMenuStart
import com.lingshot.screenshot_presentation.ui.component.ScreenShotCropImage
import com.lingshot.screenshot_presentation.ui.component.ScreenShotDictionaryFullScreenDialog
import com.lingshot.screenshot_presentation.ui.component.ScreenShotLottieLoading
import com.lingshot.screenshot_presentation.ui.component.ScreenShotSnackBarError
import com.lingshot.screenshot_presentation.ui.component.ScreenShotTranslateBottomSheet
import es.dmoral.toasty.Toasty

@Composable
internal fun ScreenShotRoute(
    viewModel: ScreenShotViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScreenShotScreen(
        uiState = uiState,
        handleEvent = viewModel::handleEvent,
    )
}

@Composable
internal fun ScreenShotScreen(
    uiState: ScreenShotUiState,
    modifier: Modifier = Modifier,
    handleEvent: (event: ScreenShotEvent) -> Unit,
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .background(Color.Black),
    ) {
        val illegiblePhrase = stringResource(id = R.string.text_message_illegible_phrase)
        ScreenShotCropImage(
            actionCropImage = uiState.actionCropImage,
            onCropImageResult = { bitmap ->
                handleEvent(FetchTextRecognizer(bitmap, illegiblePhrase))
            },
            onCroppedImage = { actionCropImage ->
                handleEvent(CroppedImage(actionCropImage))
            },
        )
        ScreenShotButtonMenuStart(
            modifier = Modifier.align(Alignment.BottomStart),
            onSelectedOptionsButtonMenuItem = {
                handleEvent(SelectedOptionsButtonMenuItem(it))
            },
        )
        ScreenShotButtonMenuEnd(
            modifier = Modifier.align(Alignment.BottomEnd),
            onSelectedOptionsButtonMenuItem = {
                if (!uiState.screenShotStatus.isLoadingStatus) {
                    handleEvent(SelectedOptionsButtonMenuItem(it))
                }
            },
        )
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxSize(),
        ) {
            Spacer(modifier = Modifier.weight(1f))

            uiState.screenShotStatus.onEmpty {
                Toasty.warning(context, illegiblePhrase).show()
                handleEvent(ClearStatus)
            }.onLoading {
                val loading = uiState.buttonMenuItem
                    .takeIf { it == TRANSLATE }
                    ?.let { R.raw.loading_translate } ?: R.raw.loading_listen

                ScreenShotLottieLoading(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    loading = loading,
                )
            }.onSuccess {
                if (uiState.buttonMenuItem == TRANSLATE) {
                    ScreenShotTranslateBottomSheet(
                        languageTranslationDomain = it,
                        correctedOriginalTextStatus = uiState.correctedOriginalTextStatus,
                        onCorrectedOriginalText = { original ->
                            handleEvent(FetchCorrectedOriginalText(original))
                        },
                        onToggleDictionaryFullScreenDialog = { url ->
                            handleEvent(ToggleDictionaryFullScreenDialog(url))
                        },
                        onDismiss = {
                            handleEvent(ClearStatus)
                        },
                    )
                }
            }.onError {
                ScreenShotSnackBarError(
                    modifier = Modifier.padding(bottom = 16.dp),
                    message = it,
                    onDismiss = {
                        handleEvent(ClearStatus)
                    },
                )
            }
        }
    }
    uiState.dictionaryUrl?.let { url ->
        ScreenShotDictionaryFullScreenDialog(url) {
            handleEvent(ToggleDictionaryFullScreenDialog(null))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ScreenShotScreenPreview() {
    ScreenShotScreen(
        uiState = ScreenShotUiState(),
        handleEvent = {},
    )
}
