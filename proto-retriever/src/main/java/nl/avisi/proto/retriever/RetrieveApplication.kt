package nl.avisi.proto.retriever

import io.grpc.ManagedChannelBuilder
import qrl.BaseGrpc

fun main(args: Array<String>) {
    val channel = ManagedChannelBuilder.forAddress("mainnet-1.automated.theqrl.org", 19009).build()
    val client = qrl.BaseGrpc.newBlockingStub(channel)
}
