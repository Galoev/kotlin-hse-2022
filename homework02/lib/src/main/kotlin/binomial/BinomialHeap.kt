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

    private fun <T: Comparable<T>> BinomialTree<T>?.merge(other: BinomialTree<T>?) = when {
        this == null -> other
        other == null -> this
        else -> this + other
    }

    private fun merge(forest1: FList<BinomialTree<T>?>, forest2: FList<BinomialTree<T>?>): FList<BinomialTree<T>?> {
        val iter1 = forest1.iterator()
        val iter2 = forest2.iterator()
        val firstRes = (iter1.asSequence() zip iter2.asSequence()).fold(Pair<FList<BinomialTree<T>?>, BinomialTree<T>?>(FList.nil(), null)) { (res, tree3), (tree1, tree2) ->
            if (tree3 == null) {
                when {
                    tree1 == null -> Pair(FList.Cons(tree2, res), null)
                    tree2 == null -> Pair(FList.Cons(tree1, res), null)
                    else -> Pair(FList.Cons(null, res), tree1 + tree2)
                }
            } else {
                when {
                    tree1 == null && tree2 == null -> Pair(FList.Cons(tree3, res), null)
                    tree2 == null -> Pair(FList.Cons(null, res), tree1.merge(tree3))
                    tree1 == null -> Pair(FList.Cons(null, res), tree2 + tree3)
                    else -> Pair(FList.Cons(tree3, res), tree1 + tree2)
                }
            }
        }

        val res = (if (iter1.hasNext()) iter1 else iter2).asSequence().fold(firstRes) { (res, tree2), tree1 ->
            when {
                tree1 == null -> Pair(FList.Cons(tree2, res), null)
                tree2 == null -> Pair(FList.Cons(tree1, res), null)
                else -> Pair(FList.Cons(null, res), tree1 + tree2)
            }
        }

        return if (res.second == null) res.first.reverse() else FList.Cons(res.second, res.first).reverse()
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
        val forest1 = createHeapTrees(minTree.children, 0).reverse()
        val forest2 = trees.map { if (it == minTree) null else it }
        return BinomialHeap(merge(forest1, forest2))
    }

    private fun createHeapTrees(trees: FList<BinomialTree<T>>, i: Int) : FList<BinomialTree<T>?> = when {
        trees !is FList.Cons -> FList.nil()
        i < trees.head.order -> FList.Cons(null, createHeapTrees(trees, i + 1))
        else -> FList.Cons(trees.head, createHeapTrees(trees.tail, i + 1))
    }
}

