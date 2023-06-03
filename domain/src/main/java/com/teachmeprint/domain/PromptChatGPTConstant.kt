package com.teachmeprint.domain

object PromptChatGPTConstant {
    val PROMPT_TRANSLATE: (String?, String) -> String = { language, text ->
        "Translate this into 1. $language and improve the meaning:\\n\\n${text}\\n\\n1."
    }
    val PROMPT_CORRECT_SPELLING: (String) -> String = { originalText ->
        "Correct the spelling: \\n\\n${originalText}\\n\\n"
    }
}