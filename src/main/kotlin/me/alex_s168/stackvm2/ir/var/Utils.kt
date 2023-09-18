package me.alex_s168.stackvm2.ir.`var`

import me.alex_s168.stackvm2.ir.ast.ASTTypeNode

fun getTypeGenerics(node: ASTTypeNode): List<Type> =
    node.generics.map { Type(it.name) }