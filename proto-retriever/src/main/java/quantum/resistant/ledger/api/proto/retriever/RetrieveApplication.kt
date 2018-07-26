package quantum.resistant.ledger.api.proto.retriever

import io.grpc.ManagedChannelBuilder
import io.grpc.StatusRuntimeException
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

data class Connection(val host:String, val port:Int)

class RetrieveApplication
/** Construct client for accessing RouteGuide server using the existing channel.  */
internal constructor(private val connections: List<Connection>) {

    fun retrieveProto(): Boolean {
        connections.forEach { connection -> if(retrieveProtoFromConnection(connection)) return true }
        return false
    }

    fun retrieveProtoFromConnection(connection: Connection): Boolean {
        logger.log(Level.INFO, "Trying to retrieve proto from node: ${connection.host}:${connection.port}")

        val channel = ManagedChannelBuilder.forAddress(connection.host, connection.port).usePlaintext().build()
        val blockingStub = qrl.BaseGrpc.newBlockingStub(channel)

        val request = qrl.GetNodeInfoReq.getDefaultInstance()
        val response = try {
            blockingStub.getNodeInfo(request)
        } catch (e: StatusRuntimeException) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.status)
            return false
        } finally {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
        }

        val qrlProto = response.grpcProto
        logger.info("proto: ${qrlProto != null}, writing to file now...")
        File("api/src/main/proto/qrl.proto").writeText(qrlProto)
        return true
    }

    companion object {
        private val logger = Logger.getLogger(RetrieveApplication::class.java.name)

        @JvmStatic
        fun main(args: Array<String>) {
            val client = RetrieveApplication(listOf(
                    Connection("testnet-4.automated.theqrl.org", 19009),
                    Connection("testnet-2.automated.theqrl.org", 19009),
                    Connection("testnet-3.automated.theqrl.org", 19009),
                    Connection("testnet-1.automated.theqrl.org", 19009)))
            client.retrieveProto()
            logger.info("Done writing to file")
            return
        }
    }
}
