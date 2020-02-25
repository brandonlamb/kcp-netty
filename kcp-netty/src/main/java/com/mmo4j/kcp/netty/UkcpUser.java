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

  public UkcpChannel getChannel() {
    return channel;
  }

  public void setChannel(UkcpChannel channel) {
    this.channel = channel;
  }

  public InetSocketAddress getLocalAddress() {
    return localAddress;
  }

  public void setLocalAddress(InetSocketAddress localAddress) {
    this.localAddress = localAddress;
  }

  public InetSocketAddress getRemoteAddress() {
    return remoteAddress;
  }

  public void setRemoteAddress(InetSocketAddress remoteAddress) {
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
