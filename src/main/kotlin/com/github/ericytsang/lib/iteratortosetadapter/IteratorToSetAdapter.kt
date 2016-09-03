package com.github.ericytsang.lib.iteratortosetadapter

import java.util.ArrayList
import java.util.LinkedHashSet

/**
 * Created by surpl on 5/8/2016.
 */
// todo: add test cases!
class IteratorToSetAdapter<T>(private val iterator:Iterator<T>):Set<T>
{
    override val size:Int get() = generator.generateIfPossibleUntil({false}).size
    override fun contains(element:T):Boolean = generator.generateIfPossibleUntil({it.contains(element)}).contains(element)
    override fun containsAll(elements:Collection<T>):Boolean = generator.generateIfPossibleUntil({it.containsAll(elements)}).containsAll(elements)
    override fun isEmpty():Boolean = !iterator().hasNext()
    override fun equals(other:Any?):Boolean = generator.generateIfPossibleUntil({false}).equals(other)
    override fun hashCode():Int = generator.generateIfPossibleUntil({false}).hashCode()
    override fun toString():String = generator.generateIfPossibleUntil({false}).toString()

    override fun iterator():Iterator<T> = object:AbstractIterator<T>()
    {
        var nextElementIndex = 0
        override fun computeNext()
        {
            // set the next element
            try
            {
                setNext(generator.getElementAt(nextElementIndex++))
            }

            // or call done if there is no next element
            catch (ex:IndexOutOfBoundsException)
            {
                done()
            }
        }
    }

    private val generator = object
    {
        private val generatedElementsList = ArrayList<T>()

        private val _generatedElementsSet = LinkedHashSet<T>()

        fun getElementAt(index:Int):T
        {
            generateIfPossibleUntil({index in generatedElementsList.indices})
            return generatedElementsList[index]
        }

        fun generateIfPossibleUntil(predicate:(Set<T>)->Boolean):Set<T>
        {
            if (canGenerateNext() && !predicate(_generatedElementsSet))
            {
                synchronized(this)
                {
                    while (canGenerateNext() && !predicate(_generatedElementsSet))
                    {
                        generateNext()
                    }
                }
            }
            return _generatedElementsSet
        }

        private fun canGenerateNext():Boolean
        {
            return iterator.hasNext()
        }

        private fun generateNext():Unit
        {
            val nextGeneratedItem = iterator.next()
            if (nextGeneratedItem !in _generatedElementsSet)
            {
                generatedElementsList.add(nextGeneratedItem)
                _generatedElementsSet.add(nextGeneratedItem)
            }
        }
    }
}