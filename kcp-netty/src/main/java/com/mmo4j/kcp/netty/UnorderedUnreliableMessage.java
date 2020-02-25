package com.mmo4j.kcp.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;

public interface UnorderedUnreliableMessage {
  default void writeUnorderedUnReliableMessage(Kcp kcp, ByteBuf byteBuf) {
    final int UNORDERED_UNRELIABLE_PROTOCOL = 1;
    final UkcpUser user = (UkcpUser) kcp.getUser();
    byteBuf = byteBuf.retainedDuplicate();

    final ByteBuf head = PooledByteBufAllocator.DEFAULT.directBuffer(1);
    head.writeByte(UNORDERED_UNRELIABLE_PROTOCOL);
    final ByteBuf content = Unpooled.wrappedBuffer(head, byteBuf);

    final DatagramPacket temp = new DatagramPacket(content, user.getLocalAddress(), user.getRemoteAddress());
    user.getChannel().writeAndFlush(temp);
  }
}
