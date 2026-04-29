package com.example.simulator.domain.ipv4;

public final class Ipv4SubnetCalculator {
    private Ipv4SubnetCalculator() {
    }

    public static Ipv4SubnetResult calculate(String sourceIp, String destinationIp, int cidr) {
        if (cidr < 0 || cidr > 32) {
            throw new IllegalArgumentException("El CIDR debe estar entre /0 y /32.");
        }
        long source = parseIpv4(sourceIp);
        long destination = parseIpv4(destinationIp);
        long mask = maskFor(cidr);
        long sourceNetwork = source & mask;
        long destinationNetwork = destination & mask;
        long wildcard = (~mask) & 0xFFFFFFFFL;
        long sourceBroadcast = sourceNetwork | wildcard;
        long destinationBroadcast = destinationNetwork | wildcard;
        boolean sameNetwork = sourceNetwork == destinationNetwork;

        return new Ipv4SubnetResult(
                formatIpv4(source),
                formatIpv4(destination),
                cidr,
                formatIpv4(mask),
                formatIpv4(sourceNetwork),
                formatIpv4(destinationNetwork),
                formatIpv4(sourceBroadcast),
                formatIpv4(destinationBroadcast),
                hostRange(sourceNetwork, sourceBroadcast, cidr),
                hostRange(destinationNetwork, destinationBroadcast, cidr),
                sameNetwork,
                !sameNetwork
        );
    }

    public static long parseIpv4(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("La IP no puede estar vacía.");
        }
        String[] parts = value.trim().split("\\.");
        if (parts.length != 4) {
            throw new IllegalArgumentException("La IP debe tener cuatro octetos.");
        }

        long ip = 0;
        for (String part : parts) {
            int octet;
            try {
                octet = Integer.parseInt(part);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Cada octeto debe ser un número.");
            }
            if (octet < 0 || octet > 255) {
                throw new IllegalArgumentException("Cada octeto debe estar entre 0 y 255.");
            }
            ip = (ip << 8) | octet;
        }
        return ip & 0xFFFFFFFFL;
    }

    public static String toBinaryOctets(String ip) {
        long value = parseIpv4(ip);
        return String.format("%8s.%8s.%8s.%8s",
                        Long.toBinaryString((value >>> 24) & 0xFF),
                        Long.toBinaryString((value >>> 16) & 0xFF),
                        Long.toBinaryString((value >>> 8) & 0xFF),
                        Long.toBinaryString(value & 0xFF))
                .replace(' ', '0');
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
        if (cidr == 32) {
            return formatIpv4(network) + " (host único)";
        }
        if (cidr == 31) {
            return formatIpv4(network) + " - " + formatIpv4(broadcast);
        }
        return formatIpv4(network + 1) + " - " + formatIpv4(broadcast - 1);
    }
}
