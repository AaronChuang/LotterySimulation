import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter

data class People(val no:Int, val group:String, var personTicket:Int, var personalPrice:Int, var groupTickets:Int,
                  var groupPrice:Int, var totalPersonTicket:Long, var totalPersonalPrice:Long, var totalGroupTickets:Long,
                  var totalGroupPrice:Long){
    fun price():Int{
        return personalPrice + groupPrice
    }

    fun totalPrice():Long{
        return totalPersonalPrice + totalGroupPrice
    }
}
var seq = 1
data class Ticket(val no:String, val seq:Int, var isPerson:Boolean)
data class Awards(val name:String, val price:Int, val choose:Int, val order: Int)
val allTickets = mutableListOf<Ticket>()
val allPeople = mutableListOf<People>()
val totalAwards = listOf(
    Awards(name = "頭獎", price = 10000, choose = 1, order = 4),
    Awards(name = "二獎", price = 6000, choose = 2, order = 1),
    Awards(name = "三獎", price = 4000, choose = 6, order = 0),
    Awards(name = "四獎", price = 2000, choose = 12, order = 2),
    Awards(name = "五獎", price = 1000, choose = 30, order = 3),
    Awards(name = "董事長獎", price = 5000, choose = 6, order = 5)
).sortedBy { it.order }
// 玩遊戲的時機
val game = listOf("三獎", "二獎", "四獎")
val groups = listOf("A", "B", "C", "D", "E", "F")
var bestPrice = -1
var worstPrice = -1
val allPriceCount = mutableMapOf<Int, Int>()
fun main() {
    initPeople()

    val simulationTimes = 1000000
    for (i in 1..simulationTimes){
        resetTickets()
        resetSimulation()
        lotterySimulation()
        allPeople.sortBy { it.price() }
        val singleBest = allPeople.last().price()
        val singleWorst = allPeople.first().price()
        if(bestPrice == -1 || singleBest > bestPrice){
            bestPrice = singleBest
        }
        if(worstPrice == -1 || singleWorst < worstPrice){
            worstPrice = singleWorst
        }
        allPeople.forEach {
            val price = it.price()
            if(allPriceCount.containsKey(price)){
                allPriceCount[price] = allPriceCount[price]!!.plus(1)
            } else {
                allPriceCount[price] = 1
            }
        }
    }
    var totalAvg = 0L
    allPeople.forEach {
        totalAvg += (it.totalPrice()/simulationTimes)
        println("${it.no} ${it.group} personTicket:${it.totalPersonTicket} groupTickets:${it.totalGroupTickets} avg: ${it.totalPrice()/simulationTimes}")
    }
    val sortedMap = LinkedHashMap<Int, Int>()
    allPriceCount.entries.sortedBy { it.key }.forEach { sortedMap[it.key] = it.value }
    println("模擬次數 $simulationTimes 最佳: $bestPrice 最差 $worstPrice 平均: ${totalAvg/allPeople.size}")
    csvWriter().open("output.csv") {
        writeRow(listOf("模擬次數", "最佳", "最差", "平均"))
        writeRow(listOf(simulationTimes, bestPrice, worstPrice, (totalAvg/allPeople.size)))
        sortedMap.forEach {
            writeRow(listOf(it.key, it.value))
        }
    }
    println(sortedMap)
}

fun initPeople(){
    for(i in 1..55){
        var div = i / 9
        val mod = i % 9
        if(mod == 0)
            div--
        val people = when(div){
            0 -> People(i, "A", 0, 0, 0, 0, 0, 0, 0, 0)
            1 -> People(i, "B", 0, 0, 0, 0, 0, 0, 0, 0)
            2 -> People(i, "C", 0, 0, 0, 0, 0, 0, 0, 0)
            3 -> People(i, "D", 0, 0, 0, 0, 0, 0, 0, 0)
            4 -> People(i, "E", 0, 0, 0, 0, 0, 0, 0, 0)
            else -> People(i, "F", 0, 0, 0, 0, 0, 0, 0, 0)
        }
        allPeople.add(people)
    }
}

fun resetTickets(){
    allTickets.clear()
    seq = 1
    for(i in 1..55){
        allTickets.add(Ticket(i.toString(), seq,true))
        seq++
    }
    var wins = (1..5).random()
    // group a
    for(i in 1..(9 + wins * 2)){
        allTickets.add(Ticket("A", seq, false))
        seq++
    }
    wins = (1..5).random()
    // group b
    for(i in 1..(9 + wins * 2)){
        allTickets.add(Ticket("B", seq, false))
        seq++
    }
    wins = (1..5).random()
    // group c
    for(i in 1..(9 + wins * 2)){
        allTickets.add(Ticket("C", seq, false))
        seq++
    }
    wins = (1..5).random()
    // group d
    for(i in 1..(9 + wins * 2)){
        allTickets.add(Ticket("D", seq, false))
        seq++
    }
    wins = (1..5).random()
    // group e
    for(i in 1..(9 + wins * 2)){
        allTickets.add(Ticket("E", seq, false))
        seq++
    }
    // group F
    for(i in 1..9){
        allTickets.add(Ticket("F", seq,false))
        seq++
    }
}

fun resetSimulation(){
    allPeople.forEach {
        it.personTicket = 0
        it.personalPrice = 0
        it.groupTickets = 0
        it.groupPrice = 0
    }
}

fun lotterySimulation(){
    // 第一次遊戲完
    var gameWin = (0..5).random()
    for(i in 1..2){
        allTickets.add(Ticket(groups[gameWin], seq, false))
        seq++
    }
    totalAwards.forEach { award ->
        val randomTickets = allTickets.asSequence().shuffled().take(award.choose).toList()
        randomTickets.forEach { tic ->
            when(tic.isPerson){
                true -> {
                    val people = allPeople.first { it.no == tic.no.toInt() }
                    people.personTicket++
                    people.personalPrice += award.price
                    people.totalPersonTicket++
                    people.totalPersonalPrice += award.price
                }
                false -> {
                    val group = allPeople.filter { it.group == tic.no }
                    group.forEach {
                        it.groupTickets++
                        it.groupPrice += award.price/group.size
                        it.totalGroupTickets++
                        it.totalGroupPrice += award.price/group.size
                    }
                }
            }
        }
        val removeIds = randomTickets.map { it.seq }
        allTickets.removeIf { i -> removeIds.contains(i.seq) }
        //遊戲完 +2 張券
        if(game.contains(award.name)){
            gameWin = (0..5).random()
            for(i in 1..2){
                allTickets.add(Ticket(groups[gameWin], seq, false))
                seq++
            }
        }
    }

}
