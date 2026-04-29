package com.example.simulator.domain.ipv4;

import java.util.ArrayList;
import java.util.List;

public final class Ipv4FragmentationSimulator {
    public static final int IPV4_HEADER_BYTES = 20;
    public static final int TEACHING_PACKET_BYTES = 4000;

    private Ipv4FragmentationSimulator() {
    }

    public static Ipv4FragmentationResult fragment(int mtuBytes) {
        return fragment(TEACHING_PACKET_BYTES, mtuBytes);
    }

    public static Ipv4FragmentationResult fragment(int originalPacketSizeBytes, int mtuBytes) {
        if (originalPacketSizeBytes <= IPV4_HEADER_BYTES) {
            throw new IllegalArgumentException("El paquete debe ser mayor que la cabecera IP.");
        }
        if (mtuBytes <= IPV4_HEADER_BYTES) {
            throw new IllegalArgumentException("La MTU debe ser mayor que la cabecera IP de 20 bytes.");
        }

        int payloadSize = originalPacketSizeBytes - IPV4_HEADER_BYTES;
        if (mtuBytes >= originalPacketSizeBytes) {
            return new Ipv4FragmentationResult(
                    originalPacketSizeBytes,
                    mtuBytes,
                    IPV4_HEADER_BYTES,
                    payloadSize,
                    payloadSize,
                    false,
                    List.of(new Ipv4Fragment(1, 0, 0, payloadSize, originalPacketSizeBytes, false))
            );
        }

        int maxDataPerFragment = ((mtuBytes - IPV4_HEADER_BYTES) / 8) * 8;
        if (maxDataPerFragment <= 0) {
            throw new IllegalArgumentException("La MTU no deja datos suficientes para un fragmento IPv4.");
        }

        List<Ipv4Fragment> fragments = new ArrayList<>();
        int remaining = payloadSize;
        int offsetBytes = 0;
        int number = 1;
        while (remaining > 0) {
            int dataSize = Math.min(maxDataPerFragment, remaining);
            remaining -= dataSize;
            boolean moreFragments = remaining > 0;
            fragments.add(new Ipv4Fragment(
                    number,
                    offsetBytes / 8,
                    offsetBytes,
                    dataSize,
                    dataSize + IPV4_HEADER_BYTES,
                    moreFragments
            ));
            offsetBytes += dataSize;
            number++;
        }

        return new Ipv4FragmentationResult(
                originalPacketSizeBytes,
                mtuBytes,
                IPV4_HEADER_BYTES,
                payloadSize,
                maxDataPerFragment,
                true,
                fragments
        );
    }
}
