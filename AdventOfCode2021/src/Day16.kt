package day16

const val INPUT = "C20D718021600ACDC372CD8DE7A057252A49C940239D68978F7970194EA7CCB310088760088803304A0AC1B100721EC298D3307440041CD8B8005D12DFD27CBEEF27D94A4E9B033006A45FE71D665ACC0259C689B1F99679F717003225900465800804E39CE38CE161007E52F1AEF5EE6EC33600BCC29CFFA3D8291006A92CA7E00B4A8F497E16A675EFB6B0058F2D0BD7AE1371DA34E730F66009443C00A566BFDBE643135FEDF321D000C6269EA66545899739ADEAF0EB6C3A200B6F40179DE31CB7B277392FA1C0A95F6E3983A100993801B800021B0722243D00042E0DC7383D332443004E463295176801F29EDDAA853DBB5508802859F2E9D2A9308924F9F31700AA4F39F720C733A669EC7356AC7D8E85C95E123799D4C44C0109C0AF00427E3CC678873F1E633C4020085E60D340109E3196023006040188C910A3A80021B1763FC620004321B4138E52D75A20096E4718D3E50016B19E0BA802325E858762D1802B28AD401A9880310E61041400043E2AC7E8A4800434DB24A384A4019401C92C154B43595B830002BC497ED9CC27CE686A6A43925B8A9CFFE3A9616E5793447004A4BBB749841500B26C5E6E306899C5B4C70924B77EF254B48688041CD004A726ED3FAECBDB2295AEBD984E08E0065C101812E006380126005A80124048CB010D4C03DC900E16A007200B98E00580091EE004B006902004B00410000AF00015933223100688010985116A311803D05E3CC4B300660BC7283C00081CF26491049F3D690E9802739661E00D400010A8B91F2118803310A2F43396699D533005E37E8023311A4BB9961524A4E2C027EC8C6F5952C2528B333FA4AD386C0A56F39C7DB77200C92801019E799E7B96EC6F8B7558C014977BD00480010D89D106240803518E31C4230052C01786F272FF354C8D4D437DF52BC2C300567066550A2A900427E0084C254739FB8E080111E0"

fun main() {
    val p = parsePacket(INPUT)
    println("Version sum = ${p.versionSum()}")
    println("Value = ${p.evaluate()}")
}

abstract class Packet(val version: Int) {
    abstract fun versionSum(): Int
    abstract fun evaluate(): Long
}

class Literal(version: Int, private val number: Long): Packet(version) {
    override fun versionSum(): Int = version
    override fun evaluate(): Long = number

    companion object {
        fun fromBinaryString(binary: BinaryString, version: Int): Literal {
            val sb = StringBuilder()
            while (true) {
                val segment = binary.getNext(5)
                sb.append(segment.substring(1))
                if (segment[0] == '0') {
                    break
                }
            }

            return Literal(version, sb.toString().toLong(2))
        }
    }
}

class Operator(version: Int, private val typeId: Int, private val subpackets: List<Packet>): Packet(version) {
    override fun versionSum(): Int = version + subpackets.sumOf { it.versionSum() }

    override fun evaluate(): Long =
        when (typeId) {
            0 -> subpackets.sumOf { it.evaluate() }
            1 -> subpackets.fold(1L) { prod, next -> prod * next.evaluate() }
            2 -> subpackets.minOf { it.evaluate() }
            3 -> subpackets.maxOf { it.evaluate() }
            5 -> if (subpackets[0].evaluate() > subpackets[1].evaluate()) 1 else 0
            6 -> if (subpackets[0].evaluate() < subpackets[1].evaluate()) 1 else 0
            7 -> if (subpackets[0].evaluate() == subpackets[1].evaluate()) 1 else 0
            else -> throw Exception("unsupported op")
        }

    companion object {
        fun fromBinaryString(binary: BinaryString, version: Int, typeId: Int): Operator {
            val lengthType = binary.getNext(1)
            return if (lengthType == "0") {
                Operator(version, typeId, lengthType0(binary))
            } else {
                Operator(version, typeId, lengthType1(binary))
            }
        }

        private fun lengthType0(binary: BinaryString): List<Packet> {
            val numBits = binary.getNextAsInt(15)
            val packetData = BinaryString(binary.getNext(numBits))
            val subpackets = mutableListOf<Packet>()
            while (packetData.hasNext()) {
                subpackets.add(parseBinaryPacket(packetData))
            }

            return subpackets
        }

        private fun lengthType1(binary: BinaryString): List<Packet> {
            val numPackets = binary.getNextAsInt(11)
            return (1..numPackets).map { parseBinaryPacket(binary) }
        }
    }
}

fun parsePacket(packet: String): Packet = parseBinaryPacket(toBinaryString(packet))

fun parseBinaryPacket(binary: BinaryString): Packet {
    val version = binary.getNextAsInt(3)
    val typeId = binary.getNextAsInt(3)
    return if (typeId == 4) {
        Literal.fromBinaryString(binary, version)
    } else {
        Operator.fromBinaryString(binary, version, typeId)
    }
}

class BinaryString(private val binary: String) {
    private var startIdx = 0

    fun getNext(n: Int): String {
        val result = binary.substring(startIdx, startIdx + n)
        startIdx += n
        return result
    }

    fun getNextAsInt(n: Int): Int = getNext(n).toInt(2)

    fun hasNext(): Boolean = startIdx < binary.lastIndex
}

fun toBinaryString(hex: String): BinaryString =
   hex.map { it.digitToInt(16).toString(2).padStart(4, '0') }
       .joinToString("").let { BinaryString(it) }
