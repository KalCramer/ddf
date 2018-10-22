/**
 * Copyright (c) Codice Foundation
 *
 * <p>This is free software: you can redistribute it and/or modify it under the terms of the GNU
 * Lesser General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or any later version.
 *
 * <p>This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details. A copy of the GNU Lesser General Public
 * License is distributed along with this program and can be found at
 * <http://www.gnu.org/licenses/lgpl.html>.
 */
package org.codice.ddf.pax.web.jetty;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.codice.ddf.platform.filter.InjectFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InjectorFilter implements Filter {

  private static final Logger LOGGER = LoggerFactory.getLogger(InjectorFilter.class);
  private List<InjectFilter> filters;

  public InjectorFilter(List<InjectFilter> filters) {
    this.filters = filters;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    LOGGER.trace("Init InjectorFilter...");
  }

  @Override
  public void doFilter(
      ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
      throws IOException, ServletException {

    InjectFilterChain injectFilterChain = new InjectFilterChain(filterChain);
    injectFilterChain.doFilter(servletRequest, servletResponse);
  }

  @Override
  public void destroy() {
    // not used
  }

  private class InjectFilterChain implements FilterChain {
    private Iterator<InjectFilter> iterator;
    private FilterChain mainFilterChain;

    public InjectFilterChain(FilterChain filterChain) {
      this.mainFilterChain = filterChain;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse)
        throws IOException, ServletException {

      if (iterator == null) {
        iterator = filters.iterator();
      }
      if (iterator.hasNext()) {
        InjectFilter filter = iterator.next();
        LOGGER.debug(
            "Calling filter {}.doFilter({}, {}, {})",
            filter.getClass().getName(),
            servletRequest,
            servletResponse,
            this);
        filter.doFilter(servletRequest, servletResponse, this);
      } else {
        mainFilterChain.doFilter(servletRequest, servletResponse);
      }
    }
  }
}
