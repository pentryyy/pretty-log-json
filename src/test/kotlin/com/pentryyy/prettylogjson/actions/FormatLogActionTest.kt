package com.pentryyy.prettylogjson.actions

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class FormatLogActionTest : BasePlatformTestCase() {

    private lateinit var action: FormatLogAction

    override fun setUp() {
        super.setUp()
        action = FormatLogAction()
    }

    private fun runAction() {
        val dataContext = DataManager.getInstance().getDataContext(myFixture.editor.component)
        val event = AnActionEvent.createEvent(
            dataContext,
            Presentation(),
            ActionPlaces.UNKNOWN,
            ActionUiKind.NONE,
            null
        )
        action.actionPerformed(event)
    }

    fun testFormatSimpleJsonWithoutPrefix() {
        val input = """{"name":"John","age":30}"""
        val expected = """
            |{
            |  "name": "John",
            |  "age": 30
            |}
        """.trimMargin()
        myFixture.configureByText("test.log", input)
        runAction()
        myFixture.checkResult(expected)
    }

    fun testFormatJsonWithEntrypointPrefix() {
        val input = """/entrypoint.sh: {"status":"ok","code":200}"""
        val expected = """
            |{
            |  "status": "ok",
            |  "code": 200
            |}
        """.trimMargin()
        myFixture.configureByText("test.log", input)
        runAction()
        myFixture.checkResult(expected)
    }

    fun testIgnoreLinesThatStartWithPrefix() {
        val input = """
            |/entrypoint.sh: Starting service
            |/entrypoint.sh: {"event":"init"}
            |/entrypoint.sh: {"event":"ready"}
        """.trimMargin()
        val expected = """
            |{
            |  "event": "init"
            |}
            |{
            |  "event": "ready"
            |}
        """.trimMargin()
        myFixture.configureByText("test.log", input)
        runAction()
        myFixture.checkResult(expected)
    }

    fun testKeepNonJsonLinesUnchanged() {
        val input = """
            |This is a plain text line
            |/entrypoint.sh: {"a":1}
            |Another plain line
        """.trimMargin()
        val expected = """
            |This is a plain text line
            |{
            |  "a": 1
            |}
            |Another plain line
        """.trimMargin()
        myFixture.configureByText("test.log", input)
        runAction()
        myFixture.checkResult(expected)
    }

    fun testJsonWithTimestampBeforeCurlyBrace() {
        val input = """2023-08-20 12:34:56.789 {"level":"INFO","msg":"Started"}"""
        val expected = """
            |{
            |  "level": "INFO",
            |  "msg": "Started"
            |}
        """.trimMargin()
        myFixture.configureByText("test.log", input)
        runAction()
        myFixture.checkResult(expected)
    }

    fun testEmptyOrBlankTextDoesNothing() {
        val input = "   "
        myFixture.configureByText("test.log", input)
        runAction()
        myFixture.checkResult(input)
    }

    fun testSelectionWithNoJson() {
        val input = """
            |Line 1
            |Line 2
        """.trimMargin()
        myFixture.configureByText("test.log", input)

        val document = myFixture.editor.document
        val startOffset = document.getLineStartOffset(1)
        val endOffset = document.getLineEndOffset(1)
        myFixture.editor.selectionModel.setSelection(startOffset, endOffset)

        runAction()

        myFixture.checkResult(input)
    }

    fun testMalformedJsonRemainsUnchanged() {
        val input = """{ "broken": "value }"""
        myFixture.configureByText("test.log", input)
        runAction()
        myFixture.checkResult(input)
    }
}
