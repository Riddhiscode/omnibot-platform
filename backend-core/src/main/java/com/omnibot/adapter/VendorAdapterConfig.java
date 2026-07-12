package com.omnibot.adapter;

import com.omnibot.adapter.food.*;
import com.omnibot.adapter.transport.*;
import com.omnibot.adapter.shopping.*;
import com.omnibot.config.VendorProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration that wires all vendor adapters into the registry.
 *
 * Each adapter is instantiated as a Spring-managed bean and registered
 * with VendorAdapterRegistry at startup. To add a new vendor:
 *   1. Create the adapter class implementing VendorAdapter
 *   2. Add a @Bean method here that instantiates and returns it
 *   3. It auto-registers with the registry
 */
@Configuration
public class VendorAdapterConfig {

    private static final Logger log = LoggerFactory.getLogger(VendorAdapterConfig.class);

    // ============================================================
    // FOOD ADAPTERS
    // ============================================================

    @Bean
    public ZomatoAdapter zomatoAdapter(VendorProperties props, VendorAdapterRegistry registry) {
        ZomatoAdapter adapter = new ZomatoAdapter(props);
        registry.register(adapter);
        return adapter;
    }

    @Bean
    public SwiggyAdapter swiggyAdapter(VendorProperties props, VendorAdapterRegistry registry) {
        SwiggyAdapter adapter = new SwiggyAdapter(props);
        registry.register(adapter);
        return adapter;
    }

    @Bean
    public UberEatsAdapter uberEatsAdapter(VendorProperties props, VendorAdapterRegistry registry) {
        UberEatsAdapter adapter = new UberEatsAdapter(props);
        registry.register(adapter);
        return adapter;
    }

    @Bean
    public DoorDashAdapter doorDashAdapter(VendorProperties props, VendorAdapterRegistry registry) {
        DoorDashAdapter adapter = new DoorDashAdapter(props);
        registry.register(adapter);
        return adapter;
    }

    // ============================================================
    // TRANSPORT ADAPTERS
    // ============================================================

    @Bean
    public UberAdapter uberAdapter(VendorProperties props, VendorAdapterRegistry registry) {
        UberAdapter adapter = new UberAdapter(props);
        registry.register(adapter);
        return adapter;
    }

    @Bean
    public OlaAdapter olaAdapter(VendorProperties props, VendorAdapterRegistry registry) {
        OlaAdapter adapter = new OlaAdapter(props);
        registry.register(adapter);
        return adapter;
    }

    @Bean
    public LyftAdapter lyftAdapter(VendorProperties props, VendorAdapterRegistry registry) {
        LyftAdapter adapter = new LyftAdapter(props);
        registry.register(adapter);
        return adapter;
    }

    @Bean
    public BoltAdapter boltAdapter(VendorProperties props, VendorAdapterRegistry registry) {
        BoltAdapter adapter = new BoltAdapter(props);
        registry.register(adapter);
        return adapter;
    }

    @Bean
    public RapidoAdapter rapidoAdapter(VendorProperties props, VendorAdapterRegistry registry) {
        RapidoAdapter adapter = new RapidoAdapter(props);
        registry.register(adapter);
        return adapter;
    }

    @Bean
    public YuluAdapter yuluAdapter(VendorProperties props, VendorAdapterRegistry registry) {
        YuluAdapter adapter = new YuluAdapter(props);
        registry.register(adapter);
        return adapter;
    }

    // ============================================================
    // SHOPPING ADAPTERS
    // ============================================================

    @Bean
    public AmazonAdapter amazonAdapter(VendorProperties props, VendorAdapterRegistry registry) {
        AmazonAdapter adapter = new AmazonAdapter(props);
        registry.register(adapter);
        return adapter;
    }

    @Bean
    public FlipkartAdapter flipkartAdapter(VendorProperties props, VendorAdapterRegistry registry) {
        FlipkartAdapter adapter = new FlipkartAdapter(props);
        registry.register(adapter);
        return adapter;
    }

    @Bean
    public MeeshoAdapter meeshoAdapter(VendorProperties props, VendorAdapterRegistry registry) {
        MeeshoAdapter adapter = new MeeshoAdapter(props);
        registry.register(adapter);
        return adapter;
    }

    @Bean
    public MyntraAdapter myntraAdapter(VendorProperties props, VendorAdapterRegistry registry) {
        MyntraAdapter adapter = new MyntraAdapter(props);
        registry.register(adapter);
        return adapter;
    }

    @Bean
    public EbayAdapter ebayAdapter(VendorProperties props, VendorAdapterRegistry registry) {
        EbayAdapter adapter = new EbayAdapter(props);
        registry.register(adapter);
        return adapter;
    }

    @Bean
    public VendorAdapterRegistry vendorAdapterRegistry() {
        log.info("VendorAdapterRegistry initialized — adapters will self-register via @Bean methods");
        return new VendorAdapterRegistry();
    }
}
