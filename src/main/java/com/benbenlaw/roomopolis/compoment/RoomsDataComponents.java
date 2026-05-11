package com.benbenlaw.roomopolis.compoment;

import com.benbenlaw.Roomopolis;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class RoomsDataComponents {

    public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Roomopolis.MOD_ID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Identifier>> TEMPLATE_ID =
            COMPONENTS.register("template_id", () ->
                    DataComponentType.<Identifier>builder()
                            .persistent(Identifier.CODEC)
                            .networkSynchronized(Identifier.STREAM_CODEC)
                            .cacheEncoding()
                            .build());
}
