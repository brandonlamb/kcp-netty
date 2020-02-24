package io.netty.bootstrap;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.mmo4j.kcp.netty.UkcpServerChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.util.AttributeKey;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

/**
 * The constructure of {@link AbstractBootstrap} is package-private,
 * so the package of this class is 'io.netty.bootstrap'
 * <p>
 * <h3>diffrence:</h3>
 * <ul>
 * <li>childGroup</li>
 * <li>ServerUkcpBootstrapAcceptor</li>
 * </ul>
 *
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public class UkcpServerBootstrap extends AbstractBootstrap<UkcpServerBootstrap, UkcpServerChannel> implements
                                                                                                   Cloneable {

  private static final InternalLogger logger = InternalLoggerFactory.getInstance(ServerBootstrap.class);

  private final Map<ChannelOption<?>, Object> childOptions = new ConcurrentHashMap<>();
  private final Map<AttributeKey<?>, Object> childAttrs = new ConcurrentHashMap<>();
  private final UkcpServerBootstrapConfig config = new UkcpServerBootstrapConfig(this);
  private volatile ChannelHandler childHandler;

  public UkcpServerBootstrap() {
  }

  private UkcpServerBootstrap(UkcpServerBootstrap bootstrap) {
    super(bootstrap);
    childHandler = bootstrap.childHandler;
    childOptions.putAll(bootstrap.childOptions);
    childAttrs.putAll(bootstrap.childAttrs);
  }

  @SuppressWarnings("unchecked")
  static Map.Entry<AttributeKey<?>, Object>[] newAttrArray(int size) {
    return new Map.Entry[size];
  }

  @SuppressWarnings("unchecked")
  static Map.Entry<ChannelOption<?>, Object>[] newOptionArray(int size) {
    return new Map.Entry[size];
  }

  /**
   * Allow to specify a {@link ChannelOption} which is used for the {@link Channel} instances once they get created
   * (after the acceptor accepted the {@link Channel}). Use a value of {@code null} to remove a previous set
   * {@link ChannelOption}.
   */
  public <T> UkcpServerBootstrap childOption(ChannelOption<T> childOption, T value) {
    if (childOption == null) {
      throw new NullPointerException("childOption");
    }
    if (value == null) {
      childOptions.remove(childOption);
    } else {
      childOptions.put(childOption, value);
    }
    return this;
  }

  /**
   * Set the specific {@link AttributeKey} with the given value on every child {@link Channel}. If the value is
   * {@code null} the {@link AttributeKey} is removed
   */
  public <T> UkcpServerBootstrap childAttr(AttributeKey<T> childKey, T value) {
    if (childKey == null) {
      throw new NullPointerException("childKey");
    }
    if (value == null) {
      childAttrs.remove(childKey);
    } else {
      childAttrs.put(childKey, value);
    }
    return this;
  }

  /**
   * Set the {@link ChannelHandler} which is used to serve the request for the {@link Channel}'s.
   */
  public UkcpServerBootstrap childHandler(ChannelHandler childHandler) {
    if (childHandler == null) {
      throw new NullPointerException("childHandler");
    }
    this.childHandler = childHandler;
    return this;
  }

  @Override
  void init(Channel channel) {
    setChannelOptions(channel, options0().entrySet().toArray(newOptionArray(0)), logger);
    setAttributes(channel, attrs0().entrySet().toArray(newAttrArray(0)));

    ChannelPipeline p = channel.pipeline();

    final ChannelHandler currentChildHandler = childHandler;
    final Entry<ChannelOption<?>, Object>[] currentChildOptions = childOptions.entrySet().toArray(newOptionArray(0));
    final Entry<AttributeKey<?>, Object>[] currentChildAttrs = childAttrs.entrySet().toArray(newAttrArray(0));

    p.addLast(new ChannelInitializer<Channel>() {
      @Override
      public void initChannel(final Channel ch) throws Exception {
        final ChannelPipeline pipeline = ch.pipeline();
        ChannelHandler handler = config.handler();
        if (handler != null) {
          pipeline.addLast(handler);
        }

        ch.eventLoop().execute(new Runnable() {
          @Override
          public void run() {
            pipeline.addLast(new ServerUkcpBootstrapAcceptor(
              ch, currentChildHandler, currentChildOptions, currentChildAttrs));
          }
        });
      }
    });
  }

  @Override
  public UkcpServerBootstrap validate() {
    super.validate();
    if (childHandler == null) {
      throw new IllegalStateException("childHandler not set");
    }
    return this;
  }

  @Override
  public UkcpServerBootstrap clone() {
    return new UkcpServerBootstrap(this);
  }

  final ChannelHandler childHandler() {
    return childHandler;
  }

  final Map<ChannelOption<?>, Object> childOptions() {
    return copiedMap(childOptions);
  }

  final Map<AttributeKey<?>, Object> childAttrs() {
    return copiedMap(childAttrs);
  }

  @Override
  public final UkcpServerBootstrapConfig config() {
    return config;
  }

  private static class ServerUkcpBootstrapAcceptor extends ChannelInboundHandlerAdapter {

    private final ChannelHandler childHandler;
    private final Map.Entry<ChannelOption<?>, Object>[] childOptions;
    private final Map.Entry<AttributeKey<?>, Object>[] childAttrs;

    ServerUkcpBootstrapAcceptor(
      final Channel channel, ChannelHandler childHandler,
      Map.Entry<ChannelOption<?>, Object>[] childOptions, Map.Entry<AttributeKey<?>, Object>[] childAttrs) {
      this.childHandler = childHandler;
      this.childOptions = childOptions;
      this.childAttrs = childAttrs;
    }

    private static void forceClose(Channel child, Throwable t) {
      child.unsafe().closeForcibly();
      logger.warn("Failed to register an accepted channel: {}", child, t);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
      final Channel parent = ctx.channel();
      final Channel child = (Channel) msg;

      child.pipeline().addLast(childHandler);

      setChannelOptions(child, childOptions, logger);
      setAttributes(child, childAttrs);

      try {
        parent.eventLoop().register(child).addListener((ChannelFutureListener) future -> {
          if (!future.isSuccess()) {
            forceClose(child, future.cause());
          }
        });
      } catch (Throwable t) {
        forceClose(child, t);
      }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
      // still let the exceptionCaught event flow through the pipeline to give the user
      // a chance to do something with it
      ctx.fireExceptionCaught(cause);
    }
  }
}
