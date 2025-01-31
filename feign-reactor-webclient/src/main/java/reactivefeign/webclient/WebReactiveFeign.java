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
package reactivefeign.webclient;

import org.springframework.web.reactive.function.client.WebClient;
import reactivefeign.ReactiveFeign;
import reactivefeign.ReactiveOptions;
import reactivefeign.client.ReactiveHttpRequest;
import reactivefeign.client.ReadTimeoutException;

import java.util.function.BiFunction;

import static reactivefeign.webclient.NettyClientHttpConnectorBuilder.buildNettyClientHttpConnector;
import static reactivefeign.webclient.client.WebReactiveHttpClient.webClient;


/**
 * {@link WebClient} based implementation of reactive Feign
 *
 * @author Sergii Karpenko
 */
public class WebReactiveFeign {

    public static <T> Builder<T> builder() {
        return builder(WebClient.builder());
    }

    public static <T> Builder<T> builder(WebClient.Builder webClientBuilder) {
        return new Builder<>(webClientBuilder);
    }

    public static <T> Builder<T> builder(WebClient.Builder webClientBuilder,
                                         WebClientFeignCustomizer webClientCustomizer) {
        return new Builder<>(webClientBuilder, webClientCustomizer);
    }

    public static class Builder<T> extends ReactiveFeign.Builder<T> {

        protected CustomizableWebClientBuilder webClientBuilder;

        protected Builder(WebClient.Builder webClientBuilder) {
            this.webClientBuilder = new CustomizableWebClientBuilder(webClientBuilder);
            this.webClientBuilder.clientConnector(
                    buildNettyClientHttpConnector(WebReactiveOptions.DEFAULT_OPTIONS));
            updateClientFactory();
        }

        protected Builder(WebClient.Builder webClientBuilder, WebClientFeignCustomizer webClientCustomizer) {
            this.webClientBuilder = new CustomizableWebClientBuilder(webClientBuilder);
            this.webClientBuilder.clientConnector(
                    buildNettyClientHttpConnector(WebReactiveOptions.DEFAULT_OPTIONS));
            webClientCustomizer.accept(this.webClientBuilder);
            updateClientFactory();
        }

        @Override
        public Builder<T> options(ReactiveOptions options) {
            webClientBuilder.clientConnector(
                    buildNettyClientHttpConnector((WebReactiveOptions)options));
            updateClientFactory();
            return this;
        }

        protected void updateClientFactory(){
            clientFactory(methodMetadata -> webClient(
                    methodMetadata, webClientBuilder.build(), errorMapper()));
        }

        public static BiFunction<ReactiveHttpRequest, Throwable, Throwable> errorMapper(){
            return (request, throwable) -> {
                if(throwable instanceof io.netty.handler.timeout.ReadTimeoutException){
                    return new ReadTimeoutException(throwable, request);
                }
                return null;
            };
        }
    }



}


