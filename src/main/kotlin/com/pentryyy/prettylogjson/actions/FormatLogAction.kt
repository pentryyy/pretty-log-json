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
        val resultLines = lines.mapNotNull { line -> processLine(line) }

        val formattedText = resultLines.joinToString("\n")
        val isSelection = !selectedText.isNullOrBlank()

        replaceTextInEditor(editor, selectionModel, formattedText, isSelection)
    }

    private fun extractJsonFromLine(line: String): String? {
        val startIndex = line.indexOf('{')
        return if (startIndex == -1) null else line.substring(startIndex)
    }

    private fun processLine(line: String): String? = when {
        line.isBlank() -> line
        else -> {
            val jsonPart = extractJsonFromLine(line)
            when {
                jsonPart != null -> try {
                    gson.toJson(JsonParser.parseString(jsonPart))
                } catch (_: Exception) {
                    line
                }
                line.startsWith("/entrypoint.sh:") -> null
                else -> line
            }
        }
    }

    private fun replaceTextInEditor(
        editor: Editor,
        selectionModel: SelectionModel,
        formattedText: String,
        isSelection: Boolean
    ) {
        ApplicationManager.getApplication().runWriteAction {
            if (isSelection) {
                val start = selectionModel.selectionStart
                val end = selectionModel.selectionEnd
                editor.document.replaceString(start, end, formattedText)
            } else {
                editor.document.setText(formattedText)
            }
        }
    }
}
