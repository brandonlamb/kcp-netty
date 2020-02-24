package com.mmo4j.kcp.netty.internal;

import java.util.Collection;

/**
 * @author <a href="mailto:szhnet@gmail.com">szh</a>
 */
public interface ReusableCollection<E> extends Collection<E> {

  @Override
  ReusableIterator<E> iterator();

}
