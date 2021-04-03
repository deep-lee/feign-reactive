package reactivefeign.cloud2;

import feign.RetryableException;
import org.junit.BeforeClass;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryProperties;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.loadbalancer.support.ServiceInstanceListSuppliers;
import org.springframework.cloud.loadbalancer.support.SimpleObjectProvider;
import reactivefeign.ReactiveFeignBuilder;
import reactivefeign.cloud.common.AbstractLoadBalancingReactiveHttpClientTest;
import reactivefeign.publisher.retry.OutOfRetriesException;

import java.net.URI;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static reactivefeign.retry.BasicReactiveRetryPolicy.retry;

/**
 * @author Sergii Karpenko
 */
public class LoadBalancingReactiveHttpClientTest extends AbstractLoadBalancingReactiveHttpClientTest {

    private static ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerFactory;

    static class CustomLoadBalanceClientFactory extends LoadBalancerClientFactory {

        int[] ports;
        String serviceName;

        public CustomLoadBalanceClientFactory(int[] ports, String serviceName) {
            this.ports = ports;
            this.serviceName = serviceName;
        }

        @Override
        public ReactiveLoadBalancer<ServiceInstance> getInstance(String serviceId) {
            return new RoundRobinLoadBalancer(
                    new SimpleObjectProvider<>(ServiceInstanceListSuppliers.from(serviceName,
                            IntStream.of(ports).mapToObj(port ->
                                    {
                                        DefaultServiceInstance serviceInstance = new DefaultServiceInstance();
                                        serviceInstance.setUri(URI.create("http://localhost:" + port));
                                        return serviceInstance;
                                    }
                            )
                                    .toArray(ServiceInstance[]::new))),
                    serviceName);
        }
    }

    @BeforeClass
    public static void setupServersList() {
        loadBalancerFactory = loadBalancerFactory(serviceName, server1.port(), server2.port());
    }

    static ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerFactory(String serviceName, int... ports) {
        return new CustomLoadBalanceClientFactory(ports, serviceName);
    }

    @Override
    protected <T> ReactiveFeignBuilder<T> cloudBuilderWithLoadBalancerEnabled() {
        return BuilderUtils.<T>cloudBuilder()
                .enableLoadBalancer(loadBalancerFactory);
    }

    @Override
    protected <T> ReactiveFeignBuilder<T> cloudBuilderWithLoadBalancerEnabled(
            int retryOnSame, int retryOnNext) {
        return BuilderUtils.<T>cloudBuilder()
                .enableLoadBalancer(loadBalancerFactory)
                .retryOnSame(retry(retryOnSame))
                .retryOnNext(retry(retryOnNext));
    }

    @Override
    protected boolean isOutOfRetries(Throwable t) {
        assertThat(t).isInstanceOf(OutOfRetriesException.class);
        return true;
    }

    @Override
    protected boolean isOutOutOfRetries(Throwable t) {
        assertThat(t).isInstanceOf(OutOfRetriesException.class);
        assertThat(t.getCause()).isInstanceOf(OutOfRetriesException.class);
        assertThat(t.getCause().getCause()).isInstanceOf(RetryableException.class);
        return true;
    }

}
