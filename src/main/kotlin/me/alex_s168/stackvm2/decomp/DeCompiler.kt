package me.alex_s168.stackvm2.decomp

import me.alex_s168.stackvm2.inst.InstructionProperties
import me.alex_s168.stackvm2.inst.Instructions

object DeCompiler {

    fun decomp(code: String, origMem: IntArray): String =
        finishUp(stage5(stage4(stage3(stage3(stage3(stage2(stage1(stage0(prep(code))))))))).trim())

    private fun isExpr(line: String): Boolean {
        val args = line.trim().split(" ")
        val inst = Instructions.getInst(args[0])

        return ((args[0].any { it.isDigit() }) || (InstructionProperties.OP_2_1 in (inst?.properties
            ?: listOf()) && args.size == 3) || (InstructionProperties.OP_1_1 in (inst?.properties
            ?: listOf()) && args.size == 2))
    }

    private fun argType(arg: String?): Char? =
        arg?.getOrNull(0)

    private fun finishUp(code: String): String =
        code.split("\n").map {
            if (it.endsWith(':')) it else "    $it"
        }.joinToString("\n")

    private fun stage5(code: String): String {
        val lines = code.split("\n")
        val sb = StringBuilder()

        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            val args = line.trim().split(" ")
            val inst = Instructions.getInst(args[0])

            if (line.endsWith(':')) {
                sb.append(line)
                sb.append("\n")
                i ++
                continue
            }

            if (i + 1 < lines.size) {
                val nextLine = lines[i + 1].trim()
                val nextArgs = nextLine.trim().split(" ")
                val nextInst = Instructions.getInst(nextArgs[0])

                if (line.startsWith("cond = ") && nextLine.endsWith(" = cond")) {
                    sb.append(nextLine.trim().substring(0, nextLine.length - 7))
                    sb.append(" = ")
                    sb.append(line.trim().substring(7))
                    sb.append("\n")

                    i += 2
                    continue
                }
            }


            sb.append(line)
            sb.append("\n")
            i ++
        }

