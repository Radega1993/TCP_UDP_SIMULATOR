package com.example.simulator.domain.subnetting;

import com.example.simulator.domain.ipv4.Ipv4SubnetCalculator;

import java.util.ArrayList;
import java.util.List;

public final class SubnettingCalculator {
    private SubnettingCalculator() {
    }

    public static SubnettingResult calculate(String baseNetwork, int baseCidr, SubnettingMode mode, int target) {
        if (baseCidr < 0 || baseCidr > 30) {
            throw new IllegalArgumentException("La máscara base debe estar entre /0 y /30.");
        }
        if (target < 1) {
            throw new IllegalArgumentException("El objetivo debe ser mayor que 0.");
        }

        long baseIp = Ipv4SubnetCalculator.parseIpv4(baseNetwork);
        long baseMask = maskFor(baseCidr);
        long normalizedBase = baseIp & baseMask;
        int borrowedBits = mode == SubnettingMode.BY_SUBNETS
                ? bitsForSubnets(target)
                : bitsForHosts(baseCidr, target);
        int newCidr = baseCidr + borrowedBits;
        if (newCidr > 30) {
            throw new IllegalArgumentException("El objetivo requiere una máscara mayor que /30 y no deja hosts útiles.");
        }

        int generatedSubnets = 1 << borrowedBits;
        long blockSize = 1L << (32 - newCidr);
        long hostsPerSubnet = blockSize <= 2 ? blockSize : blockSize - 2;
        List<SubnetBlock> visible = visibleSubnets(normalizedBase, generatedSubnets, blockSize, newCidr);

        return new SubnettingResult(
                formatIpv4(normalizedBase),
                baseCidr,
                newCidr,
                formatIpv4(maskFor(newCidr)),
                borrowedBits,
                generatedSubnets,
                hostsPerSubnet,
                blockSize,
                visible
        );
    }

    private static int bitsForSubnets(int targetSubnets) {
        int bits = 0;
        int generated = 1;
        while (generated < targetSubnets) {
            bits++;
            generated <<= 1;
        }
        return bits;
    }

    private static int bitsForHosts(int baseCidr, int targetHosts) {
        for (int newCidr = 30; newCidr >= baseCidr; newCidr--) {
            long blockSize = 1L << (32 - newCidr);
            long hosts = blockSize <= 2 ? blockSize : blockSize - 2;
            if (hosts >= targetHosts) {
                return newCidr - baseCidr;
            }
        }
        throw new IllegalArgumentException("La red base no puede ofrecer tantos hosts por subred.");
    }

    private static List<SubnetBlock> visibleSubnets(long base, int generatedSubnets, long blockSize, int cidr) {
        int limit = Math.min(generatedSubnets, 256);
        List<SubnetBlock> blocks = new ArrayList<>();
        for (int index = 0; index < limit; index++) {
            long network = base + (blockSize * index);
            long broadcast = network + blockSize - 1;
            blocks.add(new SubnetBlock(
                    index + 1,
                    formatIpv4(network),
                    firstHost(network, broadcast, cidr),
                    lastHost(network, broadcast, cidr),
                    formatIpv4(broadcast),
                    hostRange(network, broadcast, cidr)
            ));
        }
        return blocks;
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

    private static String hostRange(long network, long broadcast, int cidr) {
        return firstHost(network, broadcast, cidr) + " - " + lastHost(network, broadcast, cidr);
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
}
