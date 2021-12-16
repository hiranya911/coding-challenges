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
        fun fromBinaryString(binary: String, version: Int, startIdx: Int): Pair<Literal, Int> {
            var idx = startIdx
            val sb = StringBuilder()
            while (true) {
                val segment = binary.substring(idx, idx + 5)
                sb.append(segment.substring(1))
                idx += 5
                if (segment[0] == '0') {
                    break
                }
            }

            return Pair(Literal(version, sb.toString().toLong(2)), idx)
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
        fun fromBinaryString(binary: String, version: Int, typeId: Int, startIdx: Int): Pair<Operator, Int> {
            val lengthType = binary[startIdx]
            return if (lengthType == '0') {
                lengthType0(binary, version, typeId, startIdx + 1)
            } else {
                lengthType1(binary, version, typeId, startIdx + 1)
            }
        }

        private fun lengthType0(binary: String, version: Int, typeId: Int, startIdx: Int): Pair<Operator, Int> {
            val numBits = binary.substring(startIdx, startIdx + 15).toInt(2)
            val endOfPacket = startIdx + 15 + numBits
            val subpackets = mutableListOf<Packet>()
            var start = startIdx + 15
            while (start < endOfPacket) {
                val (packet, idx) = parseBinaryPacket(binary, start)
                subpackets.add(packet)
                start = idx
            }

            return Pair(Operator(version, typeId, subpackets), start)
        }

        private fun lengthType1(binary: String, version: Int, typeId: Int, startIdx: Int): Pair<Operator, Int> {
            val numPackets = binary.substring(startIdx, startIdx + 11).toInt(2)
            val subpackets = mutableListOf<Packet>()
            var start = startIdx + 11
            for (i in 1..numPackets) {
                val (packet, idx) = parseBinaryPacket(binary, start)
                subpackets.add(packet)
                start = idx
            }

            return Pair(Operator(version, typeId,subpackets), start)
        }
    }
}

fun parsePacket(packet: String): Packet {
    val binary = toBinaryString(packet)
    val (result, _) = parseBinaryPacket(binary, 0)
    return result
}

fun parseBinaryPacket(binary: String, startIdx: Int): Pair<Packet, Int> {
    val version = binary.substring(startIdx, startIdx + 3).toInt(2)
    val typeId = binary.substring(startIdx + 3, startIdx + 6).toInt(2)
    return if (typeId == 4) {
        Literal.fromBinaryString(binary, version, startIdx + 6)
    } else {
        Operator.fromBinaryString(binary, version, typeId, startIdx + 6)
    }
}

class BinaryString(private val binary: String) {
    private var startIdx = 0

    fun getNext(n: Int): String {
        val result = binary.substring(startIdx, n)
        startIdx += n
        return result
    }
}

fun toBinaryString(hex: String): String =
   hex.map { it.digitToInt(16).toString(2).padStart(4, '0') }
       .joinToString("")
