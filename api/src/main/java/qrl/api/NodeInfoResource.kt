package qrl.api

import io.grpc.ManagedChannelBuilder
import io.grpc.StatusRuntimeException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

data class Connection(val host:String, val port:Int)

@RestController
class NodeInfoResource {

    companion object {
        private val logger = Logger.getLogger(NodeInfoResource::class.java.name)
        private val client = Connection("mainnet-1.automated.theqrl.org", 19009)
    }

    @GetMapping("/node")
    fun getNode(): String {
        logger.log(Level.INFO, "Trying to retrieve node info from node: ${client.host}:${client.port}")

        val channel = ManagedChannelBuilder.forAddress(client.host, client.port).usePlaintext().build()
        val blockingStub = qrl.PublicAPIGrpc.newBlockingStub(channel)

        val request = qrl.Qrl.GetKnownPeersReq.getDefaultInstance()
        val response = try {
            blockingStub.getKnownPeers(request)
        } catch (e: StatusRuntimeException) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.status)
            return "Failed"
        } finally {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
        }
        val peersList = response.knownPeersList
        return peersList.toString()
    }
}