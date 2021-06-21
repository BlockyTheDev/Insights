package dev.frankheijden.insights.api.reflection;

import dev.frankheijden.minecraftreflection.MinecraftReflection;
import dev.frankheijden.minecraftreflection.Reflection;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import org.bukkit.World;

public class RCraftWorld {

    private static final MinecraftReflection reflection = MinecraftReflection
            .of("org.bukkit.craftbukkit.%s.CraftWorld");
    private static MethodHandle worldMethodHandle;

    static {
        try {
            worldMethodHandle = MethodHandles.lookup().unreflectGetter(Reflection.getAccessibleField(
                    reflection.getClazz(),
                    "world"
            ));
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private RCraftWorld() {}

    public static Object getServerLevel(World world) throws Throwable {
        return worldMethodHandle.invoke(world);
    }
}
