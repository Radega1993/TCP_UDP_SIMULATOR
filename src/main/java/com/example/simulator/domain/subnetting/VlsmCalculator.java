package com.example.simulator.domain.subnetting;

import com.example.simulator.domain.ipv4.Ipv4SubnetCalculator;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class VlsmCalculator {
    private VlsmCalculator() {
    }

    public static VlsmResult calculate(String baseNetwork, int baseCidr, List<VlsmNetworkRequest> requests) {
        if (baseCidr < 0 || baseCidr > 30) {
            throw new IllegalArgumentException("La máscara base debe estar entre /0 y /30.");
        }
        if (requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("Añade al menos una red con hosts requeridos.");
        }

        long baseIp = Ipv4SubnetCalculator.parseIpv4(baseNetwork);
        long baseMask = maskFor(baseCidr);
        long normalizedBase = baseIp & baseMask;
        long baseBlockSize = 1L << (32 - baseCidr);
        long baseBroadcast = normalizedBase + baseBlockSize - 1;
        long cursor = normalizedBase;

        AtomicInteger stableOrder = new AtomicInteger(0);
        List<OrderedRequest> sortedRequests = requests.stream()
                .map(request -> new OrderedRequest(stableOrder.getAndIncrement(), request))
                .sorted(Comparator
                        .comparingInt((OrderedRequest request) -> request.request().requiredHosts()).reversed()
                        .thenComparingInt(OrderedRequest::originalOrder))
                .toList();

        java.util.ArrayList<VlsmAssignment> assignments = new java.util.ArrayList<>();
        int order = 1;
        for (OrderedRequest ordered : sortedRequests) {
            VlsmNetworkRequest request = ordered.request();
            long blockSize = blockSizeForHosts(request.requiredHosts());
            int cidr = cidrForBlockSize(blockSize);
            if (cidr < baseCidr) {
                throw new IllegalArgumentException(request.name() + " necesita una subred mayor que la red base.");
            }
            long network = alignToBlock(cursor, blockSize);
            long broadcast = network + blockSize - 1;
            if (broadcast > baseBroadcast) {
                throw new IllegalArgumentException("La red base no tiene direcciones suficientes para esa lista VLSM.");
            }
            assignments.add(new VlsmAssignment(
                    order,
                    request.name(),
                    request.requiredHosts(),
                    cidr,
                    formatIpv4(maskFor(cidr)),
                    blockSize,
                    usableHosts(blockSize, cidr),
                    formatIpv4(network),
                    firstHost(network, broadcast, cidr),
                    lastHost(network, broadcast, cidr),
                    formatIpv4(broadcast),
                    request.name() + " pide " + request.requiredHosts()
                            + " hosts, por eso recibe /" + cidr + " antes que las redes más pequeñas."
            ));
            cursor = broadcast + 1;
            order++;
        }

        long usedAddresses = assignments.stream().mapToLong(VlsmAssignment::blockSize).sum();
        return new VlsmResult(
                formatIpv4(normalizedBase),
                baseCidr,
                baseBlockSize,
                usedAddresses,
                baseBlockSize - usedAddresses,
                List.copyOf(assignments)
        );
    }

    private static long blockSizeForHosts(int requiredHosts) {
        long needed = (long) requiredHosts + 2;
        long blockSize = 4;
        while (blockSize < needed) {
            blockSize <<= 1;
        }
        return blockSize;
    }

    private static int cidrForBlockSize(long blockSize) {
        return 32 - Long.numberOfTrailingZeros(blockSize);
    }

    private static long alignToBlock(long value, long blockSize) {
        long remainder = value % blockSize;
        return remainder == 0 ? value : value + blockSize - remainder;
    }

    private static long usableHosts(long blockSize, int cidr) {
        if (cidr >= 31) {
            return blockSize;
        }
        return blockSize - 2;
    }

    private static long maskFor(int cidr) {
        if (cidr == 0) {
            return 0;
        }
        return (0xFFFFFFFFL << (32 - cidr)) & 0xFFFFFFFFL;
    }

    private static String formatIpv4(long value) {
        return ((value >>> 24) & 0xFF) + "."
                + ((value >>> 16) & 0xFF) + "."
                + ((value >>> 8) & 0xFF) + "."
                + (value & 0xFF);
    }

    private static String firstHost(long network, long broadcast, int cidr) {
        if (cidr >= 31) {
            return formatIpv4(network);
        }
        return formatIpv4(network + 1);
    }

    private static String lastHost(long network, long broadcast, int cidr) {
        if (cidr == 32) {
            return formatIpv4(network);
        }
        if (cidr == 31) {
            return formatIpv4(broadcast);
        }
        return formatIpv4(broadcast - 1);
    }

    private record OrderedRequest(int originalOrder, VlsmNetworkRequest request) {
    }
}
