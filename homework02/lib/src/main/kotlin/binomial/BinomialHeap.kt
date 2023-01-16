package binomial

/*
 * BinomialHeap - реализация биномиальной кучи
 *
 * https://en.wikipedia.org/wiki/Binomial_heap
 *
 * Запрещено использовать
 *
 *  - var
 *  - циклы
 *  - стандартные коллекции
 *
 * Детали внутренней реазации должны быть спрятаны
 * Создание - только через single() и plus()
 *
 * Куча совсем без элементов не предусмотрена
 *
 * Операции
 *
 * plus с кучей
 * plus с элементом
 * top - взятие минимального элемента
 * drop - удаление минимального элемента
 */
class BinomialHeap<T: Comparable<T>> private constructor(private val trees: FList<BinomialTree<T>?>) : SelfMergeable<BinomialHeap<T>> {
    companion object {
        fun <T : Comparable<T>> single(value: T): BinomialHeap<T> = BinomialHeap(flistOf(BinomialTree.single(value)))
    }

    private fun merge(forest1: FList<BinomialTree<T>?>, forest2: FList<BinomialTree<T>?>): FList<BinomialTree<T>?> {
        if (forest1 !is FList.Cons) return forest2
        if (forest2 !is FList.Cons) return forest1
        val tree1 = forest1.iterator().next()
        val tree2 = forest2.iterator().next()
        return when {
            tree1 == null -> return merge(forest1.tail, forest2)
            tree2 == null -> return merge(forest1, forest2.tail)
            tree1.order < tree2.order -> FList.Cons(tree1, merge(forest1.tail, forest2))
            tree1.order == tree2.order -> unionTreesSameOrder(tree1 + tree2, merge(forest1.tail, forest2.tail))
            else -> FList.Cons(tree2, merge(forest1, forest2.tail))
        }
    }

    private fun unionTreesSameOrder(tree: BinomialTree<T>, merged: FList<BinomialTree<T>?>): FList<BinomialTree<T>?> {
        if (merged !is FList.Cons) return FList.Cons(tree, FList.nil())

        val nextTree = merged.iterator().next()
        return when {
            nextTree == null -> unionTreesSameOrder(tree, merged.tail)
            tree.order == nextTree.order -> unionTreesSameOrder(tree + nextTree, merged.tail)
            else -> FList.Cons(tree, merged)
        }
    }

    /*
     * слияние куч
     *
     * Требуемая сложность - O(log(n))
     */
    override fun plus(other: BinomialHeap<T>): BinomialHeap<T> = BinomialHeap(merge(this.trees, other.trees))

    /*
     * добавление элемента
     *
     * Требуемая сложность - O(log(n))
     */
    operator fun plus(elem: T): BinomialHeap<T> = plus(BinomialHeap(flistOf(BinomialTree.single(elem))))

    private fun getMinimum(): BinomialTree<T> =
        (trees.filter { it != null } as FList<BinomialTree<T>>).minByOrNull { it.value }
            ?: throw IllegalArgumentException("There are no elements")

    /*
     * минимальный элемент
     *
     * Требуемая сложность - O(log(n))
     */
    fun top(): T = getMinimum().value

    /*
     * удаление элемента
     *
     * Требуемая сложность - O(log(n))
     */
    fun drop(): BinomialHeap<T> {
        val minTree = getMinimum()
        return BinomialHeap(merge(trees.filter { it != minTree }, minTree.children as FList<BinomialTree<T>?>))
    }
}

