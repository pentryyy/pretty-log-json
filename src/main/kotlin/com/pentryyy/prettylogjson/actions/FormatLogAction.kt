package com.pentryyy.prettylogjson.actions

import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.SelectionModel

class FormatLogAction : AnAction() {

    private val gson = GsonBuilder().setPrettyPrinting().create()

    override fun actionPerformed(e: AnActionEvent) {
        val editor: Editor = e.getData(CommonDataKeys.EDITOR) ?: return

        val selectionModel: SelectionModel = editor.selectionModel
        val selectedText: String? = selectionModel.selectedText

        val textToProcess = if (selectedText.isNullOrBlank()) {
            editor.document.text
        } else {
            selectedText
        }

        val lines = textToProcess.lines()
        val resultLines = mutableListOf<String>()

        for (line in lines) {
            if (line.startsWith("/entrypoint.sh:")) {
                continue
            }

            if (line.isBlank()) {
                resultLines.add(line)
                continue
            }

            val jsonPart = extractJsonFromLine(line)
            if (jsonPart != null) {
                try {
                    val jsonElement = JsonParser.parseString(jsonPart)
                    if (jsonElement.isJsonObject) {
                        val formatted = gson.toJson(jsonElement)
                        resultLines.add(formatted)
                    } else {
                        val formatted = gson.toJson(jsonElement)
                        resultLines.add(formatted)
                    }
                } catch (_: Exception) {
                    resultLines.add(line)
                }
            } else {
                resultLines.add(line)
            }
        }

        val formattedText = resultLines.joinToString("\n")
        val document = editor.document

        ApplicationManager.getApplication().runWriteAction {
            if (selectedText.isNullOrBlank()) {
                document.setText(formattedText)
            } else {
                val start = selectionModel.selectionStart
                val end = selectionModel.selectionEnd
                document.replaceString(start, end, formattedText)
            }
        }
    }

    /**
     * Извлекает JSON-часть из строки, отбрасывая временную метку в начале.
     * Возвращает строку, начинающуюся с '{', или null, если не найден JSON.
     */
    private fun extractJsonFromLine(line: String): String? {
        val startIndex = line.indexOf('{')
        if (startIndex == -1) {
            return null
        }

        return line.substring(startIndex)
    }
}