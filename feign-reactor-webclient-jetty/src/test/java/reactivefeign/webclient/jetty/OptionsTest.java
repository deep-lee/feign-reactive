/**
 * Copyright 2018 The Feign Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package reactivefeign.webclient.jetty;

import org.junit.Ignore;
import org.junit.Test;
import reactivefeign.ReactiveFeign;
import reactivefeign.ReactiveFeignBuilder;
import reactivefeign.ReactiveOptions;
import reactivefeign.client.ReadTimeoutException;
import reactivefeign.testcase.IcecreamServiceApi;

/**
 * @author Sergii Karpenko
 */
public class OptionsTest extends reactivefeign.OptionsTest {

  @Override
  protected ReactiveFeign.Builder<IcecreamServiceApi> builder(long readTimeoutInMillis) {
    return JettyWebReactiveFeign.<IcecreamServiceApi>builder().options(
            new JettyReactiveOptions.Builder().setRequestTimeoutMillis(readTimeoutInMillis).build());
  }

  @Override
  protected ReactiveFeignBuilder<IcecreamServiceApi> builder(boolean followRedirects) {
    return JettyWebReactiveFeign.<IcecreamServiceApi>builder().options(
            new JettyReactiveOptions.Builder().setFollowRedirects(followRedirects).build());
  }

  @Override
  protected ReactiveFeignBuilder<IcecreamServiceApi> builder(ReactiveOptions.ProxySettings proxySettings) {
    return JettyWebReactiveFeign.<IcecreamServiceApi>builder().options(
            new JettyReactiveOptions.Builder().setProxySettings(proxySettings).build());
  }

  //TODO fix after 5.3
  @Ignore
  @Override
  @Test(expected = ReadTimeoutException.class)
  public void shouldFailOnReadTimeout() {
  }
}
