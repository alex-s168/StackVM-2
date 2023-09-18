package me.alex_s168.stackvm2.ir

import me.alex_s168.stackvm2.ir.token.Token
import kotlin.math.max
import kotlin.system.exitProcess
import com.github.ajalt.mordant.rendering.TextColors.*
import com.github.ajalt.mordant.rendering.TextStyles
import com.github.ajalt.mordant.terminal.Terminal
import me.alex_s168.stackvm2.ir.ct.SpecialFunctions
import me.alex_s168.stackvm2.ir.ct.minimizeCT
import me.alex_s168.stackvm2.ir.exception.HandledException

fun main() {
    val code = """
        (use! "std.asm")
        
        a := Int! = 3
        b := Int! = a
        c := Int! = (add! a b)
        
        d := Val RefArr<Int>'10 = ::1 2; :3 4; :5 6;;
        
        incFun := Fun<Int>'Int = {
            (return (add A0 1))
        }
        
        d[1] = (incFun d[1])
    """.trimIndent()
    Language.code = code
    try {
        val tokens = tokenize(code)
        val (ast, _) = parse(tokens)
        SpecialFunctions.addTo(ast)
        prepare(ast)
        minimizeCT(ast)
        check(ast)
        println(ast)
    } catch (_: HandledException) {
        exitProcess(1)
    } catch (e: Exception) {
        e.printStackTrace()
        exitProcess(1)
    }
}

object Language {

    val TYPES = arrayOf(
        "Int",
        "Ref",
        "FlatArr",
        "RefArr",
        "Val",
        "Var",
        "Fun",
        "Any",
        "Type",
        "Label",
        "Str",

        "Int!",
        "Ref!",
        "FlatArr!",
        "RefArr!",
        "Fun!",
        "Type!",
        "Label!",
        "Str!"
    )

    var code: String = ""

    fun exception(message: String, line: Int, column: Int, length: Int): Nothing {
        val t = Terminal()
        val lstr = "$line | "
        val lineStr = code.split('\n').getOrNull(line-1) ?: ""
        val before = lineStr.substring(0..<(column - 1))
        val mid = lineStr.substring((column - 1)..<(column - 1 + length))
        val after = lineStr.substring(column - 1 + length)
        t.print(gray(lstr))
        t.print(before)
        t.print(TextStyles.bold(mid))
        t.println(after)
        val l2str = " ".repeat(lstr.length + column - 1)
        t.print(l2str)
        t.println(red('^' + "~".repeat(max(0, length - 1))))
        t.print(" ".repeat(max(0, l2str.length - message.length / 2)))
        t.print(red(message))
        t.println(" (${gray(line.toString())}:${gray(column.toString())})")
        throw HandledException("$message at ($line:$column)")
    }

    fun exception(message: String, where: Token): Nothing =
        exception(message, where.line, where.column, where.value.length)

}