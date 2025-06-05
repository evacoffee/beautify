package com.evacoffee.beautify.customization.component;

import com.evacoffee.beautify.customization.data.CustomizationData;
import dev.onyxstudios.cca.api.v3.component.Component;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.NotNull;

public class CustomizationComponent implements Component {
    private CustomizationData data = new CustomizationData();

    public CustomizationData getData() {
        return data;
    }

    public void setData(CustomizationData data) {
        this.data = data;
    }

    @Override
    public void readFromNbt(CompoundTag tag) {
        if (tag.contains("CustomizationData", 10)) {
            this.data = CustomizationData.fromNbt(tag.getCompound("CustomizationData"));
        } else {
            this.data = new CustomizationData();
        }
    }

    @Override 
    public void writeToNbt(@NotNull CompoundTag tag) {
        tag.put("CustomizationData", data.toNbt());
    }
}