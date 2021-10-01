import kotlin.math.ln
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

// This data class contains all the relevant information for a Process.
// It is takes three values when it's created, a process ID number, an execution time, and the interval time.
// The timer entered, time completed and wait time values are all initialized to the value as if they were the
//      first element in a process queue. These variables are changed later.
data class Process(val processID: Int, val executionTime: Float, val intervalTime: Float){
    var timeEntered: Float = intervalTime
    var timeComplete: Float = timeEntered + executionTime
    var waitTime: Float = 0.0F
    // This function overrides the toString() function to give my processes a cleaner string representation.
    override fun toString(): String {
        return "Process( ProcessID:$processID, ExecutionTime:$executionTime, IntervalTime:$intervalTime, " +
                "TimeEntered:$timeEntered, TimeComplete:$timeComplete, WaitTime:$waitTime)"
    } // toString()
} // data class Process()

// This class contains all the code specifically for my process implementation.
// It has an argument size the determines the initial size of my process queue,
//      defaulted to 10 if it is left blank.
// processWeight and intervalWeight are the weights for the random number generator,
//      for the process execution time and interval gap respectively, defaulted to 3.0 and 5.0 respectively.
class ProcessQueue( size: Int = 10, private val processWeight: Double = 3.0, private val intervalWeight: Double = 5.0){
    // queue is a mutable list for holding all processes.
    val queue: MutableList<Process> = emptyList<Process>().toMutableList()

    // init fills the queue with processes.
    init {
        for (i in 0 until size)
            this.addProcess()
    } // init

    // addProcess will add a process to the queue.
    // Designed to be called internally or externally.
    fun addProcess() {
        // If the queue is currently empty, a "default" process is added.
        // If the queue is not empty, processes are added and timeEntered, timeCompleted
        //      and waitTime are updated accordingly.
        // waitTime has a formula, but will never be less than 0, so a max function was added.
        // In rare cases, incredibly small negative wait times were occurring, which could only
        //      be explained by rounding/truncating errors due to use of Floats.
        if (queue.size == 0)
            queue.add(Process(0, getWeighedRandom(processWeight), getWeighedRandom(intervalWeight)))
        else{
            val tempProc = Process(queue.size, getWeighedRandom(processWeight), getWeighedRandom(intervalWeight))
            tempProc.timeEntered = queue.last().timeEntered + tempProc.intervalTime
            tempProc.timeComplete = max(queue.last().timeComplete, tempProc.timeEntered) + tempProc.executionTime
            tempProc.waitTime = max(tempProc.timeComplete - tempProc.executionTime - tempProc.timeEntered, 0.0F)
            queue.add(tempProc)
        } // else
    } // addProcess()

    // This function will gather various statistics on our queue and add them to a Map.
    fun getStatistics(): Map<String, Number?>{
        var stats: MutableMap<String, Number?> = emptyMap<String, Number?>().toMutableMap()
        val waitTimes = queue.map { it.waitTime }
        stats["Count"] = waitTimes.size
        stats["Minimum"] = waitTimes.minOrNull()
        stats["Maximum"] = waitTimes.maxOrNull()
        stats["Average"] = waitTimes.average()
        stats["Median"] = waitTimes.median()
        stats["Total Wait Time"] = waitTimes.sum()
        stats["Standard Deviation"] = waitTimes.standardDeviation()
        return stats
    } // getStatistics()

    // This function gets the amount of processes ahead of a given process at the time of entry.
    // It returns a map containing all the processes and the number in front.
    fun getLineLength(): Map<Int, Int>{
        val tempMap: MutableMap<Int, Int> = mutableMapOf<Int, Int>(0 to 0)
        for (process in 1 until queue.size){
            var tempInt = 0
            for ( check in process downTo  0)
                if (queue[check].timeComplete - queue[check].executionTime > queue[process].timeEntered ) tempInt++
                else break
            tempMap[process] = tempInt
        } // outer for
        return tempMap.toMap()
    } // getLineLength()
} // class ProcessQueue()

// This extension function can find the median value of a list of Floats.
fun List<Float>.median(): Float?{
    if (this.isNullOrEmpty()) return null
    val sortedThis = this.sortedBy{ it }
    val middle = this.size / 2
    return if (this.size % 2 == 0) (sortedThis[middle] + sortedThis[middle - 1]) / 2.0F else sortedThis[middle]
} // median()

// This extension function can find the standard deviation of a list of Floats.
fun List<Float>.standardDeviation(): Float?{
    if(this.isNullOrEmpty()) return null
    var sdSum = 0.0F
    val mean = this.average().toFloat()
    for (element in this)
        sdSum += (element - mean).pow(2)
    return sqrt(sdSum/this.size)
} // standardDeviation()

// This function returns a random Float based on the weight specified.
fun getWeighedRandom(weight: Double): Float{
    val myRandom = {specifiedRandom: Double -> (-specifiedRandom * ln(Math.random())).toFloat() }
    return myRandom(weight)
} // getWeightedRandom()

// Main method.
fun main(args:Array<String>){
    // Generates a process queue with size 100.
    val processQueue = ProcessQueue(size = 100)
    // Adds a process.
    processQueue.addProcess()
    // Gets and prints the statistics for the processQueue.
    println("Wait time statistics:")
    processQueue.getStatistics().forEach { println("${it.key}: ${it.value}") }
    println()
    // Gets a list of all line lengths, filters out any 0, and prints the rest.
    processQueue.getLineLength().filterValues { it > 0 }.forEach { println("Process ${it.key} had to wait for " +
            "${it.value} process${if(it.value == 1) "" else "es"}.") }
} // main()