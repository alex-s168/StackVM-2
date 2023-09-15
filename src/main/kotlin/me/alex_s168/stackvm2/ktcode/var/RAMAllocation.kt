package me.alex_s168.stackvm2.ktcode.`var`

import me.alex_s168.stackvm2.inst.Instructions
import me.alex_s168.stackvm2.ktcode.KTCode
import kotlin.math.max

class RAMAllocation(
    ktcode: KTCode,
    size: Int,
    uuid: Long = 0
): MemoryAllocation(
    ktcode,
    size,
    uuid,
    IntArray(max(0, size)) { 0 }
), MutableStackable<MutableMemoryAllocation> {

    override fun putOntoStack() {
        if (this !in ktcode.ramAllocs)
            throw IllegalArgumentException("Cannot load from unallocated memory!")

        putOntoStack(0)
    }

    override fun loadFromStack() {
        if (this !in ktcode.ramAllocs)
            throw IllegalArgumentException("Cannot store to unallocated memory!")

        loadFromStack(0)
    }


    override fun loadFromStack(offset: Int) {
        if (offset > eAm)
            throw UnsupportedOperationException("Cannot load from offset bigger than size of MemoryAllocation!")

        store(offset)
    }

    override fun loadFromStack(offset: Stackable) {
        if (offset.getElemSize() != 1)
            throw UnsupportedOperationException("Index of MemoryAllocation needs to be Int!")

        ktcode.mem += Instructions.LD_IMM.id
        ktcode.unresolvedReferences.add(ktcode.mem.size to this)
        ktcode.mem += 0

        offset.putOntoStack()

        ktcode.add()

        ktcode.stackStore()
    }

    override var value: Stackable
        get() = this
        set(v) {
            if (v.getElemSize() != getElemSize())
                throw UnsupportedOperationException("Cannot assign to different size element!")

            forElems {
                v.putOntoStack(it)
                loadFromStack(it)
            }
        }

    fun store(index: Int) {
        if (index > eAm)
            throw UnsupportedOperationException("Cannot store to offset bigger than size of MemoryAllocation!")

        ktcode.mem += Instructions.ST_ADDR.id
        ktcode.unresolvedReferences3.add(Triple(ktcode.mem.size, this, index))
        ktcode.mem += 0
    }

}