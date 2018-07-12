package nl.avisi.proto.retriever

import io.grpc.ManagedChannelBuilder
import io.grpc.ManagedChannel
import io.grpc.StatusRuntimeException
import qrl.GetNodeInfoReq
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

class RetrieveApplication
/** Construct client for accessing RouteGuide server using the existing channel.  */
internal constructor(private val channel: ManagedChannel) {
    private val blockingStub: qrl.BaseGrpc.BaseBlockingStub = qrl.BaseGrpc.newBlockingStub(channel)

    /** Construct client connecting to HelloWorld server at `host:port`.  */
    constructor(host: String, port: Int) : this(ManagedChannelBuilder.forAddress(host, port)
            // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
            // needing certificates.
            .usePlaintext()
            .build())

    @Throws(InterruptedException::class)
    fun shutdown() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }

    fun retrieveProto() {
        logger.log(Level.INFO, "Will try to retrieve proto")
        val request = qrl.GetNodeInfoReq.getDefaultInstance()
        val response = try {
            blockingStub.getNodeInfo(request)
        } catch (e: StatusRuntimeException) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.status)
            return
        }
        val qrlProto = response.grpcProto
        logger.info("proto: ${qrlProto != null}, writing to file now...")
        File("proto-retriever/src/main/proto/qrl.proto").writeText(qrlProto)
    }

    companion object {
        private val logger = Logger.getLogger(RetrieveApplication::class.java.name)

        /**
         * Greet server. If provided, the first element of `args` is the name to use in the
         * greeting.
         */
        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val client = RetrieveApplication("mainnet-1.automated.theqrl.org", 19009)
            try {
                /* Access a service running on the local machine on port 50051 */
                client.retrieveProto()
            } finally {
                client.shutdown()
            }
        }
    }
}
