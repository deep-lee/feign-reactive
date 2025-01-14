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
import reactivefeign.client.ReadTimeoutException;
import reactivefeign.testcase.IcecreamServiceApi;

/**
 * @author Sergii Karpenko
 */
public class LoggerTest extends reactivefeign.LoggerTest<LoggerTest.IcecreamServiceApiWebClient> {

  @Override
  protected String appenderPrefix(){
    return "webclient_";
  }

  @Override
  protected ReactiveFeign.Builder<IcecreamServiceApiWebClient> builder() {
    return JettyWebReactiveFeign.builder();
  }

  @Override
  protected ReactiveFeign.Builder<IcecreamServiceApiWebClient> builder(long readTimeoutInMillis) {
    return JettyWebReactiveFeign.<IcecreamServiceApiWebClient>builder().options(
            new JettyReactiveOptions.Builder().setRequestTimeoutMillis(readTimeoutInMillis).build()
    );
  }

  @Override
  protected Class<IcecreamServiceApiWebClient> target(){
    return IcecreamServiceApiWebClient.class;
  }

  interface IcecreamServiceApiWebClient extends IcecreamServiceApi{}

  //TODO fix after 5.3
  @Ignore
  @Override
  @Test(expected = ReadTimeoutException.class)
  public void shouldLogTimeout() {
  }

}
