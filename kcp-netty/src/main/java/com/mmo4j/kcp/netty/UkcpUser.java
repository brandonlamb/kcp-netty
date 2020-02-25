package com.mmo4j.kcp.netty;

import java.net.InetSocketAddress;

public class UkcpUser {

  private UkcpChannel channel;
  private InetSocketAddress localAddress;
  private InetSocketAddress remoteAddress;

  public UkcpUser(
    final UkcpChannel channel,
    final InetSocketAddress localAddress,
    final InetSocketAddress remoteAddress
  ) {
    this.channel = channel;
    this.localAddress = localAddress;
    this.remoteAddress = remoteAddress;
  }

  @Override
  public String toString() {
    return "User{" +
           "remoteAddress=" + remoteAddress +
           ", localAddress=" + localAddress +
           '}';
  }
}
