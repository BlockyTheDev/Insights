package dev.frankheijden.insights.api.reflection;

import dev.frankheijden.minecraftreflection.MinecraftReflection;

public class RChunk {

    private static final MinecraftReflection reflection = MinecraftReflection
            .of("net.minecraft.world.level.chunk.Chunk");

    private RChunk() {}

    public static MinecraftReflection getReflection() {
        return reflection;
    }
}