        return sb.toString()
    }

    private fun stage4(code: String): String =
        code.replace("not ( sub", "eq (")

    private fun stage3(code: String): String {
        val lines = code.split("\n")
        val sb = StringBuilder()

        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            val args = line.trim().split(" ")
            val inst = Instructions.getInst(args[0])

            if (line.endsWith(':')) {
                sb.append(line)
                sb.append("\n")
                i ++
                continue
            }

            if (i + 1 < lines.size) {
                val nextLine = lines[i + 1].trim()
                val nextArgs = nextLine.trim().split(" ")
                val nextInst = Instructions.getInst(nextArgs[0])

                if (args[0] == "phc" && nextLine.endsWith(" = #")) {
                    sb.append(nextArgs[0])
                    sb.append(" = cond\n")

                    i += 2
                    continue
                }

                if (line.trim().startsWith("cond =")) {
                    if (nextArgs[0] == "not") {
                        sb.append("cond = not (")
                        sb.append(line.trim().substring(6))
                        sb.append(" )\n")

                        i += 2
                        continue
                    }
                }

                if (i + 2 < lines.size) {
                    val nextNextLine = lines[i + 2].trim()
                    val nextNextArgs = nextNextLine.trim().split(" ")
                    val nextNextInst = Instructions.getInst(nextNextArgs[0])

                    if (inst?.properties?.contains(InstructionProperties.ONLY_PUSH) == true &&
                        nextInst?.properties?.contains(InstructionProperties.OP_1_1) == true &&
                        nextNextLine.endsWith(" = #")) {
                        sb.append(nextNextArgs[0])
                        sb.append(" = ")
                        sb.append(nextArgs[0])
                        sb.append(" ( ")
                        sb.append(args[1])
                        sb.append(" )\n")

                        i += 3
                        continue
                    }

                    if (isExpr(line) && nextLine.trim().startsWith("cond = ")) {
                        if (nextNextInst?.properties?.contains(InstructionProperties.CONDF_1_0) == true) {
                            sb.append("cond = ")
                            sb.append(nextNextArgs[0])
                            sb.append(" ( ( ")
                            sb.append(line)
                            sb.append(" ) ( ")
                            sb.append(nextLine.trim().substring(7))
                            sb.append(" ) )\n")

                            i += 3
                            continue
                        }
                    }
                }
            }

            sb.append(line)
            sb.append("\n")
            i ++
        }

        return sb.toString()
    }

    private fun stage2(code: String): String {
        val lines = code.split("\n")
        val sb = StringBuilder()

        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            val args = line.split(" ")
            val inst = Instructions.getInst(args[0])

            if (line.endsWith(':')) {
                sb.append(line)
                sb.append("\n")
                i ++
                continue
            }

            if (i + 1 < lines.size) {
                val nextLine = lines[i + 1].trim()
                val nextArgs = nextLine.trim().split(" ")

                if (isExpr(line)) {

                    if (nextArgs[0] == "sto") {
                        sb.append(nextArgs[1])
                        sb.append(" = ")
                        sb.append(line)
                        sb.append('\n')

                        i += 2
                        continue
                    }

                    if (nextArgs[0] == "poc") {
                        sb.append("cond = ")
                        sb.append(line)
                        sb.append('\n')

                        i += 2
                        continue
                    }

                }

                if (inst?.properties?.contains(InstructionProperties.ONLY_PUSH) == true) {

                    if (nextArgs[0] == "poc") {
                        sb.append("cond = ")
                        sb.append(args.subList(1, args.size).joinToString(" "))
                        sb.append('\n')

                        i += 2
                        continue
                    }

                }
            }

            if (args[0] == "sto") {
                sb.append(args[1])
                sb.append(" = #\n")

                i += 1
                continue
            }

            if (args[0] == "jmp") {
                sb.append("goto ")
                sb.append(args[1])
                sb.append('\n')

                i += 1
                continue
            }

            if (args[0] == "jmc") {
                sb.append("goto_cond ")
                sb.append(args[1])
                sb.append('\n')

                i += 1
                continue
            }

            if (args[0] == "cal") {
                sb.append("call ")
                sb.append(args[1])
                sb.append('\n')

                i += 1
                continue
            }

            if (args[0] == "cac") {
                sb.append("call_cond ")
                sb.append(args[1])
                sb.append('\n')

                i += 1
                continue
            }

            sb.append(line)
            sb.append("\n")
            i ++
        }

        return sb.toString()
    }

    private fun stage1(code: String): String {
        val lines = code.split("\n")
        val sb = StringBuilder()

        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            val args = line.trim().split(" ")

            if (line.endsWith(':')) {
                sb.append(line)
                sb.append("\n")
                i ++
                continue
            }

            if (i + 1 < lines.size) {
                val nextLine = lines[i + 1].trim()
                val nextArgs = nextLine.trim().split(" ")

                if (isExpr(line)) {

                    if (nextArgs[0] == "los") {
                        val lat1 = argType(args.getOrNull(1))
                        val lat2 = argType(args.getOrNull(2))

                        if (lat1 == 'i' && lat2 == 'a') {
                            sb.append("a")
                            sb.append(args[1].substring(1))
                            sb.append("[")
                            sb.append(args[2])
                            sb.append("]\n")

                            i += 2
                            continue
                        }

                        if (lat1 == 'a' && lat2 == 'i') {
                            sb.append("a")
                            sb.append(args[2].substring(1))
                            sb.append("[")
                            sb.append(args[1])
                            sb.append("]\n")

                            i += 2
                            continue
                        }

                        sb.append("*(")
                        sb.append(line)
                        sb.append(")")
                        sb.append('\n')

                        i += 2
                        continue
                    }

                }
            }

            if (i + 2 < lines.size) {
                val nextLine = lines[i + 1].trim()
                val nextArgs = nextLine.trim().split(" ")

                val nextNextLine = lines[i + 2].trim()
                val nextNextArgs = nextNextLine.trim().split(" ")

                if (isExpr(line) && isExpr(nextLine)) {

                    if (nextNextArgs[0] == "sts") {
                        val nlat1 = argType(nextArgs.getOrNull(1))
                        val nlat2 = argType(nextArgs.getOrNull(2))

                        if (nlat1 == 'i' && nlat2 == 'a') {
                            sb.append("a")
                            sb.append(nextArgs[1].substring(1))
                            sb.append("[")
                            sb.append(nextArgs[2])
                            sb.append("] = ")
                            sb.append(line)
                            sb.append('\n')

                            i += 3
                            continue
                        }

                        if (nlat1 == 'a' && nlat2 == 'i') {
                            sb.append("a")
                            sb.append(nextArgs[2].substring(1))
                            sb.append("[")
                            sb.append(nextArgs[1])
                            sb.append("] = ")
                            sb.append(line)
                            sb.append('\n')

                            i += 3
                            continue
                        }

                        sb.append("*(")
                        sb.append(nextLine)
                        sb.append(") = ")
                        sb.append(line)
                        sb.append('\n')

                        i += 3
                        continue
                    }

                }
            }

            sb.append(line)
            sb.append("\n")
            i ++
        }

        return sb.toString()
    }

    private fun stage0(code: String): String {
        val lines = code.split("\n")
        val sb = StringBuilder()

        var i = 0
        while (i < lines.size) {
            val line = lines[i]

            if (line.endsWith(':')) {
                sb.append(line)
                sb.append("\n")
                i ++
                continue
            }

            val args = line.trim().split(" ")

            if (args.isEmpty()) {
                i ++
                continue
            }

            if (args[0] == "nop") {
                i ++
                continue
            }

            if (args[0] == "sez") {
                sb.append("$0 = 0\n")
                i ++
                continue
            }

            if (i + 1 < lines.size) {
                val nextArgs = lines[i + 1].trim().split(" ")

                if (   (args[0] == "ldi" && nextArgs[0] == "sto")
                    || (args[0] == "ldo" && nextArgs[0] == "sto")
                    ) {
                    sb.append(nextArgs[1])
                    sb.append(" = ")
                    sb.append(args[1])
                    sb.append("\n")

                    i += 2
                    continue
                }
            }

            if (i + 2 < lines.size) {
                val nextArgs = lines[i + 1].trim().split(" ")
                val nextNextArgs = lines[i + 2].trim().split(" ")

                val inst = Instructions.getInst(args[0])
                val nextInst = Instructions.getInst(nextArgs[0])
                val nextNextInst = Instructions.getInst(nextNextArgs[0])

                if (inst != null && nextInst != null && nextNextInst != null) {

                    if ((InstructionProperties.ONLY_PUSH in inst.properties && inst.argCount == 1)
                        && (InstructionProperties.ONLY_PUSH in nextInst.properties && nextInst.argCount == 1)
                        && InstructionProperties.OP_2_1 in nextNextInst.properties
                    ) {

                        sb.append(nextNextInst.name)
                        sb.append(' ')
                        sb.append(nextArgs[1])
                        sb.append(' ')
                        sb.append(args[1])
                        sb.append("\n")

                        i += 3
                        continue

                    }

                }
            }

            if (args[0] == "ldi") {
                sb.append(args[1])
                sb.append("\n")
                i ++
                continue
            }

            sb.append(line)
            sb.append("\n")
            i ++
        }

        return sb.toString()
    }

    private fun prep(code: String): String =
        code.replace("addr_", "a")
            .replace("label_", "l")

}